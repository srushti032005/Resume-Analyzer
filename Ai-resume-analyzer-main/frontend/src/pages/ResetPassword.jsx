import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { KeyRound } from 'lucide-react';
import AuthShell from './AuthShell.jsx';
import { authApi } from '../api/services.js';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const token = params.get('token') || '';
  const { register, handleSubmit, watch, formState: { errors } } = useForm();
  const pwd = watch('newPassword');

  const onSubmit = async (values) => {
    setLoading(true);
    try {
      await authApi.resetPassword(token, values.newPassword);
      toast.success('Password updated — please sign in');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Reset failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      title="Set a new password"
      subtitle="Choose a strong password for your account"
      footer={<Link to="/login" className="text-brand-400 hover:underline">Back to sign in</Link>}
    >
      {!token ? (
        <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-300">
          Missing or invalid reset token. Please use the link from your email.
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="label">New password</label>
            <input
              type="password"
              className="input"
              placeholder="••••••••"
              {...register('newPassword', {
                required: 'Password is required',
                minLength: { value: 8, message: 'Minimum 8 characters' },
                pattern: {
                  value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#]).+$/,
                  message: 'Need upper, lower, digit & special character',
                },
              })}
            />
            {errors.newPassword && (
              <p className="mt-1 text-xs text-red-400">{errors.newPassword.message}</p>
            )}
          </div>
          <div>
            <label className="label">Confirm password</label>
            <input
              type="password"
              className="input"
              placeholder="••••••••"
              {...register('confirm', {
                required: 'Please confirm',
                validate: (v) => v === pwd || 'Passwords do not match',
              })}
            />
            {errors.confirm && (
              <p className="mt-1 text-xs text-red-400">{errors.confirm.message}</p>
            )}
          </div>
          <button type="submit" disabled={loading} className="btn-primary w-full py-3">
            <KeyRound size={18} />
            {loading ? 'Updating…' : 'Update password'}
          </button>
        </form>
      )}
    </AuthShell>
  );
}
