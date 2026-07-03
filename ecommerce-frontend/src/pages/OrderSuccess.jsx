import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';

function OrderSuccess() {
    const [searchParams] = useSearchParams();
    const orderCode = searchParams.get('code') || 'ORD-UNKNOWN';

    return (
        <div style={{ padding: '60px 20px', textAlign: 'center', fontFamily: 'system-ui, -apple-system, sans-serif', maxWidth: '600px', margin: '0 auto' }}>
            <div style={{ fontSize: '72px', color: '#10b981', marginBottom: '20px' }}>✓</div>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1f2937', marginBottom: '10px' }}>Đặt hàng thành công!</h2>
            <p style={{ color: '#4b5563', fontSize: '15px', marginBottom: '25px', lineHeight: '1.6' }}>
                Cảm ơn bạn đã mua sắm tại TechStore. Đơn hàng của bạn đang được xử lý.
            </p>

            <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', padding: '15px 25px', borderRadius: '8px', marginBottom: '35px', display: 'inline-block' }}>
                <span style={{ color: '#6b7280', fontSize: '14px', display: 'block', marginBottom: '5px' }}>Mã đơn hàng của bạn:</span>
                <strong style={{ fontSize: '20px', color: '#111827', letterSpacing: '1px' }}>{orderCode}</strong>
            </div>

            <div style={{ display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap' }}>
                <Link to="/orders" style={{ textDecoration: 'none' }}>
                    <button style={{ padding: '12px 25px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }}>
                        Xem lịch sử mua hàng
                    </button>
                </Link>
                <Link to="/products" style={{ textDecoration: 'none' }}>
                    <button style={{ padding: '12px 25px', background: 'white', color: '#3643ba', border: '1px solid #3643ba', borderRadius: '6px', fontSize: '15px', fontWeight: 'bold', cursor: 'pointer', transition: 'all 0.2s' }}>
                        Tiếp tục mua sắm
                    </button>
                </Link>
            </div>
        </div>
    );
}

export default OrderSuccess;
