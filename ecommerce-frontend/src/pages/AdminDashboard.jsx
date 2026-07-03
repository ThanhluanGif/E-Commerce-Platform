import React, { useState, useEffect } from 'react';
import adminService from '../services/adminService';
import productService from '../services/productService';
import categoryService from '../services/categoryService';
import api from '../services/api';

function AdminDashboard() {
    const [activeTab, setActiveTab] = useState('stats'); // stats | orders | users | products

    // 1. Stats state
    const [stats, setStats] = useState(null);
    const [revenueData, setRevenueData] = useState([]);
    const [statsLoading, setStatsLoading] = useState(true);

    // 2. Orders state
    const [orders, setOrders] = useState([]);
    const [orderPage, setOrderPage] = useState(0);
    const [orderTotalPages, setOrderTotalPages] = useState(1);
    const [ordersLoading, setOrdersLoading] = useState(true);

    // 3. Users state
    const [users, setUsers] = useState([]);
    const [userPage, setUserPage] = useState(0);
    const [userTotalPages, setUserTotalPages] = useState(1);
    const [usersLoading, setUsersLoading] = useState(true);

    // 4. Products state
    const [products, setProducts] = useState([]);
    const [productPage, setProductPage] = useState(0);
    const [productTotalPages, setProductTotalPages] = useState(1);
    const [productsLoading, setProductsLoading] = useState(true);
    const [categories, setCategories] = useState([]);

    // Add Product Modal/Form state
    const [showAddProduct, setShowAddProduct] = useState(false);
    const [pName, setPName] = useState('');
    const [pSlug, setPSlug] = useState('');
    const [pPrice, setPPrice] = useState('');
    const [pSalePrice, setPSalePrice] = useState('');
    const [pStock, setPStock] = useState('');
    const [pDesc, setPDesc] = useState('');
    const [pCatId, setPCatId] = useState('');
    const [pImageUrl, setPImageUrl] = useState('');
    const [uploading, setUploading] = useState(false);

    // --- 1. LOAD STATISTICS ---
    const loadStats = () => {
        setStatsLoading(true);
        adminService.getDashboardStats()
            .then(res => {
                if (res && res.success) setStats(res.data);
                setStatsLoading(false);
            })
            .catch(err => {
                console.error(err);
                setStatsLoading(false);
            });

        adminService.getRevenueChart()
            .then(res => {
                if (res && res.success) setRevenueData(res.data || []);
            })
            .catch(err => console.error(err));
    };

    // --- 2. LOAD ORDERS ---
    const loadOrders = (page) => {
        setOrdersLoading(true);
        adminService.getOrders(page, 8)
            .then(res => {
                if (res && res.success && res.data) {
                    setOrders(res.data.content || []);
                    setOrderTotalPages(res.data.totalPages || 1);
                }
                setOrdersLoading(false);
            })
            .catch(err => {
                console.error(err);
                setOrdersLoading(false);
            });
    };

    // --- 3. LOAD USERS ---
    const loadUsers = (page) => {
        setUsersLoading(true);
        adminService.getUsers(page, 8)
            .then(res => {
                if (res && res.success && res.data) {
                    setUsers(res.data.content || []);
                    setUserTotalPages(res.data.totalPages || 1);
                }
                setUsersLoading(false);
            })
            .catch(err => {
                console.error(err);
                setUsersLoading(false);
            });
    };

    // --- 4. LOAD PRODUCTS ---
    const loadProducts = (page) => {
        setProductsLoading(true);
        productService.getAllProducts({ page, pageSize: 8 })
            .then(res => {
                if (res && res.success && res.data) {
                    setProducts(res.data.content || []);
                    setProductTotalPages(res.data.totalPages || 1);
                }
                setProductsLoading(false);
            })
            .catch(err => {
                console.error(err);
                setProductsLoading(false);
            });
    };

    const loadCategories = () => {
        categoryService.getCategoryTree()
            .then(res => {
                if (res && res.success) setCategories(res.data || []);
            })
            .catch(err => console.error(err));
    };

    useEffect(() => {
        if (activeTab === 'stats') loadStats();
        if (activeTab === 'orders') loadOrders(orderPage);
        if (activeTab === 'users') loadUsers(userPage);
        if (activeTab === 'products') {
            loadProducts(productPage);
            loadCategories();
        }
    }, [activeTab, orderPage, userPage, productPage]);

    // Handle Order status update
    const handleStatusUpdate = async (orderId, newStatus) => {
        try {
            const res = await adminService.updateOrderStatus(orderId, newStatus);
            if (res && res.success) {
                alert(`Cập nhật đơn hàng thành công sang trạng thái: ${newStatus}`);
                loadOrders(orderPage);
            }
        } catch (err) {
            alert("Lỗi: " + (err.response?.data?.message || err.message));
        }
    };

    // Handle User role change
    const handleRoleChange = async (userId, newRole) => {
        try {
            const res = await adminService.changeUserRole(userId, newRole);
            if (res && res.success) {
                alert(`Đã đổi vai trò người dùng thành: ${newRole}`);
                loadUsers(userPage);
            }
        } catch (err) {
            alert("Lỗi: " + (err.response?.data?.message || err.message));
        }
    };

    // Handle Product Image Upload
    const handleImageUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);
        setUploading(true);

        try {
            const res = await api.post('/api/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            if (res.data && res.data.success) {
                setPImageUrl(res.data.data);
            }
        } catch (err) {
            alert("Upload ảnh thất bại: " + (err.response?.data?.message || err.message));
        } finally {
            setUploading(false);
        }
    };

    // Handle Product Creation Submission
    const handleProductSubmit = async (e) => {
        e.preventDefault();
        if (!pName || !pSlug || !pPrice || !pCatId) {
            alert("Vui lòng nhập đầy đủ tên, slug, giá và danh mục!");
            return;
        }

        try {
            const payload = {
                name: pName,
                slug: pSlug,
                price: parseFloat(pPrice),
                salePrice: pSalePrice ? parseFloat(pSalePrice) : 0,
                stockQuantity: parseInt(pStock || 0),
                description: pDesc,
                categoryId: parseInt(pCatId),
                imageUrl: pImageUrl || "https://via.placeholder.com/400"
            };

            const res = await api.post('/api/products', payload);
            if (res.data && res.data.success) {
                alert("Thêm sản phẩm thành công!");
                setShowAddProduct(false);
                setPName('');
                setPSlug('');
                setPPrice('');
                setPSalePrice('');
                setPStock('');
                setPDesc('');
                setPCatId('');
                setPImageUrl('');
                loadProducts(productPage);
            }
        } catch (err) {
            alert("Lỗi thêm sản phẩm: " + (err.response?.data?.message || err.message));
        }
    };

    // Handle Product Delete
    const handleProductDelete = async (productId) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa sản phẩm này không?")) return;

        try {
            const res = await api.delete(`/api/products/${productId}`);
            if (res.data && res.data.success) {
                alert("Xóa sản phẩm thành công!");
                loadProducts(productPage);
            }
        } catch (err) {
            alert("Lỗi xóa sản phẩm: " + (err.response?.data?.message || err.message));
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING': return { bg: '#fef3c7', text: '#d97706' };
            case 'SHIPPING': return { bg: '#dbeafe', text: '#2563eb' };
            case 'DELIVERED': return { bg: '#d1fae5', text: '#059669' };
            case 'CANCELLED': return { bg: '#ffeeeb', text: '#e11d48' };
            default: return { bg: '#f3f4f6', text: '#4b5563' };
        }
    };

    return (
        <div style={{ display: 'flex', gap: '30px', minHeight: '80vh', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            
            {/* SIDEBAR */}
            <div style={{ width: '220px', background: '#1e293b', borderRadius: '8px', padding: '20px 10px', display: 'flex', flexDirection: 'column', gap: '8px', color: 'white', flexShrink: 0 }}>
                <h3 style={{ fontSize: '15px', color: '#64748b', fontWeight: 'bold', margin: '0 0 15px 12px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Bảng quản trị</h3>
                
                <button 
                    onClick={() => setActiveTab('stats')}
                    style={{ padding: '12px 15px', background: activeTab === 'stats' ? '#3b82f6' : 'transparent', color: activeTab === 'stats' ? 'white' : '#cbd5e1', border: 'none', borderRadius: '6px', textAlign: 'left', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', transition: 'all 0.2s' }}
                >
                    📊 Thống kê chung
                </button>
                <button 
                    onClick={() => setActiveTab('orders')}
                    style={{ padding: '12px 15px', background: activeTab === 'orders' ? '#3b82f6' : 'transparent', color: activeTab === 'orders' ? 'white' : '#cbd5e1', border: 'none', borderRadius: '6px', textAlign: 'left', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', transition: 'all 0.2s' }}
                >
                    📦 Đơn hàng
                </button>
                <button 
                    onClick={() => setActiveTab('users')}
                    style={{ padding: '12px 15px', background: activeTab === 'users' ? '#3b82f6' : 'transparent', color: activeTab === 'users' ? 'white' : '#cbd5e1', border: 'none', borderRadius: '6px', textAlign: 'left', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', transition: 'all 0.2s' }}
                >
                    👤 Người dùng
                </button>
                <button 
                    onClick={() => setActiveTab('products')}
                    style={{ padding: '12px 15px', background: activeTab === 'products' ? '#3b82f6' : 'transparent', color: activeTab === 'products' ? 'white' : '#cbd5e1', border: 'none', borderRadius: '6px', textAlign: 'left', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px', transition: 'all 0.2s' }}
                >
                    🏷️ Sản phẩm
                </button>
            </div>

            {/* MAIN CONTENT AREA */}
            <div style={{ flex: 1, background: 'white', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '25px', overflowX: 'auto' }}>
                
                {/* TAB 1: STATISTICS */}
                {activeTab === 'stats' && (
                    <div>
                        <h2 style={{ fontSize: '22px', fontWeight: 'bold', margin: '0 0 20px 0', color: '#1e293b' }}>Thống kê kinh doanh</h2>
                        
                        {statsLoading ? (
                            <div>Đang tải số liệu...</div>
                        ) : stats ? (
                            <div>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '40px' }}>
                                    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #eff6ff, #dbeafe)', borderRadius: '8px', border: '1px solid #bfdbfe' }}>
                                        <div style={{ fontSize: '13px', color: '#1e40af', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px' }}>Doanh thu (Đã giao)</div>
                                        <div style={{ fontSize: '24px', fontWeight: '800', color: '#1e3a8a' }}>{stats.totalRevenue.toLocaleString()} đ</div>
                                    </div>
                                    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #fef3c7, #fde68a)', borderRadius: '8px', border: '1px solid #fde047' }}>
                                        <div style={{ fontSize: '13px', color: '#92400e', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px' }}>Tổng số đơn hàng</div>
                                        <div style={{ fontSize: '24px', fontWeight: '800', color: '#78350f' }}>{stats.totalOrders} đơn</div>
                                    </div>
                                    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #ecfdf5, #d1fae5)', borderRadius: '8px', border: '1px solid #a7f3d0' }}>
                                        <div style={{ fontSize: '13px', color: '#065f46', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px' }}>Thành viên</div>
                                        <div style={{ fontSize: '24px', fontWeight: '800', color: '#064e3b' }}>{stats.totalUsers} tài khoản</div>
                                    </div>
                                    <div style={{ padding: '20px', background: 'linear-gradient(135deg, #fdf2f8, #fbcfe8)', borderRadius: '8px', border: '1px solid #f9a8d4' }}>
                                        <div style={{ fontSize: '13px', color: '#9d174d', fontWeight: '600', textTransform: 'uppercase', marginBottom: '5px' }}>Số lượng sản phẩm</div>
                                        <div style={{ fontSize: '24px', fontWeight: '800', color: '#831843' }}>{stats.totalProducts} mặt hàng</div>
                                    </div>
                                </div>

                                {/* Daily Revenue CSS Bar Chart */}
                                <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#1e293b', marginBottom: '20px' }}>Biểu đồ doanh thu hàng ngày (đ)</h3>
                                {revenueData.length === 0 ? (
                                    <p style={{ fontStyle: 'italic', color: '#64748b' }}>Chưa có đơn hàng nào được giao thành công để hiển thị doanh thu.</p>
                                ) : (
                                    <div style={{ display: 'flex', alignItems: 'flex-end', gap: '20px', height: '220px', background: '#f8fafc', padding: '20px', borderRadius: '8px', border: '1px solid #e2e8f0', overflowX: 'auto', minWidth: '400px' }}>
                                        {revenueData.map((data, index) => {
                                            const maxRevenue = Math.max(...revenueData.map(d => d.revenue), 1);
                                            const heightPct = (data.revenue / maxRevenue) * 150; // Max height 150px
                                            return (
                                                <div key={index} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1, minWidth: '50px' }}>
                                                    <span style={{ fontSize: '10px', color: '#475569', fontWeight: 'bold', marginBottom: '5px' }}>{data.revenue.toLocaleString()}</span>
                                                    <div style={{ width: '25px', height: `${Math.max(5, heightPct)}px`, background: '#3b82f6', borderRadius: '4px 4px 0 0', transition: 'background 0.2s', cursor: 'pointer' }}
                                                         onMouseEnter={(e) => e.target.style.background = '#2563eb'}
                                                         onMouseLeave={(e) => e.target.style.background = '#3b82f6'}
                                                         title={`Ngày ${data.date}: ${data.revenue.toLocaleString()} đ`} />
                                                    <span style={{ fontSize: '11px', color: '#64748b', marginTop: '8px', whiteSpace: 'nowrap' }}>{data.date.substring(5)}</span>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div>Lỗi khi tải số liệu thống kê.</div>
                        )}
                    </div>
                )}

                {/* TAB 2: ORDERS MANAGEMENT */}
                {activeTab === 'orders' && (
                    <div>
                        <h2 style={{ fontSize: '22px', fontWeight: 'bold', margin: '0 0 20px 0', color: '#1e293b' }}>Quản lý đơn hàng</h2>

                        {ordersLoading ? (
                            <div>Đang tải đơn hàng...</div>
                        ) : (
                            <div>
                                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px', minWidth: '700px' }}>
                                    <thead>
                                        <tr style={{ borderBottom: '2px solid #e2e8f0', textAlign: 'left', background: '#f8fafc' }}>
                                            <th style={{ padding: '12px' }}>Mã Đơn Hàng</th>
                                            <th style={{ padding: '12px' }}>Khách Hàng</th>
                                            <th style={{ padding: '12px' }}>Tổng Tiền</th>
                                            <th style={{ padding: '12px' }}>Trạng Thái</th>
                                            <th style={{ padding: '12px' }}>Cập Nhật Trạng Thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {orders.map(order => {
                                            const statusStyle = getStatusColor(order.status);
                                            const isImmutable = order.status === 'DELIVERED' || order.status === 'CANCELLED';
                                            return (
                                                <tr key={order.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                                    <td style={{ padding: '12px', fontWeight: 'bold', color: '#0f172a' }}>{order.orderCode}</td>
                                                    <td style={{ padding: '12px' }}>{order.shippingName}</td>
                                                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{order.totalPrice.toLocaleString()} đ</td>
                                                    <td style={{ padding: '12px' }}>
                                                        <span style={{ background: statusStyle.bg, color: statusStyle.text, padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 'bold' }}>
                                                            {order.status}
                                                        </span>
                                                    </td>
                                                    <td style={{ padding: '12px' }}>
                                                        {isImmutable ? (
                                                            <span style={{ color: '#94a3b8', fontStyle: 'italic', fontSize: '13px' }}>Không thể thay đổi</span>
                                                        ) : (
                                                            <select 
                                                                value={order.status}
                                                                onChange={(e) => handleStatusUpdate(order.id, e.target.value)}
                                                                style={{ padding: '6px', borderRadius: '4px', border: '1px solid #cbd5e1', background: 'white' }}
                                                            >
                                                                <option value="PENDING">PENDING</option>
                                                                <option value="SHIPPING">SHIPPING</option>
                                                                <option value="DELIVERED">DELIVERED</option>
                                                                <option value="CANCELLED">CANCELLED</option>
                                                            </select>
                                                        )}
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>

                                {/* Pagination */}
                                <div style={{ display: 'flex', gap: '5px', marginTop: '20px', justifyContent: 'center' }}>
                                    {Array.from({ length: orderTotalPages }).map((_, i) => (
                                        <button 
                                            key={i} 
                                            onClick={() => setOrderPage(i)}
                                            style={{ padding: '6px 12px', border: '1px solid #cbd5e1', background: orderPage === i ? '#3b82f6' : 'white', color: orderPage === i ? 'white' : '#1e293b', borderRadius: '4px', cursor: 'pointer' }}
                                        >
                                            {i + 1}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* TAB 3: USERS MANAGEMENT */}
                {activeTab === 'users' && (
                    <div>
                        <h2 style={{ fontSize: '22px', fontWeight: 'bold', margin: '0 0 20px 0', color: '#1e293b' }}>Quản lý người dùng</h2>

                        {usersLoading ? (
                            <div>Đang tải người dùng...</div>
                        ) : (
                            <div>
                                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px', minWidth: '700px' }}>
                                    <thead>
                                        <tr style={{ borderBottom: '2px solid #e2e8f0', textAlign: 'left', background: '#f8fafc' }}>
                                            <th style={{ padding: '12px' }}>ID</th>
                                            <th style={{ padding: '12px' }}>Tên Đăng Nhập</th>
                                            <th style={{ padding: '12px' }}>Email</th>
                                            <th style={{ padding: '12px' }}>Vai Trò</th>
                                            <th style={{ padding: '12px' }}>Thay Đổi Vai Trò</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {users.map(user => (
                                            <tr key={user.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                                <td style={{ padding: '12px' }}>{user.id}</td>
                                                <td style={{ padding: '12px', fontWeight: 'bold' }}>{user.username}</td>
                                                <td style={{ padding: '12px' }}>{user.email}</td>
                                                <td style={{ padding: '12px' }}>
                                                    <span style={{ background: user.role === 'ADMIN' ? '#fee2e2' : '#f0fdf4', color: user.role === 'ADMIN' ? '#dc2626' : '#15803d', padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 'bold' }}>
                                                        {user.role}
                                                    </span>
                                                </td>
                                                <td style={{ padding: '12px' }}>
                                                    <button 
                                                        onClick={() => handleRoleChange(user.id, user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN')}
                                                        style={{ padding: '6px 12px', background: 'white', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer', fontSize: '13px' }}
                                                    >
                                                        Chuyển thành {user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>

                                {/* Pagination */}
                                <div style={{ display: 'flex', gap: '5px', marginTop: '20px', justifyContent: 'center' }}>
                                    {Array.from({ length: userTotalPages }).map((_, i) => (
                                        <button 
                                            key={i} 
                                            onClick={() => setUserPage(i)}
                                            style={{ padding: '6px 12px', border: '1px solid #cbd5e1', background: userPage === i ? '#3b82f6' : 'white', color: userPage === i ? 'white' : '#1e293b', borderRadius: '4px', cursor: 'pointer' }}
                                        >
                                            {i + 1}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}

                {/* TAB 4: PRODUCTS MANAGEMENT */}
                {activeTab === 'products' && (
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <h2 style={{ fontSize: '22px', fontWeight: 'bold', margin: 0, color: '#1e293b' }}>Quản lý sản phẩm</h2>
                            <button 
                                onClick={() => setShowAddProduct(true)}
                                style={{ padding: '8px 16px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                            >
                                ＋ Thêm sản phẩm
                            </button>
                        </div>

                        {/* Add Product Inline Dialog */}
                        {showAddProduct && (
                            <div style={{ background: '#f8fafc', border: '1px solid #cbd5e1', borderRadius: '8px', padding: '20px', marginBottom: '30px' }}>
                                <h3 style={{ margin: '0 0 15px 0', fontSize: '16px', fontWeight: 'bold' }}>Thêm sản phẩm mới</h3>
                                <form onSubmit={handleProductSubmit} style={{ display: 'flex', flexWrap: 'wrap', gap: '15px' }}>
                                    <div style={{ flex: '1 1 200px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Tên sản phẩm *</label>
                                        <input type="text" value={pName} onChange={(e) => setPName(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} required />
                                    </div>
                                    <div style={{ flex: '1 1 200px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Slug *</label>
                                        <input type="text" value={pSlug} onChange={(e) => setPSlug(e.target.value)} placeholder="tivi-sony-4k" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} required />
                                    </div>
                                    <div style={{ flex: '1 1 120px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Giá tiền (đ) *</label>
                                        <input type="number" value={pPrice} onChange={(e) => setPPrice(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} required />
                                    </div>
                                    <div style={{ flex: '1 1 120px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Giá khuyến mãi</label>
                                        <input type="number" value={pSalePrice} onChange={(e) => setPSalePrice(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} />
                                    </div>
                                    <div style={{ flex: '1 1 100px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Kho hàng</label>
                                        <input type="number" value={pStock} onChange={(e) => setPStock(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} />
                                    </div>
                                    <div style={{ flex: '1 1 200px' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Danh mục *</label>
                                        <select value={pCatId} onChange={(e) => setPCatId(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} required>
                                            <option value="">-- Chọn danh mục --</option>
                                            {categories.map(cat => (
                                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div style={{ flex: '1 1 100%' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Ảnh sản phẩm</label>
                                        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                                            <label style={{ cursor: 'pointer', padding: '8px 15px', background: 'white', border: '1px solid #cbd5e1', borderRadius: '4px', fontSize: '13px', fontWeight: 'bold' }}>
                                                {uploading ? 'Đang tải lên...' : 'Tải lên hình ảnh'}
                                                <input type="file" onChange={handleImageUpload} style={{ display: 'none' }} disabled={uploading} />
                                            </label>
                                            {pImageUrl && <span style={{ fontSize: '12px', color: '#10b981' }}>✓ {pImageUrl}</span>}
                                        </div>
                                    </div>
                                    <div style={{ flex: '1 1 100%' }}>
                                        <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '5px' }}>Mô tả sản phẩm</label>
                                        <textarea rows="3" value={pDesc} onChange={(e) => setPDesc(e.target.value)} style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} />
                                    </div>
                                    <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                                        <button type="submit" style={{ padding: '8px 20px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Lưu lại</button>
                                        <button type="button" onClick={() => setShowAddProduct(false)} style={{ padding: '8px 20px', background: 'white', border: '1px solid #cbd5e1', borderRadius: '4px', cursor: 'pointer' }}>Hủy</button>
                                    </div>
                                </form>
                            </div>
                        )}

                        {productsLoading ? (
                            <div>Đang tải sản phẩm...</div>
                        ) : (
                            <div>
                                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px', minWidth: '700px' }}>
                                    <thead>
                                        <tr style={{ borderBottom: '2px solid #e2e8f0', textAlign: 'left', background: '#f8fafc' }}>
                                            <th style={{ padding: '12px' }}>Ảnh</th>
                                            <th style={{ padding: '12px' }}>Tên Sản Phẩm</th>
                                            <th style={{ padding: '12px' }}>Giá</th>
                                            <th style={{ padding: '12px' }}>Kho Hàng</th>
                                            <th style={{ padding: '12px' }}>Hành Động</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {products.map(prod => (
                                            <tr key={prod.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                                <td style={{ padding: '12px' }}>
                                                    <img src={prod.imageUrl} alt={prod.name} style={{ width: '40px', height: '40px', objectFit: 'contain', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '4px' }} />
                                                </td>
                                                <td style={{ padding: '12px', fontWeight: 'bold' }}>{prod.name}</td>
                                                <td style={{ padding: '12px' }}>{prod.price.toLocaleString()} đ</td>
                                                <td style={{ padding: '12px' }}>{prod.stockQuantity}</td>
                                                <td style={{ padding: '12px' }}>
                                                    <button 
                                                        onClick={() => handleProductDelete(prod.id)}
                                                        style={{ padding: '6px 12px', background: '#fee2e2', border: 'none', color: '#dc2626', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                                                    >
                                                        Xóa
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>

                                {/* Pagination */}
                                <div style={{ display: 'flex', gap: '5px', marginTop: '20px', justifyContent: 'center' }}>
                                    {Array.from({ length: productTotalPages }).map((_, i) => (
                                        <button 
                                            key={i} 
                                            onClick={() => setProductPage(i)}
                                            style={{ padding: '6px 12px', border: '1px solid #cbd5e1', background: productPage === i ? '#3b82f6' : 'white', color: productPage === i ? 'white' : '#1e293b', borderRadius: '4px', cursor: 'pointer' }}
                                        >
                                            {i + 1}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminDashboard;
