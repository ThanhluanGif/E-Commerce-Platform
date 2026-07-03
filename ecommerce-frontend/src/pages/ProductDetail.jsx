import React, { useState, useContext, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import productService from '../services/productService';
import reviewService from '../services/reviewService';

function ProductDetail() {
    const { id } = useParams();
    const { addToCart } = useContext(CartContext);

    const [product, setProduct] = useState(null);
    const [relatedProducts, setRelatedProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Image and UI states
    const [activeImage, setActiveImage] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [activeTab, setActiveTab] = useState('description'); // description | reviews
    const [zoomStyle, setZoomStyle] = useState({ display: 'none' });

    // Review states
    const [reviews, setReviews] = useState([]);
    const [canSubmitReview, setCanSubmitReview] = useState(false);
    const [newRating, setNewRating] = useState(5);
    const [newComment, setNewComment] = useState('');

    const loadReviews = () => {
        reviewService.getReviews(id)
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setReviews(res.data);
                }
            })
            .catch(err => console.error("Error loading reviews:", err));
    };

    // Fetch product details & reviews
    useEffect(() => {
        setLoading(true);
        setError(null);
        setQuantity(1);

        productService.getProductById(id)
            .then((res) => {
                if (res && res.success && res.data) {
                    const p = res.data;
                    setProduct(p);
                    setActiveImage(p.imageUrl || "https://via.placeholder.com/400");
                    
                    // Fetch related products in the same category
                    if (p.categoryId) {
                        productService.getProductsByCategoryId(p.categoryId)
                            .then(relatedRes => {
                                if (relatedRes && relatedRes.success && Array.isArray(relatedRes.data)) {
                                    const filtered = relatedRes.data.filter(item => item.id !== p.id).slice(0, 4);
                                    setRelatedProducts(filtered);
                                }
                            })
                            .catch(err => console.error("Error loading related products:", err));
                    }
                } else {
                    setError("Sản phẩm không tồn tại.");
                }
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });

        // Load reviews and review permissions
        loadReviews();
        reviewService.canReview(id)
            .then(res => {
                if (res && res.success) {
                    setCanSubmitReview(res.data);
                }
            })
            .catch(err => console.error("Error checking review permission:", err));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleAddToCartClick = () => {
        if (product) {
            addToCart(product, quantity);
            alert(`🛒 Đã thêm ${quantity} sản phẩm "${product.name}" vào giỏ hàng!`);
        }
    };

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (newComment.trim() === '') return;

        try {
            const res = await reviewService.addReview(id, {
                rating: newRating,
                comment: newComment
            });
            if (res && res.success) {
                alert("Đăng đánh giá thành công!");
                setNewComment('');
                setNewRating(5);
                setCanSubmitReview(false);
                loadReviews();
            }
        } catch (err) {
            alert("Không thể gửi đánh giá: " + (err.response?.data?.message || err.message));
        }
    };

    // Zoom on Hover handlers
    const handleMouseMove = (e) => {
        const { left, top, width, height } = e.target.getBoundingClientRect();
        const x = ((e.pageX - left - window.scrollX) / width) * 100;
        const y = ((e.pageY - top - window.scrollY) / height) * 100;
        
        setZoomStyle({
            display: 'block',
            position: 'absolute',
            top: '0',
            left: '105%',
            width: '100%',
            height: '100%',
            backgroundImage: `url(${activeImage})`,
            backgroundPosition: `${x}% ${y}%`,
            backgroundSize: '200%',
            backgroundRepeat: 'no-repeat',
            border: '1px solid #e5e7eb',
            borderRadius: '8px',
            zIndex: 10,
            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
            backgroundColor: '#fff'
        });
    };

    const handleMouseLeave = () => {
        setZoomStyle({ display: 'none' });
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px', color: '#6b7280' }}>⏳ Đang tải thông tin sản phẩm...</div>;
    if (error) return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center', fontSize: '18px' }}>❌ Lỗi: {error}</div>;
    if (!product) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px' }}>Sản phẩm không tồn tại.</div>;

    const hasSale = product.salePrice && product.salePrice > 0;
    const thumbnails = product.images && product.images.length > 0 
        ? product.images 
        : [{ id: 'default', imageUrl: product.imageUrl || "https://via.placeholder.com/400" }];

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <Link to="/products" style={{ textDecoration: 'none', color: '#3643ba', fontWeight: '600', display: 'inline-block', marginBottom: '20px' }}>
                &larr; Quay lại danh sách sản phẩm
            </Link>

            {/* MAIN PRODUCT DISPLAY */}
            <div style={{ display: 'flex', gap: '40px', flexWrap: 'wrap', marginBottom: '40px' }}>
                
                {/* Image Gallery */}
                <div style={{ flex: '1 1 400px', maxWidth: '500px' }}>
                    <div style={{ border: '1px solid #e5e7eb', borderRadius: '8px', overflow: 'hidden', height: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#fff', position: 'relative', cursor: 'zoom-in' }}
                         onMouseMove={handleMouseMove}
                         onMouseLeave={handleMouseLeave}>
                        <img 
                            src={activeImage} 
                            alt={product.name} 
                            style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }}
                        />
                        <div style={zoomStyle}></div>
                    </div>

                    {thumbnails.length > 1 && (
                        <div style={{ display: 'flex', gap: '10px', marginTop: '15px', overflowX: 'auto', paddingBottom: '5px' }}>
                            {thumbnails.map(thumb => (
                                <div key={thumb.id} 
                                     onClick={() => setActiveImage(thumb.imageUrl)}
                                     style={{ width: '80px', height: '80px', border: activeImage === thumb.imageUrl ? '2px solid #3643ba' : '1px solid #e5e7eb', borderRadius: '6px', overflow: 'hidden', cursor: 'pointer', background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                                    <img src={thumb.imageUrl} alt="preview" style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Product Info */}
                <div style={{ flex: '1 1 450px', display: 'flex', flexDirection: 'column' }}>
                    <span style={{ fontSize: '13px', color: '#6b7280', textTransform: 'uppercase', fontWeight: 'bold', letterSpacing: '1px', marginBottom: '8px' }}>
                        {product.categoryName || 'Sản phẩm'}
                    </span>
                    <h2 style={{ fontSize: '32px', fontWeight: '800', color: '#111827', margin: '0 0 15px 0', lineHeight: '1.2' }}>{product.name}</h2>
                    
                    <div style={{ display: 'flex', gap: '15px', alignItems: 'center', marginBottom: '20px' }}>
                        {hasSale ? (
                            <>
                                <span style={{ fontSize: '28px', fontWeight: 'bold', color: '#f94e30' }}>{product.salePrice.toLocaleString('vi-VN')} đ</span>
                                <span style={{ fontSize: '18px', color: '#9ca3af', textDecoration: 'line-through' }}>{product.price.toLocaleString('vi-VN')} đ</span>
                            </>
                        ) : (
                            <span style={{ fontSize: '28px', fontWeight: 'bold', color: '#f94e30' }}>{product.price.toLocaleString('vi-VN')} đ</span>
                        )}
                        <span style={{ marginLeft: 'auto', background: product.stockQuantity > 0 ? '#ecfdf5' : '#fef2f2', color: product.stockQuantity > 0 ? '#059669' : '#dc2626', fontSize: '13px', fontWeight: '600', padding: '4px 10px', borderRadius: '12px' }}>
                            {product.stockQuantity > 0 ? `Còn hàng (${product.stockQuantity})` : 'Hết hàng'}
                        </span>
                    </div>

                    <div style={{ width: '100%', height: '1px', background: '#e5e7eb', marginBottom: '20px' }}></div>

                    {product.stockQuantity > 0 && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '25px' }}>
                            <span style={{ fontSize: '15px', fontWeight: 'bold', color: '#4b5563' }}>Số lượng:</span>
                            <div style={{ display: 'flex', alignItems: 'center', border: '1px solid #d1d5db', borderRadius: '4px', background: '#fff' }}>
                                <button 
                                    onClick={() => setQuantity(q => Math.max(1, q - 1))}
                                    style={{ border: 'none', background: 'none', padding: '10px 15px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}
                                >
                                    -
                                </button>
                                <span style={{ padding: '0 15px', fontSize: '16px', fontWeight: 'bold', minWidth: '30px', textAlign: 'center' }}>{quantity}</span>
                                <button 
                                    onClick={() => setQuantity(q => Math.min(product.stockQuantity, q + 1))}
                                    style={{ border: 'none', background: 'none', padding: '10px 15px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}
                                >
                                    +
                                </button>
                            </div>
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: '15px' }}>
                        <button 
                            onClick={handleAddToCartClick}
                            disabled={product.stockQuantity <= 0}
                            style={{ flex: 1, padding: '15px 30px', background: product.stockQuantity <= 0 ? '#d1d5db' : '#f94e30', color: 'white', border: 'none', borderRadius: '6px', fontSize: '16px', fontWeight: 'bold', cursor: product.stockQuantity <= 0 ? 'not-allowed' : 'pointer', transition: 'background 0.2s' }}
                        >
                            {product.stockQuantity <= 0 ? 'HẾT HÀNG' : 'THÊM VÀO GIỎ HÀNG'}
                        </button>
                    </div>
                </div>
            </div>

            {/* TAB SYSTEM */}
            <div style={{ marginBottom: '50px' }}>
                <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '20px' }}>
                    <button 
                        onClick={() => setActiveTab('description')}
                        style={{ padding: '12px 25px', background: 'none', border: 'none', borderBottom: activeTab === 'description' ? '3px solid #f94e30' : 'none', color: activeTab === 'description' ? '#f94e30' : '#6b7280', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                        Mô tả sản phẩm
                    </button>
                    <button 
                        onClick={() => setActiveTab('reviews')}
                        style={{ padding: '12px 25px', background: 'none', border: 'none', borderBottom: activeTab === 'reviews' ? '3px solid #f94e30' : 'none', color: activeTab === 'reviews' ? '#f94e30' : '#6b7280', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer' }}
                    >
                        Đánh giá & Bình luận ({reviews.length})
                    </button>
                </div>

                {activeTab === 'description' ? (
                    <div style={{ padding: '10px 0', color: '#374151', lineHeight: '1.7', fontSize: '15px' }}>
                        {product.description || "Chưa có mô tả chi tiết cho sản phẩm này."}
                    </div>
                ) : (
                    <div style={{ padding: '10px 0' }}>
                        {/* Write a review section */}
                        {canSubmitReview && (
                            <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', marginBottom: '30px' }}>
                                <h4 style={{ margin: '0 0 15px 0', fontSize: '16px', fontWeight: 'bold', color: '#1f2937' }}>Đánh giá sản phẩm này</h4>
                                <form onSubmit={handleReviewSubmit}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '15px' }}>
                                        <span style={{ fontSize: '14px', fontWeight: '600', color: '#4b5563' }}>Chọn số sao:</span>
                                        <div style={{ display: 'flex', gap: '5px', cursor: 'pointer', fontSize: '20px', color: '#fbbf24' }}>
                                            {[1, 2, 3, 4, 5].map(star => (
                                                <span key={star} onClick={() => setNewRating(star)}>
                                                    {star <= newRating ? '★' : '☆'}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '15px' }}>
                                        <label style={{ fontSize: '14px', fontWeight: '600', color: '#4b5563' }}>Nhận xét chi tiết *</label>
                                        <textarea 
                                            rows="3"
                                            value={newComment}
                                            onChange={(e) => setNewComment(e.target.value)}
                                            placeholder="Cảm nhận của bạn về sản phẩm này (chất lượng, đóng gói, vận chuyển...)"
                                            style={{ padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', width: '100%', boxSizing: 'border-box' }}
                                            required
                                        />
                                    </div>
                                    <button type="submit" style={{ padding: '8px 20px', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' }}>
                                        Gửi đánh giá
                                    </button>
                                </form>
                            </div>
                        )}

                        {/* List reviews */}
                        {reviews.length === 0 ? (
                            <p style={{ color: '#6b7280', fontStyle: 'italic', margin: '20px 0' }}>Chưa có đánh giá nào cho sản phẩm này.</p>
                        ) : (
                            reviews.map((rev) => {
                                const dateStr = new Date(rev.createdAt).toLocaleDateString('vi-VN');
                                return (
                                    <div key={rev.id} style={{ borderBottom: '1px solid #f3f4f6', padding: '15px 0' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                                            <strong style={{ fontSize: '15px', color: '#1f2937' }}>{rev.username}</strong>
                                            <span style={{ fontSize: '13px', color: '#9ca3af' }}>{dateStr}</span>
                                        </div>
                                        <div style={{ display: 'flex', gap: '3px', color: '#fbbf24', fontSize: '14px', marginBottom: '8px' }}>
                                            {Array.from({ length: rev.rating }).map((_, i) => <span key={i}>★</span>)}
                                            {Array.from({ length: 5 - rev.rating }).map((_, i) => <span key={i} style={{ color: '#e5e7eb' }}>★</span>)}
                                        </div>
                                        <p style={{ margin: 0, fontSize: '14px', color: '#4b5563' }}>{rev.comment}</p>
                                    </div>
                                );
                            })
                        )}
                    </div>
                )}
            </div>

            {/* RELATED PRODUCTS */}
            {relatedProducts.length > 0 && (
                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '40px', marginBottom: '20px' }}>
                    <h3 style={{ fontSize: '22px', fontWeight: '800', color: '#1f2937', marginBottom: '20px' }}>Sản phẩm liên quan</h3>
                    
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '20px' }}>
                        {relatedProducts.map(rel => {
                            const relSale = rel.salePrice && rel.salePrice > 0;
                            return (
                                <div key={rel.id} style={{ border: '1px solid #e5e7eb', padding: '15px', borderRadius: '8px', background: '#fff', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s', cursor: 'pointer' }}
                                     onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-4px)'}
                                     onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}>
                                    
                                    <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f9fafb', borderRadius: '6px', overflow: 'hidden', marginBottom: '12px' }}>
                                        <img src={rel.imageUrl || "https://via.placeholder.com/150"} alt={rel.name} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                    </div>
                                    <h4 style={{ fontSize: '14px', fontWeight: 'bold', margin: '0 0 8px 0', minHeight: '35px', color: '#1f2937' }}>{rel.name}</h4>
                                    
                                    <div style={{ margin: 'auto 0 10px 0' }}>
                                        {relSale ? (
                                            <div style={{ display: 'flex', gap: '6px', alignItems: 'center', flexWrap: 'wrap' }}>
                                                <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#ef4444' }}>{rel.salePrice.toLocaleString('vi-VN')} đ</span>
                                                <span style={{ fontSize: '11px', color: '#9ca3af', textDecoration: 'line-through' }}>{rel.price.toLocaleString('vi-VN')} đ</span>
                                            </div>
                                        ) : (
                                            <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#f94e30' }}>{rel.price.toLocaleString('vi-VN')} đ</span>
                                        )}
                                    </div>

                                    <Link to={`/products/${rel.id}`} style={{ textDecoration: 'none' }}>
                                        <button style={{ width: '100%', padding: '8px 0', background: 'white', border: '1px solid #f94e30', color: '#f94e30', borderRadius: '4px', fontSize: '13px', fontWeight: 'bold', cursor: 'pointer', transition: 'all 0.2s' }}
                                                onMouseEnter={(e) => { e.target.style.background = '#f94e30'; e.target.style.color = 'white'; }}
                                                onMouseLeave={(e) => { e.target.style.background = 'white'; e.target.style.color = '#f94e30'; }}>
                                            Xem chi tiết
                                        </button>
                                    </Link>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProductDetail;