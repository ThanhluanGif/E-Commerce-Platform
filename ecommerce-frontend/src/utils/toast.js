import React from 'react';
import { IconCheckCircle, IconXCircle, IconWarning, IconInfo, IconClose } from './icons';

const ToastContext = React.createContext();

const TOAST_ICONS = {
  success: IconCheckCircle,
  error: IconXCircle,
  warning: IconWarning,
  info: IconInfo,
};

const TOAST_COLORS = {
  success: 'var(--color-success)',
  error: 'var(--color-danger)',
  warning: 'var(--color-warning)',
  info: 'var(--color-info)',
};

function ToastItem({ toast, onRemove }) {
  const [exiting, setExiting] = React.useState(false);
  const Icon = TOAST_ICONS[toast.type] || IconInfo;

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setExiting(true);
      setTimeout(() => onRemove(toast.id), 300);
    }, toast.duration || 3000);
    return () => clearTimeout(timer);
  }, [toast.id, toast.duration, onRemove]);

  return (
    <div className={`toast toast-${toast.type} ${exiting ? 'toast-exit' : ''}`}>
      <Icon size={20} color={TOAST_COLORS[toast.type]} />
      <div style={{ flex: 1 }}>
        {toast.title && (
          <div style={{ fontWeight: 600, fontSize: 'var(--font-size-base)', marginBottom: 2 }}>
            {toast.title}
          </div>
        )}
        <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-gray-600)' }}>
          {toast.message}
        </div>
      </div>
      <button
        onClick={() => { setExiting(true); setTimeout(() => onRemove(toast.id), 300); }}
        style={{ color: 'var(--color-gray-400)', padding: 4, flexShrink: 0 }}
      >
        <IconClose size={14} />
      </button>
    </div>
  );
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = React.useState([]);

  const removeToast = React.useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  const addToast = React.useCallback((message, type = 'info', options = {}) => {
    const id = Date.now() + Math.random();
    setToasts(prev => [...prev, { id, message, type, ...options }]);
  }, []);

  const toast = React.useMemo(() => ({
    success: (message, options) => addToast(message, 'success', options),
    error: (message, options) => addToast(message, 'error', options),
    warning: (message, options) => addToast(message, 'warning', options),
    info: (message, options) => addToast(message, 'info', options),
  }), [addToast]);

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div className="toast-container">
        {toasts.map(t => (
          <ToastItem key={t.id} toast={t} onRemove={removeToast} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = React.useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}

export default ToastContext;
