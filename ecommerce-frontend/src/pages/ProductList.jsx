import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import productService from '../services/productService';
import categoryService from '../services/categoryService';

function ProductList() {
    const [searchParams] = useSearchParams();
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Filter states
    const [searchTerm, setSearchTerm] = useState(searchParams.get('name') || '');
    const [selectedCategory, setSelectedCategory] = useState(searchParams.get('categoryId') || '');

    // Sync search input when name param in URL changes
    useEffect(() => {
        const nameParam = searchParams.get('name');
        if (nameParam !== null) {
            setSearchTerm(nameParam);
            setPage(0);
        }
    }, [searchParams]);
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [sortBy, setSortBy] = useState('createdAt,desc');
    
    // Pagination states
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [pageSize] = useState(6);

    // Fetch categories on mount
    useEffect(() => {
        categoryService.getAllCategories()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setCategories(res.data);
                }
            })
            .catch(err => console.error("Error loading categories:", err));
    }, []);

    // Sync selectedCategory when categoryId param in URL changes
    useEffect(() => {
        const catId = searchParams.get('categoryId');
        if (catId !== null) {
            setSelectedCategory(catId);
            setPage(0);
        }
    }, [searchParams]);

    // Fetch products when filters or pagination changes
    useEffect(() => {
        setLoading(true);
        const params = {
            page: page,
            size: pageSize,
            sort: sortBy
        };

        if (searchTerm.trim() !== '') params.name = searchTerm;
        if (selectedCategory !== '') params.categoryId = selectedCategory;
        if (minPrice !== '') params.minPrice = minPrice;
        if (maxPrice !== '') params.maxPrice = maxPrice;

        productService.getAllProducts(params)
            .then(res => {
                if (res && res.success && res.data) {
                    setProducts(res.data.content || []);
                    setTotalPages(res.data.totalPages || 1);
                } else {
                    setProducts([]);
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [page, sortBy, selectedCategory, minPrice, maxPrice, pageSize]);

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        setPage(0); // Reset to page 1
        // Trigger fetch by dependency array logic
        setLoading(true);
        const params = {
            page: 0,
            size: pageSize,
            sort: sortBy,
            name: searchTerm
        };
        if (selectedCategory !== '') params.categoryId = selectedCategory;
        if (minPrice !== '') params.minPrice = minPrice;
        if (maxPrice !== '') params.maxPrice = maxPrice;

        productService.getAllProducts(params)
            .then(res => {
                if (res && res.success && res.data) {
                    setProducts(res.data.content || []);
                    setTotalPages(res.data.totalPages || 1);
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    const handleClearFilters = () => {
        setSearchTerm('');
        setSelectedCategory('');
        setMinPrice('');
        setMaxPrice('');
        setSortBy('createdAt,desc');
        setPage(0);
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '20px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Danh Mục Sản Phẩm
            </h2>

            <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap' }}>
                {/* 1. SIDEBAR FILTER (Bên trái) */}
                <div style={{ flex: '1 1 250px', maxWidth: '300px', background: '#f9fafb', padding: '20px', borderRadius: '8px', border: '1px solid #e5e7eb', height: 'fit-content' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '15px', color: '#2c3e50', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span>Bộ lọc tìm kiếm</span>
                        <button onClick={handleClearFilters} style={{ background: 'none', border: 'none', color: '#3643ba', fontSize: '12px', cursor: 'pointer', textDecoration: 'underline' }}>Xóa bộ lọc</button>
                    </h3>

                    {/* Lọc theo danh mục */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', fontWeight: '600', fontSize: '14px', marginBottom: '8px', color: '#4b5563' }}>Danh mục</label>
                        <select 
                            value={selectedCategory} 
                            onChange={(e) => { setSelectedCategory(e.target.value); setPage(0); }}
                            style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' }}
                        >
                            <option value="">Tất cả danh mục</option>
                            {categories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* Lọc theo giá */}
                    <div style={{ marginBottom: '20px' }}>
                        <label style={{ display: 'block', fontWeight: '600', fontSize: '14px', marginBottom: '8px', color: '#4b5563' }}>Khoảng giá (VNĐ)</label>
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                            <input 
                                type="number" 
                                placeholder="Từ" 
                                value={minPrice}
                                onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px' }}
                            />
                            <span style={{ color: '#9ca3af' }}>-</span>
                            <input 
                                type="number" 
                                placeholder="Đến" 
                                value={maxPrice}
                                onChange={(e) => { setMaxPrice(e.target.value); setPage(0); }}
                                style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px' }}
                            />
                        </div>
                    </div>
                </div>

                {/* 2. PRODUCT GRID & SEARCH/SORT (Bên phải) */}
                <div style={{ flex: '3 1 600px' }}>
                    {/* Thanh tìm kiếm và sắp xếp */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: '15px', flexWrap: 'wrap', marginBottom: '20px', alignItems: 'center' }}>
                        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', flex: '1 1 300px', gap: '10px' }}>
                            <input 
                                type="text" 
                                placeholder="Tìm kiếm sản phẩm..." 
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                style={{ flex: 1, padding: '10px 15px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' }}
                            />
                            <button type="submit" style={{ background: '#3643ba', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                                Tìm
                            </button>
                        </form>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <label style={{ fontSize: '14px', color: '#4b5563', fontWeight: '500' }}>Sắp xếp:</label>
                            <select 
                                value={sortBy}
                                onChange={(e) => { setSortBy(e.target.value); setPage(0); }}
                                style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' }}
                            >
                                <option value="createdAt,desc">Mới nhất</option>
                                <option value="price,asc">Giá: Thấp đến Cao</option>
                                <option value="price,desc">Giá: Cao đến Thấp</option>
                            </select>
                        </div>
                    </div>

                    {/* Hiển thị lỗi hoặc loading */}
                    {error && <div style={{ padding: '20px', color: 'red', background: '#fef2f2', borderRadius: '6px', marginBottom: '20px', border: '1px solid #fee2e2' }}>❌ Lỗi: {error}</div>}

                    {loading ? (
                        /* SKELETON LOADING GRID */
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px' }}>
                            {[1, 2, 3, 4, 5, 6].map(i => (
                                <div key={i} style={{ border: '1px solid #f3f4f6', padding: '15px', borderRadius: '8px', background: '#fff' }}>
                                    <div style={{ height: '180px', background: '#f3f4f6', borderRadius: '6px', marginBottom: '15px', animation: 'pulse 1.5s infinite ease-in-out' }}></div>
                                    <div style={{ height: '20px', background: '#f3f4f6', width: '70%', marginBottom: '10px', borderRadius: '3px' }}></div>
                                    <div style={{ height: '15px', background: '#f3f4f6', width: '50%', marginBottom: '15px', borderRadius: '3px' }}></div>
                                    <div style={{ height: '35px', background: '#f3f4f6', width: '100%', borderRadius: '4px' }}></div>
                                </div>
                            ))}
                        </div>
                    ) : products.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px', background: '#f9fafb', borderRadius: '8px', border: '1px dashed #d1d5db', color: '#6b7280' }}>
                            <p style={{ fontSize: '18px', fontWeight: '600' }}>Không tìm thấy sản phẩm nào!</p>
                            <p style={{ fontSize: '14px' }}>Hãy thử điều chỉnh lại bộ lọc hoặc từ khóa tìm kiếm của bạn.</p>
                        </div>
                    ) : (
                        /* PRODUCT CARD GRID */
                        <>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '20px' }}>
                                {products.map((product) => {
                                    const hasSale = product.salePrice && product.salePrice > 0;
                                    const discountPct = hasSale ? Math.round(((product.price - product.salePrice) / product.price) * 100) : 0;
                                    return (
                                        <div key={product.id} style={{ border: '1px solid #f3f4f6', borderRadius: '4px', background: '#fff', display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden' }}
                                             className="hover-lift">
                                            
                                            {/* Discount Badge */}
                                            {hasSale && (
                                                <div style={{ position: 'absolute', top: 0, right: 0, background: '#ffe9e4', color: '#f94e30', fontSize: '11px', fontWeight: 'bold', padding: '3px 8px', borderRadius: '0 0 0 8px', zIndex: 2 }}>
                                                    -{discountPct}%
                                                </div>
                                            )}

                                            {/* Product Image */}
                                            <div style={{ height: '180px', background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', borderBottom: '1px solid #f3f4f6', padding: '10px' }}>
                                                <img 
                                                    src={product.imageUrl || "https://via.placeholder.com/200"} 
                                                    alt={product.name} 
                                                    style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }}
                                                />
                                            </div>

                                            {/* Product Details */}
                                            <div style={{ padding: '12px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                                <h4 style={{ fontSize: '13px', color: '#333', fontWeight: '500', margin: '0 0 8px 0', lineHeight: '1.4', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', minHeight: '36px' }}>
                                                    {product.name}
                                                </h4>
                                                
                                                {/* Badges row */}
                                                <div style={{ display: 'flex', gap: '5px', marginBottom: '8px' }}>
                                                    <span style={{ fontSize: '9px', background: '#ffe4de', color: '#f94e30', padding: '1px 4px', borderRadius: '2px', fontWeight: 'bold' }}>Freeship+</span>
                                                </div>

                                                {/* Price Display */}
                                                <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', flexWrap: 'wrap', marginBottom: '8px', marginTop: 'auto' }}>
                                                    {hasSale ? (
                                                        <>
                                                            <span style={{ fontSize: '15px', fontWeight: 'bold', color: '#f94e30' }}>{product.salePrice.toLocaleString('vi-VN')} đ</span>
                                                            <span style={{ fontSize: '11px', color: '#9ca3af', textDecoration: 'line-through' }}>{product.price.toLocaleString('vi-VN')} đ</span>
                                                        </>
                                                    ) : (
                                                        <span style={{ fontSize: '15px', fontWeight: 'bold', color: '#f94e30' }}>{product.price.toLocaleString('vi-VN')} đ</span>
                                                    )}
                                                </div>

                                                {/* Rating & Sold count */}
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '11px', color: '#757575', marginBottom: '10px' }}>
                                                    <div style={{ color: '#fbbf24' }}>★ 4.8</div>
                                                    <div>Đã bán 120</div>
                                                </div>

                                                {/* CTA Button */}
                                                <Link to={`/products/${product.id}`} style={{ textDecoration: 'none', width: '100%' }}>
                                                    <button style={{ width: '100%', padding: '8px 0', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', fontSize: '13px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }}
                                                            onMouseEnter={(e) => e.target.style.background = '#d73211'}
                                                            onMouseLeave={(e) => e.target.style.background = '#f94e30'}>
                                                        Xem chi tiết
                                                    </button>
                                                </Link>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            {/* PAGINATION CONTROLS */}
                            <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '30px', alignItems: 'center' }}>
                                <button 
                                    onClick={() => setPage(p => Math.max(0, p - 1))}
                                    disabled={page === 0}
                                    style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #d1d5db', background: page === 0 ? '#f3f4f6' : 'white', cursor: page === 0 ? 'not-allowed' : 'pointer', fontSize: '14px', color: page === 0 ? '#9ca3af' : '#1f2937' }}
                                >
                                    &larr; Trước
                                </button>
                                
                                <span style={{ fontSize: '14px', fontWeight: '500', color: '#4b5563' }}>
                                    Trang {page + 1} / {totalPages}
                                </span>

                                <button 
                                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                    disabled={page === totalPages - 1}
                                    style={{ padding: '8px 16px', borderRadius: '4px', border: '1px solid #d1d5db', background: page === totalPages - 1 ? '#f3f4f6' : 'white', cursor: page === totalPages - 1 ? 'not-allowed' : 'pointer', fontSize: '14px', color: page === totalPages - 1 ? '#9ca3af' : '#1f2937' }}
                                >
                                    Sau &rarr;
                                </button>
                            </div>
                        </>
                    )}
                </div>
            </div>
            
            {/* CSS Animation injection for loading spinner skeleton */}
            <style>{`
                @keyframes pulse {
                    0%, 100% { opacity: 1; }
                    50% { opacity: .5; }
                }
            `}</style>
        </div>
    );
}

export default ProductList;