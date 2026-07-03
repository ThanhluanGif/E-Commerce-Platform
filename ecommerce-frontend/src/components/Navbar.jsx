import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

function Navbar() {
    const { cartItems } = React.useContext(CartContext);
    const { isAuthenticated, username, logout } = React.useContext(AuthContext);
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
        <nav style={{ background: '#2c3e50', padding: '15px', color: '#fff', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px' }}>

            {/* Khối bên trái */}
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <Link to="/" style={{ color: '#fff', textDecoration: 'none', fontWeight: 'bold', fontSize: '18px' }}>TechStore</Link>
                <Link to="/" style={{ color: '#fff', textDecoration: 'none' }}>Trang Chủ</Link>
                <Link to="/products" style={{ color: '#fff', textDecoration: 'none' }}>Sản Phẩm</Link>
                <Link to="/categories" style={{ color: '#fff', textDecoration: 'none' }}>Danh Mục</Link>
            </div>

            {/* Khối ở giữa: Tìm kiếm nhanh */}
            <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '5px', flex: '0 1 350px' }}>
                <input 
                    type="text" 
                    placeholder="Tìm kiếm sản phẩm..." 
                    value={navSearch}
                    onChange={(e) => setNavSearch(e.target.value)}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '4px', border: 'none', outline: 'none', fontSize: '14px' }}
                />
                <button type="submit" style={{ background: '#3498db', color: 'white', border: 'none', padding: '8px 15px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', transition: 'background 0.2s' }}
                        onMouseEnter={(e) => e.target.style.background = '#2980b9'}
                        onMouseLeave={(e) => e.target.style.background = '#3498db'}>
                    Tìm
                </button>
            </form>

            {/* Khối bên phải */}
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                {isAuthenticated ? (
                    <>
                        <span style={{ color: '#ecf0f1' }}>Chào, <strong>{username}</strong>!</span>
                        <Link to="/orders" style={{ color: '#fff', textDecoration: 'none' }}>Đơn hàng</Link>
                        <button onClick={handleLogout} style={{ background: 'transparent', border: '1px solid #fff', color: '#fff', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}>
                            Đăng xuất
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" style={{ color: '#fff', textDecoration: 'none' }}>Đăng Nhập</Link>
                        <Link to="/register" style={{ color: '#fff', textDecoration: 'none' }}>Đăng Ký</Link>
                    </>
                )}
                <Link to="/cart" style={{ color: '#fff', textDecoration: 'none' }}>
                    Giỏ Hàng ({totalItems})
                </Link>
            </div>

        </nav>
    );
}
export default Navbar;
