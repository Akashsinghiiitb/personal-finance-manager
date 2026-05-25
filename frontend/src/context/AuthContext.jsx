import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Check login status on reload
  const checkAuthStatus = async () => {
    try {
      const response = await api.get('/auth/me');
      setUser(response.data);
      setIsAuthenticated(true);
    } catch (error) {
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    checkAuthStatus();

    // Listen for global unauthorized events (session expiry, etc.)
    const handleUnauthorized = () => {
      setUser(null);
      setIsAuthenticated(false);
    };

    window.addEventListener('auth-unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('auth-unauthorized', handleUnauthorized);
    };
  }, []);

  const login = async (username, password) => {
    setIsLoading(true);
    try {
      await api.post('/auth/login', { username, password });
      // Fetch user profile info on successful login
      const response = await api.get('/auth/me');
      setUser(response.data);
      setIsAuthenticated(true);
      return { success: true };
    } catch (error) {
      setUser(null);
      setIsAuthenticated(false);
      const msg = error.response?.data?.message || 'Invalid username or password';
      throw new Error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (username, password, fullName, phoneNumber) => {
    setIsLoading(true);
    try {
      const response = await api.post('/auth/register', {
        username,
        password,
        fullName,
        phoneNumber
      });
      return response.data;
    } catch (error) {
      const msg = error.response?.data?.message || 'Registration failed';
      throw new Error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      setIsAuthenticated(false);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, isLoading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
