import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import orderService from '../services/orderService';
import Breadcrumb from '../components/Breadcrumb';
import { formatPrice } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { IconCreditCard, IconWarning } from '../utils/icons';
import './Checkout.css';

function Checkout() {
    const { cartItems, clearCart } = useContext(CartContext);
    const navigate = useNavigate();
    const toast = useToast();

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
            toast.error("Vui lòng nhập địa chỉ nhận hàng!");
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
                toast.success("Đặt hàng thành công!");
                
                // Navigate to success page or simulated payment gateway
                if (paymentMethod === 'COD') {
                    navigate(`/order-success?code=${createdOrder.orderCode}`);
                } else {
                    navigate(`/payment-simulation?orderId=${createdOrder.id}&code=${createdOrder.orderCode}&method=${paymentMethod}`);
                }
            } else {
                setError(res?.message || "Đặt hàng không thành công, vui lòng thử lại!");
                toast.error(res?.message || "Đặt hàng không thành công!");
            }
        } catch (err) {
            console.error("Order creation error:", err);
            setError(err.response?.data?.message || err.message);
            toast.error(err.response?.data?.message || err.message);
        } finally {
            setLoading(false);
        }
    };

    if (cartItems.length === 0) {
        return (
            <div className="container">
                <Breadcrumb items={[{ label: 'Trang chủ', to: '/' }, { label: 'Thanh toán' }]} />
                <div className="empty-state card" style={{ marginTop: 'var(--space-4)' }}>
                    <div className="empty-state-icon"><IconWarning /></div>
                    <h2 className="empty-state-title">Không có sản phẩm nào để thanh toán</h2>
                    <button className="btn btn-primary" onClick={() => navigate('/products')}>
                        Quay lại xem sản phẩm
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="container">
            {/* Breadcrumb */}
            <Breadcrumb items={[
                { label: 'Trang chủ', to: '/' },
                { label: 'Giỏ hàng', to: '/cart' },
                { label: 'Thanh toán' }
            ]} />

            <h2 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: 800, color: 'var(--color-gray-900)', marginTop: 'var(--space-2)' }}>
                Thanh Toán Đơn Hàng
            </h2>

            {error && (
                <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3) var(--space-4)', margin: 'var(--space-3) 0', fontSize: 'var(--font-size-base)', display: 'flex', gap: 6, alignItems: 'center' }}>
                    <IconWarning size={16} /> Lỗi đặt hàng: {error}
                </div>
            )}

            <form onSubmit={handleSubmitOrder} className="checkout-layout">
                {/* 1. SHIPPING & PAYMENT INFO */}
                <div style={{ minWidth: 0 }}>
                    {/* Shipping Address */}
                    <div className="checkout-section-card">
                        <h3 className="checkout-section-title">1. Thông tin giao hàng</h3>
                        <div className="form-group">
                            <label className="form-label">Địa chỉ nhận hàng chi tiết *</label>
                            <textarea 
                                className="form-textarea"
                                rows="3"
                                placeholder="Ví dụ: Số 123 Đường Nguyễn Trãi, Quận Thanh Xuân, Hà Nội"
                                value={shippingAddress}
                                onChange={(e) => setShippingAddress(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    {/* Payment Method */}
                    <div className="checkout-section-card">
                        <h3 className="checkout-section-title">2. Phương thức thanh toán</h3>
                        <div className="payment-methods-list">
                            <label className={`payment-method-label ${paymentMethod === 'COD' ? 'active' : ''}`}>
                                <input 
                                    type="radio" 
                                    className="payment-method-radio"
                                    name="payment" 
                                    value="COD" 
                                    checked={paymentMethod === 'COD'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong className="payment-method-title">Thanh toán khi nhận hàng (COD)</strong>
                                    <div className="payment-method-desc">Thanh toán bằng tiền mặt khi shipper giao hàng tận nơi.</div>
                                </div>
                            </label>

                            <label className={`payment-method-label ${paymentMethod === 'VNPAY' ? 'active' : ''}`}>
                                <input 
                                    type="radio" 
                                    className="payment-method-radio"
                                    name="payment" 
                                    value="VNPAY" 
                                    checked={paymentMethod === 'VNPAY'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong className="payment-method-title">Thanh toán qua ví VNPay (QR Code)</strong>
                                    <div className="payment-method-desc">Quét mã QR bằng ứng dụng ngân hàng hoặc ví VNPay.</div>
                                </div>
                            </label>

                            <label className={`payment-method-label ${paymentMethod === 'MOMO' ? 'active' : ''}`}>
                                <input 
                                    type="radio" 
                                    className="payment-method-radio"
                                    name="payment" 
                                    value="MOMO" 
                                    checked={paymentMethod === 'MOMO'}
                                    onChange={(e) => setPaymentMethod(e.target.value)}
                                />
                                <div>
                                    <strong className="payment-method-title">Thanh toán qua ví MoMo</strong>
                                    <div className="payment-method-desc">Thanh toán trực tuyến bằng số dư ví MoMo tiện lợi.</div>
                                </div>
                            </label>
                        </div>
                    </div>
                </div>

                {/* 2. ORDER SUMMARY & REVIEW (Bên phải) */}
                <aside className="checkout-summary">
                    <h3 className="checkout-summary-title">3. Xem lại đơn hàng</h3>

                    {/* Cart Items List */}
                    <div style={{ maxHeight: '180px', overflowY: 'auto', marginBottom: '20px', display: 'flex', flexDirection: 'column', gap: '10px', paddingRight: '4px' }}>
                        {cartItems.map((item) => (
                            <div key={item.id} className="checkout-item-row">
                                <span style={{ maxWidth: '70%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={item.productName}>
                                    {item.productName} <strong style={{ color: 'var(--color-gray-900)' }}>x{item.quantity}</strong>
                                </span>
                                <span className="font-semibold">{formatPrice(getPrice(item) * item.quantity)}</span>
                            </div>
                        ))}
                    </div>

                    <div className="divider" />

                    <div className="summary-row">
                        <span>Tạm tính:</span>
                        <span>{formatPrice(subtotal)}</span>
                    </div>

                    <div className="summary-row">
                        <span>Phí giao hàng:</span>
                        <span>{shippingFee === 0 ? 'Miễn phí' : formatPrice(shippingFee)}</span>
                    </div>

                    <div className="summary-row total-row" style={{ marginTop: 'var(--space-4)', paddingTop: 'var(--space-3)' }}>
                        <span>Tổng thanh toán:</span>
                        <span className="total-price">{formatPrice(total)}</span>
                    </div>

                    <button 
                        type="submit"
                        disabled={loading}
                        className="btn btn-primary btn-block btn-lg"
                        style={{ marginTop: 'var(--space-5)', display: 'flex', gap: 8 }}
                    >
                        <IconCreditCard size={18} />
                        {loading ? 'ĐANG ĐẶT HÀNG...' : 'XÁC NHẬN ĐẶT HÀNG'}
                    </button>
                </aside>
            </form>
        </div>
    );
}

export default Checkout;
