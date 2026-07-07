import React, { useState, useContext, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CartContext } from '../context/CartContext';
import productService from '../services/productService';
import reviewService from '../services/reviewService';
import api from '../services/api';
import Breadcrumb from '../components/Breadcrumb';
import ProductCard from '../components/ProductCard';
import { formatPrice, getDiscountPercent, getProductImage } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { 
  IconStar, IconCart, IconPlus, IconMinus, 
  IconStore, IconMessage, IconArrowLeft 
} from '../utils/icons';
import './ProductDetail.css';

function ProductDetail() {
    const { id } = useParams();
    const { addToCart } = useContext(CartContext);
    const toast = useToast();

    const [product, setProduct] = useState(null);
    const [relatedProducts, setRelatedProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Image and UI states
    const [activeImage, setActiveImage] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [activeTab, setActiveTab] = useState('description'); // description | reviews
    const [zoomStyle, setZoomStyle] = useState({ display: 'none' });
    const [selectedVariant, setSelectedVariant] = useState(null);

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
        setSelectedVariant(null);

        // Track product view action
        api.post(`/api/activities/track?type=VIEW&productId=${id}`)
            .catch(err => console.error("Error tracking view activity:", err));

        productService.getProductById(id)
            .then((res) => {
                if (res && res.success && res.data) {
                    const p = res.data;
                    setProduct(p);
                    setActiveImage(p.imageUrl || "https://via.placeholder.com/400");
                    
                    // Fetch similar products with category and price filters
                    api.get(`/api/recommendations/similar/${p.id}`)
                        .then(similarRes => {
                            if (similarRes && similarRes.data && similarRes.data.success && Array.isArray(similarRes.data.data)) {
                                setRelatedProducts(similarRes.data.data);
                            }
                        })
                        .catch(err => console.error("Error loading similar products:", err));
                } else {
                    setError("Không thể tải thông tin sản phẩm!");
                }
                setLoading(false);
            })
            .catch((err) => {
                console.error(err);
                setError(err.response?.data?.message || "Không thể tải thông tin sản phẩm!");
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

    const handleSelectVariant = (variant) => {
        setSelectedVariant(variant);
        if (variant.imageUrl) {
            setActiveImage(variant.imageUrl);
        }
    };

    const handleAddToCartClick = () => {
        if (product) {
            if (product.variants && product.variants.length > 0 && !selectedVariant) {
                toast.error("Vui lòng chọn phân loại sản phẩm (màu sắc/kích thước)!");
                return;
            }
            addToCart(product, quantity, selectedVariant);
            toast.success(`Đã thêm ${quantity} sản phẩm vào giỏ hàng!`);
            
            // Track Add to Cart activity
            api.post(`/api/activities/track?type=ADD_TO_CART&productId=${product.id}`)
                .catch(err => console.error(err));
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
                toast.success("Đăng đánh giá thành công!");
                setNewComment('');
                setNewRating(5);
                setCanSubmitReview(false);
                loadReviews();
            }
        } catch (err) {
            toast.error("Không thể gửi đánh giá: " + (err.response?.data?.message || err.message));
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
            left: '102%',
            width: '100%',
            height: '100%',
            backgroundImage: `url(${getProductImage(activeImage)})`,
            backgroundPosition: `${x}% ${y}%`,
            backgroundSize: '200%',
            backgroundRepeat: 'no-repeat',
            border: '1px solid var(--color-gray-200)',
            borderRadius: 'var(--radius-lg)',
            zIndex: 10,
            boxShadow: 'var(--shadow-xl)',
            backgroundColor: '#fff'
        });
    };

    const handleMouseLeave = () => {
        setZoomStyle({ display: 'none' });
    };

    if (loading) {
        return (
            <div className="container loading-center">
                <div className="spinner spinner-lg" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="container" style={{ padding: 'var(--space-8) 0' }}>
                <div className="badge badge-danger" style={{ display: 'flex', gap: 6, width: '100%', padding: 'var(--space-4)' }}>
                    Lỗi tải sản phẩm: {error}
                </div>
            </div>
        );
    }

    if (!product) return null;

    // Resolve dynamic price & stock for selected variant
    let currentPrice = product.price;
    let currentSalePrice = product.salePrice;
    let currentStock = product.stockQuantity;

    if (selectedVariant) {
        if (selectedVariant.price !== null) currentPrice = selectedVariant.price;
        if (selectedVariant.salePrice !== null) currentSalePrice = selectedVariant.salePrice;
        currentStock = selectedVariant.stockQuantity;
    }

    const hasSale = currentSalePrice && currentSalePrice > 0;
    const discount = hasSale ? getDiscountPercent(currentPrice, currentSalePrice) : 0;
    const hasVariants = product.variants && product.variants.length > 0;

    const thumbnails = product.images && product.images.length > 0 
        ? product.images 
        : [{ id: 'default', imageUrl: product.imageUrl || "https://via.placeholder.com/400" }];

    return (
        <div className="container product-detail-container">
            {/* Breadcrumb */}
            <Breadcrumb items={[
                { label: 'Trang chủ', to: '/' },
                { label: 'Sản phẩm', to: '/products' },
                { label: product.name }
            ]} />

            <Link to="/products" className="btn btn-ghost btn-sm" style={{ marginBottom: 'var(--space-4)' }}>
                <IconArrowLeft size={14} /> Quay lại danh sách
            </Link>

            {/* MAIN PRODUCT DISPLAY */}
            <div className="detail-layout">
                {/* Image Gallery */}
                <div className="gallery-container">
                    <div 
                        className="gallery-main"
                        onMouseMove={handleMouseMove}
                        onMouseLeave={handleMouseLeave}
                    >
                        <img 
                            src={getProductImage(activeImage)} 
                            alt={product.name} 
                        />
                        <div style={zoomStyle}></div>
                    </div>

                    {thumbnails.length > 1 && (
                        <div className="gallery-thumbnails">
                            {thumbnails.map(thumb => (
                                <div 
                                    key={thumb.id} 
                                    onClick={() => setActiveImage(thumb.imageUrl)}
                                    className={`thumbnail-item ${activeImage === thumb.imageUrl ? 'active' : ''}`}
                                >
                                    <img src={getProductImage(thumb.imageUrl)} alt="preview" />
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Product Info */}
                <div className="info-container">
                    <span className="info-category">
                        {product.categoryName || 'Sản phẩm'}
                    </span>
                    <h2 className="info-title">{product.name}</h2>
                    
                    <div className="info-meta-row">
                        <div className="stars">
                            {Array.from({ length: 5 }).map((_, i) => (
                                <IconStar 
                                    key={i} 
                                    size={14} 
                                    fill={i < Math.round(product.averageRating || 5) ? 'var(--color-star)' : 'none'} 
                                    color={i < Math.round(product.averageRating || 5) ? 'var(--color-star)' : 'var(--color-star-empty)'} 
                                />
                            ))}
                            <span style={{ marginLeft: 4, color: 'var(--color-gray-900)', fontWeight: 600 }}>
                                {product.averageRating ? product.averageRating.toFixed(1) : '5.0'}
                            </span>
                        </div>
                        <span style={{ color: 'var(--color-gray-300)' }}>|</span>
                        <span>Đã bán {product.soldCount || 0}</span>
                    </div>

                    <div className="info-price-card">
                        {hasSale ? (
                            <>
                                <span className="info-price-main">{formatPrice(currentSalePrice)}</span>
                                <span className="info-price-original">{formatPrice(currentPrice)}</span>
                                <span className="info-price-discount">-{discount}% GIẢM</span>
                            </>
                        ) : (
                            <span className="info-price-main">{formatPrice(currentPrice)}</span>
                        )}
                    </div>

                    <div className="divider" style={{ margin: 0 }} />

                    {/* Product Variants (Màu sắc/Kích thước) */}
                    {hasVariants && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: 'var(--space-2)' }}>
                            <span className="font-semibold" style={{ color: 'var(--color-gray-700)', fontSize: 'var(--font-size-sm)' }}>Phân loại sản phẩm *</span>
                            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                {product.variants.map(v => (
                                    <button 
                                        key={v.id}
                                        type="button"
                                        onClick={() => handleSelectVariant(v)}
                                        className={`btn btn-sm ${selectedVariant?.id === v.id ? 'btn-primary' : 'btn-secondary'}`}
                                        style={{ 
                                            borderRadius: '4px', 
                                            padding: '6px 12px', 
                                            border: selectedVariant?.id === v.id ? '1px solid var(--color-primary)' : '1px solid var(--color-gray-300)',
                                            fontWeight: selectedVariant?.id === v.id ? 'bold' : 'normal',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '6px',
                                            height: 'auto',
                                            textTransform: 'none'
                                        }}
                                    >
                                        {v.imageUrl && (
                                            <img src={getProductImage(v.imageUrl)} alt={v.name} style={{ width: '18px', height: '18px', objectFit: 'cover', borderRadius: '2px' }} onError={(e) => { e.target.style.display = 'none'; }} />
                                        )}
                                        {v.name}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Stock Status */}
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                        <span className="font-semibold" style={{ color: 'var(--color-gray-700)' }}>Trạng thái:</span>
                        <span className={`badge ${currentStock > 0 ? 'badge-success' : 'badge-danger'}`}>
                            {currentStock > 0 ? `Còn hàng (${currentStock})` : 'Hết hàng'}
                        </span>
                    </div>

                    {/* Quantity Selector */}
                    {currentStock > 0 && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                            <span className="font-semibold" style={{ color: 'var(--color-gray-700)' }}>Số lượng:</span>
                            <div className="qty-control">
                                <button 
                                    className="qty-btn"
                                    onClick={() => setQuantity(q => Math.max(1, q - 1))}
                                >
                                    <IconMinus size={14} />
                                </button>
                                <span className="qty-value" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    {quantity}
                                </span>
                                <button 
                                    className="qty-btn"
                                    onClick={() => setQuantity(q => Math.min(currentStock, q + 1))}
                                >
                                    <IconPlus size={14} />
                                </button>
                            </div>
                        </div>
                    )}

                    {/* CTA Actions */}
                    <div style={{ display: 'flex', gap: '15px', marginTop: 'var(--space-2)' }}>
                        <button 
                            className="btn btn-primary btn-lg btn-block"
                            onClick={handleAddToCartClick}
                            disabled={currentStock <= 0}
                            style={{ display: 'flex', gap: 8 }}
                        >
                            <IconCart size={20} />
                            {currentStock <= 0 ? 'HẾT HÀNG' : 'THÊM VÀO GIỎ HÀNG'}
                        </button>
                    </div>
                </div>
            </div>

            {/* Shop info banner */}
            {product.shopName && (
                <div className="shop-card">
                    <div className="shop-info-left">
                        <div className="shop-avatar">
                            {product.shopName.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <h4 style={{ fontWeight: 700, fontSize: 'var(--font-size-md)', margin: 0 }}>{product.shopName}</h4>
                            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)', margin: 0 }}>Đang hoạt động</p>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: 10 }}>
                        <Link to={`/shop/${product.shopSlug || ''}`} className="btn btn-secondary btn-sm" style={{ display: 'flex', gap: 4 }}>
                            <IconStore size={14} /> Xem Cửa Hàng
                        </Link>
                        <Link to="/messages" className="btn btn-ghost btn-sm" style={{ display: 'flex', gap: 4, border: '1px solid var(--color-gray-300)' }}>
                            <IconMessage size={14} /> Chat Ngay
                        </Link>
                    </div>
                </div>
            )}

            {/* TAB SYSTEM */}
            <div className="card" style={{ marginTop: 'var(--space-6)', padding: 'var(--space-6)' }}>
                <div className="tabs" style={{ marginBottom: 'var(--space-4)' }}>
                    <button 
                        className={`tab ${activeTab === 'description' ? 'active' : ''}`}
                        onClick={() => setActiveTab('description')}
                    >
                        Mô tả sản phẩm
                    </button>
                    <button 
                        className={`tab ${activeTab === 'reviews' ? 'active' : ''}`}
                        onClick={() => setActiveTab('reviews')}
                    >
                        Đánh giá & Bình luận ({reviews.length})
                    </button>
                </div>

                {activeTab === 'description' ? (
                    <div style={{ color: 'var(--color-gray-800)', lineHeight: '1.7', fontSize: 'var(--font-size-base)', whiteSpace: 'pre-line' }}>
                        {product.description || "Chưa có mô tả chi tiết cho sản phẩm này."}
                    </div>
                ) : (
                    <div className="reviews-section">
                        {/* Write a review section */}
                        {canSubmitReview && (
                            <div className="review-form">
                                <h4 style={{ margin: '0 0 15px 0', fontSize: 'var(--font-size-md)' }}>Đánh giá sản phẩm này</h4>
                                <form onSubmit={handleReviewSubmit}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '15px' }}>
                                        <span className="font-semibold" style={{ fontSize: 'var(--font-size-sm)' }}>Chọn số sao:</span>
                                        <div style={{ display: 'flex', gap: '5px', cursor: 'pointer', color: 'var(--color-star)' }}>
                                            {[1, 2, 3, 4, 5].map(star => (
                                                <span key={star} onClick={() => setNewRating(star)}>
                                                    <IconStar size={20} fill={star <= newRating ? 'var(--color-star)' : 'none'} color={star <= newRating ? 'var(--color-star)' : 'var(--color-star-empty)'} />
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                    <div className="form-group" style={{ marginBottom: '15px' }}>
                                        <label className="form-label">Nhận xét chi tiết *</label>
                                        <textarea 
                                            className="form-textarea"
                                            rows="3"
                                            value={newComment}
                                            onChange={(e) => setNewComment(e.target.value)}
                                            placeholder="Cảm nhận của bạn về sản phẩm này (chất lượng, đóng gói, vận chuyển...)"
                                            required
                                        />
                                    </div>
                                    <button type="submit" className="btn btn-primary">
                                        Gửi đánh giá
                                    </button>
                                </form>
                            </div>
                        )}

                        {/* List reviews */}
                        {reviews.length === 0 ? (
                            <div className="empty-state" style={{ padding: 'var(--space-6) 0' }}>
                                <p style={{ color: 'var(--color-gray-500)', fontStyle: 'italic' }}>Chưa có đánh giá nào cho sản phẩm này.</p>
                            </div>
                        ) : (
                            reviews.map((rev) => {
                                const dateStr = new Date(rev.createdAt).toLocaleDateString('vi-VN');
                                return (
                                    <div key={rev.id} className="review-item">
                                        <div className="review-header">
                                            <strong className="review-author">{rev.username}</strong>
                                            <span className="review-date">{dateStr}</span>
                                        </div>
                                        <div style={{ display: 'flex', gap: '3px', color: 'var(--color-star)', marginBottom: '8px' }}>
                                            {Array.from({ length: 5 }).map((_, i) => (
                                                <IconStar key={i} size={12} fill={i < rev.rating ? 'var(--color-star)' : 'none'} color={i < rev.rating ? 'var(--color-star)' : 'var(--color-star-empty)'} />
                                            ))}
                                        </div>
                                        <p className="review-content">{rev.comment}</p>
                                    </div>
                                );
                            })
                        )}
                    </div>
                )}
            </div>

            {/* RELATED PRODUCTS */}
            {relatedProducts.length > 0 && (
                <div style={{ borderTop: '1px solid var(--color-gray-200)', paddingTop: 'var(--space-10)', marginTop: 'var(--space-10)', marginBottom: 'var(--space-6)' }}>
                    <h3 style={{ fontSize: 'var(--font-size-xl)', fontWeight: 800, marginBottom: 'var(--space-4)' }}>Sản phẩm liên quan</h3>
                    <div className="product-grid">
                        {relatedProducts.map(rel => (
                            <ProductCard key={rel.id} product={rel} />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}

export default ProductDetail;