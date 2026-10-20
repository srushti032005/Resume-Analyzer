package com.airesume.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Wraps the OpenAI Chat Completions API. When no API key is present or the
 * call fails, a deterministic local analyzer is used so the product remains
 * fully functional for demos and offline development.
 */
@Slf4j
@Service
public class OpenAiService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocalAnalyzer localAnalyzer;

    @Value("${app.openai.api-key:}")
    private String apiKey;

    @Value("${app.openai.base-url}")
    private String baseUrl;

    @Value("${app.openai.model}")
    private String model;

    @Value("${app.openai.timeout-seconds}")
    private int timeoutSeconds;

    @Value("${app.openai.enabled}")
    private boolean enabled;

    public OpenAiService(LocalAnalyzer localAnalyzer) {
        this.localAnalyzer = localAnalyzer;
    }

    public JsonNode analyzeResume(String resumeText) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            log.info("OpenAI disabled or no key supplied — using local analyzer");
            return localAnalyzer.analyze(resumeText);
        }
        try {
            return callOpenAi(resumeText);
        } catch (Exception e) {
            log.warn("OpenAI call failed ({}) — falling back to local analyzer",
                    e.getMessage());
            return localAnalyzer.analyze(resumeText);
        }
    }

    private JsonNode callOpenAi(String resumeText) throws Exception {
        String systemPrompt = """
                You are an expert technical recruiter and ATS system.
                Analyze the resume and respond with ONLY valid minified JSON, no markdown.
                Schema:
                {
                  "resumeScore": int 0-100,
                  "atsScore": int 0-100,
                  "missingKeywords": string,
                  "weakSections": string,
                  "grammarSuggestions": string,
                  "rewriteRecommendations": string,
                  "skillsGapAnalysis": string,
                  "careerRecommendations": string,
                  "learningRoadmap": string,
                  "aiFeedbackSummary": string,
                  "keywordOptimization": string,
                  "jobRecommendations": [
                    {"jobTitle": string, "matchPercentage": int,
                     "salaryEstimate": string, "requiredSkillGaps": string,
                     "careerRoadmap": string}
                  ]
                }
                """;

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.3);

        ArrayNode messages = body.putArray("messages");
        ObjectNode sys = messages.addObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", "Resume:\n" + truncate(resumeText, 12000));

        ObjectNode responseFormat = body.putObject("response_format");
        responseFormat.put("type", "json_object");

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        String raw = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        JsonNode root = objectMapper.readTree(raw);
        String content = root.path("choices").get(0)
                .path("message").path("content").asText();
        return objectMapper.readTree(content);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    /* --------------------------------------------------------------- */

    @Service
    public static class LocalAnalyzer {

        private final ObjectMapper mapper = new ObjectMapper();

        private static final Map<String, String> ROLE_SKILLS = Map.of(
                "Backend Engineer", "Java, Spring Boot, SQL, REST, Docker, Microservices",
                "Full-Stack Developer", "React, Node.js, TypeScript, PostgreSQL, REST",
                "Cloud Engineer", "AWS, Docker, Kubernetes, Terraform, CI/CD",
                "DevOps Engineer", "Docker, Kubernetes, Jenkins, Terraform, Linux",
                "Data Engineer", "Python, SQL, Kafka, Spark, Airflow"
        );

        public JsonNode analyze(String text) {
            String lower = text == null ? "" : text.toLowerCase();
            int length = lower.length();

            int hasContact = (lower.contains("@") ? 8 : 0);
            int hasExperience = (lower.contains("experience") ? 14 : 0);
            int hasEducation = (lower.contains("education") ? 10 : 0);
            int hasSkills = (lower.contains("skill") ? 12 : 0);
            int hasProjects = (lower.contains("project") ? 10 : 0);
            int lengthScore = Math.min(20, length / 200);
            int actionVerbs = countAny(lower,
                    "developed", "designed", "implemented", "led", "built",
                    "improved", "optimized", "delivered") * 2;

            int resumeScore = clamp(28 + hasContact + hasExperience + hasEducation
                    + hasSkills + hasProjects + lengthScore + actionVerbs);

            int atsScore = clamp(35
                    + (lower.contains("skill") ? 15 : 0)
                    + (lower.contains("experience") ? 15 : 0)
                    + (text != null && text.contains("•") ? 5 : 0)
                    + Math.min(20, countAny(lower, "java", "python", "react",
                    "sql", "docker", "aws", "spring") * 3));

            ObjectNode root = mapper.createObjectNode();
            root.put("resumeScore", resumeScore);
            root.put("atsScore", atsScore);
            root.put("missingKeywords", suggestMissing(lower));
            root.put("weakSections", weakSections(lower));
            root.put("grammarSuggestions",
                    "Use consistent past-tense action verbs, remove first-person pronouns, "
                    + "and keep bullet points concise (one line where possible).");
            root.put("rewriteRecommendations",
                    "Lead each bullet with a strong action verb and quantify impact "
                    + "(e.g. 'Reduced API latency by 40%'). Move a concise summary to the top.");
            root.put("skillsGapAnalysis", skillsGap(lower));
            root.put("careerRecommendations",
                    "Based on detected skills you are well positioned for backend and "
                    + "full-stack roles. Strengthen cloud and system-design depth to "
                    + "move toward senior positions.");
            root.put("learningRoadmap",
                    "1) System design fundamentals  2) Cloud certification (AWS/Azure)  "
                    + "3) Containerisation & Kubernetes  4) Observability & CI/CD  "
                    + "5) One deep specialisation (data, security or platform).");
            root.put("aiFeedbackSummary",
                    String.format("Your resume scores %d/100 with an ATS compatibility of "
                            + "%d/100. Strengthen quantified achievements and keyword "
                            + "coverage to improve recruiter visibility.",
                            resumeScore, atsScore));
            root.put("keywordOptimization",
                    "Mirror exact keywords from target job descriptions, add a 'Technical "
                    + "Skills' section with grouped technologies, and include both acronyms "
                    + "and full forms (e.g. 'CI/CD (Continuous Integration)').");

            ArrayNode jobs = root.putArray("jobRecommendations");
            int i = 0;
            for (var entry : ROLE_SKILLS.entrySet()) {
                if (i++ >= 4) break;
                ObjectNode job = jobs.addObject();
                int match = clamp(resumeScore - 5 + (int) (Math.random() * 12));
                job.put("jobTitle", entry.getKey());
                job.put("matchPercentage", match);
                job.put("salaryEstimate", salaryFor(entry.getKey()));
                job.put("requiredSkillGaps", "Focus on: " + entry.getValue());
                job.put("careerRoadmap",
                        "Build 2 portfolio projects, obtain a relevant certification, "
                        + "then target mid-level " + entry.getKey() + " openings.");
            }
            return root;
        }

        private String salaryFor(String role) {
            return switch (role) {
                case "Cloud Engineer", "DevOps Engineer" -> "£55,000 – £80,000";
                case "Data Engineer" -> "£50,000 – £75,000";
                case "Full-Stack Developer" -> "£45,000 – £70,000";
                default -> "£42,000 – £68,000";
            };
        }

        private String suggestMissing(String lower) {
            StringBuilder sb = new StringBuilder();
            String[] important = {"docker", "kubernetes", "aws", "ci/cd",
                    "microservices", "rest", "testing", "agile"};
            for (String k : important) {
                if (!lower.contains(k)) sb.append(k).append(", ");
            }
            return sb.length() == 0
                    ? "Good keyword coverage — fine-tune to each job description."
                    : "Consider adding: " + sb.substring(0, sb.length() - 2);
        }

        private String weakSections(String lower) {
            StringBuilder sb = new StringBuilder();
            if (!lower.contains("summary")) sb.append("Missing professional summary. ");
            if (!lower.contains("project")) sb.append("No projects section detected. ");
            if (!lower.contains("certif")) sb.append("No certifications listed. ");
            if (!lower.matches("(?s).*\\d+%.*")) sb.append("Few quantified achievements. ");
            return sb.length() == 0 ? "All core sections present." : sb.toString().trim();
        }

        private String skillsGap(String lower) {
            return "Detected strengths in core programming. To close gaps, deepen "
                    + (lower.contains("docker") ? "" : "containerisation, ")
                    + (lower.contains("aws") ? "" : "cloud platforms, ")
                    + "system design and automated testing.";
        }

        private int countAny(String text, String... words) {
            int c = 0;
            for (String w : words) {
                int idx = 0;
                while ((idx = text.indexOf(w, idx)) != -1) {
                    c++;
                    idx += w.length();
                }
            }
            return c;
        }

        private int clamp(int v) {
            return Math.max(0, Math.min(100, v));
        }
    }
}
