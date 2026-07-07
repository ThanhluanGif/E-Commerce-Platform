import React from 'react';
import './Button.css';

export default function Button({ 
  children, 
  variant = 'primary', 
  size = 'md', 
  loading = false, 
  disabled = false, 
  type = 'button',
  onClick,
  style,
  ...props 
}) {
  return (
    <button
      type={type}
      className={`btn-ui btn-ui-${variant} btn-ui-${size} ${loading ? 'btn-ui-loading' : ''}`}
      disabled={disabled || loading}
      onClick={onClick}
      style={style}
      {...props}
    >
      {loading && <span className="btn-ui-spinner" />}
      <span className="btn-ui-content">{children}</span>
    </button>
  );
}
