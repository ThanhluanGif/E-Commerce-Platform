import React from 'react';
import NavbarLogin from './NavbarLogin';
import { Outlet } from 'react-router-dom';

const AuthLayout = () => {
  return (
    <>
      <NavbarLogin title="TECHSTORE" />
      <main>
        <Outlet />
      </main>
    </>
  );
};

export default AuthLayout;
