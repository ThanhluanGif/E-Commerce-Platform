import React, { Component } from 'react';
import { IconWarning } from '../../utils/icons';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught an error:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          padding: '40px var(--space-4)',
          textAlign: 'center',
          maxWidth: '500px',
          margin: '50px auto',
          background: '#fff',
          borderRadius: 'var(--border-radius-sm, 4px)',
          border: '1px solid var(--color-gray-200, #e5e7eb)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ color: 'var(--color-primary, #ee4d2d)', marginBottom: '15px' }}>
            <IconWarning size={48} />
          </div>
          <h2 style={{ fontSize: '20px', fontWeight: 'bold', color: 'var(--color-gray-900)', marginBottom: '10px' }}>
            Đã xảy ra lỗi hệ thống
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--color-gray-600)', marginBottom: '25px', lineHeight: '1.5' }}>
            Rất tiếc, đã xảy ra sự cố ngoài ý muốn. Vui lòng tải lại trang hoặc quay lại trang chủ.
          </p>
          <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <button 
              onClick={() => window.location.reload()} 
              className="btn btn-primary"
              style={{ padding: '8px 20px' }}
            >
              Tải lại trang
            </button>
            <a 
              href="/" 
              className="btn btn-secondary"
              style={{ padding: '8px 20px', textDecoration: 'none' }}
            >
              Về trang chủ
            </a>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
