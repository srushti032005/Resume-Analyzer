import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Sparkles,
  Target,
  Gauge,
  Brain,
  Briefcase,
  ShieldCheck,
  ArrowRight,
} from 'lucide-react';

const features = [
  {
    icon: Gauge,
    title: 'Resume & ATS Scoring',
    desc: 'Instant 0–100 scoring with ATS compatibility analysis tuned to recruiter systems.',
  },
  {
    icon: Brain,
    title: 'AI Feedback',
    desc: 'Section-by-section weaknesses, grammar and rewrite recommendations powered by AI.',
  },
  {
    icon: Target,
    title: 'Keyword Optimization',
    desc: 'Detect missing keywords and skill gaps against your target roles.',
  },
  {
    icon: Briefcase,
    title: 'Job Match',
    desc: 'Suggested roles, salary estimates and a tailored career roadmap.',
  },
];

export default function Landing() {
  return (
    <div className="min-h-screen bg-ink-900 bg-mesh text-white overflow-hidden">
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-2.5">
          <div className="rounded-xl bg-brand-600 p-2 shadow-glow">
            <Sparkles size={20} />
          </div>
          <span className="font-display text-lg font-bold">AI Resume Analyzer</span>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/login" className="btn-ghost text-sm">
            Sign in
          </Link>
          <Link to="/register" className="btn-primary text-sm">
            Get started
          </Link>
        </div>
      </nav>

      <header className="mx-auto max-w-5xl px-6 pt-20 pb-28 text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
        >
          <span className="pill mb-6">
            <Sparkles size={14} className="mr-1" /> Powered by OpenAI
          </span>
          <h1 className="font-display text-5xl md:text-7xl font-extrabold leading-[1.05] tracking-tight">
            Land more interviews with an{' '}
            <span className="bg-gradient-to-r from-brand-400 via-brand-300 to-emerald-300 bg-clip-text text-transparent">
              AI-optimized resume
            </span>
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-lg text-white/60">
            Upload your resume and get an instant score, ATS compatibility check,
            missing-skill detection, AI rewrite suggestions and matched job roles —
            all in one professional dashboard.
          </p>
          <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
            <Link to="/register" className="btn-primary px-7 py-3 text-base">
              Analyze my resume <ArrowRight size={18} />
            </Link>
            <Link to="/login" className="btn-ghost px-7 py-3 text-base">
              I already have an account
            </Link>
          </div>
          <p className="mt-6 text-xs text-white/40">
            Demo accounts available — see the README for credentials.
          </p>
        </motion.div>
      </header>

      <section className="mx-auto max-w-6xl px-6 pb-28">
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {features.map((f, i) => (
            <motion.div
              key={f.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.08 }}
              className="card p-6"
            >
              <div className="mb-4 inline-flex rounded-xl bg-brand-500/15 p-3 text-brand-300">
                <f.icon size={22} />
              </div>
              <h3 className="font-display text-lg font-semibold">{f.title}</h3>
              <p className="mt-2 text-sm text-white/50">{f.desc}</p>
            </motion.div>
          ))}
        </div>

        <div className="mt-12 flex flex-wrap items-center justify-center gap-x-8 gap-y-3 text-sm text-white/40">
          <span className="flex items-center gap-2">
            <ShieldCheck size={16} /> JWT-secured
          </span>
          <span className="flex items-center gap-2">
            <ShieldCheck size={16} /> Bcrypt password hashing
          </span>
          <span className="flex items-center gap-2">
            <ShieldCheck size={16} /> Dockerized & production-ready
          </span>
        </div>
      </section>

      <footer className="border-t border-white/5 py-8 text-center text-sm text-white/30">
        © {new Date().getFullYear()} AI Resume Analyzer — Full-Stack Portfolio Project
      </footer>
    </div>
  );
}
