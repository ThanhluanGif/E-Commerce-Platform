import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import orderService from '../services/orderService';

function Orders() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchOrders = () => {
        setLoading(true);
        orderService.getMyOrders()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setOrders(res.data);
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const handleCancelOrder = async (orderId) => {
        if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này không?")) {
            try {
                const res = await orderService.cancelOrder(orderId);
                if (res && res.success) {
                    alert("Đã hủy đơn hàng thành công!");
                    fetchOrders(); // Reload orders list
                } else {
                    alert(res?.message || "Hủy đơn hàng thất bại!");
                }
            } catch (err) {
                alert("Lỗi: " + (err.response?.data?.message || err.message));
            }
        }
    };

    const getStatusStyle = (status) => {
        switch (status) {
            case 'PENDING':
                return { background: '#fef3c7', color: '#d97706', text: 'Chờ duyệt' };
            case 'SHIPPING':
                return { background: '#dbeafe', color: '#2563eb', text: 'Đang giao' };
            case 'DELIVERED':
                return { background: '#d1fae5', color: '#059669', text: 'Đã giao' };
            case 'CANCELLED':
                return { background: '#f3f4f6', color: '#4b5563', text: 'Đã hủy' };
            default:
                return { background: '#f3f4f6', color: '#1f2937', text: status };
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px', color: '#6b7280' }}>⏳ Đang tải lịch sử đơn hàng...</div>;
    if (error) return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center', fontSize: '18px' }}>❌ Lỗi: {error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1000px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Đơn Hàng Của Bạn
            </h2>

            {orders.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '50px 20px', background: '#f9fafb', borderRadius: '8px', border: '1px dashed #d1d5db' }}>
                    <p style={{ fontSize: '16px', color: '#6b7280', margin: 0 }}>Bạn chưa thực hiện bất kỳ đơn đặt hàng nào.</p>
                    <Link to="/products" style={{ textDecoration: 'none', display: 'inline-block', marginTop: '15px' }}>
                        <button style={{ padding: '10px 20px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                            Mua sắm ngay
                        </button>
                    </Link>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {orders.map((order) => {
                        const statusStyle = getStatusStyle(order.status);
                        const dateStr = new Date(order.createdAt).toLocaleDateString('vi-VN', {
                            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
                        });

                        return (
                            <div key={order.id} style={{ border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', background: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,0.02)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '15px', borderBottom: '1px solid #f3f4f6', paddingBottom: '15px', marginBottom: '15px' }}>
                                    <div>
                                        <span style={{ fontSize: '12px', color: '#9ca3af', display: 'block' }}>Mã đơn hàng</span>
                                        <strong style={{ fontSize: '16px', color: '#111827' }}>{order.orderCode}</strong>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <span style={{ fontSize: '12px', color: '#9ca3af', display: 'block' }}>Ngày đặt</span>
                                        <span style={{ fontSize: '14px', color: '#4b5563' }}>{dateStr}</span>
                                    </div>
                                </div>

                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px' }}>
                                    {/* Order Items description summary */}
                                    <div style={{ flex: '1' }}>
                                        {order.items && order.items.length > 0 ? (
                                            <div style={{ fontSize: '14px', color: '#4b5563' }}>
                                                {order.items[0].productName} {order.items.length > 1 && `và ${order.items.length - 1} sản phẩm khác...`}
                                            </div>
                                        ) : (
                                            <div style={{ fontSize: '14px', color: '#9ca3af' }}>Không tìm thấy chi tiết sản phẩm</div>
                                        )}
                                        <div style={{ display: 'inline-block', marginTop: '10px', padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold', background: statusStyle.background, color: statusStyle.color }}>
                                            {statusStyle.text}
                                        </div>
                                    </div>

                                    {/* Action buttons and prices */}
                                    <div style={{ display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
                                        <div style={{ textAlign: 'right', minWidth: '150px' }}>
                                            <span style={{ fontSize: '12px', color: '#9ca3af', display: 'block' }}>Tổng tiền</span>
                                            <strong style={{ fontSize: '18px', color: '#ef4444' }}>{order.totalPrice.toLocaleString('vi-VN')} đ</strong>
                                        </div>

                                        <Link to={`/orders/${order.id}`} style={{ textDecoration: 'none' }}>
                                            <button style={{ padding: '8px 16px', background: 'white', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '13px', fontWeight: '600', cursor: 'pointer', color: '#374151' }}>
                                                Chi tiết
                                            </button>
                                        </Link>

                                        {order.status === 'PENDING' && (
                                            <button 
                                                onClick={() => handleCancelOrder(order.id)}
                                                style={{ padding: '8px 16px', background: '#fef2f2', border: '1px solid #fee2e2', borderRadius: '4px', fontSize: '13px', fontWeight: '600', cursor: 'pointer', color: '#ef4444' }}
                                            >
                                                Hủy đơn
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default Orders;
