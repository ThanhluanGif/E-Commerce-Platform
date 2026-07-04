import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

function Wishlist() {
    const [wishlist, setWishlist] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const token = localStorage.getItem('token');

    const fetchWishlist = () => {
        setLoading(true);
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
    };

    useEffect(() => {
        if (!token) {
            setError('Vui lòng đăng nhập để xem danh sách yêu thích!');
            setLoading(false);
            return;
        }
        fetchWishlist();
    }, [token]);

    const handleRemove = (productId, e) => {
        e.preventDefault(); // Prevent linking
        axios.delete(`http://localhost:8080/api/users/wishlist/${productId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setWishlist(prev => prev.filter(p => p.id !== productId));
            }
        })
        .catch(err => {
            console.error(err);
            alert('Không thể xóa sản phẩm khỏi danh sách yêu thích');
        });
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải danh sách yêu thích...</div>;
    if (error) return <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>{error}</div>;

    return (
        <div style={{ fontFamily: 'system-ui, sans-serif' }}>
            <h2 style={{ borderBottom: '2px solid #f94e30', paddingBottom: '10px', fontSize: '20px', fontWeight: 'bold' }}>
                ❤️ Sản Phẩm Yêu Thích Của Tôi ({wishlist.length})
            </h2>

            {wishlist.length === 0 ? (
                <div style={{ padding: '50px', textAlign: 'center', background: 'white', borderRadius: '8px', marginTop: '15px', boxShadow: 'var(--card-shadow)' }}>
                    <p style={{ fontSize: '16px', color: '#666', margin: '0 0 15px 0' }}>Bạn chưa thích sản phẩm nào.</p>
                    <Link to="/products">
                        <button style={{ padding: '10px 24px', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                            Khám phá sản phẩm ngay
                        </button>
                    </Link>
                </div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(185px, 1fr))', gap: '15px', marginTop: '20px' }}>
                    {wishlist.map(prod => {
                        const hasSale = prod.salePrice && prod.salePrice > 0;
                        const displayPrice = hasSale ? prod.salePrice : prod.price;
                        return (
                            <Link key={prod.id} to={`/products/${prod.id}`} style={{ textDecoration: 'none', background: 'white', borderRadius: '6px', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                                
                                <button onClick={(e) => handleRemove(prod.id, e)} style={{
                                    position: 'absolute',
                                    top: '8px',
                                    right: '8px',
                                    background: 'rgba(255,255,255,0.8)',
                                    border: 'none',
                                    borderRadius: '50%',
                                    width: '30px',
                                    height: '30px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '16px',
                                    color: '#ef4444',
                                    zIndex: 2,
                                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                                }}>
                                    ✕
                                </button>

                                <div style={{ height: '175px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#fff', padding: '10px' }}>
                                    <img src={prod.imageUrl} alt={prod.name} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                </div>
                                
                                <div style={{ padding: '12px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                    <h4 style={{ fontSize: '13px', color: '#333', margin: '0 0 8px 0', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', minHeight: '36px' }}>
                                        {prod.name}
                                    </h4>
                                    <div style={{ fontWeight: 'bold', color: '#f94e30', fontSize: '15px', marginTop: 'auto' }}>
                                        {displayPrice.toLocaleString()} đ
                                    </div>
                                </div>
                            </Link>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default Wishlist;
