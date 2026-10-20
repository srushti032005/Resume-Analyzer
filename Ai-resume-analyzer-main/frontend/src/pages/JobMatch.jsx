import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Briefcase, MapPin, TrendingUp, UploadCloud } from 'lucide-react';
import { resumeApi } from '../api/services.js';
import { Loader, EmptyState, ProgressBar } from '../components/UI.jsx';
import { CareerFitRadar } from '../components/charts/Charts.jsx';

export default function JobMatch() {
  const [resume, setResume] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    resumeApi
      .list()
      .then((res) => {
        const list = res.data.data;
        const withAnalysis = list.find(
          (r) => r.analysis?.status === 'COMPLETED'
        );
        setResume(withAnalysis || list[0] || null);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader label="Finding job matches…" />;

  const jobs = resume?.analysis?.jobRecommendations || [];

  if (!resume || jobs.length === 0) {
    return (
      <div>
        <h1 className="font-display text-3xl font-bold mb-2">Job Match</h1>
        <p className="text-current/50 mb-8">
          Suggested roles, salary estimates and career roadmap.
        </p>
        <EmptyState
          icon={Briefcase}
          title="No job matches yet"
          hint="Upload and analyze a resume to see roles matched to your profile."
          action={
            <Link to="/app/upload" className="btn-primary mt-2">
              <UploadCloud size={18} /> Upload resume
            </Link>
          }
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-3xl font-bold">Job Match</h1>
        <p className="text-current/50">
          Based on{' '}
          <span className="text-current/70">{resume.originalFilename}</span>
        </p>
      </div>

      <div className="card p-6">
        <h3 className="font-display text-lg font-semibold mb-4">
          Role Fit Overview
        </h3>
        <CareerFitRadar jobs={jobs} />
      </div>

      <div className="grid gap-5 md:grid-cols-2">
        {jobs.map((j, i) => (
          <motion.div
            key={j.id || j.jobTitle}
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.06 }}
            className="card p-6"
          >
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <div className="rounded-xl bg-brand-500/15 p-3 text-brand-400">
                  <Briefcase size={20} />
                </div>
                <div>
                  <p className="font-display text-lg font-semibold">
                    {j.jobTitle}
                  </p>
                  <p className="text-xs text-current/40 flex items-center gap-1">
                    <MapPin size={12} /> {j.salaryEstimate}
                  </p>
                </div>
              </div>
              <span className="pill">{j.matchPercentage}% match</span>
            </div>

            <div className="mt-4">
              <ProgressBar value={j.matchPercentage} />
            </div>

            <div className="mt-4 space-y-2 text-sm text-current/60">
              <p>
                <span className="font-semibold text-current/80">
                  Skill gaps:{' '}
                </span>
                {j.requiredSkillGaps}
              </p>
              <p className="flex items-start gap-1.5">
                <TrendingUp size={14} className="mt-0.5 text-emerald-400" />
                <span>{j.careerRoadmap}</span>
              </p>
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
}
