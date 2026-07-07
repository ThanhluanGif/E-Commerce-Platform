import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import orderService from '../services/orderService';
import UserLayout from '../components/UserLayout';
import { formatPrice, getOrderStatusLabel } from '../utils/helpers';
import { useToast } from '../utils/toast';
import { IconPackage, IconTrash, IconInfo, IconWarning } from '../utils/icons';
import './Orders.css';

function Orders() {
    const [orders, setOrders] = useState([]);
    const [filteredOrders, setFilteredOrders] = useState([]);
    const [activeTab, setActiveTab] = useState('ALL'); // ALL, PENDING, SHIPPING, COMPLETED, CANCELLED
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const toast = useToast();

    const fetchOrders = () => {
        setLoading(true);
        orderService.getMyOrders()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setOrders(res.data);
                    filterOrdersList(res.data, activeTab);
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    const filterOrdersList = (list, tab) => {
        if (tab === 'ALL') {
            setFilteredOrders(list);
        } else {
            setFilteredOrders(list.filter(o => o.status === tab));
        }
    };

    useEffect(() => {
        fetchOrders();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleTabChange = (tabName) => {
        setActiveTab(tabName);
        filterOrdersList(orders, tabName);
    };

    const handleCancelOrder = async (orderId) => {
        if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này không?")) {
            try {
                const res = await orderService.cancelOrder(orderId);
                if (res && res.success) {
                    toast.success("Đã hủy đơn hàng thành công!");
                    fetchOrders();
                } else {
                    toast.error(res?.message || "Hủy đơn hàng thất bại!");
                }
            } catch (err) {
                toast.error("Lỗi: " + (err.response?.data?.message || err.message));
            }
        }
    };

    if (loading) {
        return (
            <UserLayout activeTab="orders">
                <div className="loading-center">
                    <div className="spinner spinner-lg" />
                </div>
            </UserLayout>
        );
    }

    return (
        <UserLayout activeTab="orders">
            <h3 className="user-content-title">Đơn hàng của tôi</h3>
            <p className="user-content-subtitle">Quản lý và theo dõi lịch sử mua hàng</p>

            {/* Tabs Filter */}
            <div className="order-filter-tabs">
                {[
                    { key: 'ALL', label: 'Tất cả' },
                    { key: 'PENDING', label: 'Chờ xác nhận' },
                    { key: 'SHIPPING', label: 'Đang giao' },
                    { key: 'DELIVERED', label: 'Đã giao' },
                    { key: 'COMPLETED', label: 'Hoàn thành' },
                    { key: 'CANCELLED', label: 'Đã hủy' }
                ].map(tab => (
                    <div 
                        key={tab.key}
                        className={`order-filter-tab ${activeTab === tab.key ? 'active' : ''}`}
                        onClick={() => handleTabChange(tab.key)}
                    >
                        {tab.label}
                    </div>
                ))}
            </div>

            {error && (
                <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3) var(--space-4)', marginBottom: 'var(--space-4)', display: 'flex', gap: 6, alignItems: 'center' }}>
                    <IconWarning size={14} /> Lỗi tải đơn hàng: {error}
                </div>
            )}

            {filteredOrders.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon"><IconPackage /></div>
                    <h3 className="empty-state-title">Chưa có đơn hàng nào</h3>
                    <p className="empty-state-text">Bạn chưa có đơn hàng nào thuộc trạng thái này.</p>
                    <Link to="/products">
                        <button className="btn btn-primary">
                            Mua sắm ngay
                        </button>
                    </Link>
                </div>
            ) : (
                <div className="orders-list">
                    {filteredOrders.map((order) => {
                        const statusObj = getOrderStatusLabel(order.status);
                        const dateStr = new Date(order.createdAt).toLocaleDateString('vi-VN', {
                            year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
                        });

                        return (
                            <div key={order.id} className="order-group-card">
                                <div className="order-group-header">
                                    <div className="order-code-block">
                                        <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Mã đơn hàng</span>
                                        <strong style={{ fontSize: 'var(--font-size-md)', color: 'var(--color-gray-900)' }}>{order.orderCode}</strong>
                                    </div>
                                    <div className="order-meta-info">
                                        <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)', display: 'block' }}>Ngày đặt</span>
                                        <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-gray-700)' }}>{dateStr}</span>
                                    </div>
                                </div>

                                <div className="order-group-body">
                                    <div className="order-items-summary">
                                        {order.items && order.items.length > 0 ? (
                                            <div style={{ fontSize: 'var(--font-size-base)', color: 'var(--color-gray-700)' }}>
                                                {order.items[0].productName} {order.items.length > 1 && `và ${order.items.length - 1} sản phẩm khác...`}
                                            </div>
                                        ) : (
                                            <div style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-gray-400)' }}>Không tìm thấy chi tiết sản phẩm</div>
                                        )}
                                        <span className={`badge ${statusObj.className}`} style={{ marginTop: 'var(--space-2)' }}>
                                            {statusObj.label}
                                        </span>
                                    </div>

                                    {/* Action buttons and prices */}
                                    <div className="order-price-details">
                                        <div style={{ textAlign: 'right', minWidth: '150px' }}>
                                            <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)', display: 'block' }}>Tổng tiền</span>
                                            <strong style={{ fontSize: 'var(--font-size-md)', color: 'var(--color-primary)' }}>{formatPrice(order.totalPrice)}</strong>
                                        </div>

                                        <Link to={`/orders/${order.id}`}>
                                            <button className="btn btn-secondary btn-sm" style={{ display: 'flex', gap: 4 }}>
                                                <IconInfo size={14} /> Chi tiết
                                            </button>
                                        </Link>

                                        {order.status === 'PENDING' && (
                                            <button 
                                                onClick={() => handleCancelOrder(order.id)}
                                                className="btn btn-danger btn-sm"
                                                style={{ display: 'flex', gap: 4 }}
                                            >
                                                <IconTrash size={14} /> Hủy đơn
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </UserLayout>
    );
}

export default Orders;
