import React from 'react';
import { Link } from 'react-router-dom';
import { IconChevronRight, IconHome } from '../utils/icons';

/**
 * Breadcrumb component
 * @param {{ items: Array<{ label: string, to?: string }> }} props
 * Usage: <Breadcrumb items={[{ label: 'Trang chủ', to: '/' }, { label: 'Laptop' }]} />
 */
function Breadcrumb({ items = [] }) {
  if (!items.length) return null;

  return (
    <nav className="breadcrumb" aria-label="Breadcrumb">
      <Link to="/" className="breadcrumb-link" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
        <IconHome size={14} />
      </Link>
      {items.map((item, index) => (
        <React.Fragment key={index}>
          <IconChevronRight size={12} className="breadcrumb-separator" />
          {item.to && index < items.length - 1 ? (
            <Link to={item.to}>{item.label}</Link>
          ) : (
            <span className="breadcrumb-current">{item.label}</span>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
}

export default Breadcrumb;
