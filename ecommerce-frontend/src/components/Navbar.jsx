import React, { useEffect, useState, useRef, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';
import NotificationBell from './NotificationBell';
import {
  IconSearch, IconCart, IconUser, IconHeart,
  IconMessage, IconStore, IconPackage, IconLogout,
  IconDashboard, IconChevronDown, IconSettings, IconTrash
} from '../utils/icons';
import './Navbar.css';

function Navbar() {
    const { cartItems } = useContext(CartContext);
    const { isAuthenticated, username, isAdmin, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const [navSearch, setNavSearch] = useState('');
    const [showDropdown, setShowDropdown] = useState(false);
    const [suggestions, setSuggestions] = useState([]);
    const [history, setHistory] = useState([]);
    
    const searchRef = useRef(null);

    const totalItems = cartItems.reduce((total, item) => total + item.quantity, 0);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Fetch search history from backend
    const fetchHistory = () => {
        if (!isAuthenticated) return;
        api.get('/api/search/history')
            .then(res => {
                if (res && res.data && res.data.success) {
                    setHistory(res.data.data || []);
                }
            })
            .catch(err => console.error("Error loading search history:", err));
    };

    useEffect(() => {
        if (isAuthenticated) {
            fetchHistory();
        } else {
            setHistory([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAuthenticated]);

    // Autocomplete query with 300ms debounce
    useEffect(() => {
        if (navSearch.trim() === '') {
            setSuggestions([]);
            return;
        }

        const delayDebounce = setTimeout(() => {
            api.get(`/api/search/suggestions?q=${encodeURIComponent(navSearch.trim())}`)
                .then(res => {
                    if (res && res.data && res.data.success) {
                        setSuggestions(res.data.data || []);
                    }
                })
                .catch(err => console.error("Error fetching search suggestions:", err));
        }, 300);

        return () => clearTimeout(delayDebounce);
    }, [navSearch]);

    // Handle click outside to close dropdown
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (searchRef.current && !searchRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        handleSearch(navSearch);
    };

    const handleSearch = (query) => {
        if (!query.trim()) return;
        const cleanQuery = query.trim();

        // Save to search history (silently)
        if (isAuthenticated) {
            api.post(`/api/search/history?query=${encodeURIComponent(cleanQuery)}`)
                .then(() => fetchHistory())
                .catch(err => console.error(err));
        }

        navigate(`/products?name=${encodeURIComponent(cleanQuery)}`);
        setShowDropdown(false);
        setNavSearch('');
    };

    const handleClearHistory = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!isAuthenticated) return;
        api.delete('/api/search/history')
            .then(() => setHistory([]))
            .catch(err => console.error(err));
    };

    return (
        <header className="header">
            {/* 1. TOP MINI BAR */}
            <div className="header-topbar">
                <div className="container header-topbar-inner">
                    <div className="topbar-left">
                        <Link to="/seller" className="topbar-link">
                            <IconStore size={14} />
                            <span>Kênh Người Bán</span>
                        </Link>
                        <span className="topbar-divider hide-mobile" />
                        <span className="topbar-link hide-mobile">Tải ứng dụng</span>
                        <span className="topbar-divider hide-mobile" />
                        <span className="topbar-link hide-mobile">Kết nối</span>
                    </div>

                    <div className="topbar-right">
                        <NotificationBell />
                        <span className="topbar-divider hide-mobile" />
                        <span className="topbar-link hide-mobile">Trợ Giúp</span>
                        <span className="topbar-divider" />

                        {isAuthenticated ? (
                            <div className="topbar-user">
                                <div className="topbar-avatar">
                                    <IconUser size={14} />
                                </div>
                                <span>{username}</span>
                                <IconChevronDown size={12} />

                                <div className="topbar-dropdown">
                                    <Link to="/profile" className="topbar-dropdown-item">
                                        <IconUser size={16} /> Tài khoản của tôi
                                    </Link>
                                    <Link to="/orders" className="topbar-dropdown-item">
                                        <IconPackage size={16} /> Đơn mua
                                    </Link>
                                    <Link to="/wishlist" className="topbar-dropdown-item">
                                        <IconHeart size={16} /> Yêu thích
                                    </Link>
                                    <Link to="/messages" className="topbar-dropdown-item">
                                        <IconMessage size={16} /> Tin nhắn
                                    </Link>
                                    {isAdmin && (
                                        <>
                                            <div className="topbar-dropdown-divider" />
                                            <Link to="/admin" className="topbar-dropdown-item">
                                                <IconDashboard size={16} /> Quản trị Admin
                                            </Link>
                                        </>
                                    )}
                                    <Link to="/seller" className="topbar-dropdown-item">
                                        <IconStore size={16} /> Kênh Người Bán
                                    </Link>
                                    <div className="topbar-dropdown-divider" />
                                    <Link to="/profile" className="topbar-dropdown-item">
                                        <IconSettings size={16} /> Cài đặt
                                    </Link>
                                    <div className="topbar-dropdown-item" onClick={handleLogout}>
                                        <IconLogout size={16} /> Đăng xuất
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center' }}>
                                <Link to="/register" className="topbar-link" style={{ fontWeight: 600 }}>Đăng Ký</Link>
                                <span className="topbar-divider" />
                                <Link to="/login" className="topbar-link" style={{ fontWeight: 600 }}>Đăng Nhập</Link>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* 2. MAIN HEADER BAR */}
            <div className="header-main">
                <div className="container header-main-inner">
                    {/* Logo */}
                    <Link to="/" className="header-logo">
                        <div className="header-logo-icon">
                            <IconStore size={22} />
                        </div>
                        <span>E-Commerce</span>
                    </Link>

                    {/* Search Bar */}
                    <div className="header-search" ref={searchRef}>
                        <form onSubmit={handleSearchSubmit} className="header-search-form">
                            <input
                                type="text"
                                className="header-search-input"
                                placeholder="Tìm kiếm sản phẩm, thương hiệu, và cửa hàng..."
                                value={navSearch}
                                onChange={(e) => setNavSearch(e.target.value)}
                                onFocus={() => setShowDropdown(true)}
                            />
                            <button type="submit" className="header-search-btn">
                                <IconSearch size={18} />
                            </button>
                        </form>

                        {showDropdown && (suggestions.length > 0 || (navSearch.trim() === '' && history.length > 0)) && (
                            <div className="search-dropdown">
                                {/* Case 1: Suggestions */}
                                {navSearch.trim() !== '' && suggestions.length > 0 && (
                                    <div className="search-dropdown-section">
                                        <div className="search-dropdown-title">Gợi ý tìm kiếm</div>
                                        {suggestions.map((s, idx) => (
                                            <div 
                                                key={idx} 
                                                className="search-dropdown-item"
                                                onClick={() => handleSearch(s)}
                                            >
                                                <span className="search-icon-inline"><IconSearch size={14} /></span>
                                                <span>{s}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {/* Case 2: History */}
                                {navSearch.trim() === '' && history.length > 0 && (
                                    <div className="search-dropdown-section">
                                        <div className="search-dropdown-title">
                                            <span>Lịch sử tìm kiếm</span>
                                            <button 
                                                onClick={handleClearHistory} 
                                                className="search-clear-history-btn"
                                            >
                                                <IconTrash size={12} style={{ marginRight: '4px' }} />
                                                Xóa lịch sử
                                            </button>
                                        </div>
                                        {history.map((h) => (
                                            <div 
                                                key={h.id} 
                                                className="search-dropdown-item"
                                                onClick={() => handleSearch(h.queryText)}
                                            >
                                                <span className="search-icon-inline"><IconSearch size={14} /></span>
                                                <span>{h.queryText}</span>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}
                        <div className="header-keywords">
                            <Link to="/products?name=Laptop" className="header-keyword">Laptop</Link>
                            <Link to="/products?name=Điện thoại" className="header-keyword">Điện thoại</Link>
                            <Link to="/products?name=Tai nghe" className="header-keyword">Tai nghe</Link>
                            <Link to="/flash-sale" className="header-keyword">Flash Sale</Link>
                            <Link to="/products?name=Bàn phím" className="header-keyword">Bàn phím</Link>
                            <Link to="/products?name=Màn hình" className="header-keyword">Màn hình</Link>
                        </div>
                    </div>

                    {/* Cart */}
                    <Link to="/cart" className="header-cart">
                        <IconCart size={28} />
                        {totalItems > 0 && (
                            <span className="header-cart-badge">{totalItems > 99 ? '99+' : totalItems}</span>
                        )}
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default Navbar;
