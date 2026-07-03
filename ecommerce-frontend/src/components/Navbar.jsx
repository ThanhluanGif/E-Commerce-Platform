import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

function Navbar() {
    const { cartItems } = React.useContext(CartContext);
    const { isAuthenticated, isAdmin, logout } = React.useContext(AuthContext);
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
        <nav style={{ 
            background: '#1e293b', 
            padding: '12px 24px', 
            color: '#fff', 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            flexWrap: 'wrap', 
            gap: '15px',
            boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)',
            borderRadius: '0 0 8px 8px',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            marginBottom: '20px'
        }}>

            {/* Left Block: Logo & Menu links */}
            <div style={{ display: 'flex', gap: '25px', alignItems: 'center' }}>
                <Link to="/" style={{ 
                    color: '#3b82f6', 
                    textDecoration: 'none', 
                    fontWeight: '800', 
                    fontSize: '22px',
                    letterSpacing: '0.5px',
                    background: 'linear-gradient(135deg, #60a5fa, #3b82f6)',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent'
                }}>
                    TechStore
                </Link>
                <div style={{ display: 'flex', gap: '15px', fontSize: '14px', fontWeight: '500' }}>
                    <Link to="/" className="nav-link" style={{ color: '#cbd5e1', textDecoration: 'none', transition: 'color 0.2s' }}>Trang Chủ</Link>
                    <Link to="/products" className="nav-link" style={{ color: '#cbd5e1', textDecoration: 'none', transition: 'color 0.2s' }}>Sản Phẩm</Link>
                    <Link to="/categories" className="nav-link" style={{ color: '#cbd5e1', textDecoration: 'none', transition: 'color 0.2s' }}>Danh Mục</Link>
                </div>
            </div>

            {/* Middle Block: Search bar */}
            <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '8px', flex: '0 1 380px', position: 'relative' }}>
                <input 
                    type="text" 
                    placeholder="Tìm kiếm sản phẩm..." 
                    value={navSearch}
                    onChange={(e) => setNavSearch(e.target.value)}
                    style={{ 
                        width: '100%', 
                        padding: '8px 14px', 
                        borderRadius: '20px', 
                        border: '1px solid #475569', 
                        background: '#334155',
                        color: 'white',
                        outline: 'none', 
                        fontSize: '13px',
                        transition: 'border-color 0.2s, box-shadow 0.2s'
                    }}
                    onFocus={(e) => { e.target.style.borderColor = '#3b82f6'; e.target.style.boxShadow = '0 0 0 3px rgba(59,130,246,0.3)'; }}
                    onBlur={(e) => { e.target.style.borderColor = '#475569'; e.target.style.boxShadow = 'none'; }}
                />
                <button type="submit" style={{ 
                    background: '#3b82f6', 
                    color: 'white', 
                    border: 'none', 
                    padding: '8px 16px', 
                    borderRadius: '20px', 
                    cursor: 'pointer', 
                    fontWeight: 'bold', 
                    fontSize: '13px',
                    transition: 'background 0.2s' 
                }}
                        onMouseEnter={(e) => e.target.style.background = '#2563eb'}
                        onMouseLeave={(e) => e.target.style.background = '#3b82f6'}>
                    Tìm
                </button>
            </form>

            {/* Right Block: User settings & Cart info */}
            <div style={{ display: 'flex', gap: '15px', alignItems: 'center', fontSize: '14px' }}>
                {isAuthenticated ? (
                    <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                        {isAdmin && (
                            <Link to="/admin" style={{ 
                                color: '#f59e0b', 
                                textDecoration: 'none', 
                                border: '1px solid #f59e0b', 
                                padding: '6px 12px', 
                                borderRadius: '20px', 
                                fontWeight: 'bold', 
                                fontSize: '12px', 
                                transition: 'all 0.2s' 
                            }}
                                  onMouseEnter={(e) => { e.target.style.background = '#f59e0b'; e.target.style.color = '#1e2937'; }}
                                  onMouseLeave={(e) => { e.target.style.background = 'transparent'; e.target.style.color = '#f59e0b'; }}>
                                Quản trị 👑
                            </Link>
                        )}
                        <Link to="/profile" style={{ color: '#f8fafc', textDecoration: 'none', fontWeight: '500' }}>Tài khoản</Link>
                        <Link to="/orders" style={{ color: '#cbd5e1', textDecoration: 'none' }}>Đơn hàng</Link>
                        <button onClick={handleLogout} style={{ 
                            background: 'transparent', 
                            border: '1px solid #ef4444', 
                            color: '#ef4444', 
                            padding: '6px 14px', 
                            borderRadius: '20px', 
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '600',
                            transition: 'all 0.2s'
                        }}
                                onMouseEnter={(e) => { e.target.style.background = '#ef4444'; e.target.style.color = 'white'; }}
                                onMouseLeave={(e) => { e.target.style.background = 'transparent'; e.target.style.color = '#ef4444'; }}>
                            Đăng xuất
                        </button>
                    </div>
                ) : (
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                        <Link to="/login" style={{ color: '#cbd5e1', textDecoration: 'none', fontWeight: '500' }}>Đăng nhập</Link>
                        <Link to="/register" style={{ 
                            background: '#3b82f6', 
                            color: 'white', 
                            textDecoration: 'none', 
                            padding: '6px 16px', 
                            borderRadius: '20px', 
                            fontWeight: 'bold',
                            fontSize: '13px'
                        }}>
                            Đăng ký
                        </Link>
                    </div>
                )}
                
                {/* Cart link pill */}
                <Link to="/cart" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '8px', 
                    color: '#fff', 
                    textDecoration: 'none', 
                    background: '#0f172a', 
                    padding: '8px 16px', 
                    borderRadius: '20px', 
                    fontWeight: 'bold', 
                    fontSize: '13px',
                    transition: 'background 0.2s',
                    border: '1px solid #334155'
                }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#020617'}
                      onMouseLeave={(e) => e.currentTarget.style.background = '#0f172a'}>
                    <span>🛒</span>
                    <span style={{ display: 'none', '@media (min-width: 640px)': { display: 'inline' } }}>Giỏ hàng</span>
                    <span style={{ 
                        background: '#ef4444', 
                        color: 'white', 
                        fontSize: '11px', 
                        borderRadius: '10px', 
                        padding: '2px 6px',
                        fontWeight: '800'
                    }}>
                        {totalItems}
                    </span>
                </Link>
            </div>
            
            {/* CSS styles to handle link hover highlights */}
            <style>{`
                .nav-link:hover {
                    color: #ffffff !important;
                }
            `}</style>
        </nav>
    );
}

export default Navbar;
