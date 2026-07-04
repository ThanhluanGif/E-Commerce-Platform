import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import { AuthContext } from '../context/AuthContext';
import Breadcrumb from '../components/Breadcrumb';
import { formatPrice, getProductImage } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { IconCart, IconPlus, IconMinus, IconTrash } from '../utils/icons';
import './Cart.css';

function Cart() {
    const { cartItems, updateQuantity, removeFromCart } = useContext(CartContext);
    const { isAuthenticated } = useContext(AuthContext);
    const navigate = useNavigate();
    const toast = useToast();

    // Calculations
    const getPrice = (item) => {
        return item.productSalePrice && item.productSalePrice > 0 ? item.productSalePrice : item.productPrice;
    };

    const subtotal = cartItems.reduce((sum, item) => sum + getPrice(item) * item.quantity, 0);
    const shippingFee = subtotal > 500000 || subtotal === 0 ? 0 : 30000; // Free ship over 500k
    const total = subtotal + shippingFee;

    const handleCheckoutClick = () => {
        if (!isAuthenticated) {
            toast.warning("Vui lòng đăng nhập để tiến hành thanh toán!");
            navigate('/login?redirect=checkout');
        } else {
            navigate('/checkout');
        }
    };

    const handleUpdateQuantity = (item, newQty) => {
        if (newQty < 1) return;
        updateQuantity(item.id, newQty);
        toast.info("Đã cập nhật số lượng!");
    };

    const handleRemove = (item) => {
        removeFromCart(item.id);
        toast.success(`Đã xóa ${item.productName} khỏi giỏ hàng!`);
    };

    if (cartItems.length === 0) {
        return (
            <div className="container">
                <Breadcrumb items={[{ label: 'Trang chủ', to: '/' }, { label: 'Giỏ hàng' }]} />
                <div className="empty-state card" style={{ marginTop: 'var(--space-4)' }}>
                    <div className="empty-state-icon"><IconCart /></div>
                    <h2 className="empty-state-title">Giỏ hàng của bạn đang trống</h2>
                    <p className="empty-state-text">Hãy duyệt qua các danh mục của cửa hàng và tìm thêm các sản phẩm ưng ý nhé!</p>
                    <Link to="/products">
                        <button className="btn btn-primary">
                            Quay lại mua sắm
                        </button>
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="container">
            {/* Breadcrumb */}
            <Breadcrumb items={[
                { label: 'Trang chủ', to: '/' },
                { label: 'Giỏ hàng' }
            ]} />

            <h2 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: 800, color: 'var(--color-gray-900)', marginTop: 'var(--space-2)' }}>
                Giỏ Hàng Của Bạn
            </h2>

            <div className="cart-layout">
                {/* 1. LIST OF ITEMS */}
                <div className="cart-items-list">
                    {cartItems.map((item) => {
                        const isSale = item.productSalePrice && item.productSalePrice > 0;
                        const price = getPrice(item);
                        return (
                            <div key={item.id} className="cart-item-card">
                                {/* Product Image */}
                                <div className="cart-item-image">
                                    <img 
                                        src={getProductImage(item.productImageUrl)} 
                                        alt={item.productName} 
                                        onError={(e) => { e.target.src = '/no-image.png'; }}
                                    />
                                </div>

                                {/* Name & Price */}
                                <div className="cart-item-info">
                                    <h4 className="cart-item-title">{item.productName}</h4>
                                    <div className="cart-item-prices">
                                        <span className="cart-item-price-main">{formatPrice(price)}</span>
                                        {isSale && (
                                            <span className="cart-item-price-original">{formatPrice(item.productPrice)}</span>
                                        )}
                                    </div>
                                </div>

                                {/* Quantity Editor */}
                                <div className="qty-control" style={{ height: 36 }}>
                                    <button 
                                        className="qty-btn"
                                        onClick={() => handleUpdateQuantity(item, item.quantity - 1)}
                                    >
                                        <IconMinus size={12} />
                                    </button>
                                    <span className="qty-value" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', fontSize: 'var(--font-size-sm)' }}>
                                        {item.quantity}
                                    </span>
                                    <button 
                                        className="qty-btn"
                                        onClick={() => handleUpdateQuantity(item, item.quantity + 1)}
                                    >
                                        <IconPlus size={12} />
                                    </button>
                                </div>

                                {/* Line Total */}
                                <div className="cart-item-subtotal">
                                    {formatPrice(price * item.quantity)}
                                </div>

                                {/* Delete Button */}
                                <button 
                                    className="btn btn-ghost" 
                                    onClick={() => handleRemove(item)}
                                    style={{ color: 'var(--color-danger)', padding: 'var(--space-2)' }}
                                >
                                    <IconTrash size={16} />
                                </button>
                            </div>
                        );
                    })}
                </div>

                {/* 2. SUMMARY (Bên phải) */}
                <aside className="cart-summary">
                    <h3 className="summary-title">Tóm tắt đơn hàng</h3>
                    
                    <div className="summary-row">
                        <span>Tạm tính ({cartItems.length} sản phẩm)</span>
                        <span>{formatPrice(subtotal)}</span>
                    </div>
                    
                    <div className="summary-row">
                        <span>Phí vận chuyển</span>
                        <span>{shippingFee === 0 ? 'Miễn phí' : formatPrice(shippingFee)}</span>
                    </div>

                    {shippingFee > 0 && (
                        <div className="text-xs text-muted" style={{ marginTop: '-8px', marginBottom: '12px' }}>
                            Mua thêm <strong>{formatPrice(500000 - subtotal)}</strong> để được Miễn Phí Giao Hàng!
                        </div>
                    )}

                    <div className="summary-row total-row">
                        <span>Tổng tiền</span>
                        <span className="total-price">{formatPrice(total)}</span>
                    </div>

                    <button 
                        className="btn btn-primary btn-block btn-lg" 
                        onClick={handleCheckoutClick}
                        style={{ marginTop: 'var(--space-5)' }}
                    >
                        MUA HÀNG NGAY
                    </button>
                </aside>
            </div>
        </div>
    );
}

export default Cart;
