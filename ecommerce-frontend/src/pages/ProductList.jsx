import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import productService from '../services/productService';
import categoryService from '../services/categoryService';
import ProductCard, { ProductCardSkeleton } from '../components/ProductCard';
import Breadcrumb from '../components/Breadcrumb';
import { IconFilter, IconSearch, IconClose, IconChevronLeft, IconChevronRight } from '../utils/icons';
import './ProductList.css';

function ProductList() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Filter states
    const [searchTerm, setSearchTerm] = useState(searchParams.get('name') || '');
    const [selectedCategory, setSelectedCategory] = useState(searchParams.get('categoryId') || '');
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [sortBy, setSortBy] = useState('createdAt,desc');
    
    // Pagination states
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [pageSize] = useState(12);

    // Sync selected filters when URL search parameters change
    useEffect(() => {
        const nameParam = searchParams.get('name');
        if (nameParam !== null) setSearchTerm(nameParam);
        
        const catParam = searchParams.get('categoryId');
        if (catParam !== null) setSelectedCategory(catParam);
    }, [searchParams]);

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

    // Fetch products when filters or pagination changes
    useEffect(() => {
        setLoading(true);
        const params = {
            page: page,
            size: pageSize,
            sort: sortBy
        };

        const nameQuery = searchParams.get('name') || '';
        const catQuery = searchParams.get('categoryId') || '';

        if (nameQuery.trim() !== '') params.name = nameQuery;
        if (catQuery !== '') params.categoryId = catQuery;
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
    }, [page, sortBy, searchParams, minPrice, maxPrice, pageSize]);

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        setPage(0);
        
        // Update Search Params URL
        const newParams = {};
        if (searchTerm.trim()) newParams.name = searchTerm;
        if (selectedCategory) newParams.categoryId = selectedCategory;
        setSearchParams(newParams);
    };

    const handleClearFilters = () => {
        setSearchTerm('');
        setSelectedCategory('');
        setMinPrice('');
        setMaxPrice('');
        setSortBy('createdAt,desc');
        setPage(0);
        setSearchParams({});
    };

    return (
        <div className="container product-list-container">
            {/* Breadcrumbs */}
            <Breadcrumb items={[
                { label: 'Trang chủ', to: '/' },
                { label: 'Tất cả sản phẩm' }
            ]} />

            <div className="product-list-layout">
                {/* 1. SIDEBAR FILTER */}
                <aside className="filter-sidebar">
                    <div className="filter-header">
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <IconFilter size={16} /> Bộ lọc tìm kiếm
                        </span>
                        <span className="filter-clear-btn" onClick={handleClearFilters}>
                            Xóa tất cả
                        </span>
                    </div>

                    {/* Category Filter */}
                    <div className="filter-section">
                        <h4 className="filter-section-title">Danh mục</h4>
                        <select 
                            className="form-select"
                            value={selectedCategory} 
                            onChange={(e) => { setSelectedCategory(e.target.value); setPage(0); }}
                        >
                            <option value="">Tất cả danh mục</option>
                            {categories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* Price Range Filter */}
                    <div className="filter-section">
                        <h4 className="filter-section-title">Khoảng giá (VNĐ)</h4>
                        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                            <input 
                                type="number" 
                                className="form-input"
                                placeholder="Từ" 
                                value={minPrice}
                                onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                            />
                            <span style={{ color: 'var(--color-gray-400)' }}>-</span>
                            <input 
                                type="number" 
                                className="form-input"
                                placeholder="Đến" 
                                value={maxPrice}
                                onChange={(e) => { setMaxPrice(e.target.value); setPage(0); }}
                            />
                        </div>
                    </div>
                </aside>

                {/* 2. PRODUCT GRID & SORTING */}
                <main className="product-list-content">
                    {/* Sort & Search Bar */}
                    <div className="sorting-bar">
                        <div className="sorting-options">
                            <span className="sorting-label">Sắp xếp theo</span>
                            <button 
                                className={`sort-btn ${sortBy === 'createdAt,desc' ? 'active' : ''}`}
                                onClick={() => { setSortBy('createdAt,desc'); setPage(0); }}
                            >
                                Mới nhất
                            </button>
                            <button 
                                className={`sort-btn ${sortBy === 'price,asc' ? 'active' : ''}`}
                                onClick={() => { setSortBy('price,asc'); setPage(0); }}
                            >
                                Giá thấp &rarr; cao
                            </button>
                            <button 
                                className={`sort-btn ${sortBy === 'price,desc' ? 'active' : ''}`}
                                onClick={() => { setSortBy('price,desc'); setPage(0); }}
                            >
                                Giá cao &rarr; thấp
                            </button>
                        </div>

                        {/* Custom search inside lists */}
                        <form onSubmit={handleSearchSubmit} className="sorting-search-form">
                            <input 
                                type="text" 
                                className="sorting-search-input"
                                placeholder="Tìm trong kết quả..." 
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <button type="submit" className="btn sorting-search-btn">
                                <IconSearch size={16} />
                            </button>
                        </form>
                    </div>

                    {/* Errors */}
                    {error && (
                        <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3) var(--space-4)', marginBottom: 'var(--space-4)', fontSize: 'var(--font-size-base)', display: 'flex', gap: 6, alignItems: 'center' }}>
                            <IconClose size={16} /> Đã xảy ra lỗi: {error}
                        </div>
                    )}

                    {/* Loading Skeletal state */}
                    {loading ? (
                        <div className="product-grid">
                            {Array(pageSize).fill(0).map((_, idx) => (
                                <ProductCardSkeleton key={idx} />
                            ))}
                        </div>
                    ) : products.length === 0 ? (
                        <div className="empty-state card">
                            <div className="empty-state-icon"><IconSearch /></div>
                            <h3 className="empty-state-title">Không tìm thấy sản phẩm nào</h3>
                            <p className="empty-state-text">Hãy thử thay đổi từ khóa hoặc bộ lọc giá của bạn.</p>
                            <button className="btn btn-primary" onClick={handleClearFilters}>
                                Xóa bộ lọc
                            </button>
                        </div>
                    ) : (
                        <>
                            {/* Grid View */}
                            <div className="product-grid">
                                {products.map((prod) => (
                                    <ProductCard key={prod.id} product={prod} />
                                ))}
                            </div>

                            {/* Pagination Controls */}
                            {totalPages > 1 && (
                                <div className="pagination">
                                    <button 
                                        className="pagination-btn"
                                        onClick={() => setPage(p => Math.max(0, p - 1))}
                                        disabled={page === 0}
                                    >
                                        <IconChevronLeft size={16} />
                                    </button>
                                    
                                    {Array.from({ length: totalPages }, (_, idx) => (
                                        <button 
                                            key={idx}
                                            className={`pagination-btn ${page === idx ? 'active' : ''}`}
                                            onClick={() => setPage(idx)}
                                        >
                                            {idx + 1}
                                        </button>
                                    ))}

                                    <button 
                                        className="pagination-btn"
                                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                        disabled={page === totalPages - 1}
                                    >
                                        <IconChevronRight size={16} />
                                    </button>
                                </div>
                            )}
                        </>
                    )}
                </main>
            </div>
        </div>
    );
}

export default ProductList;