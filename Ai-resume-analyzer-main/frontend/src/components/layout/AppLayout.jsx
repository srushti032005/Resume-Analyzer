import { useState } from 'react';
import { NavLink, useNavigate, Outlet } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard,
  UploadCloud,
  FileText,
  Briefcase,
  Shield,
  LogOut,
  Sun,
  Moon,
  Menu,
  X,
  Sparkles,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';
import { useTheme } from '../../context/ThemeContext.jsx';

const navItems = [
  { to: '/app/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/app/upload', label: 'Upload Resume', icon: UploadCloud },
  { to: '/app/resumes', label: 'My Resumes', icon: FileText },
  { to: '/app/job-match', label: 'Job Match', icon: Briefcase },
];

export default function AppLayout() {
  const { user, logout, isAdmin } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const SidebarContent = () => (
    <div className="flex h-full flex-col">
      <div className="flex items-center gap-2.5 px-6 py-6">
        <div className="rounded-xl bg-brand-600 p-2 shadow-glow">
          <Sparkles size={20} className="text-white" />
        </div>
        <div>
          <p className="font-display font-bold leading-tight">AI Resume</p>
          <p className="text-xs text-current/40 leading-tight">Analyzer</p>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={() => setMobileOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition ${
                isActive
                  ? 'bg-brand-600/20 text-brand-300 border border-brand-500/20'
                  : 'text-current/60 hover:bg-white/5 hover:text-current'
              }`
            }
          >
            <item.icon size={18} />
            {item.label}
          </NavLink>
        ))}

        {isAdmin && (
          <NavLink
            to="/app/admin"
            onClick={() => setMobileOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition ${
                isActive
                  ? 'bg-purple-600/20 text-purple-300 border border-purple-500/20'
                  : 'text-current/60 hover:bg-white/5 hover:text-current'
              }`
            }
          >
            <Shield size={18} />
            Admin Panel
          </NavLink>
        )}
      </nav>

      <div className="p-3">
        <div className="card p-4">
          <p className="text-sm font-semibold truncate">{user?.fullName}</p>
          <p className="text-xs text-current/40 truncate">{user?.email}</p>
          <span className="pill mt-2">{user?.role}</span>
          <button
            onClick={handleLogout}
            className="btn-ghost mt-3 w-full text-sm py-2"
          >
            <LogOut size={16} /> Sign out
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-mesh">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-72 border-r border-white/5 glass lg:block">
        <SidebarContent />
      </aside>

      {/* Mobile drawer */}
      <AnimatePresence>
        {mobileOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setMobileOpen(false)}
              className="fixed inset-0 z-40 bg-black/60 lg:hidden"
            />
            <motion.aside
              initial={{ x: -300 }}
              animate={{ x: 0 }}
              exit={{ x: -300 }}
              transition={{ type: 'spring', damping: 24 }}
              className="fixed inset-y-0 left-0 z-50 w-72 glass border-r border-white/10 lg:hidden"
            >
              <SidebarContent />
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <div className="lg:pl-72">
        <header className="sticky top-0 z-20 flex items-center justify-between border-b border-white/5 glass px-5 py-4">
          <button
            onClick={() => setMobileOpen(true)}
            className="btn-ghost p-2 lg:hidden"
          >
            <Menu size={20} />
          </button>
          <div className="hidden lg:block" />
          <button onClick={toggleTheme} className="btn-ghost p-2.5">
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
        </header>

        <main className="mx-auto max-w-7xl px-5 py-8">
          <Outlet />
        </main>
      </div>

      <button
        onClick={() => setMobileOpen(false)}
        className={`fixed right-5 top-4 z-50 lg:hidden ${
          mobileOpen ? 'block' : 'hidden'
        }`}
      >
        <X size={20} />
      </button>
    </div>
  );
}
