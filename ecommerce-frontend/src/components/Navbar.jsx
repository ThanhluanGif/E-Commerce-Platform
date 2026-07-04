import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

function Navbar() {
    const { cartItems } = React.useContext(CartContext);
    const { isAuthenticated, username, isAdmin, logout } = React.useContext(AuthContext);
    const navigate = useNavigate();
    const [navSearch, setNavSearch] = React.useState('');

    const totalItems = cartItems.reduce((total, item) => total + item.quantity, 0);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        if (navSearch.trim() !== '') {
            navigate(`/products?name=${encodeURIComponent(navSearch.trim())}`);
            setNavSearch('');
        }
    };

    return (
        <header style={{ background: '#f94e30', fontFamily: 'system-ui, -apple-system, sans-serif', borderBottom: '1px solid #e11d48' }}>
            
            {/* 1. TOP MINI BAR (Shopee Style) */}
            <div style={{ 
                maxWidth: '1200px', 
                margin: '0 auto', 
                padding: '4px 20px', 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                fontSize: '12px', 
                color: 'rgba(255,255,255,0.9)' 
            }}>
                <div style={{ display: 'flex', gap: '15px' }}>
                    <Link to="/seller" style={{ color: 'white', textDecoration: 'none' }}>Kênh Người Bán</Link>
                    <span style={{ cursor: 'pointer' }}>Tải ứng dụng</span>
                    <span style={{ cursor: 'pointer' }}>Kết nối 🅵 🅸</span>
                </div>
                
                <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                    <span style={{ cursor: 'pointer' }}>🔔 Thông Báo</span>
                    <span style={{ cursor: 'pointer' }}>❓ Trợ Giúp</span>
                    
                    {isAuthenticated ? (
                        <div style={{ display: 'flex', gap: '15px', alignItems: 'center', fontWeight: 'bold' }}>
                            <Link to="/messages" style={{ color: 'white', textDecoration: 'none' }}>
                                💬 Chat
                            </Link>
                            <Link to="/wishlist" style={{ color: 'white', textDecoration: 'none' }}>
                                ❤️ Yêu thích
                            </Link>
                            <Link to="/profile" style={{ color: 'white', textDecoration: 'none' }}>
                                👤 {username}
                            </Link>
                            {isAdmin && (
                                <Link to="/admin" style={{ color: '#fde047', textDecoration: 'none' }}>
                                    👑 Quản Trị
                                </Link>
                            )}
                            <span onClick={handleLogout} style={{ cursor: 'pointer', color: 'rgba(255,255,255,0.8)' }}>Đăng Xuất</span>
                        </div>
                    ) : (
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <Link to="/login" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold' }}>Đăng Nhập</Link>
                            <span style={{ opacity: 0.5 }}>|</span>
                            <Link to="/register" style={{ color: 'white', textDecoration: 'none', fontWeight: 'bold' }}>Đăng Ký</Link>
                        </div>
                    )}
                </div>
            </div>

            {/* 2. MAIN HEADER BAR */}
            <div style={{ 
                maxWidth: '1200px', 
                margin: '0 auto', 
                padding: '16px 20px 20px 20px', 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                flexWrap: 'wrap', 
                gap: '20px' 
            }}>
                
                {/* Logo Section */}
                <Link to="/" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '8px', 
                    textDecoration: 'none', 
                    color: 'white', 
                    fontWeight: '800', 
                    fontSize: '28px' 
                }}>
                    <span style={{ fontSize: '32px' }}>🛍️</span>
                    <span>TechStore</span>
                </Link>

                {/* Big Search Bar */}
                <div style={{ flex: '0 1 650px', display: 'flex', flexDirection: 'column', gap: '5px' }}>
                    <form onSubmit={handleSearchSubmit} style={{ 
                        display: 'flex', 
                        background: 'white', 
                        padding: '3px', 
                        borderRadius: '4px', 
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)' 
                    }}>
                        <input 
                            type="text" 
                            placeholder="Đăng ký ngay để nhận voucher giảm 50%..." 
                            value={navSearch}
                            onChange={(e) => setNavSearch(e.target.value)}
                            style={{ 
                                width: '100%', 
                                border: 'none', 
                                outline: 'none', 
                                padding: '10px 15px', 
                                fontSize: '14px',
                                color: '#333'
                            }}
                        />
                        <button type="submit" style={{ 
                            background: '#f94e30', 
                            color: 'white', 
                            border: 'none', 
                            padding: '0 25px', 
                            borderRadius: '3px', 
                            cursor: 'pointer', 
                            fontWeight: 'bold',
                            fontSize: '14px',
                            transition: 'background 0.2s' 
                        }}
                                onMouseEnter={(e) => e.target.style.background = '#e13b1d'}
                                onMouseLeave={(e) => e.target.style.background = '#f94e30'}>
                            🔍
                        </button>
                    </form>

                    {/* Quick Search keywords */}
                    <div style={{ display: 'flex', gap: '12px', fontSize: '11px', color: 'rgba(255,255,255,0.85)', paddingLeft: '5px' }}>
                        <Link to="/products?name=Tivi" style={{ color: 'white', textDecoration: 'none' }}>Tivi</Link>
                        <Link to="/products?name=Laptop" style={{ color: 'white', textDecoration: 'none' }}>Laptop</Link>
                        <Link to="/products?name=Sony" style={{ color: 'white', textDecoration: 'none' }}>Sony</Link>
                        <Link to="/products?name=Smart" style={{ color: 'white', textDecoration: 'none' }}>Smart</Link>
                        <Link to="/products?name=Bàn" style={{ color: 'white', textDecoration: 'none' }}>Bàn Phím</Link>
                    </div>
                </div>

                {/* Cart Icon with badge */}
                <Link to="/cart" style={{ 
                    position: 'relative', 
                    textDecoration: 'none', 
                    color: 'white', 
                    fontSize: '26px', 
                    padding: '8px' 
                }}>
                    🛒
                    {totalItems > 0 && (
                        <span style={{ 
                            position: 'absolute', 
                            top: '0', 
                            right: '-5px', 
                            background: 'white', 
                            color: '#f94e30', 
                            fontSize: '11px', 
                            fontWeight: '800', 
                            borderRadius: '10px', 
                            padding: '1px 6px',
                            border: '2px solid #f94e30',
                            minWidth: '18px',
                            textAlign: 'center'
                        }}>
                            {totalItems}
                        </span>
                    )}
                </Link>

            </div>

        </header>
    );
}

export default Navbar;
