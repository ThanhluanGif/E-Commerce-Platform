import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import productService from '../services/productService';
import categoryService from '../services/categoryService';
import api from '../services/api';
import ProductCard, { ProductCardSkeleton } from '../components/ProductCard';
import { 
  IconFlash, IconTruck, IconTicket, IconGift, 
  IconStore, IconChevronRight, IconPackage 
} from '../utils/icons';
import './Home.css';

function Home() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [trending, setTrending] = useState([]);
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
        productService.getAllProducts({ pageSize: 12 })
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

        // Load trending products
        api.get('/api/recommendations/trending?limit=6')
            .then(res => {
                if (res.data && res.data.success) {
                    setTrending(res.data.data || []);
                }
            })
            .catch(err => console.error("Error loading trending products:", err));
    }, []);

    // Get Lucide icon dynamically based on category name
    const getCategoryIcon = (name) => {
        const lowerName = name.toLowerCase();
        if (lowerName.includes('laptop') || lowerName.includes('máy tính')) return <IconPackage size={24} />;
        if (lowerName.includes('điện thoại') || lowerName.includes('mobile')) return <IconPackage size={24} />;
        if (lowerName.includes('phụ kiện') || lowerName.includes('accessory')) return <IconGift size={24} />;
        if (lowerName.includes('tivi') || lowerName.includes('màn hình')) return <IconStore size={24} />;
        return <IconPackage size={24} />;
    };

    return (
        <div className="container home-container">
            
            {/* 1. HERO CAROUSEL BANNER SECTION */}
            <div className="hero-section">
                {/* Main slide banner */}
                <div className="hero-main-banner">
                    <span className="hero-tag">Khuyến mãi hot</span>
                    <h2 className="hero-title">SIÊU HỘI CÔNG NGHỆ 7.7</h2>
                    <p className="hero-subtitle">Voucher giảm đến 500k + Miễn phí vận chuyển 0đ cho toàn bộ đơn hàng</p>
                    <Link to="/products">
                        <button className="btn hero-btn">MUA SẮM NGAY</button>
                    </Link>
                </div>

                {/* Sub promo banners */}
                <div className="hero-side-banners">
                    <div className="hero-side-banner banner-blue">
                        <h4 className="side-banner-title">Freeship Xtra +</h4>
                        <p className="side-banner-text">Miễn phí giao hàng tới 70k</p>
                    </div>
                    <div className="hero-side-banner banner-green">
                        <h4 className="side-banner-title">Thành Toán Momo</h4>
                        <p className="side-banner-text">Nhập mã MOMOTECH giảm ngay 50k</p>
                    </div>
                </div>
            </div>

            {/* 2. CIRCULAR QUICK NAVIGATION (Shopee Style) */}
            <div className="quick-nav">
                {[
                    { icon: <IconFlash size={24} />, label: 'Flash Sale', link: '#flash-sale' },
                    { icon: <IconTruck size={24} />, label: 'Freeship Xtra', link: '/products' },
                    { icon: <IconTicket size={24} />, label: 'Mã Giảm Giá', link: '/profile' },
                    { icon: <IconGift size={24} />, label: 'Quà Tặng', link: '/products' },
                    { icon: <IconStore size={24} />, label: 'Hàng Hiệu', link: '/products' },
                ].map((item, index) => (
                    <Link key={index} to={item.link} className="quick-nav-item">
                        <div className="quick-nav-icon-wrapper">
                            {item.icon}
                        </div>
                        <span className="quick-nav-label">{item.label}</span>
                    </Link>
                ))}
            </div>

            {/* 3. FLASH SALE BLOCK (Shopee/Tiki Style) */}
            <div id="flash-sale" className="flashsale-block">
                <div className="flashsale-header">
                    <div className="flashsale-title-row">
                        <h3 className="flashsale-title">
                            <IconFlash size={22} /> FLASH SALE
                        </h3>
                        
                        {/* Live countdown timer boxes */}
                        <div className="flashsale-timer">
                            <span className="timer-box">{time.h}</span>
                            <span className="timer-separator">:</span>
                            <span className="timer-box">{time.m}</span>
                            <span className="timer-separator">:</span>
                            <span className="timer-box">{time.s}</span>
                        </div>
                    </div>
                    <Link to="/flash-sale" style={{ display: 'flex', alignItems: 'center', gap: 4, color: 'var(--color-primary)', fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>
                        Xem tất cả <IconChevronRight size={14} />
                    </Link>
                </div>

                {/* Horizontal Sale Products Grid */}
                <div className="flashsale-products-row">
                    {loading ? (
                        Array(5).fill(0).map((_, idx) => (
                            <div key={idx} className="flashsale-card-wrapper">
                                <ProductCardSkeleton />
                            </div>
                        ))
                    ) : (
                        products.slice(0, 8).map(prod => (
                            <div key={prod.id} className="flashsale-card-wrapper">
                                <ProductCard product={prod} showProgress={true} />
                            </div>
                        ))
                    )}
                </div>
            </div>

            {/* 4. CATEGORIES SECTION */}
            <div className="categories-block">
                <h3 className="block-title">Danh Mục Nổi Bật</h3>
                <div className="categories-grid">
                    {categories.map(cat => (
                        <Link key={cat.id} to={`/products?category=${cat.id}`} className="category-card">
                            <div className="category-icon-wrapper">
                                {getCategoryIcon(cat.name)}
                            </div>
                            <span className="category-name">{cat.name}</span>
                        </Link>
                    ))}
                </div>
            </div>

            {/* 4B. TRENDING PRODUCTS */}
            {trending.length > 0 && (
                <div className="discover-block" style={{ marginTop: 'var(--space-6)' }}>
                    <h3 className="block-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <IconGift size={20} color="var(--color-primary)" /> Xu Hướng Mua Sắm
                    </h3>
                    <div className="product-grid" style={{ marginTop: 'var(--space-4)' }}>
                        {trending.map(prod => (
                            <ProductCard key={prod.id} product={prod} />
                        ))}
                    </div>
                </div>
            )}

            {/* 5. RECOMMENDATION / DAILY DISCOVER GRID */}
            <div className="discover-block">
                <div className="discover-tabs-header">
                    <h3 className="discover-title">Gợi Ý Hôm Nay</h3>
                </div>

                {loading ? (
                    <div className="product-grid" style={{ marginTop: 'var(--space-4)' }}>
                        {Array(12).fill(0).map((_, idx) => (
                            <ProductCardSkeleton key={idx} />
                        ))}
                    </div>
                ) : (
                    <div className="product-grid" style={{ marginTop: 'var(--space-4)' }}>
                        {products.map(prod => (
                            <ProductCard key={prod.id} product={prod} />
                        ))}
                    </div>
                )}
            </div>

        </div>
    );
}

export default Home;