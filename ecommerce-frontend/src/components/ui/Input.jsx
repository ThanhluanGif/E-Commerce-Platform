import React from 'react';
import './Input.css';

export default function Input({
  label,
  error,
  type = 'text',
  placeholder,
  value,
  onChange,
  disabled = false,
  required = false,
  ...props
}) {
  return (
    <div className={`input-ui-group ${error ? 'input-ui-has-error' : ''}`}>
      {label && (
        <label className="input-ui-label">
          {label} {required && <span className="input-ui-required">*</span>}
        </label>
      )}
      <input
        type={type}
        className="input-ui-field"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        disabled={disabled}
        required={required}
        {...props}
      />
      {error && <span className="input-ui-error-msg">{error}</span>}
    </div>
  );
}
