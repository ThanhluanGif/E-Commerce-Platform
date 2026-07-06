import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import promotionService from '../services/promotionService';
import { useToast } from '../utils/toast';

function FlashSale() {
    const toast = useToast();
    const [activeSale, setActiveSale] = useState(null);
    const [upcomingSales, setUpcomingSales] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [timeLeft, setTimeLeft] = useState(0);
    const [subscribedSales, setSubscribedSales] = useState({}); // { saleId: boolean }

    useEffect(() => {
        setLoading(true);
        promotionService.getActiveFlashSale()
        .then(res => {
            if (res && res.success) {
                const sales = res.data || [];
                
                const now = new Date();
                
                // Phân loại: Tìm sale đang chạy và sale sắp diễn ra
                const active = sales.find(s => new Date(s.startTime) <= now && new Date(s.endTime) >= now);
                const upcoming = sales.filter(s => new Date(s.startTime) > now);
                
                setActiveSale(active);
                setUpcomingSales(upcoming);

                if (active) {
                    const end = new Date(active.endTime).getTime();
                    const diff = Math.max(0, Math.floor((end - now.getTime()) / 1000));
                    setTimeLeft(diff);
                }

                // Kiểm tra trạng thái đăng ký nhận thông báo cho các sale sắp tới
                upcoming.forEach(sale => {
                    promotionService.isSubscribedFlashSale(sale.id)
                    .then(subRes => {
                        if (subRes && subRes.success) {
                            setSubscribedSales(prev => ({
                                ...prev,
                                [sale.id]: subRes.data
                            }));
                        }
                    })
                    .catch(err => console.error("Error checking subscription status", err));
                });
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
        if (timeLeft <= 0) return;
        const interval = setInterval(() => {
            setTimeLeft(prev => (prev > 0 ? prev - 1 : 0));
        }, 1000);
        return () => clearInterval(interval);
    }, [timeLeft]);

    const handleToggleReminder = async (saleId) => {
        const isSubbed = !!subscribedSales[saleId];
        try {
            if (isSubbed) {
                const res = await promotionService.unsubscribeFlashSale(saleId);
                if (res && res.success) {
                    setSubscribedSales(prev => ({ ...prev, [saleId]: false }));
                    toast.success("Đã hủy nhận nhắc nhở cho khung giờ này.");
                } else {
                    toast.error(res.message || "Hủy nhận nhắc nhở thất bại.");
                }
            } else {
                const res = await promotionService.subscribeFlashSale(saleId);
                if (res && res.success) {
                    setSubscribedSales(prev => ({ ...prev, [saleId]: true }));
                    toast.success("Đăng ký nhận nhắc nhở thành công! Chúng tôi sẽ thông báo trước 5 phút.");
                } else {
                    toast.error(res.message || "Đăng ký nhận nhắc nhở thất bại.");
                }
            }
        } catch (err) {
            console.error(err);
            toast.error("Có lỗi xảy ra khi xử lý yêu cầu.");
        }
    };

    const formatTime = (seconds) => {
        const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
        const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
        const s = (seconds % 60).toString().padStart(2, '0');
        return { h, m, s };
    };

    const formatDateTime = (dateStr) => {
        const date = new Date(dateStr);
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        return `${hours}:${minutes} ngày ${day}/${month}`;
    };

    const time = formatTime(timeLeft);

    if (loading) return <div style={{ padding: '50px', textAlign: 'center' }}>Đang tải Flash Sale...</div>;
    if (error) return <div style={{ padding: '50px', color: 'red', textAlign: 'center' }}>{error}</div>;

    return (
        <div style={{ fontFamily: 'system-ui, sans-serif', maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
            {/* Header Banner */}
            <div style={{
                background: 'linear-gradient(135deg, #ff416c, #ff4b2b)',
                padding: '40px 20px',
                borderRadius: '12px',
                color: 'white',
                textAlign: 'center',
                boxShadow: '0 4px 15px rgba(255, 75, 43, 0.3)',
                marginBottom: '40px'
            }}>
                <h1 style={{ fontSize: '32px', fontWeight: 'bold', margin: '0 0 10px 0', letterSpacing: '1px' }}>⚡ KHUNG GIỜ VÀNG FLASH SALE ⚡</h1>
                <p style={{ margin: '0 0 20px 0', opacity: 0.9 }}>Sản phẩm bán chạy với giá cực sốc giới hạn thời gian!</p>
                
                {activeSale && (
                    /* Countdown */
                    <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', alignItems: 'center' }}>
                        <span style={{ fontSize: '14px', fontWeight: 'bold', marginRight: '10px', textTransform: 'uppercase' }}>KẾT THÚC TRONG</span>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.h}</span>
                        <span style={{ fontWeight: 'bold', fontSize: '20px' }}>:</span>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.m}</span>
                        <span style={{ fontWeight: 'bold', fontSize: '20px' }}>:</span>
                        <span style={{ background: '#222', color: 'white', fontWeight: 'bold', padding: '5px 10px', borderRadius: '4px', fontSize: '16px' }}>{time.s}</span>
                    </div>
                )}
            </div>

            {/* 1. ACTIVE SALE SECTION */}
            {activeSale ? (
                <div style={{ marginBottom: '50px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #ff4b2b', paddingBottom: '10px', marginBottom: '20px' }}>
                        <h2 style={{ fontSize: '22px', color: '#ff4b2b', margin: 0, fontWeight: 'bold' }}>🔥 ĐANG DIỄN RA</h2>
                        <span style={{ fontSize: '14px', color: '#666' }}>Khung giờ: {formatDateTime(activeSale.startTime)} - {formatDateTime(activeSale.endTime)}</span>
                    </div>
                    
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '20px' }}>
                        {activeSale.items.map(item => {
                            const pct = Math.round(((item.saleQuantity - item.soldCount) / item.saleQuantity) * 100);
                            return (
                                <Link key={item.id} to={`/products/${item.productId}`} style={{ textDecoration: 'none', background: 'white', borderRadius: '8px', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', flexDirection: 'column', position: 'relative', transition: 'all 0.2s', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}
                                      onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-4px)'}
                                      onMouseLeave={(e) => e.currentTarget.style.transform = 'none'}>
                                    <div style={{ position: 'absolute', top: 0, right: 0, background: '#ff4b2b', color: 'white', fontSize: '11px', fontWeight: 'bold', padding: '4px 8px', borderRadius: '0 0 0 8px', zIndex: 1 }}>
                                        GIẢM SỐC
                                    </div>
                                    
                                    <div style={{ height: '180px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#fff', padding: '10px' }}>
                                        <img src={item.productImageUrl || 'https://via.placeholder.com/150'} alt={item.productName} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                    </div>
                                    
                                    <div style={{ padding: '15px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                        <h4 style={{ fontSize: '14px', color: '#333', margin: '0 0 10px 0', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', minHeight: '38px', lineHeight: '1.4' }}>
                                            {item.productName}
                                        </h4>
                                        
                                        <div style={{ display: 'flex', gap: '8px', alignItems: 'baseline', marginBottom: '12px' }}>
                                            <span style={{ fontSize: '18px', fontWeight: 'bold', color: '#ff4b2b' }}>{item.salePrice.toLocaleString()} đ</span>
                                        </div>

                                        {/* Progress Sold Bar */}
                                        <div style={{ background: '#ffe4e0', borderRadius: '10px', height: '16px', position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', marginTop: 'auto' }}>
                                            <div style={{ background: '#ff4b2b', width: `${100 - pct}%`, height: '100%', position: 'absolute', left: 0, top: 0 }}></div>
                                            <span style={{ color: 'white', fontSize: '9px', fontWeight: 'bold', zIndex: 1 }}>ĐÃ BÁN {item.soldCount}</span>
                                        </div>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                </div>
            ) : (
                <div style={{ padding: '40px', textAlign: 'center', background: 'white', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '40px', color: '#666' }}>
                    Không có khung giờ Flash Sale nào đang chạy vào lúc này.
                </div>
            )}

            {/* 2. UPCOMING SALES SECTION */}
            <div style={{ marginTop: '40px' }}>
                <h2 style={{ fontSize: '22px', color: '#333', borderBottom: '2px solid #ddd', paddingBottom: '10px', marginBottom: '30px', fontWeight: 'bold' }}>⏰ KHUNG GIỜ TIẾP THEO</h2>
                
                {upcomingSales.length === 0 ? (
                    <div style={{ padding: '30px', textAlign: 'center', color: '#777' }}>
                        Không có chương trình Flash Sale sắp tới nào được lên lịch.
                    </div>
                ) : (
                    upcomingSales.map(sale => {
                        const isSubbed = !!subscribedSales[sale.id];
                        return (
                            <div key={sale.id} style={{ background: 'white', borderRadius: '10px', border: '1px solid #e2e8f0', padding: '25px', marginBottom: '30px', boxShadow: '0 2px 10px rgba(0,0,0,0.02)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px', borderBottom: '1px dashed #e2e8f0', paddingBottom: '15px', marginBottom: '20px' }}>
                                    <div>
                                        <h3 style={{ margin: 0, fontSize: '18px', color: '#1a202c', fontWeight: '700' }}>⚡ Khung giờ: {formatDateTime(sale.startTime)}</h3>
                                        <p style={{ margin: '5px 0 0 0', fontSize: '13px', color: '#718096' }}>Sắp diễn ra - Săn ưu đãi độc quyền khi mở bán!</p>
                                    </div>
                                    <button 
                                        onClick={() => handleToggleReminder(sale.id)}
                                        style={{
                                            backgroundColor: isSubbed ? '#edf2f7' : '#ff4b2b',
                                            color: isSubbed ? '#4a5568' : 'white',
                                            border: 'none',
                                            padding: '10px 20px',
                                            borderRadius: '25px',
                                            fontWeight: 'bold',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s',
                                            fontSize: '14px',
                                            boxShadow: isSubbed ? 'none' : '0 4px 6px rgba(255, 75, 43, 0.15)'
                                        }}
                                        onMouseEnter={(e) => {
                                            if (!isSubbed) e.currentTarget.style.backgroundColor = '#e03d1a';
                                        }}
                                        onMouseLeave={(e) => {
                                            if (!isSubbed) e.currentTarget.style.backgroundColor = '#ff4b2b';
                                        }}
                                    >
                                        {isSubbed ? '🔕 Hủy Nhắc Tôi' : '🔔 Nhắc Tôi Trước 5 Phút'}
                                    </button>
                                </div>

                                {/* Preview Products in Upcoming Sale */}
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '15px' }}>
                                    {sale.items.map(item => (
                                        <div key={item.id} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', background: '#f8fafc', padding: '15px', borderRadius: '8px', border: '1px solid #f1f5f9' }}>
                                            <div style={{ height: '100px', display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', marginBottom: '10px' }}>
                                                <img src={item.productImageUrl || 'https://via.placeholder.com/150'} alt={item.productName} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                            </div>
                                            <h4 style={{ fontSize: '12px', color: '#4a5568', margin: '0 0 8px 0', textOverflow: 'ellipsis', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', width: '100%', textAlign: 'center', minHeight: '34px', lineHeight: '1.4' }}>
                                                {item.productName}
                                            </h4>
                                            <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#ff4b2b' }}>{item.salePrice.toLocaleString()} đ</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}

export default FlashSale;
