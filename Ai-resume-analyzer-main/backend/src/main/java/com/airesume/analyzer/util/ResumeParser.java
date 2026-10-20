package com.airesume.analyzer.util;

import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic, dependency-free heuristic parser. This produces a reliable
 * baseline extraction even when the OpenAI integration is disabled or rate
 * limited, so the application is always functional.
 */
@Component
public class ResumeParser {

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE =
            Pattern.compile("(\\+?\\d{1,3}[\\s-]?)?(\\(?\\d{2,4}\\)?[\\s-]?){2,4}\\d{2,4}");

    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Spring Boot", "Spring", "Python", "JavaScript", "TypeScript",
            "React", "Angular", "Vue", "Node.js", "Express", "SQL", "PostgreSQL",
            "MySQL", "MongoDB", "Redis", "Docker", "Kubernetes", "AWS", "Azure",
            "GCP", "Terraform", "Jenkins", "Git", "CI/CD", "REST", "GraphQL",
            "Kafka", "RabbitMQ", "Microservices", "HTML", "CSS", "TailwindCSS",
            "C++", "C#", ".NET", "Go", "Rust", "Kotlin", "Swift", "PHP", "Ruby",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch",
            "Pandas", "NumPy", "Linux", "Bash", "Agile", "Scrum", "JIRA",
            "Spring Security", "JPA", "Hibernate", "Maven", "Gradle"
    );

    private static final String[] SECTION_HEADERS = {
            "experience", "work experience", "professional experience",
            "education", "skills", "technical skills", "certifications",
            "projects", "summary", "objective", "achievements"
    };

    @Getter
    @Builder
    public static class ParsedResume {
        private String name;
        private String email;
        private String phone;
        private List<String> skills;
        private String experience;
        private String education;
        private String certifications;
        private String projects;
        private List<String> keywords;
    }

    public ParsedResume parse(String text) {
        String email = firstMatch(EMAIL, text);
        String phone = extractPhone(text);
        String name = extractName(text, email);
        List<String> skills = extractSkills(text);
        List<String> keywords = extractKeywords(text);

        return ParsedResume.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .skills(skills)
                .keywords(keywords)
                .experience(extractSection(text, "experience"))
                .education(extractSection(text, "education"))
                .certifications(extractSection(text, "certifications"))
                .projects(extractSection(text, "projects"))
                .build();
    }

    private String firstMatch(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? m.group().trim() : null;
    }

    private String extractPhone(String text) {
        Matcher m = PHONE.matcher(text);
        while (m.find()) {
            String candidate = m.group().trim();
            String digits = candidate.replaceAll("\\D", "");
            if (digits.length() >= 9 && digits.length() <= 15) {
                return candidate;
            }
        }
        return null;
    }

    private String extractName(String text, String email) {
        String[] lines = text.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.length() > 50) continue;
            if (EMAIL.matcher(line).find()) continue;
            if (line.matches(".*\\d{3,}.*")) continue;
            String[] words = line.split("\\s+");
            if (words.length >= 2 && words.length <= 4) {
                boolean looksLikeName = true;
                for (String w : words) {
                    if (!w.matches("[A-Z][a-zA-Z.'-]+")) {
                        looksLikeName = false;
                        break;
                    }
                }
                if (looksLikeName) return line;
            }
        }
        if (email != null) {
            String local = email.split("@")[0].replaceAll("[._\\d]", " ").trim();
            if (!local.isEmpty()) return capitalize(local);
        }
        return "Unknown Candidate";
    }

    private String capitalize(String s) {
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("\\s+")) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private List<String> extractSkills(String text) {
        String lower = text.toLowerCase();
        Set<String> found = new LinkedHashSet<>();
        for (String skill : KNOWN_SKILLS) {
            String needle = skill.toLowerCase();
            if (lower.contains(needle)) {
                found.add(skill);
            }
        }
        return new ArrayList<>(found);
    }

    private List<String> extractKeywords(String text) {
        Set<String> keywords = new LinkedHashSet<>();
        Matcher m = Pattern.compile("[A-Za-z][A-Za-z+#.]{2,}").matcher(text);
        int count = 0;
        while (m.find() && count < 400) {
            String w = m.group();
            if (w.length() >= 4 && Character.isUpperCase(w.charAt(0))) {
                keywords.add(w);
            }
            count++;
        }
        return keywords.stream().limit(60).toList();
    }

    private String extractSection(String text, String sectionName) {
        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        boolean capturing = false;
        for (String raw : lines) {
            String line = raw.trim();
            String lower = line.toLowerCase();
            if (!capturing && lower.startsWith(sectionName)) {
                capturing = true;
                continue;
            }
            if (capturing) {
                boolean isOtherHeader = false;
                for (String h : SECTION_HEADERS) {
                    if (!h.equals(sectionName) && lower.equals(h)) {
                        isOtherHeader = true;
                        break;
                    }
                }
                if (isOtherHeader) break;
                if (!line.isEmpty()) sb.append(line).append('\n');
                if (sb.length() > 2500) break;
            }
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }
}
