import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import {
  FileText,
  Trash2,
  ChevronRight,
  UploadCloud,
  Clock,
} from 'lucide-react';
import { resumeApi } from '../api/services.js';
import { Loader, EmptyState } from '../components/UI.jsx';

const statusColor = {
  COMPLETED: 'text-emerald-300 bg-emerald-500/10 border-emerald-500/20',
  PROCESSING: 'text-amber-300 bg-amber-500/10 border-amber-500/20',
  PENDING: 'text-sky-300 bg-sky-500/10 border-sky-500/20',
  FAILED: 'text-red-300 bg-red-500/10 border-red-500/20',
};

export default function Resumes() {
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () =>
    resumeApi
      .list()
      .then((res) => setResumes(res.data.data))
      .finally(() => setLoading(false));

  useEffect(() => {
    load();
  }, []);

  const handleDelete = async (id, e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!confirm('Delete this resume permanently?')) return;
    try {
      await resumeApi.remove(id);
      toast.success('Resume deleted');
      setResumes((r) => r.filter((x) => x.id !== id));
    } catch {
      toast.error('Could not delete resume');
    }
  };

  if (loading) return <Loader label="Loading your resumes…" />;

  return (
    <div>
      <div className="flex flex-wrap items-end justify-between gap-4 mb-8">
        <div>
          <h1 className="font-display text-3xl font-bold">My Resumes</h1>
          <p className="text-current/50">
            Version history and analysis results
          </p>
        </div>
        <Link to="/app/upload" className="btn-primary">
          <UploadCloud size={18} /> Upload new
        </Link>
      </div>

      {resumes.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="No resumes uploaded"
          hint="Upload your first resume to start tracking versions and scores."
          action={
            <Link to="/app/upload" className="btn-primary mt-2">
              <UploadCloud size={18} /> Upload resume
            </Link>
          }
        />
      ) : (
        <div className="space-y-3">
          {resumes.map((r, i) => (
            <motion.div
              key={r.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.04 }}
            >
              <Link
                to={`/app/resumes/${r.id}`}
                className="card flex items-center gap-4 p-5 hover:shadow-glow transition"
              >
                <div className="rounded-xl bg-brand-500/15 p-3 text-brand-400">
                  <FileText size={22} />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="font-medium truncate">{r.originalFilename}</p>
                  <p className="text-xs text-current/40 flex items-center gap-1.5 mt-0.5">
                    <Clock size={12} />
                    v{r.version} ·{' '}
                    {new Date(r.createdAt).toLocaleDateString()}
                  </p>
                </div>

                {r.analysis && (
                  <div className="hidden sm:flex items-center gap-6">
                    <div className="text-center">
                      <p className="font-display text-xl font-bold">
                        {r.analysis.resumeScore ?? '—'}
                      </p>
                      <p className="text-[10px] uppercase text-current/40">
                        Score
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="font-display text-xl font-bold">
                        {r.analysis.atsScore ?? '—'}
                      </p>
                      <p className="text-[10px] uppercase text-current/40">
                        ATS
                      </p>
                    </div>
                  </div>
                )}

                <span
                  className={`hidden md:inline-flex rounded-full border px-3 py-1 text-xs ${
                    statusColor[r.analysis?.status] || statusColor.PENDING
                  }`}
                >
                  {r.analysis?.status || 'PENDING'}
                </span>

                <button
                  onClick={(e) => handleDelete(r.id, e)}
                  className="btn-ghost p-2 text-red-400 hover:bg-red-500/10"
                  aria-label="Delete"
                >
                  <Trash2 size={16} />
                </button>
                <ChevronRight size={18} className="text-current/30" />
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
