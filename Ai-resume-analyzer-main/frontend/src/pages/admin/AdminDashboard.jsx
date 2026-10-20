import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import {
  Users,
  FileText,
  Award,
  Gauge,
  Trash2,
  Power,
  ShieldCheck,
} from 'lucide-react';
import { adminApi } from '../../api/services.js';
import { StatCard, Loader } from '../../components/UI.jsx';
import { UploadsTrendChart } from '../../components/charts/Charts.jsx';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('users');

  const load = async () => {
    try {
      const [s, u] = await Promise.all([adminApi.stats(), adminApi.users()]);
      setStats(s.data.data);
      setUsers(u.data.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleDelete = async (id) => {
    if (!confirm('Delete this user and all their data?')) return;
    try {
      await adminApi.deleteUser(id);
      toast.success('User deleted');
      setUsers((u) => u.filter((x) => x.id !== id));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Could not delete user');
    }
  };

  const handleToggle = async (id) => {
    try {
      await adminApi.toggleUser(id);
      toast.success('User status updated');
      load();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Action failed');
    }
  };

  if (loading) return <Loader label="Loading admin panel…" />;

  return (
    <div className="space-y-8">
      <div className="flex items-center gap-3">
        <div className="rounded-xl bg-purple-600/20 p-2.5 text-purple-300">
          <ShieldCheck size={22} />
        </div>
        <div>
          <h1 className="font-display text-3xl font-bold">Admin Panel</h1>
          <p className="text-current/50">
            Platform-wide analytics and user management
          </p>
        </div>
      </div>

      <div className="grid gap-5 md:grid-cols-4">
        <StatCard
          icon={Users}
          label="Total users"
          value={stats.totalUsers}
          sub={`${stats.activeUsers} active`}
        />
        <StatCard
          icon={FileText}
          label="Total resumes"
          value={stats.totalResumes}
        />
        <StatCard
          icon={Award}
          label="Avg resume score"
          value={stats.averageResumeScore ?? '—'}
        />
        <StatCard
          icon={Gauge}
          label="Avg ATS score"
          value={stats.averageAtsScore ?? '—'}
        />
      </div>

      <div className="card p-6">
        <h3 className="font-display text-lg font-semibold mb-4">
          Upload Activity
        </h3>
        <UploadsTrendChart data={stats.uploadsTrend} />
      </div>

      <div>
        <div className="mb-4 flex gap-2">
          {['users'].map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`btn ${
                tab === t ? 'btn-primary' : 'btn-ghost'
              } text-sm capitalize`}
            >
              {t}
            </button>
          ))}
        </div>

        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-white/10 text-left text-current/50">
                <tr>
                  <th className="px-5 py-3 font-medium">Name</th>
                  <th className="px-5 py-3 font-medium">Email</th>
                  <th className="px-5 py-3 font-medium">Role</th>
                  <th className="px-5 py-3 font-medium">Status</th>
                  <th className="px-5 py-3 font-medium">Joined</th>
                  <th className="px-5 py-3 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u, i) => (
                  <motion.tr
                    key={u.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: i * 0.03 }}
                    className="border-b border-white/5 last:border-0"
                  >
                    <td className="px-5 py-3 font-medium">{u.fullName}</td>
                    <td className="px-5 py-3 text-current/60">{u.email}</td>
                    <td className="px-5 py-3">
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs ${
                          u.role === 'ADMIN'
                            ? 'bg-purple-500/15 text-purple-300'
                            : 'bg-brand-500/15 text-brand-300'
                        }`}
                      >
                        {u.role}
                      </span>
                    </td>
                    <td className="px-5 py-3">
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs ${
                          u.enabled
                            ? 'bg-emerald-500/15 text-emerald-300'
                            : 'bg-red-500/15 text-red-300'
                        }`}
                      >
                        {u.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-current/50">
                      {new Date(u.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-5 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleToggle(u.id)}
                          disabled={u.role === 'ADMIN'}
                          className="btn-ghost p-2 disabled:opacity-30"
                          title="Toggle active"
                        >
                          <Power size={15} />
                        </button>
                        <button
                          onClick={() => handleDelete(u.id)}
                          disabled={u.role === 'ADMIN'}
                          className="btn-ghost p-2 text-red-400 hover:bg-red-500/10 disabled:opacity-30"
                          title="Delete user"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
