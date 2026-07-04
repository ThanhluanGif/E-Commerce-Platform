import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import UserLayout from '../components/UserLayout';
import ProductCard from '../components/ProductCard';
import { useToast } from '../utils/toast';
import { IconHeart, IconTrash, IconWarning } from '../utils/icons';

function Wishlist() {
    const [wishlist, setWishlist] = useState([]);
    const [flashSaleSuggestions, setFlashSaleSuggestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const toast = useToast();

    const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');

    const fetchWishlist = () => {
        setLoading(true);
        
        // Fetch regular wishlist
        axios.get('http://localhost:8080/api/users/wishlist', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setWishlist(res.data.data || []);
            }
            setLoading(false);
        })
        .catch(err => {
            console.error(err);
            setError('Lỗi khi tải danh sách yêu thích');
            setLoading(false);
        });

        // Fetch wishlist items currently on active Flash Sale
        axios.get('http://localhost:8080/api/users/wishlist/flash-sales', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setFlashSaleSuggestions(res.data.data || []);
            }
        })
        .catch(err => {
            console.error("Error loading wishlist flash sales:", err);
        });
    };

    useEffect(() => {
        if (!token) {
            setError('Vui lòng đăng nhập để xem danh sách yêu thích!');
            setLoading(false);
            return;
        }
        fetchWishlist();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token]);

    const handleRemove = (productId, e) => {
        e.preventDefault(); // Prevent linking
        e.stopPropagation();
        axios.delete(`http://localhost:8080/api/users/wishlist/${productId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setWishlist(prev => prev.filter(p => p.id !== productId));
                toast.success("Đã xóa khỏi danh sách yêu thích!");
            }
        })
        .catch(err => {
            console.error(err);
            toast.error('Không thể xóa sản phẩm khỏi danh sách yêu thích');
        });
    };

    if (loading) {
        return (
            <UserLayout activeTab="wishlist">
                <div className="loading-center">
                    <div className="spinner spinner-lg" />
                </div>
            </UserLayout>
        );
    }

    return (
        <UserLayout activeTab="wishlist">
            <h3 className="user-content-title">Sản phẩm yêu thích</h3>
            <p className="user-content-subtitle">Danh sách các sản phẩm bạn đã lưu giữ</p>

            {error ? (
                <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3)' }}>
                    <IconWarning size={14} /> {error}
                </div>
            ) : (
                <>
                    {/* Flash Sale Suggestions */}
                    {flashSaleSuggestions.length > 0 && (
                        <div style={{ marginBottom: 'var(--space-6)', background: 'linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%)', padding: 'var(--space-4)', borderRadius: '12px', border: '1px solid #ffe3e3' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--space-3)' }}>
                                <span style={{ fontSize: '20px' }}>🔥</span>
                                <h4 style={{ margin: 0, color: '#e53e3e', fontWeight: 'bold' }}>Sản phẩm yêu thích đang Flash Sale!</h4>
                            </div>
                            <div className="product-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '15px' }}>
                                {flashSaleSuggestions.map(prod => (
                                    <div key={`fs_${prod.id}`} style={{ position: 'relative' }}>
                                        <ProductCard product={prod} />
                                        <span className="badge badge-danger" style={{ position: 'absolute', top: '8px', left: '8px', zIndex: 5, padding: '4px 8px', fontWeight: 'bold' }}>
                                            FLASH SALE
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {wishlist.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon"><IconHeart /></div>
                    <h3 className="empty-state-title">Danh sách yêu thích trống</h3>
                    <p className="empty-state-text">Hãy khám phá các sản phẩm và nhấn nút thích để lưu trữ tại đây.</p>
                    <Link to="/products">
                        <button className="btn btn-primary">
                            Khám phá ngay
                        </button>
                    </Link>
                </div>
            ) : (
                <div className="product-grid">
                    {wishlist.map(prod => (
                        <div key={prod.id} style={{ position: 'relative' }}>
                            <ProductCard product={prod} />
                            
                            {/* Overlay Trash Button */}
                            <button 
                                onClick={(e) => handleRemove(prod.id, e)} 
                                style={{
                                    position: 'absolute',
                                    top: '8px',
                                    right: '8px',
                                    background: 'rgba(255,255,255,0.9)',
                                    border: 'none',
                                    borderRadius: '50%',
                                    width: '32px',
                                    height: '32px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    color: 'var(--color-danger)',
                                    zIndex: 10,
                                    boxShadow: 'var(--shadow-sm)',
                                    transition: 'background var(--transition-fast)'
                                }}
                                onMouseEnter={(e) => { e.target.style.background = '#fff'; }}
                                onMouseLeave={(e) => { e.target.style.background = 'rgba(255,255,255,0.9)'; }}
                                title="Xóa khỏi danh sách"
                            >
                                <IconTrash size={14} />
                            </button>
                        </div>
                    ))}
                </div>
            )}
                </>
            )}
        </UserLayout>
    );
}

export default Wishlist;
