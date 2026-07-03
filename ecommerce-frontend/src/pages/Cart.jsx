import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';

function Cart() {
    const { cartItems, updateQuantity, removeFromCart } = useContext(CartContext);
    const { isAuthenticated } = useContext(AuthContext);
    const navigate = useNavigate();

    // Calculations
    const getPrice = (item) => {
        return item.productSalePrice && item.productSalePrice > 0 ? item.productSalePrice : item.productPrice;
    };

    const subtotal = cartItems.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
    const shippingFee = subtotal > 500000 || subtotal === 0 ? 0 : 30000; // Free ship over 500k
    const total = subtotal + shippingFee;

    const handleCheckoutClick = () => {
        if (!isAuthenticated) {
            alert("Vui lòng đăng nhập để tiến hành thanh toán!");
            navigate('/login?redirect=checkout');
        } else {
            navigate('/checkout');
        }
    };

    if (cartItems.length === 0) {
        return (
            <div style={{ padding: '60px 20px', textAlign: 'center', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
                <div style={{ fontSize: '72px', marginBottom: '20px' }}>🛒</div>
                <h2 style={{ fontSize: '26px', fontWeight: '800', color: '#1f2937', marginBottom: '10px' }}>Giỏ hàng của bạn đang trống</h2>
                <p style={{ color: '#6b7280', fontSize: '15px', marginBottom: '30px' }}>Hãy duyệt qua các danh mục của cửa hàng và tìm thêm các sản phẩm ưng ý nhé!</p>
                <Link to="/products" style={{ textDecoration: 'none' }}>
                    <button style={{ padding: '12px 30px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = '#2a3494'}
                            onMouseLeave={(e) => e.target.style.background = '#3643ba'}>
                        Quay lại mua sắm
                    </button>
                </Link>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Giỏ Hàng Của Bạn
            </h2>

            <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
                {/* 1. LIST OF ITEMS (Bên trái) */}
                <div style={{ flex: '2 1 700px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {cartItems.map((item) => {
                            const isSale = item.productSalePrice && item.productSalePrice > 0;
                            const price = getPrice(item);
                            return (
                                <div key={item.id} style={{ display: 'flex', gap: '20px', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '15px', background: '#fff', alignItems: 'center', flexWrap: 'wrap' }}>
                                    {/* Product Image */}
                                    <div style={{ width: '90px', height: '90px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f9fafb', borderRadius: '6px', overflow: 'hidden', flexShrink: 0 }}>
                                        <img src={item.productImageUrl || "https://via.placeholder.com/90"} alt={item.productName} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                    </div>

                                    {/* Name & price */}
                                    <div style={{ flex: '1 1 200px' }}>
                                        <h4 style={{ fontSize: '16px', fontWeight: 'bold', margin: '0 0 8px 0', color: '#1f2937' }}>{item.productName}</h4>
                                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
                                            <span style={{ fontSize: '15px', fontWeight: 'bold', color: isSale ? '#ef4444' : '#3643ba' }}>{price.toLocaleString('vi-VN')} đ</span>
                                            {isSale && (
                                                <span style={{ fontSize: '12px', color: '#9ca3af', textDecoration: 'line-through' }}>{item.productPrice.toLocaleString('vi-VN')} đ</span>
                                            )}
                                        </div>
                                    </div>

                                    {/* Quantity editor */}
                                    <div style={{ display: 'flex', alignItems: 'center', border: '1px solid #d1d5db', borderRadius: '4px', background: '#fff', height: '35px' }}>
                                        <button 
                                            onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                            style={{ border: 'none', background: 'none', padding: '0 12px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' }}
                                        >
                                            -
                                        </button>
                                        <span style={{ padding: '0 10px', fontSize: '14px', fontWeight: 'bold', minWidth: '20px', textAlign: 'center' }}>{item.quantity}</span>
                                        <button 
                                            onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                            style={{ border: 'none', background: 'none', padding: '0 12px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' }}
                                        >
                                            +
                                        </button>
                                    </div>

                                    {/* Line Total */}
                                    <div style={{ width: '120px', textAlign: 'right' }}>
                                        <span style={{ fontSize: '16px', fontWeight: '800', color: '#1f2937' }}>{(price * item.quantity).toLocaleString('vi-VN')} đ</span>
                                    </div>

                                    {/* Delete Button */}
                                    <button 
                                        onClick={() => removeFromCart(item.id)}
                                        style={{ background: 'none', border: 'none', color: '#9ca3af', cursor: 'pointer', fontSize: '18px', padding: '5px', transition: 'color 0.2s' }}
                                        onMouseEnter={(e) => e.target.style.color = '#ef4444'}
                                        onMouseLeave={(e) => e.target.style.color = '#9ca3af'}
                                        title="Xóa khỏi giỏ hàng"
                                    >
                                        🗑️
                                    </button>
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* 2. ORDER SUMMARY (Bên phải) */}
                <div style={{ flex: '1 1 350px' }}>
                    <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', position: 'sticky', top: '20px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '20px', borderBottom: '1px solid #e5e7eb', paddingBottom: '10px' }}>
                            Tóm tắt đơn hàng
                        </h3>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: '#4b5563', marginBottom: '12px' }}>
                            <span>Tạm tính:</span>
                            <span>{subtotal.toLocaleString('vi-VN')} đ</span>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px', color: '#4b5563', marginBottom: '12px' }}>
                            <span>Phí vận chuyển:</span>
                            <span>{shippingFee === 0 ? 'Miễn phí' : `${shippingFee.toLocaleString('vi-VN')} đ`}</span>
                        </div>

                        {shippingFee > 0 && (
                            <p style={{ fontSize: '11px', color: '#6b7280', margin: '-5px 0 15px 0', fontStyle: 'italic' }}>
                                (Mua thêm {(500000 - subtotal).toLocaleString('vi-VN')} đ để được miễn phí giao hàng)
                            </p>
                        )}

                        <div style={{ width: '100%', height: '1px', background: '#e5e7eb', margin: '15px 0' }}></div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '18px', fontWeight: '800', color: '#111827', marginBottom: '25px' }}>
                            <span>Tổng cộng:</span>
                            <span style={{ color: '#ef4444' }}>{total.toLocaleString('vi-VN')} đ</span>
                        </div>

                        <button 
                            onClick={handleCheckoutClick}
                            style={{ width: '100%', padding: '14px 0', background: '#3643ba', color: 'white', border: 'none', borderRadius: '6px', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = '#2a3494'}
                            onMouseLeave={(e) => e.target.style.background = '#3643ba'}
                        >
                            TIẾN HÀNH THANH TOÁN
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Cart;
