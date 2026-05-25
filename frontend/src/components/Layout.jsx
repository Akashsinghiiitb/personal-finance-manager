import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import { Menu } from 'lucide-react';

const Layout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      {/* Sidebar navigation */}
      <Sidebar isOpen={sidebarOpen} toggleSidebar={toggleSidebar} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top bar for Mobile Devices */}
        <header className="flex items-center justify-between px-6 py-4 bg-white border-b border-slate-200 lg:hidden flex-shrink-0">
          <div className="flex items-center gap-3">
            <span className="font-semibold text-slate-800 heading-font">Syfe Finance Manager</span>
          </div>
          <button
            onClick={toggleSidebar}
            className="p-2 text-slate-600 hover:bg-slate-100 rounded-lg focus:outline-none"
          >
            <Menu className="w-6 h-6" />
          </button>
        </header>

        {/* Dynamic Nested Content */}
        <main className="flex-1 overflow-y-auto px-6 py-8 md:px-10">
          <div className="max-w-7xl mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;
