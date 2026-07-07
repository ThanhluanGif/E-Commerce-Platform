import React from 'react';
import NavbarLogin from './NavbarLogin';
import { Outlet } from 'react-router-dom';

const AuthLayout = () => {
  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-gray-100)' }}>
      <NavbarLogin title="E-Commerce" />
      <main>
        <Outlet />
      </main>
    </div>
  );
};

export default AuthLayout;
