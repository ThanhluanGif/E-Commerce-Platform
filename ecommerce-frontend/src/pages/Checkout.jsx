import React, { useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import orderService from '../services/orderService';
import addressService from '../services/addressService';
import api from '../services/api';
import Breadcrumb from '../components/Breadcrumb';
import { formatPrice } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { IconCreditCard, IconWarning } from '../utils/icons';
import './Checkout.css';

function Checkout() {
    const { cartItems, clearCart } = useContext(CartContext);
    const navigate = useNavigate();
    const toast = useToast();

    // Address book states
    const [addresses, setAddresses] = useState([]);
    const [selectedAddressId, setSelectedAddressId] = useState('');

    // Form inputs
    const [shippingAddress, setShippingAddress] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('COD');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [usePoints, setUsePoints] = useState(false);
    const [userPointsVal, setUserPointsVal] = useState(0);
    const [userTier, setUserTier] = useState('BRONZE');

    // Shipping options states
    const [shippingOptions, setShippingOptions] = useState([]);
    const [selectedShippingOption, setSelectedShippingOption] = useState(null);
    const [shippingFee, setShippingFee] = useState(30000);

    // Calculate shipping fee dynamically when address changes
    useEffect(() => {
        if (!shippingAddress || shippingAddress.trim() === '') {
            setShippingOptions([]);
            setSelectedShippingOption(null);
            setShippingFee(30000);
            return;
        }

        // Try to guess city (split by comma and get last element)
        const parts = shippingAddress.split(',');
        const city = parts[parts.length - 1].trim();

        api.get(`/api/shipping/calculate?city=${encodeURIComponent(city)}`)
            .then(res => {
                if (res && res.data && res.data.success && Array.isArray(res.data.data)) {
                    setShippingOptions(res.data.data);
                    if (res.data.data.length > 0) {
                        setSelectedShippingOption(res.data.data[0]);
                        setShippingFee(res.data.data[0].fee);
                    }
                }
            })
            .catch(err => {
                console.error("Error calculating shipping fee:", err);
                setShippingFee(30000); // fallback
            });
    }, [shippingAddress]);

    // Fetch address book and points on mount
    useEffect(() => {
        api.get('/api/loyalty/points')
            .then(res => {
                if (res.data && res.data.success && res.data.data) {
                    setUserPointsVal(res.data.data.points || 0);
                    setUserTier(res.data.data.tier || 'BRONZE');
                }
            })
            .catch(err => console.error("Error loading points for checkout:", err));

        addressService.getAllAddresses()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setAddresses(res.data);
                    
                    // Preselect default address if present
                    const defaultAddr = res.data.find(a => a.isDefault);
                    if (defaultAddr) {
                        setSelectedAddressId(defaultAddr.id.toString());
                        const formatted = `${defaultAddr.fullName} (${defaultAddr.phone}) - ${defaultAddr.street}, ${defaultAddr.ward}, ${defaultAddr.district}, ${defaultAddr.city}`;
                        setShippingAddress(formatted);
                    } else if (res.data.length > 0) {
                        setSelectedAddressId(res.data[0].id.toString());
                        const firstAddr = res.data[0];
                        const formatted = `${firstAddr.fullName} (${firstAddr.phone}) - ${firstAddr.street}, ${firstAddr.ward}, ${firstAddr.district}, ${firstAddr.city}`;
                        setShippingAddress(formatted);
                    }
                }
            })
            .catch(err => console.error("Error loading address book:", err));
    }, []);

    // Handle Address change selection
    const handleAddressChange = (addrId) => {
        setSelectedAddressId(addrId);
        if (addrId === 'custom') {
            setShippingAddress('');
        } else {
            const match = addresses.find(a => a.id.toString() === addrId);
            if (match) {
                const formatted = `${match.fullName} (${match.phone}) - ${match.street}, ${match.ward}, ${match.district}, ${match.city}`;
                setShippingAddress(formatted);
            }
        }
    };

    // Calculation helper
    const getPrice = (item) => {
        return item.productSalePrice && item.productSalePrice > 0 ? item.productSalePrice : item.productPrice;
    };

    const subtotal = cartItems.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);

    // Member tier discount
    let tierDiscountPercent = 0;
    if (userTier === 'GOLD') tierDiscountPercent = 0.05;
    else if (userTier === 'PLATINUM') tierDiscountPercent = 0.10;
    else if (userTier === 'DIAMOND') tierDiscountPercent = 0.15;
    const tierDiscountAmount = subtotal * tierDiscountPercent;

    // Points discount (1 xu = 1,000 VND)
    const maxPointsDiscount = Math.min(subtotal - tierDiscountAmount, userPointsVal * 1000);
    const pointsDiscountAmount = usePoints ? maxPointsDiscount : 0;
    const totalDiscount = tierDiscountAmount + pointsDiscountAmount;

    // Diamond tier gets Free Shipping
    const finalShippingFee = (userTier === 'DIAMOND' || subtotal > 500000) ? 0 : shippingFee;
    const total = Math.max(0, subtotal - totalDiscount + finalShippingFee);

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
                paymentMethod,
                discountAmount: totalDiscount,
                pointsUsed: usePoints ? Math.floor(pointsDiscountAmount / 1000) : 0
            });

            if (res && res.success && res.data) {
                const createdOrder = res.data;
                // Clear the local cart items state
                await clearCart();
                toast.success("Đặt hàng thành công!");
                
                // Navigate to success page or simulated payment gateway
                if (paymentMethod === 'COD') {
                    navigate(`/order-success?code=${createdOrder.orderCode}`);
                } else if (paymentMethod === 'VNPAY') {
                    try {
                        const payRes = await api.post(`/api/payments/create-url/${createdOrder.id}`);
                        if (payRes && payRes.data && payRes.data.success && payRes.data.data) {
                            window.location.href = payRes.data.data;
                        } else {
                            toast.error("Không khởi tạo được link thanh toán VNPay!");
                            navigate(`/orders/${createdOrder.id}`);
                        }
                    } catch (payErr) {
                        console.error("VNPay redirect error:", payErr);
                        toast.error("Không khởi tạo được cổng thanh toán VNPay!");
                        navigate(`/orders/${createdOrder.id}`);
                    }
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
                        {addresses.length > 0 && (
                            <div className="form-group" style={{ marginBottom: '15px' }}>
                                <label className="form-label">Chọn địa chỉ đã lưu</label>
                                <select 
                                    className="form-select"
                                    value={selectedAddressId}
                                    onChange={(e) => handleAddressChange(e.target.value)}
                                >
                                    {addresses.map(a => (
                                        <option key={a.id} value={a.id}>
                                            {a.fullName} ({a.phone}) - {a.street}, {a.city} {a.isDefault ? ' [Mặc định]' : ''}
                                        </option>
                                    ))}
                                    <option value="custom">Nhập địa chỉ khác...</option>
                                </select>
                            </div>
                        )}
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
                        {shippingOptions.length > 0 && (
                            <div className="form-group" style={{ marginTop: '15px' }}>
                                <label className="form-label">Chọn đơn vị vận chuyển</label>
                                <div className="shipping-options-list" style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    {shippingOptions.map(option => (
                                        <label 
                                            key={option.id} 
                                            className={`payment-method-label ${selectedShippingOption?.id === option.id ? 'active' : ''}`}
                                            style={{ padding: '12px var(--space-4)', display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', border: '1px solid var(--color-gray-200)', borderRadius: 'var(--border-radius-sm)' }}
                                        >
                                            <input 
                                                type="radio" 
                                                name="shippingOption" 
                                                value={option.id}
                                                checked={selectedShippingOption?.id === option.id}
                                                onChange={() => {
                                                    setSelectedShippingOption(option);
                                                    setShippingFee(option.fee);
                                                }}
                                            />
                                            <div style={{ flex: 1 }}>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 600 }}>
                                                    <span style={{ color: 'var(--color-gray-900)' }}>{option.name}</span>
                                                    <span style={{ color: 'var(--color-primary)' }}>{formatPrice(option.fee)}</span>
                                                </div>
                                                <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)', marginTop: '2px' }}>Dự kiến nhận hàng: {option.estimatedDelivery}</div>
                                            </div>
                                        </label>
                                    ))}
                                </div>
                            </div>
                        )}
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
                                <span style={{ maxWidth: '70%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', display: 'flex', flexDirection: 'column' }} title={item.productName}>
                                    <span>
                                        {item.productName} <strong style={{ color: 'var(--color-gray-900)' }}>x{item.quantity}</strong>
                                    </span>
                                    {item.variantName && (
                                        <span style={{ fontSize: '10px', color: 'var(--color-gray-500)', fontStyle: 'italic' }}>
                                            Phân loại: {item.variantName}
                                        </span>
                                    )}
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

                    {tierDiscountAmount > 0 && (
                        <div className="summary-row" style={{ color: 'var(--color-primary)' }}>
                            <span>Ưu đãi hạng {userTier}:</span>
                            <span>-{formatPrice(tierDiscountAmount)}</span>
                        </div>
                    )}

                    {/* Point usage option */}
                    {userPointsVal > 0 && (
                        <div style={{ backgroundColor: 'var(--color-gray-50)', border: '1px solid var(--color-gray-200)', borderRadius: 'var(--border-radius-xs)', padding: '10px', margin: '10px 0' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', margin: 0, fontSize: '13px' }}>
                                <input 
                                    type="checkbox" 
                                    checked={usePoints} 
                                    onChange={(e) => setUsePoints(e.target.checked)} 
                                />
                                <div>
                                    Dùng <strong>{userPointsVal} xu</strong> (giảm -{formatPrice(maxPointsDiscount)})
                                </div>
                            </label>
                        </div>
                    )}

                    {pointsDiscountAmount > 0 && (
                        <div className="summary-row" style={{ color: 'var(--color-primary)' }}>
                            <span>Điểm tích lũy:</span>
                            <span>-{formatPrice(pointsDiscountAmount)}</span>
                        </div>
                    )}

                    <div className="summary-row">
                        <span>Phí giao hàng:</span>
                        <span>{finalShippingFee === 0 ? 'Miễn phí' : formatPrice(finalShippingFee)}</span>
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
