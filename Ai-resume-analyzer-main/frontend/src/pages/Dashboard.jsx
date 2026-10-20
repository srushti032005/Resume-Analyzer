import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  FileText,
  TrendingUp,
  Gauge,
  Award,
  UploadCloud,
  AlertTriangle,
} from 'lucide-react';
import { dashboardApi } from '../api/services.js';
import {
  ScoreRing,
  StatCard,
  SkeletonCard,
  EmptyState,
} from '../components/UI.jsx';
import { ScoreTrendChart, SkillsBarChart } from '../components/charts/Charts.jsx';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardApi
      .stats()
      .then((res) => setStats(res.data.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-8 w-48" />
        <div className="grid gap-5 md:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );
  }

  if (!stats || stats.totalResumes === 0) {
    return (
      <div>
        <h1 className="font-display text-3xl font-bold mb-2">Dashboard</h1>
        <p className="text-current/50 mb-8">
          Your analytics will appear here once you upload a resume.
        </p>
        <EmptyState
          icon={UploadCloud}
          title="No resumes yet"
          hint="Upload your first PDF resume to get an AI score, ATS analysis and job matches."
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
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Dashboard</h1>
          <p className="text-current/50">
            Overview of your resume performance and analytics
          </p>
        </div>
        <Link to="/app/upload" className="btn-primary">
          <UploadCloud size={18} /> New analysis
        </Link>
      </div>

      <div className="grid gap-5 md:grid-cols-4">
        <StatCard
          icon={FileText}
          label="Total resumes"
          value={stats.totalResumes}
          accent="brand"
        />
        <StatCard
          icon={Award}
          label="Latest score"
          value={stats.latestScore ?? '—'}
          sub="out of 100"
          accent="brand"
        />
        <StatCard
          icon={Gauge}
          label="Latest ATS"
          value={stats.latestAtsScore ?? '—'}
          sub="ATS compatibility"
          accent="brand"
        />
        <StatCard
          icon={TrendingUp}
          label="Average score"
          value={stats.averageScore ?? '—'}
          sub="across all resumes"
          accent="brand"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="card p-6 flex flex-col items-center justify-center gap-6"
        >
          <h3 className="font-display text-lg font-semibold self-start">
            Latest Resume Score
          </h3>
          <ScoreRing value={stats.latestScore || 0} label="Score" accent="#33a2ff" />
          <ScoreRing
            value={stats.latestAtsScore || 0}
            size={120}
            label="ATS"
            accent="#00d1b2"
          />
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="card p-6 lg:col-span-2"
        >
          <h3 className="font-display text-lg font-semibold mb-4">
            Score Trend
          </h3>
          <ScoreTrendChart data={stats.scoreTrend} />
        </motion.div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="card p-6"
        >
          <h3 className="font-display text-lg font-semibold mb-4">
            Skills Distribution
          </h3>
          {Object.keys(stats.skillsDistribution || {}).length ? (
            <SkillsBarChart data={stats.skillsDistribution} />
          ) : (
            <p className="text-sm text-current/40 py-12 text-center">
              No skills detected yet.
            </p>
          )}
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="card p-6"
        >
          <h3 className="font-display text-lg font-semibold mb-4 flex items-center gap-2">
            <AlertTriangle size={18} className="text-amber-400" />
            Recommended / Missing Skills
          </h3>
          {stats.missingSkills?.length ? (
            <div className="flex flex-wrap gap-2">
              {stats.missingSkills.map((s) => (
                <span
                  key={s}
                  className="rounded-full border border-amber-500/20 bg-amber-500/10 px-3 py-1 text-xs text-amber-300"
                >
                  {s}
                </span>
              ))}
            </div>
          ) : (
            <p className="text-sm text-current/40">
              Great keyword coverage on your latest resume.
            </p>
          )}
        </motion.div>
      </div>
    </div>
  );
}
