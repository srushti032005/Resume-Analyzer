import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { authApi } from '../api/services.js';
import { tokenStore } from '../api/client.js';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const bootstrap = useCallback(async () => {
    if (!tokenStore.access) {
      setLoading(false);
      return;
    }
    try {
      const { data } = await authApi.me();
      setUser(data.data);
    } catch {
      tokenStore.clear();
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    bootstrap();
  }, [bootstrap]);

  const persistSession = (payload) => {
    tokenStore.set(payload.accessToken, payload.refreshToken);
    setUser(payload.user);
  };

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password });
    persistSession(data.data);
    return data.data.user;
  };

  const register = async (fullName, email, password) => {
    const { data } = await authApi.register({ fullName, email, password });
    persistSession(data.data);
    return data.data.user;
  };

  const logout = async () => {
    try {
      if (tokenStore.refresh) await authApi.logout(tokenStore.refresh);
    } catch {
      /* ignore */
    }
    tokenStore.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN',
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
