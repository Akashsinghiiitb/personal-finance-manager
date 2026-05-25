import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Plus, Trash2, Shield, Tags, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Creation form state
  const [name, setName] = useState('');
  const [type, setType] = useState('EXPENSE');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await api.get('/categories');
      setCategories(response.data.categories || []);
    } catch (err) {
      console.error('Error fetching categories:', err);
      toast.error('Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!name.trim()) {
      toast.error('Category name is required');
      return;
    }

    setIsSubmitting(true);
    try {
      await api.post('/categories', { name: name.trim(), type });
      toast.success('Custom category created successfully!');
      setName('');
      fetchCategories();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to create category';
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (catName) => {
    if (!window.confirm(`Are you sure you want to delete the category "${catName}"?`)) {
      return;
    }

    try {
      await api.delete(`/categories/${encodeURIComponent(catName)}`);
      toast.success('Category deleted successfully!');
      fetchCategories();
    } catch (err) {
      const msg = err.response?.data?.message || 'Cannot delete category. It might be used by active transactions.';
      toast.error(msg);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-10 h-10 text-sky-500 animate-spin" />
          <p className="text-slate-500 text-sm font-medium">Fetching categories list...</p>
        </div>
      </div>
    );
  }

  // Group categories
  const incomeCats = categories.filter(c => c.type === 'INCOME');
  const expenseCats = categories.filter(c => c.type === 'EXPENSE');

  return (
    <div className="space-y-8 animate-slide-up">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-900 heading-font">Categories</h1>
        <p className="text-slate-500 mt-1">Manage global default and custom financial categories.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Creation form */}
        <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm h-fit">
          <h2 className="text-lg font-bold text-slate-900 heading-font mb-6">Create Custom Category</h2>
          
          <form onSubmit={handleCreate} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Category Name
              </label>
              <input
                type="text"
                maxLength={50}
                required
                placeholder="e.g. Shopping"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Transaction Type
              </label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="block w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-800 focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 transition-all text-sm"
              >
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center justify-center w-full py-2.5 px-4 bg-sky-500 hover:bg-sky-400 active:bg-sky-600 text-white font-medium rounded-xl transition-all duration-200 shadow-md shadow-sky-500/10 disabled:opacity-50 text-sm"
            >
              {isSubmitting ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <>
                  <Plus className="w-4 h-4 mr-1.5" />
                  <span>Add Category</span>
                </>
              )}
            </button>
          </form>
        </div>

        {/* Categories display list */}
        <div className="lg:col-span-2 space-y-8">
          
          {/* Income Categories */}
          <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-6">
              <Tags className="w-5 h-5 text-emerald-500" />
              <h2 className="text-lg font-bold text-slate-900 heading-font">Income Categories</h2>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {incomeCats.map((cat) => (
                <div 
                  key={cat.name} 
                  className={`flex items-center justify-between p-4 rounded-2xl border transition-all duration-200 ${
                    cat.isCustom 
                      ? 'bg-slate-50 border-slate-100 hover:border-slate-200' 
                      : 'bg-emerald-50/20 border-emerald-500/10'
                  }`}
                >
                  <span className="font-semibold text-sm text-slate-800">{cat.name}</span>
                  
                  {cat.isCustom ? (
                    <button
                      onClick={() => handleDelete(cat.name)}
                      className="p-1.5 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-all"
                      title="Delete category"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  ) : (
                    <span className="flex items-center gap-1 text-[10px] font-semibold text-emerald-600 bg-emerald-100/40 px-2 py-0.5 rounded-full uppercase tracking-wider">
                      <Shield className="w-3 h-3" />
                      <span>Default</span>
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Expense Categories */}
          <div className="bg-white border border-slate-100 rounded-3xl p-6 shadow-sm">
            <div className="flex items-center gap-2 mb-6">
              <Tags className="w-5 h-5 text-rose-500" />
              <h2 className="text-lg font-bold text-slate-900 heading-font">Expense Categories</h2>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {expenseCats.map((cat) => (
                <div 
                  key={cat.name} 
                  className={`flex items-center justify-between p-4 rounded-2xl border transition-all duration-200 ${
                    cat.isCustom 
                      ? 'bg-slate-50 border-slate-100 hover:border-slate-200' 
                      : 'bg-rose-50/20 border-rose-500/10'
                  }`}
                >
                  <span className="font-semibold text-sm text-slate-800">{cat.name}</span>
                  
                  {cat.isCustom ? (
                    <button
                      onClick={() => handleDelete(cat.name)}
                      className="p-1.5 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-all"
                      title="Delete category"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  ) : (
                    <span className="flex items-center gap-1 text-[10px] font-semibold text-rose-600 bg-rose-100/40 px-2 py-0.5 rounded-full uppercase tracking-wider">
                      <Shield className="w-3 h-3" />
                      <span>Default</span>
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

export default Categories;
