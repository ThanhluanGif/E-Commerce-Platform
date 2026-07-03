import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import productService from '../services/productService';
import categoryService from '../services/categoryService';

function Home() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);

    // Countdown timer for Flash Sale (simulating 3 hours)
    const [timeLeft, setTimeLeft] = useState(10800); 

    useEffect(() => {
        const interval = setInterval(() => {
            setTimeLeft(prev => (prev > 0 ? prev - 1 : 10800));
        }, 1000);
        return () => clearInterval(interval);
    }, []);

    const formatTime = (seconds) => {
        const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
        const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
        const s = (seconds % 60).toString().padStart(2, '0');
        return { h, m, s };
    };

    const time = formatTime(timeLeft);

    useEffect(() => {
        setLoading(true);
        // Load categories for navigation
        categoryService.getCategoryTree()
            .then(res => {
                if (res && res.success) setCategories(res.data || []);
            })
            .catch(err => console.error(err));

        // Load recommended products
        productService.getAllProducts({ pageSize: 6 })
            .then(res => {
                if (res && res.success && res.data) {
                    setProducts(res.data.content || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, []);

    return (
        <div style={{ maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            
            {/* 1. HERO CAROUSEL BANNER SECTION */}
            <div style={{ display: 'flex', gap: '10px', marginTop: '15px', flexWrap: 'wrap' }}>
                {/* Main slide banner */}
                <div style={{ 
                    flex: '2 1 600px', 
                    background: 'linear-gradient(135deg, #ff7e5f, #feb47b)', 
                    borderRadius: '8px', 
                    padding: '40px', 
                    color: 'white', 
                    display: 'flex', 
                    flexDirection: 'column', 
                    justifyContent: 'center', 
                    minHeight: '280px',
                    position: 'relative',
                    overflow: 'hidden',
                    boxShadow: 'var(--card-shadow)'
                }}>
                    <span style={{ position: 'absolute', top: '15px', right: '15px', background: 'rgba(255,255,255,0.2)', padding: '4px 10px', borderRadius: '15px', fontSize: '12px', fontWeight: 'bold' }}>
                        Khuyến mãi hot
                    </span>
                    <h2 style={{ fontSize: '32px', fontWeight: '800', margin: '0 0 10px 0', textShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>SIÊU HỘI CÔNG NGHỆ 7.7</h2>
                    <p style={{ fontSize: '16px', margin: '0 0 25px 0', opacity: 0.95 }}>Voucher giảm đến 500k + Miễn phí vận chuyển 0đ cho toàn bộ đơn hàng</p>
                    <Link to="/products">
                        <button style={{ 
                            alignSelf: 'flex-start', 
                            padding: '12px 28px', 
                            background: '#f94e30', 
                            color: 'white', 
                            border: 'none', 
                            borderRadius: '4px', 
                            fontWeight: 'bold', 
                            fontSize: '15px', 
                            cursor: 'pointer',
                            boxShadow: '0 4px 6px rgba(0,0,0,0.15)',
                            transition: 'transform 0.2s'
                        }}
                                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}>
                            MUA SẮM NGAY
                        </button>
                    </Link>
                </div>

                {/* Sub promo banners */}
                <div style={{ flex: '1 1 300px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    <div style={{ 
                        flex: 1, 
                        background: 'linear-gradient(135deg, #00c6ff, #0072ff)', 
                        borderRadius: '8px', 
                        padding: '20px', 
                        color: 'white', 
                        display: 'flex', 
                        flexDirection: 'column', 
                        justifyContent: 'center',
                        boxShadow: 'var(--card-shadow)'
                    }}>
                        <h4 style={{ margin: '0 0 5px 0', fontSize: '16px', fontWeight: 'bold' }}>Freeship Xtra +</h4>
                        <p style={{ margin: 0, fontSize: '12px', opacity: 0.9 }}>Miễn phí giao hàng tới 70k</p>
                    </div>
                    <div style={{ 
                        flex: 1, 
                        background: 'linear-gradient(135deg, #11998e, #38ef7d)', 
                        borderRadius: '8px', 
                        padding: '20px', 
                        color: 'white', 
                        display: 'flex', 
                        flexDirection: 'column', 
                        justifyContent: 'center',
                        boxShadow: 'var(--card-shadow)'
                    }}>
                        <h4 style={{ margin: '0 0 5px 0', fontSize: '16px', fontWeight: 'bold' }}>Thanh Toán Momo</h4>
                        <p style={{ margin: 0, fontSize: '12px', opacity: 0.9 }}>Nhập mã MOMOTECH giảm ngay 50k</p>
                    </div>
                </div>
            </div>

            {/* 2. CIRCULAR QUICK NAVIGATION (Shopee Style) */}
            <div style={{ 
                background: 'white', 
                borderRadius: '8px', 
                padding: '20px', 
                marginTop: '15px', 
                boxShadow: 'var(--card-shadow)',
                display: 'flex',
                justifyContent: 'space-around',
                flexWrap: 'wrap',
                gap: '15px'
            }}>
                {[
                    { icon: '⚡', label: 'Flash Sale', link: '#flash-sale' },
                    { icon: '🚚', label: 'Freeship Xtra', link: '/products' },
                    { icon: '🎟️', label: 'Mã Giảm Giá', link: '/profile' },
                    { icon: '💎', label: 'Hàng Hiệu', link: '/products' },
                    { icon: '🎁', label: 'Gợi Ý Quà', link: '/products' },
                    { icon: '📱', label: 'Điện Thoại', link: '/products?name=Điện' },
                ].map((item, index) => (
                    <Link key={index} to={item.link} style={{ textDecoration: 'none', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px', width: '90px' }}>
                        <div style={{ 
                            width: '50px', 
                            height: '50px', 
                            borderRadius: '50%', 
                            background: '#fff3f0', 
                            display: 'flex', 
                            alignItems: 'center', 
                            justifyContent: 'center', 
                            fontSize: '24px',
                            border: '1px solid #ffe4de',
                            transition: 'transform 0.2s'
                        }}
                             onMouseEnter={(e) => e.target.style.transform = 'translateY(-3px)'}
                             onMouseLeave={(e) => e.target.style.transform = 'translateY(0)'}>
                            {item.icon}
                        </div>
                        <span style={{ fontSize: '12px', color: '#333', fontWeight: '500', textAlign: 'center' }}>{item.label}</span>
                    </Link>
                ))}
            </div>

            {/* 3. FLASH SALE BLOCK (Shopee/Tiki Style) */}
            <div id="flash-sale" style={{ 
                background: 'white', 
                borderRadius: '8px', 
                marginTop: '15px', 
                boxShadow: 'var(--card-shadow)',
                overflow: 'hidden'
            }}>
                <div style={{ 
                    padding: '15px 20px', 
                    borderBottom: '1px solid #f3f4f6', 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '15px',
                    flexWrap: 'wrap'
                }}>
                    <h3 style={{ 
                        fontSize: '20px', 
                        fontWeight: 'bold', 
                        color: '#f94e30', 
                        margin: 0, 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '6px' 
                    }} className="flash-sale-pulse">
                        ⚡ FLASH SALE
                    </h3>
                    
                    {/* Live countdown timer boxes */}
                    <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '3px 6px', borderRadius: '4px', fontSize: '13px' }}>{time.h}</span>
                        <span style={{ fontWeight: 'bold' }}>:</span>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '3px 6px', borderRadius: '4px', fontSize: '13px' }}>{time.m}</span>
                        <span style={{ fontWeight: 'bold' }}>:</span>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '3px 6px', borderRadius: '4px', fontSize: '13px' }}>{time.s}</span>
                    </div>
                </div>

                {/* Horizontal Sale Products Grid */}
                <div style={{ display: 'flex', gap: '15px', padding: '20px', overflowX: 'auto' }}>
                    {products.slice(0, 5).map(prod => {
                        const hasSale = prod.salePrice && prod.salePrice > 0;
                        const displayPrice = hasSale ? prod.salePrice : prod.price;
                        return (
                            <Link key={prod.id} to={`/products/${prod.id}`} style={{ textDecoration: 'none', width: '175px', flexShrink: 0, display: 'flex', flexDirection: 'column', position: 'relative' }}>
                                
                                {/* Image and discount tag */}
                                <div style={{ height: '140px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative', overflow: 'hidden' }}>
                                    <img src={prod.imageUrl} alt={prod.name} style={{ maxHeight: '90%', maxWidth: '90%', objectFit: 'contain' }} />
                                    {hasSale && (
                                        <div style={{ position: 'absolute', top: 0, right: 0, background: '#fef3c7', color: '#d97706', fontSize: '11px', fontWeight: '800', padding: '2px 6px', borderRadius: '0 0 0 6px' }}>
                                            GIẢM SỐC
                                        </div>
                                    )}
                                </div>
                                
                                <h4 style={{ fontSize: '13px', color: '#333', fontWeight: '600', margin: '10px 0 5px 0', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', minHeight: '38px' }}>
                                    {prod.name}
                                </h4>

                                <div style={{ fontWeight: 'bold', color: '#f94e30', fontSize: '15px', marginBottom: '8px' }}>
                                    {displayPrice.toLocaleString()} đ
                                </div>

                                {/* Simulated progress sold bar */}
                                <div style={{ background: '#ffdbd4', borderRadius: '10px', height: '14px', position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                                    <div style={{ background: '#f94e30', width: '60%', height: '100%', position: 'absolute', left: 0, top: 0 }}></div>
                                    <span style={{ color: 'white', fontSize: '9px', fontWeight: 'bold', zIndex: 1, textTransform: 'uppercase' }}>Đang bán chạy</span>
                                </div>

                            </Link>
                        );
                    })}
                </div>
            </div>

            {/* 4. CHỮ DANH MỤC NỔI BẬT */}
            <div style={{ background: 'white', borderRadius: '8px', padding: '20px', marginTop: '15px', boxShadow: 'var(--card-shadow)' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#757575', textTransform: 'uppercase', marginBottom: '15px', borderBottom: '1px solid #f3f4f6', paddingBottom: '8px' }}>
                    Danh Mục Nổi Bật
                </h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '1px', background: '#f3f4f6' }}>
                    {categories.map(cat => (
                        <Link key={cat.id} to={`/products?category=${cat.id}`} style={{ 
                            textDecoration: 'none', 
                            background: 'white', 
                            padding: '20px 10px', 
                            display: 'flex', 
                            flexDirection: 'column', 
                            alignItems: 'center', 
                            gap: '10px',
                            transition: 'box-shadow 0.2s'
                        }}
                             className="hover-lift">
                            <span style={{ fontSize: '32px' }}>{cat.name.includes("Tivi") ? '📺' : cat.name.includes("Laptop") ? '💻' : '🔌'}</span>
                            <span style={{ fontSize: '13px', color: '#333', fontWeight: '500', textAlign: 'center' }}>{cat.name}</span>
                        </Link>
                    ))}
                </div>
            </div>

            {/* 5. RECOMMENDATION / DAILY DISCOVER GRID */}
            <div style={{ marginTop: '20px', marginBottom: '30px' }}>
                <div style={{ background: 'white', borderBottom: '3px solid #f94e30', padding: '15px 20px', display: 'flex', justifyContent: 'center' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: '800', color: '#f94e30', margin: 0, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                        Gợi Ý Hôm Nay
                    </h3>
                </div>

                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#757575' }}>Đang tải gợi ý sản phẩm...</div>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(185px, 1fr))', gap: '10px', marginTop: '10px' }}>
                        {products.map(prod => {
                            const hasSale = prod.salePrice && prod.salePrice > 0;
                            const discountPct = hasSale ? Math.round(((prod.price - prod.salePrice) / prod.price) * 100) : 0;
                            return (
                                <Link key={prod.id} to={`/products/${prod.id}`} style={{ 
                                    textDecoration: 'none', 
                                    background: 'white', 
                                    borderRadius: '4px', 
                                    border: '1px solid #f3f4f6',
                                    overflow: 'hidden',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    position: 'relative'
                                }}
                                     className="hover-lift">
                                    
                                    {/* Discount badge top-right */}
                                    {hasSale && (
                                        <div style={{ 
                                            position: 'absolute', 
                                            top: 0, 
                                            right: 0, 
                                            background: '#ffe9e4', 
                                            color: '#f94e30', 
                                            fontSize: '11px', 
                                            fontWeight: 'bold', 
                                            padding: '3px 8px', 
                                            borderRadius: '0 0 0 8px',
                                            zIndex: 2
                                        }}>
                                            -{discountPct}%
                                        </div>
                                    )}

                                    {/* Image box */}
                                    <div style={{ height: '175px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#fff', borderBottom: '1px solid #f3f4f6', padding: '10px' }}>
                                        <img src={prod.imageUrl} alt={prod.name} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                    </div>

                                    {/* Content box */}
                                    <div style={{ padding: '10px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                        <h4 style={{ 
                                            fontSize: '13px', 
                                            color: '#333', 
                                            margin: '0 0 8px 0', 
                                            lineHeight: '1.4',
                                            overflow: 'hidden', 
                                            textOverflow: 'ellipsis', 
                                            display: '-webkit-box', 
                                            WebkitLineClamp: 2, 
                                            WebkitBoxOrient: 'vertical',
                                            minHeight: '36px'
                                        }}>
                                            {prod.name}
                                        </h4>

                                        {/* Badges row */}
                                        <div style={{ display: 'flex', gap: '5px', marginBottom: '8px' }}>
                                            <span style={{ fontSize: '9px', background: '#ffe4de', color: '#f94e30', padding: '1px 4px', borderRadius: '2px', fontWeight: 'bold' }}>Freeship+</span>
                                            <span style={{ fontSize: '9px', background: '#dbeafe', color: '#2563eb', padding: '1px 4px', borderRadius: '2px', fontWeight: 'bold' }}>Hỏa tốc</span>
                                        </div>

                                        <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', flexWrap: 'wrap', marginTop: 'auto' }}>
                                            {hasSale ? (
                                                <>
                                                    <span style={{ fontSize: '15px', fontWeight: 'bold', color: '#f94e30' }}>{prod.salePrice.toLocaleString()} đ</span>
                                                    <span style={{ fontSize: '11px', color: '#9ca3af', textDecoration: 'line-through' }}>{prod.price.toLocaleString()}đ</span>
                                                </>
                                            ) : (
                                                <span style={{ fontSize: '15px', fontWeight: 'bold', color: '#f94e30' }}>{prod.price.toLocaleString()} đ</span>
                                            )}
                                        </div>

                                        {/* Sold items and rating info */}
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '8px', fontSize: '11px', color: '#757575' }}>
                                            <div style={{ color: '#fbbf24' }}>★ 4.9</div>
                                            <div>Đã bán 86</div>
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

export default Home;