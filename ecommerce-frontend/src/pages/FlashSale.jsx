import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import promotionService from '../services/promotionService';

function FlashSale() {
    const [flashSales, setFlashSales] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [timeLeft, setTimeLeft] = useState(10800); // 3 hours default mockup if not fetched

    useEffect(() => {
        setLoading(true);
        promotionService.getActiveFlashSale()
        .then(res => {
            if (res && res.success) {
                const sales = res.data || [];
                setFlashSales(sales);
                
                if (sales.length > 0) {
                    const end = new Date(sales[0].endTime).getTime();
                    const now = new Date().getTime();
                    const diff = Math.max(0, Math.floor((end - now) / 1000));
                    setTimeLeft(diff);
                }
            }
            setLoading(false);
        })
        .catch(err => {
            console.error(err);
            setError('Lỗi khi tải chương trình Flash Sale');
            setLoading(false);
        });
    }, []);

    useEffect(() => {
        const interval = setInterval(() => {
            setTimeLeft(prev => (prev > 0 ? prev - 1 : 0));
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

    if (loading) return <div style={{ padding: '50px', textAlign: 'center' }}>Đang tải Flash Sale...</div>;
    if (error) return <div style={{ padding: '50px', color: 'red', textAlign: 'center' }}>{error}</div>;

    const mainSale = flashSales[0];

    return (
        <div style={{ fontFamily: 'system-ui, sans-serif' }}>
            {/* Header Banner */}
            <div style={{
                background: 'linear-gradient(135deg, #f857a6, #ff5858)',
                padding: '40px',
                borderRadius: '8px',
                color: 'white',
                textAlign: 'center',
                boxShadow: 'var(--card-shadow)',
                marginBottom: '20px'
            }}>
                <h1 style={{ fontSize: '32px', fontWeight: 'bold', margin: '0 0 10px 0' }}>⚡ KHUNG GIỜ VÀNG FLASH SALE ⚡</h1>
                <p style={{ margin: '0 0 20px 0', opacity: 0.9 }}>Sản phẩm bán chạy với giá cực sốc giới hạn thời gian!</p>
                
                {/* Countdown */}
                <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', alignItems: 'center' }}>
                    <span style={{ fontSize: '14px', fontWeight: 'bold', marginRight: '10px' }}>KẾT THÚC TRONG</span>
                    <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.h}</span>
                    <span style={{ fontWeight: 'bold', fontSize: '20px' }}>:</span>
                    <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.m}</span>
                    <span style={{ fontWeight: 'bold', fontSize: '20px' }}>:</span>
                    <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.s}</span>
                </div>
            </div>

            {/* Products Grid */}
            {!mainSale || !mainSale.items || mainSale.items.length === 0 ? (
                <div style={{ padding: '50px', textAlign: 'center', background: 'white', borderRadius: '8px' }}>
                    Chưa có sản phẩm flash sale nào đang diễn ra ở khung giờ này. Vui lòng quay lại sau!
                </div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '20px' }}>
                    {mainSale.items.map(item => {
                        const pct = Math.round(((item.saleQuantity - item.soldCount) / item.saleQuantity) * 100);
                        return (
                            <Link key={item.id} to={`/products/${item.productId}`} style={{ textDecoration: 'none', background: 'white', borderRadius: '6px', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                                <div style={{ position: 'absolute', top: 0, right: 0, background: '#f94e30', color: 'white', fontSize: '11px', fontWeight: 'bold', padding: '4px 8px', borderRadius: '0 0 0 6px' }}>
                                    GIẢM SỐC
                                </div>
                                
                                <div style={{ height: '180px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#fff', padding: '10px' }}>
                                    <img src={item.productImageUrl} alt={item.productName} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                </div>
                                
                                <div style={{ padding: '15px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                    <h4 style={{ fontSize: '14px', color: '#333', margin: '0 0 10px 0', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', minHeight: '38px' }}>
                                        {item.productName}
                                    </h4>
                                    
                                    <div style={{ display: 'flex', gap: '8px', alignItems: 'baseline', marginBottom: '12px' }}>
                                        <span style={{ fontSize: '18px', fontWeight: 'bold', color: '#f94e30' }}>{item.salePrice.toLocaleString()} đ</span>
                                    </div>

                                    {/* Progress Sold Bar */}
                                    <div style={{ background: '#ffdbd4', borderRadius: '10px', height: '16px', position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', marginTop: 'auto' }}>
                                        <div style={{ background: '#f94e30', width: `${100 - pct}%`, height: '100%', position: 'absolute', left: 0, top: 0 }}></div>
                                        <span style={{ color: 'white', fontSize: '9px', fontWeight: 'bold', zIndex: 1 }}>ĐÃ BÁN {item.soldCount}</span>
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

export default FlashSale;
