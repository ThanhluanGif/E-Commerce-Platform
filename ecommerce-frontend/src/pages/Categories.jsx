import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import categoryService from '../services/categoryService';

function Categories() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        categoryService.getAllCategories()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setCategories(res.data);
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px', color: '#6b7280' }}>⏳ Đang tải danh sách danh mục...</div>;
    if (error) return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center', fontSize: '18px' }}>❌ Lỗi: {error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Tất Cả Danh Mục
            </h2>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '25px' }}>
                {categories.map(cat => (
                    <Link to={`/products?categoryId=${cat.id}`} key={cat.id} style={{ textDecoration: 'none' }}>
                        <div style={{ border: '1px solid #e5e7eb', borderRadius: '8px', overflow: 'hidden', background: '#fff', cursor: 'pointer', transition: 'transform 0.2s, box-shadow 0.2s', padding: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', height: '100%', boxSizing: 'border-box' }}
                             onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 10px 15px -3px rgba(0,0,0,0.05)'; }}
                             onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'none'; }}>
                            
                            <div style={{ width: '80px', height: '80px', borderRadius: '50%', background: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '15px' }}>
                                <img 
                                    src={cat.imageUrl || "https://img.icons8.com/color/96/category.png"} 
                                    alt={cat.name} 
                                    style={{ width: '50%', height: 'auto' }}
                                />
                            </div>

                            <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', margin: '0 0 8px 0' }}>{cat.name}</h3>
                            <p style={{ fontSize: '14px', color: '#6b7280', margin: 0 }}>
                                {cat.description || "Khám phá các sản phẩm trong danh mục này."}
                            </p>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
}

export default Categories;
