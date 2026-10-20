import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Legend,
  AreaChart,
  Area,
} from 'recharts';

const tooltipStyle = {
  background: '#111726',
  border: '1px solid rgba(255,255,255,0.1)',
  borderRadius: 12,
  color: '#e8edf7',
  fontSize: 12,
};

export function ScoreTrendChart({ data = [] }) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <AreaChart data={data} margin={{ top: 10, right: 12, left: -10, bottom: 0 }}>
        <defs>
          <linearGradient id="scoreFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#33a2ff" stopOpacity={0.5} />
            <stop offset="95%" stopColor="#33a2ff" stopOpacity={0} />
          </linearGradient>
          <linearGradient id="atsFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#00d1b2" stopOpacity={0.4} />
            <stop offset="95%" stopColor="#00d1b2" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
        <XAxis dataKey="label" stroke="rgba(255,255,255,0.4)" fontSize={11} />
        <YAxis domain={[0, 100]} stroke="rgba(255,255,255,0.4)" fontSize={11} />
        <Tooltip contentStyle={tooltipStyle} />
        <Legend wrapperStyle={{ fontSize: 12 }} />
        <Area
          type="monotone"
          dataKey="resumeScore"
          name="Resume"
          stroke="#33a2ff"
          fill="url(#scoreFill)"
          strokeWidth={2.5}
        />
        <Area
          type="monotone"
          dataKey="atsScore"
          name="ATS"
          stroke="#00d1b2"
          fill="url(#atsFill)"
          strokeWidth={2.5}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}

export function SkillsBarChart({ data = [] }) {
  const formatted = Object.entries(data || {}).map(([name, count]) => ({
    name,
    count,
  }));
  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart
        data={formatted}
        layout="vertical"
        margin={{ top: 0, right: 20, left: 20, bottom: 0 }}
      >
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
        <XAxis type="number" stroke="rgba(255,255,255,0.4)" fontSize={11} />
        <YAxis
          type="category"
          dataKey="name"
          width={90}
          stroke="rgba(255,255,255,0.5)"
          fontSize={11}
        />
        <Tooltip contentStyle={tooltipStyle} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
        <Bar dataKey="count" fill="#1c82f5" radius={[0, 6, 6, 0]} barSize={16} />
      </BarChart>
    </ResponsiveContainer>
  );
}

export function CareerFitRadar({ jobs = [] }) {
  const data = jobs.map((j) => ({
    role: j.jobTitle,
    match: j.matchPercentage || 0,
  }));
  return (
    <ResponsiveContainer width="100%" height={300}>
      <RadarChart data={data} outerRadius={110}>
        <PolarGrid stroke="rgba(255,255,255,0.1)" />
        <PolarAngleAxis dataKey="role" tick={{ fill: 'rgba(255,255,255,0.6)', fontSize: 11 }} />
        <Radar
          name="Match %"
          dataKey="match"
          stroke="#7c5cff"
          fill="#7c5cff"
          fillOpacity={0.35}
        />
        <Tooltip contentStyle={tooltipStyle} />
      </RadarChart>
    </ResponsiveContainer>
  );
}

export function UploadsTrendChart({ data = [] }) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <LineChart data={data} margin={{ top: 10, right: 12, left: -10, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
        <XAxis dataKey="date" stroke="rgba(255,255,255,0.4)" fontSize={11} />
        <YAxis stroke="rgba(255,255,255,0.4)" fontSize={11} allowDecimals={false} />
        <Tooltip contentStyle={tooltipStyle} />
        <Line
          type="monotone"
          dataKey="count"
          name="Uploads"
          stroke="#33a2ff"
          strokeWidth={2.5}
          dot={{ r: 3 }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
