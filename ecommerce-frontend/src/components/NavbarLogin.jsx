import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { IconHome, IconStore } from '../utils/icons';
import './NavbarLogin.css';

const NavbarLogin = ({ title }) => {
    const navigate = useNavigate();

    return (
        <nav className="navbar-login">
            <div className="nav-left">
                <button onClick={() => navigate('/')} className="back-button">
                    <IconHome size={18} className="back-icon" />
                    <span className="back-text">Quay lại</span>
                </button>
            </div>

            <div className="nav-center">
                <Link to="/" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <IconStore size={28} color="#ee4d2d" />
                    <h1 className="navbar-title" style={{
                        color: '#ee4d2d',
                        fontWeight: 800,
                        margin: 0,
                        fontSize: '28px',
                        letterSpacing: '-0.02em'
                    }}>
                        {title || 'E-Commerce'}
                    </h1>
                </Link>
            </div>

            <div className="nav-right"></div>
        </nav>
    );
};

export default NavbarLogin;