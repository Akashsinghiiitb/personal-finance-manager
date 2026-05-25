import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Wallet, Mail, Lock, User, Phone, Eye, EyeOff, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email || !password || !fullName || !phoneNumber) {
      setError('Please fill in all fields');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    const phoneRegex = /^\+?[0-9]{10,15}$/;
    if (!phoneRegex.test(phoneNumber.trim())) {
      setError('Please enter a valid phone number (10 to 15 digits, e.g., +1234567890)');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      await register(email.trim(), password, fullName.trim(), phoneNumber.trim());
      toast.success('Registration successful! Sign in now.');
      navigate('/login');
    } catch (err) {
      setError(err.message || 'Registration failed');
      toast.error('Registration failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-slate-950 px-4">
      {/* Background ambient glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-sky-500/10 rounded-full blur-[100px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-500/10 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md relative z-10 my-8">
        {/* Logo and Header */}
        <div className="flex flex-col items-center mb-8">
          <div className="flex items-center justify-center w-14 h-14 rounded-2xl bg-sky-500 text-white shadow-xl shadow-sky-500/30 mb-4">
            <Wallet className="w-7 h-7" />
          </div>
          <h1 className="text-3xl font-bold text-white heading-font tracking-tight">Syfe</h1>
          <p className="text-slate-400 mt-1.5 text-sm">Control your savings, categories & expenses</p>
        </div>

        {/* Card Form */}
        <div className="bg-slate-900/60 border border-slate-800 backdrop-blur-md rounded-3xl p-8 shadow-2xl">
          <h2 className="text-xl font-semibold text-white mb-6 heading-font">Create Account</h2>

          {error && (
            <div className="mb-4 p-4 text-xs font-medium text-rose-400 bg-rose-500/10 border border-rose-500/20 rounded-xl">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Full Name */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Full Name
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                  <User className="w-5 h-5" />
                </div>
                <input
                  type="text"
                  required
                  placeholder="Akash Singh"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  className="block w-full pl-12 pr-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500 transition-all text-sm"
                />
              </div>
            </div>

            {/* Email Address */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                  <Mail className="w-5 h-5" />
                </div>
                <input
                  type="email"
                  required
                  placeholder="akash@gmail.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="block w-full pl-12 pr-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500 transition-all text-sm"
                />
              </div>
            </div>

            {/* Phone Number */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Phone Number
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                  <Phone className="w-5 h-5" />
                </div>
                <input
                  type="text"
                  required
                  placeholder="+919876543210"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  className="block w-full pl-12 pr-4 py-3 bg-slate-950 border border-slate-800 rounded-xl text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500 transition-all text-sm"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-500">
                  <Lock className="w-5 h-5" />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-12 pr-12 py-3 bg-slate-950 border border-slate-800 rounded-xl text-white placeholder-slate-600 focus:outline-none focus:ring-2 focus:ring-sky-500/50 focus:border-sky-500 transition-all text-sm"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center text-slate-500 hover:text-white transition-colors"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center justify-center w-full py-3 px-4 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-medium rounded-xl transition-all duration-200 shadow-lg shadow-sky-500/25 disabled:opacity-50 text-sm mt-8"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin mr-2" />
                  Creating account...
                </>
              ) : (
                'Register'
              )}
            </button>
          </form>

          {/* Switch to Login */}
          <div className="mt-6 text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="text-sky-400 hover:underline">
              Sign In
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
