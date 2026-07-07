import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import orderService from '../services/orderService';

function PaymentSimulation() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    
    const orderId = searchParams.get('orderId');
    const code = searchParams.get('code');
    const method = searchParams.get('method') || 'VNPAY';
    
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(false);

    useEffect(() => {
        if (!orderId) {
            alert("⚠️ Không tìm thấy mã đơn hàng thanh toán!");
            navigate('/');
            return;
        }

        orderService.getOrderDetails(orderId)
            .then(res => {
                if (res && res.success) {
                    setOrder(res.data);
                } else {
                    alert("⚠️ Không tìm thấy thông tin đơn hàng!");
                    navigate('/');
                }
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                alert("⚠️ Lỗi tải thông tin đơn hàng!");
                navigate('/');
                setLoading(false);
            });
    }, [orderId, navigate]);

    const handlePaymentConfirm = async () => {
        setProcessing(true);
        try {
            const res = await orderService.payOrder(orderId);
            if (res && res.success) {
                alert(`✓ Giao dịch thanh toán trực tuyến qua ${method} thành công!`);
                navigate(`/order-success?code=${code}`);
            }
        } catch (err) {
            alert("Lỗi thanh toán: " + (err.response?.data?.message || err.message));
        } finally {
            setProcessing(false);
        }
    };

    const handlePaymentCancel = () => {
        alert("⚠️ Giao dịch thanh toán đã bị hủy. Đơn hàng của bạn sẽ ở trạng thái chờ xử lý (PENDING).");
        navigate('/orders');
    };

    if (loading) return <div style={{ padding: '60px', textAlign: 'center', fontSize: '18px' }}>⏳ Đang kết nối tới cổng thanh toán {method}...</div>;

    const isMomo = method.toUpperCase() === 'MOMO';
    const primaryColor = isMomo ? '#a21caf' : '#075985'; // Momo magenta vs VNPAY deep blue
    const logoText = isMomo ? 'MOMO MOCK GATEWAY' : 'VNPAY MOCK GATEWAY';

    return (
        <div style={{ 
            maxWidth: '550px', 
            margin: '50px auto', 
            background: 'white', 
            borderRadius: '12px', 
            border: `2px solid ${primaryColor}`, 
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
            overflow: 'hidden',
            fontFamily: 'system-ui, -apple-system, sans-serif'
        }}>
            {/* Header simulated logo banner */}
            <div style={{ background: primaryColor, padding: '25px', color: 'white', textAlign: 'center' }}>
                <h2 style={{ margin: 0, fontSize: '22px', fontWeight: '800', letterSpacing: '1px' }}>
                    {logoText}
                </h2>
                <p style={{ margin: '5px 0 0 0', opacity: 0.8, fontSize: '13px' }}>Cổng thanh toán trực tuyến giả lập an toàn</p>
            </div>

            <div style={{ padding: '30px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1e293b', marginBottom: '20px', textAlign: 'center' }}>
                    Xác Nhận Thanh Toán
                </h3>

                <div style={{ background: '#f8fafc', borderRadius: '8px', padding: '20px', border: '1px solid #e2e8f0', marginBottom: '25px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                        <span style={{ color: '#64748b' }}>Mã đơn hàng:</span>
                        <strong style={{ color: '#0f172a' }}>{code}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                        <span style={{ color: '#64748b' }}>Phương thức:</span>
                        <strong style={{ color: '#0f172a' }}>Thanh toán qua ví {method}</strong>
                    </div>
                    <div style={{ width: '100%', height: '1px', background: '#e2e8f0' }}></div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: '#64748b', fontWeight: '600' }}>Số tiền cần trả:</span>
                        <strong style={{ color: primaryColor, fontSize: '20px', fontWeight: '800' }}>
                            {order ? order.totalPrice.toLocaleString() : '...'} đ
                        </strong>
                    </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <button 
                        onClick={handlePaymentConfirm}
                        disabled={processing}
                        style={{ 
                            width: '100%', 
                            padding: '14px', 
                            background: primaryColor, 
                            color: 'white', 
                            border: 'none', 
                            borderRadius: '6px', 
                            fontSize: '15px', 
                            fontWeight: 'bold', 
                            cursor: processing ? 'not-allowed' : 'pointer',
                            boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)',
                            transition: 'opacity 0.2s'
                        }}
                        onMouseEnter={(e) => e.target.style.opacity = '0.9'}
                        onMouseLeave={(e) => e.target.style.opacity = '1'}
                    >
                        {processing ? 'Đang xử lý giao dịch...' : 'XÁC NHẬN THANH TOÁN THÀNH CÔNG'}
                    </button>
                    
                    <button 
                        onClick={handlePaymentCancel}
                        disabled={processing}
                        style={{ 
                            width: '100%', 
                            padding: '14px', 
                            background: 'white', 
                            color: '#e11d48', 
                            border: '1px solid #fda4af', 
                            borderRadius: '6px', 
                            fontSize: '15px', 
                            fontWeight: '600', 
                            cursor: processing ? 'not-allowed' : 'pointer',
                            transition: 'background 0.2s'
                        }}
                        onMouseEnter={(e) => e.target.style.background = '#fff1f2'}
                        onMouseLeave={(e) => e.target.style.background = 'white'}
                    >
                        HỦY GIAO DỊCH
                    </button>
                </div>
            </div>

            <div style={{ background: '#f1f5f9', padding: '15px', textAlign: 'center', fontSize: '11px', color: '#94a3b8', borderTop: '1px solid #e2e8f0' }}>
                Bản quyền mô phỏng thuộc cổng TechStore E-Commerce. Không thực hiện trừ tiền thật.
            </div>
        </div>
    );
}

export default PaymentSimulation;
