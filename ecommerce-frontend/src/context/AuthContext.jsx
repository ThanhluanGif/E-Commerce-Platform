import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('jwtToken') || null);
  const [userId, setUserId] = useState(localStorage.getItem('userId') || null);
  const [username, setUsername] = useState(localStorage.getItem('username') || null);
  const [userRole, setUserRole] = useState(localStorage.getItem('userRole') || null);

  const isAuthenticated = !!token;
  const isAdmin = userRole === 'ADMIN';

  // Listen for storage events (e.g. login from login page)
  useEffect(() => {
    const handleStorageChange = () => {
      setToken(localStorage.getItem('jwtToken'));
      setUserId(localStorage.getItem('userId'));
      setUsername(localStorage.getItem('username'));
      setUserRole(localStorage.getItem('userRole'));
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const login = (jwtToken, user) => {
    localStorage.setItem('jwtToken', jwtToken);
    localStorage.setItem('userId', user.id);
    localStorage.setItem('username', user.username);
    localStorage.setItem('userRole', user.role);

    setToken(jwtToken);
    setUserId(user.id);
    setUsername(user.username);
    setUserRole(user.role);
    
    window.dispatchEvent(new Event('storage'));
  };

  const logout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');

    setToken(null);
    setUserId(null);
    setUsername(null);
    setUserRole(null);
    
    window.dispatchEvent(new Event('storage'));
  };

  return (
    <AuthContext.Provider value={{ token, userId, username, userRole, isAuthenticated, isAdmin, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
