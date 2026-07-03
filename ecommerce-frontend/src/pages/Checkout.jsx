import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import orderService from '../services/orderService';

function Checkout() {
    const { cartItems, clearCart } = useContext(CartContext);
    const navigate = useNavigate();

    // Form inputs
    const [shippingAddress, setShippingAddress] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('COD');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Calculation helper
    const getPrice = (item) => {
        return item.productSalePrice && item.productSalePrice > 0 ? item.productSalePrice : item.productPrice;
    };

    const subtotal = cartItems.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
    const shippingFee = subtotal > 500000 || subtotal === 0 ? 0 : 30000;
    const total = subtotal + shippingFee;

    const handleSubmitOrder = async (e) => {
        e.preventDefault();
        if (shippingAddress.trim() === '') {
            setError("Vui lòng nhập địa chỉ nhận hàng!");
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const res = await orderService.createOrder({
                shippingAddress,
                paymentMethod
            });

            if (res && res.success && res.data) {
                const createdOrder = res.data;
                // Clear the local cart items state
                await clearCart();
                // Navigate to success page or simulated payment gateway
                if (paymentMethod === 'COD') {
                    navigate(`/order-success?code=${createdOrder.orderCode}`);
                } else {
                    navigate(`/payment-simulation?orderId=${createdOrder.id}&code=${createdOrder.orderCode}&method=${paymentMethod}`);
                }
            } else {
                setError(res?.message || "Đặt hàng không thành công, vui lòng thử lại!");
            }
        } catch (err) {
            console.error("Order creation error:", err);
            setError(err.response?.data?.message || err.message);
        } finally {
            setLoading(false);
        }
    };

    if (cartItems.length === 0) {
        return (
            <div style={{ padding: '60px 20px', textAlign: 'center', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
                <h2 style={{ fontSize: '24px', fontWeight: 'bold', color: '#1f2937' }}>Không có sản phẩm nào để thanh toán</h2>
                <button onClick={() => navigate('/products')} style={{ marginTop: '20px', padding: '10px 20px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    Quay lại xem sản phẩm
                </button>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '1000px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Thanh Toán Đơn Hàng
            </h2>

            {error && (
                <div style={{ padding: '15px', color: '#b91c1c', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '6px', marginBottom: '20px', fontSize: '14px' }}>
                    ⚠️ {error}
                </div>
            )}

            <form onSubmit={handleSubmitOrder} style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
                
                {/* 1. SHIPPING & PAYMENT INFO (Bên trái) */}
                <div style={{ flex: '2 1 500px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '15px' }}>
                            1. Thông tin giao hàng
                        </h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <label style={{ fontSize: '14px', fontWeight: '600', color: '#4b5563' }}>Địa chỉ nhận hàng chi tiết *</label>
                            <textarea 
                                rows="3"
                                placeholder="Ví dụ: Số 123 Đường Nguyễn Trãi, Quận Thanh Xuân, Hà Nội"
                                value={shippingAddress}
                                onChange={(e) => setShippingAddress(e.target.value)}
                                style={{ padding: '12px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none', width: '100%', boxSizing: 'border-box' }}
                                required
                            />
                        </div>
                    </div>

                    <div style={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '15px' }}>
                            2. Phương thức thanh toán
                        </h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', cursor: 'pointer', padding: '10px', border: '1px solid #e5e7eb', borderRadius: '4px', background: paymentMethod === 'COD' ? '#eff6ff' : 'none' }}>
                                <input 
                                    type="radio" 
                                    name="payment" 
                                    value="COD" 
                                    checked={paymentMethod === 'COD'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong>Thanh toán khi nhận hàng (COD)</strong>
                                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Thanh toán bằng tiền mặt khi shipper giao hàng tận nơi.</div>
                                </div>
                            </label>

                            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', cursor: 'pointer', padding: '10px', border: '1px solid #e5e7eb', borderRadius: '4px', background: paymentMethod === 'VNPAY' ? '#eff6ff' : 'none' }}>
                                <input 
                                    type="radio" 
                                    name="payment" 
                                    value="VNPAY" 
                                    checked={paymentMethod === 'VNPAY'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong>Thanh toán qua ví VNPay (QR Code)</strong>
                                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Quét mã QR bằng ứng dụng ngân hàng hoặc ví VNPay.</div>
                                </div>
                            </label>

                            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', cursor: 'pointer', padding: '10px', border: '1px solid #e5e7eb', borderRadius: '4px', background: paymentMethod === 'MOMO' ? '#eff6ff' : 'none' }}>
                                <input 
                                    type="radio" 
                                    name="payment" 
                                    value="MOMO" 
                                    checked={paymentMethod === 'MOMO'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong>Thanh toán qua ví MoMo</strong>
                                    <div style={{ fontSize: '12px', color: '#6b7280' }}>Thanh toán trực tuyến bằng số dư ví MoMo tiện lợi.</div>
                                </div>
                            </label>
                        </div>
                    </div>
                </div>

                {/* 2. ORDER SUMMARY & REVIEW (Bên phải) */}
                <div style={{ flex: '1 1 350px' }}>
                    <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '15px', borderBottom: '1px solid #e5e7eb', paddingBottom: '10px' }}>
                            3. Xem lại đơn hàng
                        </h3>

                        {/* Cart items list */}
                        <div style={{ maxHeight: '180px', overflowY: 'auto', marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            {cartItems.map((item) => (
                                <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: '#4b5563' }}>
                                    <span style={{ maxWidth: '70%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                        {item.productName} <strong style={{ color: '#1f2937' }}>x{item.quantity}</strong>
                                    </span>
                                    <span>{(getPrice(item) * item.quantity).toLocaleString('vi-VN')} đ</span>
                                </div>
                            ))}
                        </div>

                        <div style={{ width: '100%', height: '1px', background: '#e5e7eb', margin: '15px 0' }}></div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: '#4b5563', marginBottom: '10px' }}>
                            <span>Tạm tính:</span>
                            <span>{subtotal.toLocaleString('vi-VN')} đ</span>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: '#4b5563', marginBottom: '10px' }}>
                            <span>Phí giao hàng:</span>
                            <span>{shippingFee === 0 ? 'Miễn phí' : `${shippingFee.toLocaleString('vi-VN')} đ`}</span>
                        </div>

                        <div style={{ width: '100%', height: '1px', background: '#e5e7eb', margin: '15px 0' }}></div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '18px', fontWeight: '800', color: '#111827', marginBottom: '25px' }}>
                            <span>Tổng thanh toán:</span>
                            <span style={{ color: '#ef4444' }}>{total.toLocaleString('vi-VN')} đ</span>
                        </div>

                        <button 
                            type="submit"
                            disabled={loading}
                            style={{ width: '100%', padding: '14px 0', background: loading ? '#9ca3af' : '#3643ba', color: 'white', border: 'none', borderRadius: '6px', fontSize: '16px', fontWeight: 'bold', cursor: loading ? 'not-allowed' : 'pointer', transition: 'background 0.2s' }}
                        >
                            {loading ? 'ĐANG ĐẶT HÀNG...' : 'XÁC NHẬN ĐẶT HÀNG'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    );
}

export default Checkout;
