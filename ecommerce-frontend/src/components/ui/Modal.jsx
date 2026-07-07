import React, { useEffect } from 'react';
import './Modal.css';

export default function Modal({ isOpen, onClose, title, children, footer }) {
  // Disable body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => { document.body.style.overflow = 'unset'; };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="modal-ui-overlay" onClick={onClose}>
      <div className="modal-ui-container" onClick={(e) => e.stopPropagation()}>
        <div className="modal-ui-header">
          <h3 className="modal-ui-title">{title}</h3>
          <button className="modal-ui-close" onClick={onClose}>✕</button>
        </div>
        <div className="modal-ui-content">
          {children}
        </div>
        {footer && (
          <div className="modal-ui-footer">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
