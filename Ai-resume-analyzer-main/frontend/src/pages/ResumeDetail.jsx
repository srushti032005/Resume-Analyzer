import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import {
  ArrowLeft,
  RefreshCw,
  Mail,
  Phone,
  User,
  Loader2,
} from 'lucide-react';
import { resumeApi } from '../api/services.js';
import { ScoreRing, Loader, ProgressBar } from '../components/UI.jsx';
import { CareerFitRadar } from '../components/charts/Charts.jsx';

function Section({ title, children }) {
  if (!children) return null;
  return (
    <div className="card p-6">
      <h3 className="font-display text-lg font-semibold mb-3">{title}</h3>
      <div className="text-sm text-current/70 whitespace-pre-line leading-relaxed">
        {children}
      </div>
    </div>
  );
}

export default function ResumeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [resume, setResume] = useState(null);
  const [loading, setLoading] = useState(true);
  const pollRef = useRef(null);

  const fetchResume = useCallback(async () => {
    try {
      const res = await resumeApi.get(id);
      setResume(res.data.data);
      return res.data.data;
    } catch {
      toast.error('Resume not found');
      navigate('/app/resumes');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  useEffect(() => {
    fetchResume();
  }, [fetchResume]);

  // Poll while analysis is pending/processing
  useEffect(() => {
    const status = resume?.analysis?.status;
    if (status === 'PENDING' || status === 'PROCESSING') {
      pollRef.current = setInterval(fetchResume, 3000);
      return () => clearInterval(pollRef.current);
    }
    if (pollRef.current) clearInterval(pollRef.current);
  }, [resume?.analysis?.status, fetchResume]);

  const handleReanalyze = async () => {
    try {
      await resumeApi.reanalyze(id);
      toast.success('Re-analysis started');
      fetchResume();
    } catch {
      toast.error('Could not start re-analysis');
    }
  };

  if (loading) return <Loader label="Loading analysis…" />;
  if (!resume) return null;

  const a = resume.analysis;
  const inProgress =
    a?.status === 'PENDING' || a?.status === 'PROCESSING';

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <button
          onClick={() => navigate('/app/resumes')}
          className="btn-ghost"
        >
          <ArrowLeft size={16} /> Back
        </button>
        <button onClick={handleReanalyze} className="btn-ghost">
          <RefreshCw size={16} /> Re-analyze
        </button>
      </div>

      <div className="card p-6">
        <h1 className="font-display text-2xl font-bold truncate">
          {resume.originalFilename}
        </h1>
        <div className="mt-3 flex flex-wrap gap-4 text-sm text-current/60">
          {resume.candidateName && (
            <span className="flex items-center gap-1.5">
              <User size={14} /> {resume.candidateName}
            </span>
          )}
          {resume.candidateEmail && (
            <span className="flex items-center gap-1.5">
              <Mail size={14} /> {resume.candidateEmail}
            </span>
          )}
          {resume.candidatePhone && (
            <span className="flex items-center gap-1.5">
              <Phone size={14} /> {resume.candidatePhone}
            </span>
          )}
        </div>
      </div>

      {inProgress ? (
        <div className="card p-12 flex flex-col items-center gap-4">
          <Loader2 size={36} className="animate-spin text-brand-400" />
          <p className="font-display text-lg font-semibold">
            AI analysis in progress…
          </p>
          <p className="text-sm text-current/50">
            This usually takes a few seconds. The page will update automatically.
          </p>
        </div>
      ) : a?.status === 'FAILED' ? (
        <div className="card p-8 border border-red-500/20">
          <p className="text-red-300 font-semibold">Analysis failed</p>
          <p className="text-sm text-current/50 mt-1">{a.errorMessage}</p>
          <button onClick={handleReanalyze} className="btn-primary mt-4">
            <RefreshCw size={16} /> Retry
          </button>
        </div>
      ) : (
        a && (
          <>
            <div className="grid gap-6 lg:grid-cols-3">
              <motion.div
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                className="card p-6 flex flex-col items-center gap-4"
              >
                <h3 className="font-display font-semibold self-start">
                  Resume Score
                </h3>
                <ScoreRing value={a.resumeScore} accent="#33a2ff" />
              </motion.div>
              <motion.div
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.05 }}
                className="card p-6 flex flex-col items-center gap-4"
              >
                <h3 className="font-display font-semibold self-start">
                  ATS Compatibility
                </h3>
                <ScoreRing value={a.atsScore} accent="#00d1b2" />
              </motion.div>
              <motion.div
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 }}
                className="card p-6"
              >
                <h3 className="font-display font-semibold mb-4">
                  AI Summary
                </h3>
                <p className="text-sm text-current/70 leading-relaxed">
                  {a.aiFeedbackSummary}
                </p>
              </motion.div>
            </div>

            {a.jobRecommendations?.length > 0 && (
              <div className="card p-6">
                <h3 className="font-display text-lg font-semibold mb-4">
                  Career Fit
                </h3>
                <CareerFitRadar jobs={a.jobRecommendations} />
                <div className="mt-4 grid gap-3 sm:grid-cols-2">
                  {a.jobRecommendations.map((j) => (
                    <div
                      key={j.id || j.jobTitle}
                      className="rounded-xl border border-white/10 bg-white/5 p-4"
                    >
                      <div className="flex items-center justify-between">
                        <p className="font-semibold">{j.jobTitle}</p>
                        <span className="pill">{j.matchPercentage}%</span>
                      </div>
                      <div className="mt-2">
                        <ProgressBar value={j.matchPercentage} />
                      </div>
                      <p className="mt-2 text-xs text-current/50">
                        💰 {j.salaryEstimate}
                      </p>
                      <p className="mt-1 text-xs text-current/50">
                        {j.requiredSkillGaps}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="grid gap-6 lg:grid-cols-2">
              <Section title="Missing Keywords">
                {a.missingKeywords}
              </Section>
              <Section title="Weak Sections">{a.weakSections}</Section>
              <Section title="Grammar & Professionalism">
                {a.grammarSuggestions}
              </Section>
              <Section title="Rewrite Recommendations">
                {a.rewriteRecommendations}
              </Section>
              <Section title="Skills Gap Analysis">
                {a.skillsGapAnalysis}
              </Section>
              <Section title="Keyword Optimization">
                {a.keywordOptimization}
              </Section>
              <Section title="Career Recommendations">
                {a.careerRecommendations}
              </Section>
              <Section title="Suggested Learning Roadmap">
                {a.learningRoadmap}
              </Section>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
              <Section title="Detected Skills">
                {resume.extractedSkills}
              </Section>
              <Section title="Education">
                {resume.extractedEducation}
              </Section>
            </div>
          </>
        )
      )}
    </div>
  );
}
