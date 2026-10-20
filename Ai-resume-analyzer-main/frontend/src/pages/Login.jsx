import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { LogIn } from 'lucide-react';
import AuthShell from './AuthShell.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (values) => {
    setLoading(true);
    try {
      const user = await login(values.email, values.password);
      toast.success(`Welcome back, ${user.fullName.split(' ')[0]}!`);
      const dest = location.state?.from?.pathname || '/app/dashboard';
      navigate(dest, { replace: true });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to access your resume dashboard"
      footer={
        <>
          New here?{' '}
          <Link to="/register" className="text-brand-400 hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Email</label>
          <input
            type="email"
            className="input"
            placeholder="you@example.com"
            {...register('email', { required: 'Email is required' })}
          />
          {errors.email && (
            <p className="mt-1 text-xs text-red-400">{errors.email.message}</p>
          )}
        </div>

        <div>
          <div className="flex items-center justify-between">
            <label className="label">Password</label>
            <Link
              to="/forgot-password"
              className="text-xs text-brand-400 hover:underline"
            >
              Forgot password?
            </Link>
          </div>
          <input
            type="password"
            className="input"
            placeholder="••••••••"
            {...register('password', { required: 'Password is required' })}
          />
          {errors.password && (
            <p className="mt-1 text-xs text-red-400">
              {errors.password.message}
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={loading}
          className="btn-primary w-full py-3"
        >
          <LogIn size={18} />
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <div className="mt-6 rounded-xl border border-white/10 bg-white/5 p-3 text-xs text-white/50">
        <p className="font-semibold text-white/70">Demo credentials</p>
        <p>admin@airesume.dev / Admin@12345</p>
        <p>demo@airesume.dev / Demo@12345</p>
      </div>
    </AuthShell>
  );
}
