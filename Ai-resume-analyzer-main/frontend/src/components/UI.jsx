import { motion } from 'framer-motion';

export function Loader({ label = 'Loading…' }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4">
      <div className="relative h-12 w-12">
        <div className="absolute inset-0 rounded-full border-2 border-brand-500/20" />
        <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-brand-500 animate-spin" />
      </div>
      <p className="text-sm text-current/50">{label}</p>
    </div>
  );
}

export function ScoreRing({ value = 0, size = 160, label = 'Score', accent = '#33a2ff' }) {
  const radius = (size - 18) / 2;
  const circ = 2 * Math.PI * radius;
  const safe = Math.max(0, Math.min(100, value || 0));
  const offset = circ - (safe / 100) * circ;

  return (
    <div className="relative inline-flex items-center justify-center">
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          strokeWidth="12"
          className="stroke-white/10"
          fill="none"
        />
        <motion.circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          strokeWidth="12"
          stroke={accent}
          fill="none"
          strokeLinecap="round"
          strokeDasharray={circ}
          initial={{ strokeDashoffset: circ }}
          animate={{ strokeDashoffset: offset }}
          transition={{ duration: 1.2, ease: 'easeOut' }}
          style={{ filter: `drop-shadow(0 0 8px ${accent}66)` }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="font-display text-4xl font-bold">{safe}</span>
        <span className="text-xs uppercase tracking-wider text-current/50">
          {label}
        </span>
      </div>
    </div>
  );
}

export function StatCard({ icon: Icon, label, value, sub, accent = 'brand' }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      className="card p-5 flex items-start gap-4"
    >
      <div
        className={`rounded-xl p-3 bg-${accent}-500/15 text-${accent}-400`}
      >
        {Icon && <Icon size={22} />}
      </div>
      <div>
        <p className="text-sm text-current/50">{label}</p>
        <p className="font-display text-2xl font-bold mt-0.5">{value}</p>
        {sub && <p className="text-xs text-current/40 mt-1">{sub}</p>}
      </div>
    </motion.div>
  );
}

export function ProgressBar({ value = 0, accent = '#33a2ff' }) {
  const safe = Math.max(0, Math.min(100, value || 0));
  return (
    <div className="h-2.5 w-full rounded-full bg-white/10 overflow-hidden">
      <motion.div
        className="h-full rounded-full"
        style={{ background: accent }}
        initial={{ width: 0 }}
        animate={{ width: `${safe}%` }}
        transition={{ duration: 1, ease: 'easeOut' }}
      />
    </div>
  );
}

export function SkeletonCard() {
  return (
    <div className="card p-5 space-y-3">
      <div className="skeleton h-4 w-1/3" />
      <div className="skeleton h-8 w-2/3" />
      <div className="skeleton h-3 w-1/2" />
    </div>
  );
}

export function EmptyState({ icon: Icon, title, hint, action }) {
  return (
    <div className="card p-12 flex flex-col items-center text-center gap-3">
      {Icon && (
        <div className="rounded-2xl bg-brand-500/10 p-4 text-brand-400">
          <Icon size={32} />
        </div>
      )}
      <h3 className="font-display text-xl font-semibold">{title}</h3>
      {hint && <p className="text-sm text-current/50 max-w-sm">{hint}</p>}
      {action}
    </div>
  );
}
