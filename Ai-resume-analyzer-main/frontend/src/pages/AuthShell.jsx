import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Sparkles } from 'lucide-react';

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="min-h-screen bg-ink-900 bg-mesh text-white flex items-center justify-center px-5 py-10">
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-32 -left-32 h-96 w-96 rounded-full bg-brand-600/20 blur-3xl animate-floaty" />
        <div className="absolute -bottom-32 -right-32 h-96 w-96 rounded-full bg-purple-600/20 blur-3xl animate-floaty" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="relative w-full max-w-md"
      >
        <Link
          to="/"
          className="mb-8 flex items-center justify-center gap-2.5"
        >
          <div className="rounded-xl bg-brand-600 p-2 shadow-glow">
            <Sparkles size={20} />
          </div>
          <span className="font-display text-lg font-bold">
            AI Resume Analyzer
          </span>
        </Link>

        <div className="card p-8">
          <h1 className="font-display text-2xl font-bold">{title}</h1>
          <p className="mt-1 text-sm text-white/50">{subtitle}</p>
          <div className="mt-6">{children}</div>
        </div>

        {footer && (
          <p className="mt-6 text-center text-sm text-white/50">{footer}</p>
        )}
      </motion.div>
    </div>
  );
}
