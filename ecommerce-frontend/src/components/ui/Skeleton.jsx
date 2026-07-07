import React from 'react';
import './Skeleton.css';

function Skeleton({ variant = 'text', width, height, style, className = '' }) {
  const customStyle = {
    width: width || (variant === 'text' ? '100%' : undefined),
    height: height || (variant === 'text' ? '1em' : undefined),
    ...style
  };

  return (
    <div 
      className={`skeleton-shimmer skeleton-${variant} ${className}`} 
      style={customStyle}
    />
  );
}

export default Skeleton;
