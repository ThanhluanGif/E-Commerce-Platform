import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import { formatPrice, getProductImage, getOrderStatusLabel } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { 
  IconChart, IconPackage, IconClipboard, IconUsers, 
  IconPlus, IconEdit, IconTrash, IconCheckCircle, 
  IconStore 
} from '../utils/icons';
import './SellerDashboard.css';

function SellerDashboard() {
    const toast = useToast();
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

    const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');

    // Load initial profile & shop (Wrapped in useCallback to prevent missing dependency warnings)
    const loadProfileAndShop = useCallback(() => {
        if (!token) return;
        
        axios.get('http://localhost:8080/api/users/profile', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                const userData = res.data.data;
                setUser(userData);
                
                if (userData.role === 'SELLER') {
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
    }, [token]);

    useEffect(() => {
        loadProfileAndShop();
    }, [loadProfileAndShop]);

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
        if (!regName) {
            toast.error("Vui lòng điền tên gian hàng!");
            return;
        }

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
                toast.success("Đăng ký người bán thành công!");
                const u = JSON.parse(localStorage.getItem('user') || '{}');
                u.role = 'SELLER';
                localStorage.setItem('user', JSON.stringify(u));
                loadProfileAndShop();
            }
        })
        .catch(err => {
            console.error(err);
            toast.error("Lỗi đăng ký shop: " + (err.response?.data?.message || err.message));
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
                toast.success(editingProd ? "Cập nhật sản phẩm thành công!" : "Thêm sản phẩm thành công!");
                setShowProdModal(false);
                setEditingProd(null);
                // Refresh products list
                setActiveTab('');
                setTimeout(() => setActiveTab('products'), 50);
            }
        })
        .catch(err => {
            console.error(err);
            toast.error("Lỗi lưu sản phẩm: " + (err.response?.data?.message || err.message));
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
                toast.success("Xóa sản phẩm thành công!");
                setProducts(prev => prev.filter(p => p.id !== id));
            }
        })
        .catch(err => {
            console.error(err);
            toast.error("Không thể xóa sản phẩm");
        });
    };

    // Handle Order Confirmation
    const handleConfirmOrder = (orderId) => {
        axios.put(`http://localhost:8080/api/seller/orders/${orderId}/confirm`, {}, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                toast.success("Đã xác nhận đơn hàng, đang giao hàng!");
                setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: 'SHIPPING' } : o));
            }
        })
        .catch(err => {
            console.error(err);
            toast.error("Lỗi khi xác nhận đơn hàng");
        });
    };

    if (!user) {
        return (
            <div className="container loading-center">
                <div className="spinner spinner-lg" />
            </div>
        );
    }

    // IF NOT SELLER YET
    if (user.role !== 'SELLER') {
        return (
            <div className="container">
                <div className="register-seller-card">
                    <h2 className="register-title">
                        <IconStore size={26} /> Đăng Ký Kênh Người Bán
                    </h2>
                    <p style={{ color: 'var(--color-gray-600)', fontSize: 'var(--font-size-base)', lineHeight: 1.6, marginBottom: 'var(--space-5)' }}>
                        Bắt đầu hành trình bán hàng của bạn ngay hôm nay! Điền thông tin gian hàng để bắt đầu đăng bán sản phẩm và tiếp cận hàng ngàn khách hàng.
                    </p>
                    <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        <div className="form-group">
                            <label className="form-label">Tên gian hàng *</label>
                            <input type="text" className="form-input" value={regName} onChange={(e) => setRegName(e.target.value)} required placeholder="Ví dụ: Tech World Store" />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Slug gian hàng (Đăng ký URL)</label>
                            <input type="text" className="form-input" value={regSlug} onChange={(e) => setRegSlug(e.target.value)} placeholder="ví-du: tech-world-store" />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Mô tả shop</label>
                            <textarea className="form-textarea" value={regDesc} onChange={(e) => setRegDesc(e.target.value)} placeholder="Chuyên cung cấp đồ công nghệ chính hãng..." />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Link Logo hình ảnh</label>
                            <input type="text" className="form-input" value={regLogo} onChange={(e) => setRegLogo(e.target.value)} placeholder="https://..." />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Link Banner hình nền</label>
                            <input type="text" className="form-input" value={regBanner} onChange={(e) => setRegBanner(e.target.value)} placeholder="https://..." />
                        </div>
                        <button type="submit" className="btn btn-primary btn-block btn-lg" style={{ marginTop: 'var(--space-3)' }}>
                            KÍCH HOẠT GIAN HÀNG NGAY
                        </button>
                    </form>
                </div>
            </div>
        );
    }

    // IF SELLER
    return (
        <div className="container seller-layout">
            
            {/* Sidebar navigation */}
            <aside className="seller-sidebar">
                <div className="seller-shop-profile">
                    <div className="seller-shop-logo">
                        {shop?.logoUrl ? <img src={getProductImage(shop.logoUrl)} alt="Shop Logo" /> : shop?.name?.charAt(0).toUpperCase()}
                    </div>
                    <h4 className="seller-shop-name">{shop?.name}</h4>
                    <span className="badge badge-primary">Kênh Người Bán</span>
                </div>

                <button 
                    onClick={() => setActiveTab('analytics')} 
                    className={`seller-menu-item btn btn-ghost ${activeTab === 'analytics' ? 'active' : ''}`}
                >
                    <IconChart size={16} /> <span>Tổng quan</span>
                </button>
                <button 
                    onClick={() => setActiveTab('products')} 
                    className={`seller-menu-item btn btn-ghost ${activeTab === 'products' ? 'active' : ''}`}
                >
                    <IconPackage size={16} /> <span>Sản phẩm</span>
                </button>
                <button 
                    onClick={() => setActiveTab('orders')} 
                    className={`seller-menu-item btn btn-ghost ${activeTab === 'orders' ? 'active' : ''}`}
                >
                    <IconClipboard size={16} /> <span>Đơn hàng</span>
                </button>
            </aside>

            {/* Tab content area */}
            <main className="card" style={{ padding: 'var(--space-6)', minHeight: 500, overflow: 'hidden' }}>
                
                {/* 1. ANALYTICS TAB */}
                {activeTab === 'analytics' && analytics && (
                    <div>
                        <h2 className="user-content-title" style={{ borderBottom: '1px solid var(--color-gray-200)', paddingBottom: 'var(--space-2)', marginBottom: 'var(--space-5)' }}>Thống Kê Kinh Doanh Shop</h2>
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-icon-wrapper stat-icon-green"><IconChart size={24} /></div>
                                <div className="stat-info">
                                    <span className="stat-value">{formatPrice(analytics.totalRevenue)}</span>
                                    <span className="stat-label">Doanh thu hoàn thành</span>
                                </div>
                            </div>
                            
                            <div className="stat-card">
                                <div className="stat-icon-wrapper stat-icon-blue"><IconClipboard size={24} /></div>
                                <div className="stat-info">
                                    <span className="stat-value">{analytics.totalOrders} đơn</span>
                                    <span className="stat-label">Tổng số đơn hàng</span>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon-wrapper stat-icon-orange"><IconPackage size={24} /></div>
                                <div className="stat-info">
                                    <span className="stat-value">{analytics.totalProductsSold} món</span>
                                    <span className="stat-label">Số sản phẩm đã bán</span>
                                </div>
                            </div>

                            <div className="stat-card">
                                <div className="stat-icon-wrapper stat-icon-purple"><IconUsers size={24} /></div>
                                <div className="stat-info">
                                    <span className="stat-value">Active</span>
                                    <span className="stat-label">Trạng thái shop</span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* 2. PRODUCTS TAB */}
                {activeTab === 'products' && (
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid var(--color-gray-200)', paddingBottom: 'var(--space-3)' }}>
                            <h2 className="user-content-title" style={{ margin: 0 }}>Quản Lý Sản Phẩm Shop</h2>
                            <button onClick={openAddModal} className="btn btn-primary btn-sm" style={{ display: 'flex', gap: 4 }}>
                                <IconPlus size={14} /> Thêm Sản Phẩm Mới
                            </button>
                        </div>

                        <div className="table-container">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Ảnh</th>
                                        <th>Tên Sản Phẩm</th>
                                        <th>Giá Gốc</th>
                                        <th>Giá Khuyến Mãi</th>
                                        <th>Kho Hàng</th>
                                        <th>Hành Động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.length === 0 ? (
                                        <tr>
                                            <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: 'var(--color-gray-400)' }}>Gian hàng chưa có sản phẩm.</td>
                                        </tr>
                                    ) : (
                                        products.map(prod => (
                                            <tr key={prod.id}>
                                                <td>
                                                    <img src={getProductImage(prod.imageUrl)} alt={prod.name} style={{ width: '50px', height: '50px', objectFit: 'contain' }} onError={(e) => { e.target.src = '/no-image.png'; }} />
                                                </td>
                                                <td className="font-semibold">{prod.name}</td>
                                                <td>{formatPrice(prod.price)}</td>
                                                <td className="price">{prod.salePrice ? formatPrice(prod.salePrice) : '-'}</td>
                                                <td>{prod.stockQuantity}</td>
                                                <td>
                                                    <div style={{ display: 'flex', gap: 6 }}>
                                                        <button onClick={() => openEditModal(prod)} className="btn btn-secondary btn-sm" style={{ padding: 6, height: 'auto', border: '1px solid var(--color-gray-300)' }}>
                                                            <IconEdit size={14} />
                                                        </button>
                                                        <button onClick={() => handleDeleteProduct(prod.id)} className="btn btn-danger btn-sm" style={{ padding: 6, height: 'auto' }}>
                                                            <IconTrash size={14} />
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>

                        {/* PRODUCT EDIT/ADD MODAL */}
                        {showProdModal && (
                            <div className="overlay">
                                <div className="modal" style={{ maxWidth: 550 }}>
                                    <div className="modal-header">
                                        <h3 style={{ margin: 0, fontSize: 'var(--font-size-md)' }}>
                                            {editingProd ? 'Cập Nhật Sản Phẩm' : 'Đăng Sản Phẩm Mới'}
                                        </h3>
                                    </div>
                                    <form onSubmit={handleSaveProduct}>
                                        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                                            <div className="form-group">
                                                <label className="form-label">Tên sản phẩm *</label>
                                                <input type="text" className="form-input" value={prodName} onChange={(e) => setProdName(e.target.value)} required />
                                            </div>
                                            <div className="form-group">
                                                <label className="form-label">Mô tả chi tiết</label>
                                                <textarea className="form-textarea" rows="3" value={prodDesc} onChange={(e) => setProdDesc(e.target.value)} />
                                            </div>
                                            <div style={{ display: 'flex', gap: '15px' }}>
                                                <div className="form-group" style={{ flex: 1 }}>
                                                    <label className="form-label">Giá bán gốc *</label>
                                                    <input type="number" className="form-input" value={prodPrice} onChange={(e) => setProdPrice(e.target.value)} required />
                                                </div>
                                                <div className="form-group" style={{ flex: 1 }}>
                                                    <label className="form-label">Giá sale khuyến mãi</label>
                                                    <input type="number" className="form-input" value={prodSalePrice} onChange={(e) => setProdSalePrice(e.target.value)} />
                                                </div>
                                            </div>
                                            <div style={{ display: 'flex', gap: '15px' }}>
                                                <div className="form-group" style={{ flex: 1 }}>
                                                    <label className="form-label">Số lượng trong kho *</label>
                                                    <input type="number" className="form-input" value={prodStock} onChange={(e) => setProdStock(e.target.value)} required />
                                                </div>
                                                <div className="form-group" style={{ flex: 1 }}>
                                                    <label className="form-label">Danh mục sản phẩm *</label>
                                                    <select className="form-select" value={prodCategoryId} onChange={(e) => setProdCategoryId(e.target.value)}>
                                                        {categories.map(cat => (
                                                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                                                        ))}
                                                    </select>
                                                </div>
                                            </div>
                                            <div className="form-group">
                                                <label className="form-label">Đường dẫn hình ảnh (URL)</label>
                                                <input type="text" className="form-input" value={prodImage} onChange={(e) => setProdImage(e.target.value)} placeholder="Ví dụ: /uploads/sp.png" />
                                            </div>
                                        </div>
                                        <div className="modal-footer">
                                            <button type="submit" className="btn btn-primary">
                                                Lưu Lại
                                            </button>
                                            <button type="button" onClick={() => setShowProdModal(false)} className="btn btn-secondary">
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
                        <h2 className="user-content-title" style={{ borderBottom: '1px solid var(--color-gray-200)', paddingBottom: 'var(--space-3)', marginBottom: '20px' }}>Quản Lý Đơn Hàng Của Shop</h2>
                        <div className="table-container">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Mã Đơn Hàng</th>
                                        <th>Người Đặt</th>
                                        <th>Sản Phẩm Đặt</th>
                                        <th>Tổng Tiền Đơn</th>
                                        <th>Trạng Thái</th>
                                        <th>Hành Động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.length === 0 ? (
                                        <tr>
                                            <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: 'var(--color-gray-400)' }}>Chưa nhận được đơn hàng nào.</td>
                                        </tr>
                                    ) : (
                                        orders.map(order => {
                                            const statusObj = getOrderStatusLabel(order.status);
                                            return (
                                                <tr key={order.id}>
                                                    <td className="font-semibold">{order.orderCode}</td>
                                                    <td>{order.username}</td>
                                                    <td>
                                                        {order.items?.map(i => (
                                                            <div key={i.id} style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-700)' }}>{i.productName} (x{i.quantity})</div>
                                                        ))}
                                                    </td>
                                                    <td className="price">{formatPrice(order.totalPrice)}</td>
                                                    <td>
                                                        <span className={`badge ${statusObj.className}`}>
                                                            {statusObj.label}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        {order.status === 'PENDING' && (
                                                            <button 
                                                                onClick={() => handleConfirmOrder(order.id)} 
                                                                className="btn btn-success btn-sm"
                                                                style={{ display: 'flex', gap: 4 }}
                                                            >
                                                                <IconCheckCircle size={14} /> Chuẩn Bị Hàng
                                                            </button>
                                                        )}
                                                        {order.status === 'SHIPPING' && (
                                                            <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)', fontStyle: 'italic' }}>Đang vận chuyển...</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            );
                                        })
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

            </main>
        </div>
    );
}

export default SellerDashboard;
