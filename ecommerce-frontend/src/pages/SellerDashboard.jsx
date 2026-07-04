import React, { useEffect, useState } from 'react';
import axios from 'axios';

function SellerDashboard() {
    const [user, setUser] = useState(null);
    const [shop, setShop] = useState(null);
    const [activeTab, setActiveTab] = useState('analytics');

    // Registration state
    const [regName, setRegName] = useState('');
    const [regSlug, setRegSlug] = useState('');
    const [regDesc, setRegDesc] = useState('');
    const [regLogo, setRegLogo] = useState('');
    const [regBanner, setRegBanner] = useState('');

    // Analytics state
    const [analytics, setAnalytics] = useState(null);

    // Products state
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [showProdModal, setShowProdModal] = useState(false);
    const [editingProd, setEditingProd] = useState(null);
    const [prodName, setProdName] = useState('');
    const [prodSlug, setProdSlug] = useState('');
    const [prodDesc, setProdDesc] = useState('');
    const [prodPrice, setProdPrice] = useState('');
    const [prodSalePrice, setProdSalePrice] = useState('');
    const [prodStock, setProdStock] = useState('');
    const [prodImage, setProdImage] = useState('');
    const [prodCategoryId, setProdCategoryId] = useState('');

    // Orders state
    const [orders, setOrders] = useState([]);

    const token = localStorage.getItem('token');

    // Load initial profile & shop
    const loadProfileAndShop = () => {
        if (!token) return;
        
        axios.get('http://localhost:8080/api/users/profile', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                const userData = res.data.data;
                setUser(userData);
                
                if (userData.role === 'SELLER') {
                    // Fetch shop details
                    axios.get('http://localhost:8080/api/seller/shop', {
                        headers: { 'Authorization': `Bearer ${token}` }
                    })
                    .then(sRes => {
                        if (sRes.data && sRes.data.success) {
                            setShop(sRes.data.data);
                        }
                    })
                    .catch(err => console.error("Error loading shop:", err));
                }
            }
        })
        .catch(err => console.error("Error loading profile:", err));
    };

    useEffect(() => {
        loadProfileAndShop();
    }, [token]);

    // Tab content fetchers
    useEffect(() => {
        if (!shop || !token) return;

        if (activeTab === 'analytics') {
            axios.get('http://localhost:8080/api/seller/analytics', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
            .then(res => {
                if (res.data && res.data.success) setAnalytics(res.data.data);
            })
            .catch(err => console.error(err));
        }

        if (activeTab === 'products') {
            axios.get('http://localhost:8080/api/seller/products', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
            .then(res => {
                if (res.data && res.data.success) setProducts(res.data.data.content || []);
            })
            .catch(err => console.error(err));

            axios.get('http://localhost:8080/api/categories')
            .then(res => {
                if (res.data && res.data.success) setCategories(res.data.data || []);
            })
            .catch(err => console.error(err));
        }

        if (activeTab === 'orders') {
            axios.get('http://localhost:8080/api/seller/orders', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
            .then(res => {
                if (res.data && res.data.success) setOrders(res.data.data.content || []);
            })
            .catch(err => console.error(err));
        }
    }, [activeTab, shop, token]);

    // Handle Register Seller
    const handleRegister = (e) => {
        e.preventDefault();
        if (!regName) return alert("Vui lòng điền tên gian hàng!");

        const payload = {
            name: regName,
            slug: regSlug || regName.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-"),
            description: regDesc,
            logoUrl: regLogo,
            bannerUrl: regBanner
        };

        axios.post('http://localhost:8080/api/seller/register', payload, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                alert("Đăng ký người bán thành công!");
                // Update local storage user role
                const u = JSON.parse(localStorage.getItem('user') || '{}');
                u.role = 'SELLER';
                localStorage.setItem('user', JSON.stringify(u));
                loadProfileAndShop();
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi đăng ký shop: " + (err.response?.data?.message || err.message));
        });
    };

    // Handle Add/Edit Product
    const handleSaveProduct = (e) => {
        e.preventDefault();
        const payload = {
            name: prodName,
            slug: prodSlug || prodName.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-"),
            description: prodDesc,
            price: Number(prodPrice),
            salePrice: prodSalePrice ? Number(prodSalePrice) : null,
            stockQuantity: Number(prodStock),
            imageUrl: prodImage,
            categoryId: Number(prodCategoryId)
        };

        const request = editingProd 
            ? axios.put(`http://localhost:8080/api/seller/products/${editingProd.id}`, payload, { headers: { 'Authorization': `Bearer ${token}` } })
            : axios.post('http://localhost:8080/api/seller/products', payload, { headers: { 'Authorization': `Bearer ${token}` } });

        request
        .then(res => {
            if (res.data && res.data.success) {
                alert(editingProd ? "Cập nhật sản phẩm thành công!" : "Thêm sản phẩm thành công!");
                setShowProdModal(false);
                setEditingProd(null);
                // Refresh products
                setActiveTab('');
                setTimeout(() => setActiveTab('products'), 50);
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi lưu sản phẩm: " + (err.response?.data?.message || err.message));
        });
    };

    const openEditModal = (prod) => {
        setEditingProd(prod);
        setProdName(prod.name);
        setProdSlug(prod.slug);
        setProdDesc(prod.description);
        setProdPrice(prod.price);
        setProdSalePrice(prod.salePrice || '');
        setProdStock(prod.stockQuantity);
        setProdImage(prod.imageUrl);
        setProdCategoryId(prod.categoryId);
        setShowProdModal(true);
    };

    const openAddModal = () => {
        setEditingProd(null);
        setProdName('');
        setProdSlug('');
        setProdDesc('');
        setProdPrice('');
        setProdSalePrice('');
        setProdStock('');
        setProdImage('');
        setProdCategoryId(categories[0]?.id || '');
        setShowProdModal(true);
    };

    // Handle Delete Product
    const handleDeleteProduct = (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa sản phẩm này không?")) return;
        axios.delete(`http://localhost:8080/api/seller/products/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                alert("Xóa sản phẩm thành công!");
                setProducts(prev => prev.filter(p => p.id !== id));
            }
        })
        .catch(err => {
            console.error(err);
            alert("Không thể xóa sản phẩm");
        });
    };

    // Handle Order Confirmation
    const handleConfirmOrder = (orderId) => {
        axios.put(`http://localhost:8080/api/seller/orders/${orderId}/confirm`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                alert("Đã xác nhận đang giao hàng!");
                setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: 'SHIPPING' } : o));
            }
        })
        .catch(err => {
            console.error(err);
            alert("Lỗi khi xác nhận đơn hàng");
        });
    };

    if (!user) return <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải thông tin...</div>;

    // IF NOT SELLER YET
    if (user.role !== 'SELLER') {
        return (
            <div style={{ maxWidth: '600px', margin: '40px auto', background: 'white', padding: '30px', borderRadius: '8px', boxShadow: 'var(--card-shadow)', fontFamily: 'system-ui, sans-serif' }}>
                <h2 style={{ color: '#f94e30', fontSize: '22px', fontWeight: 'bold', borderBottom: '2px solid #ffe4de', paddingBottom: '10px', marginBottom: '20px' }}>
                    🏪 Đăng Ký Người Bán Shopee
                </h2>
                <p style={{ color: '#666', fontSize: '14px', lineHeight: '1.6', marginBottom: '20px' }}>
                    Bắt đầu hành trình bán hàng của bạn ngay hôm nay! Điền thông tin gian hàng để bắt đầu đăng sản phẩm và tiếp cận hàng ngàn khách hàng.
                </p>
                <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                        <label style={{ fontSize: '13px', fontWeight: 'bold', color: '#333' }}>Tên gian hàng *</label>
                        <input type="text" value={regName} onChange={(e) => setRegName(e.target.value)} required style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} placeholder="Ví dụ: Tech World Store" />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                        <label style={{ fontSize: '13px', fontWeight: 'bold', color: '#333' }}>Slug gian hàng (Để trống sẽ tự động tạo)</label>
                        <input type="text" value={regSlug} onChange={(e) => setRegSlug(e.target.value)} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} placeholder="ví-du: tech-world-store" />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                        <label style={{ fontSize: '13px', fontWeight: 'bold', color: '#333' }}>Mô tả shop</label>
                        <textarea value={regDesc} onChange={(e) => setRegDesc(e.target.value)} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px', height: '80px' }} placeholder="Chuyên cung cấp đồ công nghệ chính hãng..." />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                        <label style={{ fontSize: '13px', fontWeight: 'bold', color: '#333' }}>Link Logo hình ảnh</label>
                        <input type="text" value={regLogo} onChange={(e) => setRegLogo(e.target.value)} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} placeholder="https://..." />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                        <label style={{ fontSize: '13px', fontWeight: 'bold', color: '#333' }}>Link Banner hình nền</label>
                        <input type="text" value={regBanner} onChange={(e) => setRegBanner(e.target.value)} style={{ padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }} placeholder="https://..." />
                    </div>
                    <button type="submit" style={{ padding: '12px', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', fontSize: '15px', cursor: 'pointer', marginTop: '10px' }}>
                        KÍCH HOẠT GIAN HÀNG NGAY
                    </button>
                </form>
            </div>
        );
    }

    // IF SELLER
    return (
        <div style={{ display: 'flex', gap: '20px', fontFamily: 'system-ui, sans-serif' }}>
            
            {/* Sidebar navigation */}
            <div style={{ width: '220px', background: 'white', borderRadius: '8px', padding: '15px', display: 'flex', flexDirection: 'column', gap: '8px', boxShadow: 'var(--card-shadow)', height: 'fit-content' }}>
                <div style={{ textAlign: 'center', paddingBottom: '15px', borderBottom: '1px solid #f1f5f9', marginBottom: '10px' }}>
                    <div style={{ width: '60px', height: '60px', borderRadius: '50%', background: '#ffdbd4', margin: '0 auto 10px auto', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <img src={shop?.logoUrl || 'https://via.placeholder.com/150'} alt="Shop Logo" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    </div>
                    <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 'bold', color: '#333' }}>{shop?.name}</h4>
                    <span style={{ fontSize: '11px', color: '#6b7280' }}>Kênh Người Bán</span>
                </div>

                {[
                    { id: 'analytics', label: '📊 Tổng quan', icon: '📊' },
                    { id: 'products', label: '📦 Sản phẩm', icon: '📦' },
                    { id: 'orders', label: '📝 Đơn hàng', icon: '📝' }
                ].map(tab => (
                    <button key={tab.id} onClick={() => setActiveTab(tab.id)} style={{
                        textAlign: 'left',
                        padding: '10px 15px',
                        background: activeTab === tab.id ? '#ffe4de' : 'transparent',
                        color: activeTab === tab.id ? '#f94e30' : '#4b5563',
                        border: 'none',
                        borderRadius: '6px',
                        fontWeight: 'bold',
                        fontSize: '14px',
                        cursor: 'pointer',
                        display: 'flex',
                        gap: '10px',
                        alignItems: 'center'
                    }}>
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Tab content area */}
            <div style={{ flex: 1, background: 'white', borderRadius: '8px', padding: '24px', boxShadow: 'var(--card-shadow)', minHeight: '500px' }}>
                
                {/* 1. ANALYTICS TAB */}
                {activeTab === 'analytics' && analytics && (
                    <div>
                        <h2 style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '20px' }}>Thống Kê Kinh Doanh Shop</h2>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
                            <div style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', background: '#f8fafc' }}>
                                <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: '#64748b' }}>Doanh Thu Hoàn Thành</h4>
                                <span style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>{analytics.totalRevenue.toLocaleString()} đ</span>
                            </div>
                            <div style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', background: '#f8fafc' }}>
                                <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: '#64748b' }}>Tổng Số Đơn Hàng</h4>
                                <span style={{ fontSize: '24px', fontWeight: 'bold', color: '#3b82f6' }}>{analytics.totalOrders} đơn</span>
                            </div>
                            <div style={{ padding: '20px', border: '1px solid #e2e8f0', borderRadius: '8px', background: '#f8fafc' }}>
                                <h4 style={{ margin: '0 0 10px 0', fontSize: '13px', color: '#64748b' }}>Số Sản Phẩm Đã Bán</h4>
                                <span style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>{analytics.totalProductsSold} món</span>
                            </div>
                        </div>
                    </div>
                )}

                {/* 2. PRODUCTS TAB */}
                {activeTab === 'products' && (
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <h2 style={{ fontSize: '18px', fontWeight: 'bold', margin: 0 }}>Quản Lý Sản Phẩm</h2>
                            <button onClick={openAddModal} style={{ padding: '8px 16px', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', fontSize: '13px', cursor: 'pointer' }}>
                                + Thêm Sản Phẩm Mới
                            </button>
                        </div>

                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                            <thead>
                                <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', textAlign: 'left' }}>
                                    <th style={{ padding: '12px' }}>Ảnh</th>
                                    <th style={{ padding: '12px' }}>Tên Sản Phẩm</th>
                                    <th style={{ padding: '12px' }}>Giá Gốc</th>
                                    <th style={{ padding: '12px' }}>Giá Khuyến Mãi</th>
                                    <th style={{ padding: '12px' }}>Kho Hàng</th>
                                    <th style={{ padding: '12px' }}>Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                {products.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: '#999' }}>Gian hàng chưa có sản phẩm.</td>
                                    </tr>
                                ) : (
                                    products.map(prod => (
                                        <tr key={prod.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                            <td style={{ padding: '12px' }}>
                                                <img src={prod.imageUrl} alt={prod.name} style={{ width: '50px', height: '50px', objectFit: 'contain' }} />
                                            </td>
                                            <td style={{ padding: '12px', fontWeight: 'bold' }}>{prod.name}</td>
                                            <td style={{ padding: '12px' }}>{prod.price.toLocaleString()}đ</td>
                                            <td style={{ padding: '12px', color: '#f94e30' }}>{prod.salePrice ? `${prod.salePrice.toLocaleString()}đ` : '-'}</td>
                                            <td style={{ padding: '12px' }}>{prod.stockQuantity}</td>
                                            <td style={{ padding: '12px' }}>
                                                <button onClick={() => openEditModal(prod)} style={{ padding: '4px 10px', marginRight: '5px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                                    Sửa
                                                </button>
                                                <button onClick={() => handleDeleteProduct(prod.id)} style={{ padding: '4px 10px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                                    Xóa
                                                </button>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>

                        {/* PRODUCT EDIT/ADD MODAL */}
                        {showProdModal && (
                            <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 10 }}>
                                <div style={{ background: 'white', padding: '30px', borderRadius: '8px', width: '500px', maxHeight: '90vh', overflowY: 'auto' }}>
                                    <h3 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>{editingProd ? 'Cập Nhật Sản Phẩm' : 'Đăng Sản Phẩm Mới'}</h3>
                                    <form onSubmit={handleSaveProduct} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                            <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Tên sản phẩm *</label>
                                            <input type="text" value={prodName} onChange={(e) => setProdName(e.target.value)} required style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }} />
                                        </div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                            <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Mô tả chi tiết</label>
                                            <textarea value={prodDesc} onChange={(e) => setProdDesc(e.target.value)} style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px', height: '60px' }} />
                                        </div>
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                                <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Giá bán *</label>
                                                <input type="number" value={prodPrice} onChange={(e) => setProdPrice(e.target.value)} required style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }} />
                                            </div>
                                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                                <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Giá khuyến mãi</label>
                                                <input type="number" value={prodSalePrice} onChange={(e) => setProdSalePrice(e.target.value)} style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }} />
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                                <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Số lượng kho *</label>
                                                <input type="number" value={prodStock} onChange={(e) => setProdStock(e.target.value)} required style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }} />
                                            </div>
                                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                                <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Danh mục sản phẩm *</label>
                                                <select value={prodCategoryId} onChange={(e) => setProdCategoryId(e.target.value)} style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }}>
                                                    {categories.map(cat => (
                                                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                                                    ))}
                                                </select>
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                            <label style={{ fontSize: '12px', fontWeight: 'bold' }}>Đường dẫn hình ảnh (URL)</label>
                                            <input type="text" value={prodImage} onChange={(e) => setProdImage(e.target.value)} style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }} />
                                        </div>
                                        <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                                            <button type="submit" style={{ flex: 1, padding: '10px', background: '#f94e30', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                                                Lưu Lại
                                            </button>
                                            <button type="button" onClick={() => setShowProdModal(false)} style={{ flex: 1, padding: '10px', background: '#6b7280', color: 'white', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                                                Đóng
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* 3. ORDERS TAB */}
                {activeTab === 'orders' && (
                    <div>
                        <h2 style={{ fontSize: '18px', fontWeight: 'bold', marginBottom: '20px' }}>Quản Lý Đơn Hàng Shop Nhận Được</h2>
                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                            <thead>
                                <tr style={{ borderBottom: '2px solid #e2e8f0', color: '#64748b', textAlign: 'left' }}>
                                    <th style={{ padding: '12px' }}>Mã Đơn Hàng</th>
                                    <th style={{ padding: '12px' }}>Người Đặt</th>
                                    <th style={{ padding: '12px' }}>Sản Phẩm Đặt</th>
                                    <th style={{ padding: '12px' }}>Tổng Tiền Đơn</th>
                                    <th style={{ padding: '12px' }}>Trạng Thái</th>
                                    <th style={{ padding: '12px' }}>Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                {orders.length === 0 ? (
                                    <tr>
                                        <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: '#999' }}>Chưa nhận được đơn hàng nào.</td>
                                    </tr>
                                ) : (
                                    orders.map(order => (
                                        <tr key={order.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                            <td style={{ padding: '12px', fontWeight: 'bold' }}>{order.orderCode}</td>
                                            <td style={{ padding: '12px' }}>{order.username}</td>
                                            <td style={{ padding: '12px' }}>
                                                {order.items?.map(i => (
                                                    <div key={i.id} style={{ fontSize: '12px' }}>{i.productName} (x{i.quantity})</div>
                                                ))}
                                            </td>
                                            <td style={{ padding: '12px', fontWeight: 'bold' }}>{order.totalPrice.toLocaleString()}đ</td>
                                            <td style={{ padding: '12px' }}>
                                                <span style={{
                                                    padding: '4px 8px',
                                                    borderRadius: '12px',
                                                    fontSize: '11px',
                                                    fontWeight: 'bold',
                                                    background: order.status === 'DELIVERED' ? '#d1fae5' : order.status === 'SHIPPING' ? '#dbeafe' : '#fef3c7',
                                                    color: order.status === 'DELIVERED' ? '#065f46' : order.status === 'SHIPPING' ? '#1e40af' : '#92400e'
                                                }}>
                                                    {order.status}
                                                </span>
                                            </td>
                                            <td style={{ padding: '12px' }}>
                                                {order.status === 'PENDING' && (
                                                    <button onClick={() => handleConfirmOrder(order.id)} style={{ padding: '4px 10px', background: '#10b981', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                                                        Chuẩn Bị Hàng
                                                    </button>
                                                )}
                                                {order.status === 'SHIPPING' && (
                                                    <span style={{ fontSize: '12px', color: '#666' }}>Đang giao vận...</span>
                                                )}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                )}

            </div>
        </div>
    );
}

export default SellerDashboard;
