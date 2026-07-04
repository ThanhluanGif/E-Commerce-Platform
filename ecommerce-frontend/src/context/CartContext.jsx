import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';
import cartService from '../services/cartService';

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [cartItems, setCartItems] = useState([]);
    const { userId } = useContext(AuthContext);

    // Load and sync cart on mount or when user changes
    useEffect(() => {
        const loadCart = async () => {
            if (!userId) {
                // Guest / LocalStorage Mode
                const savedCart = localStorage.getItem('tempCart');
                if (savedCart) {
                    try {
                        setCartItems(JSON.parse(savedCart));
                    } catch (e) {
                        setCartItems([]);
                    }
                } else {
                    setCartItems([]);
                }
            } else {
                // Logged in Mode: merge guest cart from localStorage into server database
                try {
                    const savedCart = localStorage.getItem('tempCart');
                    if (savedCart) {
                        const guestItems = JSON.parse(savedCart);
                        if (guestItems.length > 0) {
                            await cartService.mergeCart(guestItems);
                            localStorage.removeItem('tempCart');
                        }
                    }
                    const res = await cartService.getCart();
                    if (res && res.success && Array.isArray(res.data)) {
                        setCartItems(res.data);
                    }
                } catch (err) {
                    console.error("Error syncing cart with server:", err);
                }
            }
        };
        loadCart();
    }, [userId]);

    // Add item to cart
    const addToCart = async (product, quantity, variant = null) => {
        if (!userId) {
            // Guest mode: update localStorage
            setCartItems(prev => {
                const existing = prev.find(item => item.productId === product.id && item.variantId === (variant ? variant.id : null));
                let updated;
                if (existing) {
                    updated = prev.map(item =>
                        (item.productId === product.id && item.variantId === (variant ? variant.id : null))
                            ? { ...item, quantity: item.quantity + quantity }
                            : item
                    );
                } else {
                    const price = variant && variant.price !== null ? variant.price : product.price;
                    const salePrice = variant && variant.salePrice !== null ? variant.salePrice : product.salePrice;
                    const stock = variant ? variant.stockQuantity : product.stockQuantity;
                    const imageUrl = variant && variant.imageUrl ? variant.imageUrl : product.imageUrl;

                    updated = [...prev, {
                        id: variant ? `temp_${product.id}_${variant.id}` : `temp_${product.id}`,
                        quantity: quantity,
                        productId: product.id,
                        productName: product.name,
                        productImageUrl: imageUrl,
                        productPrice: price,
                        productSalePrice: salePrice,
                        productStockQuantity: stock,
                        variantId: variant ? variant.id : null,
                        variantName: variant ? variant.name : null
                    }];
                }
                localStorage.setItem('tempCart', JSON.stringify(updated));
                return updated;
            });
        } else {
            // Online mode: call backend API
            try {
                await cartService.addItem(product.id, quantity, variant ? variant.id : null);
                const res = await cartService.getCart();
                if (res && res.success && Array.isArray(res.data)) {
                    setCartItems(res.data);
                }
            } catch (err) {
                console.error("Error adding item to database cart:", err);
                alert("Không thể thêm vào giỏ hàng: " + (err.response?.data?.message || err.message));
            }
        }
    };

    // Update item quantity
    const updateQuantity = async (itemId, newQuantity) => {
        if (newQuantity <= 0) {
            removeFromCart(itemId);
            return;
        }

        if (!userId) {
            // Guest mode
            setCartItems(prev => {
                const updated = prev.map(item =>
                    item.id === itemId ? { ...item, quantity: newQuantity } : item
                );
                localStorage.setItem('tempCart', JSON.stringify(updated));
                return updated;
            });
        } else {
            // Online mode
            try {
                await cartService.updateQuantity(itemId, newQuantity);
                const res = await cartService.getCart();
                if (res && res.success && Array.isArray(res.data)) {
                    setCartItems(res.data);
                }
            } catch (err) {
                console.error("Error updating cart quantity:", err);
                alert("Không thể cập nhật số lượng: " + (err.response?.data?.message || err.message));
            }
        }
    };

    // Remove item from cart
    const removeFromCart = async (itemId) => {
        if (!userId) {
            // Guest mode
            setCartItems(prev => {
                const updated = prev.filter(item => item.id !== itemId);
                localStorage.setItem('tempCart', JSON.stringify(updated));
                return updated;
            });
        } else {
            // Online mode
            try {
                await cartService.removeItem(itemId);
                const res = await cartService.getCart();
                if (res && res.success && Array.isArray(res.data)) {
                    setCartItems(res.data);
                }
            } catch (err) {
                console.error("Error removing item from cart:", err);
            }
        }
    };

    // Clear cart (called upon checkout)
    const clearCart = async () => {
        if (!userId) {
            setCartItems([]);
            localStorage.removeItem('tempCart');
        } else {
            try {
                await cartService.clearCart();
                setCartItems([]);
            } catch (err) {
                console.error("Error clearing cart:", err);
            }
        }
    };

    return (
        <CartContext.Provider value={{ cartItems, addToCart, updateQuantity, removeFromCart, clearCart }}>
            {children}
        </CartContext.Provider>
    );
};