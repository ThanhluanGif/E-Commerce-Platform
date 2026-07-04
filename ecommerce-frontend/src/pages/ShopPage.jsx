import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

function ShopPage() {
    const { slug } = useParams();
    const navigate = useNavigate();
    const [shop, setShop] = useState(null);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isFollowed, setIsFollowed] = useState(false);

    useEffect(() => {
        setLoading(true);
        setError('');
        
        // Fetch shop details
        axios.get(`http://localhost:8080/api/shops/${slug}`)
            .then(res => {
                if (res.data && res.data.success) {
                    const shopData = res.data.data;
                    setShop(shopData);
                    
                    // Fetch products of this shop
                    return axios.get(`http://localhost:8080/api/products?shopId=${shopData.id}`);
                } else {
                    throw new Error("Không lấy được dữ liệu gian hàng!");
                }
            })
            .then(res => {
                if (res.data && res.data.success && res.data.data) {
                    setProducts(res.data.data.content || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setError(err.message || 'Lỗi khi tải trang cửa hàng');
                setLoading(false);
            });
    }, [slug]);

    const handleChat = () => {
        const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        axios.post(`http://localhost:8080/api/chat/conversations/${shop.id}`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                navigate(`/messages?convId=${res.data.data.id}`);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi khi kết nối trò chuyện: " + (err.response?.data?.message || err.message));
        });
    };

    if (loading) return <div style={{ padding: '50px', textAlign: 'center' }}>Đang tải thông tin cửa hàng...</div>;
    if (error || !shop) return <div style={{ padding: '50px', color: 'red', textAlign: 'center' }}>{error || 'Không tìm thấy gian hàng!'}</div>;

    return (
        <div style={{ fontFamily: 'system-ui, sans-serif' }}>
            {/* Banner & Logo section */}
            <div style={{
                background: shop.bannerUrl ? `url(${shop.bannerUrl}) no-repeat center/cover` : 'linear-gradient(135deg, #141e30, #243b55)',
                height: '240px',
                borderRadius: '8px',
                position: 'relative',
                display: 'flex',
                alignItems: 'flex-end',
                padding: '20px',
                boxShadow: 'var(--card-shadow)',
                color: 'white'
            }}>
                <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.4)', borderRadius: '8px' }}></div>
                
                <div style={{ display: 'flex', gap: '20px', alignItems: 'center', zIndex: 1, width: '100%' }}>
                    <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: '#fff', border: '3px solid #ff4f2f', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <img src={shop.logoUrl || 'https://via.placeholder.com/150'} alt={shop.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    <div>
                        <h2 style={{ margin: '0 0 5px 0', fontSize: '24px', fontWeight: 'bold' }}>{shop.name}</h2>
                        <div style={{ display: 'flex', gap: '15px', fontSize: '13px', opacity: 0.9 }}>
                            <span>⭐ {shop.rating.toFixed(1)} / 5.0 ({shop.reviewCount} đánh giá)</span>
                            <span>👥 {shop.followerCount} Người theo dõi</span>
                            {shop.verified && <span style={{ color: '#38ef7d', fontWeight: 'bold' }}>✓ Đã xác thực</span>}
                        </div>
                    </div>
                    
                    <div style={{ marginLeft: 'auto', display: 'flex', gap: '10px' }}>
                        <button onClick={() => setIsFollowed(!isFollowed)} style={{
                            padding: '8px 20px',
                            background: isFollowed ? '#6b7280' : '#f94e30',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}>
                            {isFollowed ? 'Đã Theo Dõi' : '+ Theo Dõi'}
                        </button>
                        <button onClick={handleChat} style={{
                            padding: '8px 20px',
                            background: 'transparent',
                            color: 'white',
                            border: '1px solid white',
                            borderRadius: '4px',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}>
                            💬 Chat Ngay
                        </button>
                    </div>
                </div>
            </div>

            {/* Shop description */}
            <div style={{ background: 'white', borderRadius: '8px', padding: '20px', marginTop: '15px', boxShadow: 'var(--card-shadow)' }}>
                <h3 style={{ margin: '0 0 10px 0', fontSize: '16px', color: '#666' }}>Giới thiệu cửa hàng</h3>
                <p style={{ margin: 0, color: '#333', lineHeight: '1.6' }}>{shop.description || 'Chưa có mô tả gian hàng.'}</p>
            </div>

            {/* Shop products grid */}
            <div style={{ marginTop: '30px' }}>
                <h3 style={{ borderBottom: '2px solid #f94e30', paddingBottom: '10px', fontSize: '18px', fontWeight: 'bold', color: '#333' }}>
                    🛍️ Sản Phẩm Của Cửa Hàng ({products.length})
                </h3>
                
                {products.length === 0 ? (
                    <div style={{ padding: '50px', textAlign: 'center', background: 'white', borderRadius: '8px', marginTop: '10px' }}>
                        Gian hàng chưa đăng sản phẩm nào.
                    </div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(185px, 1fr))', gap: '15px', marginTop: '15px' }}>
                        {products.map(prod => {
                            const hasSale = prod.salePrice && prod.salePrice > 0;
                            const displayPrice = hasSale ? prod.salePrice : prod.price;
                            return (
                                <Link key={prod.id} to={`/products/${prod.id}`} style={{ textDecoration: 'none', background: 'white', borderRadius: '6px', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
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
        </div>
    );
}

export default ShopPage;
