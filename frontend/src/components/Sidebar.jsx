import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  ArrowLeftRight,
  Tags,
  PiggyBank,
  BarChart3,
  LogOut,
  User,
  Wallet
} from 'lucide-react';

const Sidebar = ({ isOpen, toggleSidebar }) => {
  const { user, logout } = useAuth();

  const links = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
    { to: '/categories', label: 'Categories', icon: Tags },
    { to: '/goals', label: 'Savings Goals', icon: PiggyBank },
    { to: '/reports', label: 'Reports', icon: BarChart3 }
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-sm lg:hidden"
          onClick={toggleSidebar}
        />
      )}

      <aside className={`
        fixed top-0 bottom-0 left-0 z-50 flex flex-col w-64 bg-slate-900 border-r border-slate-800 text-slate-300 transition-transform duration-300 transform
        lg:translate-x-0 lg:static lg:h-screen
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-6 border-b border-slate-800">
          <div className="flex items-center justify-center w-10 h-10 rounded-xl bg-sky-500 text-white shadow-lg shadow-sky-500/30">
            <Wallet className="w-5 h-5" />
          </div>
          <div>
            <h1 className="font-semibold text-lg leading-tight text-white heading-font">Syfe</h1>
            <span className="text-xs text-slate-500">Finance Manager</span>
          </div>
        </div>

        {/* User Card */}
        <div className="px-4 py-5 border-b border-slate-800 bg-slate-950/40">
          <div className="flex items-center gap-3 px-2">
            <div className="flex items-center justify-center w-9 h-9 rounded-full bg-slate-800 border border-slate-700 text-sky-400">
              <User className="w-5 h-5" />
            </div>
            <div className="overflow-hidden">
              <h2 className="text-sm font-medium text-white truncate heading-font">
                {user?.fullName || 'User'}
              </h2>
              <p className="text-xs text-slate-500 truncate">{user?.username}</p>
            </div>
          </div>
        </div>

        {/* Nav Links */}
        <nav className="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
          {links.map((link) => {
            const Icon = link.icon;
            return (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={() => lgScreen() ? null : toggleSidebar()}
                className={({ isActive }) => `
                  flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200
                  ${isActive
                    ? 'bg-sky-500/10 text-sky-400 border-l-4 border-sky-500 pl-3 font-semibold'
                    : 'hover:bg-slate-800/60 hover:text-white border-l-4 border-transparent'}
                `}
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                <span>{link.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* Logout Section */}
        <div className="p-4 border-t border-slate-800">
          <button
            onClick={logout}
            className="flex items-center gap-3 w-full px-4 py-3 rounded-xl text-sm font-medium text-rose-400 hover:bg-rose-500/10 transition-all duration-200"
          >
            <LogOut className="w-5 h-5" />
            <span>Logout</span>
          </button>
        </div>
      </aside>
    </>
  );

  function lgScreen() {
    return window.innerWidth >= 1024;
  }
};

export default Sidebar;
