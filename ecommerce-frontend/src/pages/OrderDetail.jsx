import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import orderService from '../services/orderService';
import returnService from '../services/returnService';

function OrderDetail() {
    const { id } = useParams();
    
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Return Request states
    const [returnRequest, setReturnRequest] = useState(null);
    const [showReturnModal, setShowReturnModal] = useState(false);
    const [returnReason, setReturnReason] = useState('');
    const [returnImages, setReturnImages] = useState('');

    const fetchOrderDetails = () => {
        setLoading(true);
        orderService.getOrderDetails(id)
            .then(res => {
                if (res && res.success && res.data) {
                    setOrder(res.data);
                    
                    // Fetch return request if order is delivered
                    if (res.data.status === 'DELIVERED') {
                        returnService.getMyReturns()
                            .then(retRes => {
                                if (retRes && retRes.success && Array.isArray(retRes.data)) {
                                    const match = retRes.data.find(r => r.orderId === res.data.id);
                                    if (match) setReturnRequest(match);
                                }
                            })
                            .catch(err => console.error("Error fetching returns:", err));
                    }
                } else {
                    setError("Không tìm thấy đơn hàng.");
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchOrderDetails();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const handleCancelOrder = async () => {
        if (window.confirm("Bạn có chắc chắn muốn hủy đơn hàng này không?")) {
            try {
                const res = await orderService.cancelOrder(order.id);
                if (res && res.success) {
                    alert("Hủy đơn hàng thành công!");
                    fetchOrderDetails(); // Refresh order details
                } else {
                    alert(res?.message || "Hủy đơn hàng thất bại!");
                }
            } catch (err) {
                alert("Lỗi: " + (err.response?.data?.message || err.message));
            }
        }
    };

    const handleReturnSubmit = async (e) => {
        e.preventDefault();
        if (!returnReason.trim()) {
            alert("Vui lòng nhập lý do hoàn hàng!");
            return;
        }

        try {
            const res = await returnService.createReturnRequest(order.id, {
                reason: returnReason,
                imagesUrl: returnImages
            });
            if (res && res.success) {
                alert("Gửi yêu cầu hoàn hàng thành công!");
                setShowReturnModal(false);
                setReturnReason('');
                setReturnImages('');
                fetchOrderDetails(); // Reload details
            } else {
                alert(res?.message || "Gửi yêu cầu thất bại!");
            }
        } catch (err) {
            alert("Lỗi: " + (err.response?.data?.message || err.message));
        }
    };

    const getStatusStyle = (status) => {
        switch (status) {
            case 'PENDING':
                return { background: '#fef3c7', color: '#d97706', text: 'Chờ xử lý (PENDING)' };
            case 'SHIPPING':
                return { background: '#dbeafe', color: '#2563eb', text: 'Đang giao hàng (SHIPPING)' };
            case 'DELIVERED':
                return { background: '#d1fae5', color: '#059669', text: 'Đã giao hàng thành công (DELIVERED)' };
            case 'CANCELLED':
                return { background: '#f3f4f6', color: '#4b5563', text: 'Đã hủy đơn (CANCELLED)' };
            default:
                return { background: '#f3f4f6', color: '#1f2937', text: status };
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px', color: '#6b7280' }}>⏳ Đang tải chi tiết đơn hàng...</div>;
    if (error) return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center', fontSize: '18px' }}>❌ Lỗi: {error}</div>;
    if (!order) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px' }}>Không tìm thấy đơn hàng.</div>;

    const statusStyle = getStatusStyle(order.status);
    const dateStr = new Date(order.createdAt).toLocaleString('vi-VN', {
        year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });

    return (
        <div style={{ padding: '20px', maxWidth: '900px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <Link to="/orders" style={{ textDecoration: 'none', color: '#3643ba', fontWeight: '600', display: 'inline-block', marginBottom: '20px' }}>
                &larr; Quay lại danh sách đơn hàng
            </Link>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '15px' }}>
                <div>
                    <h2 style={{ fontSize: '24px', fontWeight: '800', color: '#111827', margin: 0 }}>Chi tiết đơn hàng: {order.orderCode}</h2>
                    <span style={{ fontSize: '14px', color: '#6b7280' }}>Đặt ngày: {dateStr}</span>
                </div>
                <div style={{ padding: '6px 16px', borderRadius: '20px', fontSize: '14px', fontWeight: 'bold', background: statusStyle.background, color: statusStyle.color }}>
                    {statusStyle.text}
                </div>
            </div>

            <div style={{ display: 'flex', gap: '30px', flexWrap: 'wrap', marginBottom: '30px' }}>
                {/* Product List in Order */}
                <div style={{ flex: '2 1 500px', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '15px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' }}>
                        Danh sách sản phẩm
                    </h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {order.items && order.items.map((item) => (
                            <div key={item.id} style={{ display: 'flex', gap: '15px', alignItems: 'center', borderBottom: '1px dashed #f3f4f6', paddingBottom: '15px' }}>
                                <div style={{ width: '60px', height: '60px', background: '#f9fafb', borderRadius: '4px', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                                    <img src={item.productImageUrl || "https://via.placeholder.com/60"} alt={item.productName} style={{ maxHeight: '100%', maxWidth: '100%', objectFit: 'contain' }} />
                                </div>
                                <div style={{ flex: 1 }}>
                                    <strong style={{ fontSize: '14px', color: '#1f2937', display: 'block' }}>{item.productName}</strong>
                                    <span style={{ fontSize: '13px', color: '#6b7280' }}>
                                        {item.priceAtPurchase.toLocaleString('vi-VN')} đ x {item.quantity}
                                    </span>
                                </div>
                                <div style={{ fontWeight: 'bold', color: '#111827', fontSize: '15px' }}>
                                    {(item.priceAtPurchase * item.quantity).toLocaleString('vi-VN')} đ
                                </div>
                            </div>
                        ))}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px', fontSize: '16px', fontWeight: 'bold' }}>
                        <span>Tổng tiền đơn hàng:</span>
                        <span style={{ color: '#ef4444', fontSize: '18px' }}>{order.totalPrice.toLocaleString('vi-VN')} đ</span>
                    </div>
                </div>

                {/* Delivery and payment details */}
                <div style={{ flex: '1 1 280px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#1f2937', marginBottom: '12px' }}>Địa chỉ nhận hàng</h3>
                        <p style={{ fontSize: '14px', color: '#4b5563', margin: 0, lineHeight: '1.6' }}>{order.shippingAddress}</p>
                    </div>

                    <div style={{ background: '#f9fafb', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#1f2937', marginBottom: '12px' }}>Phương thức thanh toán</h3>
                        <p style={{ fontSize: '14px', color: '#4b5563', margin: 0, fontWeight: 'bold' }}>
                            {order.paymentMethod === 'COD' ? 'Thanh toán COD' : order.paymentMethod}
                        </p>
                    </div>

                    {order.status === 'PENDING' && (
                        <button 
                            onClick={handleCancelOrder}
                            style={{ width: '100%', padding: '12px 0', background: '#ef4444', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.2s' }}
                            onMouseEnter={(e) => e.target.style.background = '#dc2626'}
                            onMouseLeave={(e) => e.target.style.background = '#ef4444'}
                        >
                            HỦY ĐƠN HÀNG NÀY
                        </button>
                    )}

                    {order.status === 'DELIVERED' && !returnRequest && (
                        <button 
                            onClick={() => setShowReturnModal(true)}
                            style={{ width: '100%', padding: '12px 0', background: 'var(--color-primary, #3643ba)', color: 'white', border: 'none', borderRadius: '6px', fontSize: '15px', fontWeight: 'bold', cursor: 'pointer', transition: 'opacity 0.2s' }}
                            onMouseEnter={(e) => e.target.style.opacity = '0.9'}
                            onMouseLeave={(e) => e.target.style.opacity = '1'}
                        >
                            YÊU CẦU TRẢ HÀNG/HOÀN TIỀN
                        </button>
                    )}

                    {returnRequest && (
                        <div style={{ background: '#fffbeb', border: '1px solid #fef3c7', borderRadius: '8px', padding: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            <h3 style={{ fontSize: '16px', fontWeight: 'bold', color: '#b45309', margin: 0 }}>Yêu cầu Trả hàng/Hoàn tiền</h3>
                            <div style={{ fontSize: '14px', color: '#4b5563' }}>
                                <strong>Trạng thái: </strong>
                                <span style={{ fontWeight: 'bold', color: '#b45309' }}>
                                    {returnRequest.status === 'PENDING' && 'Chờ người bán duyệt'}
                                    {returnRequest.status === 'APPROVED' && 'Người bán chấp nhận - Chờ admin hoàn tiền'}
                                    {returnRequest.status === 'REJECTED' && 'Bị từ chối'}
                                    {returnRequest.status === 'REFUNDED' && 'Đã hoàn tiền'}
                                    {returnRequest.status === 'CLOSED' && 'Đã đóng'}
                                </span>
                            </div>
                            <div style={{ fontSize: '13px', color: '#6b7280' }}>
                                <strong>Lý do: </strong> {returnRequest.reason}
                            </div>
                            {returnRequest.sellerNote && (
                                <div style={{ fontSize: '13px', color: '#6b7280', borderTop: '1px solid #fef3c7', paddingTop: '8px' }}>
                                    <strong>Phản hồi từ shop: </strong> {returnRequest.sellerNote}
                                </div>
                            )}
                            {returnRequest.adminNote && (
                                <div style={{ fontSize: '13px', color: '#6b7280', borderTop: '1px solid #fef3c7', paddingTop: '8px' }}>
                                    <strong>Phản hồi từ Admin: </strong> {returnRequest.adminNote}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            {/* Return Request Modal */}
            {showReturnModal && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ background: 'white', padding: '25px', borderRadius: '8px', maxWidth: '500px', width: '90%', boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '15px' }}>Yêu cầu trả hàng / Hoàn tiền</h3>
                        <form onSubmit={handleReturnSubmit}>
                            <div style={{ marginBottom: '15px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: 'bold', color: '#4b5563', marginBottom: '5px' }}>Lý do hoàn hàng *</label>
                                <textarea 
                                    value={returnReason} 
                                    onChange={(e) => setReturnReason(e.target.value)}
                                    placeholder="Nhập lý do hoàn hàng chi tiết (ví dụ: sản phẩm lỗi, giao sai mẫu...)" 
                                    rows="4" 
                                    style={{ width: '100%', padding: '10px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '14px', resize: 'vertical' }}
                                    required
                                />
                            </div>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: 'bold', color: '#4b5563', marginBottom: '5px' }}>Link ảnh minh chứng (tùy chọn)</label>
                                <input 
                                    type="text" 
                                    value={returnImages} 
                                    onChange={(e) => setReturnImages(e.target.value)}
                                    placeholder="URL hình ảnh sản phẩm lỗi..." 
                                    style={{ width: '100%', padding: '10px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '14px' }}
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                                <button type="button" onClick={() => setShowReturnModal(false)} className="btn btn-secondary" style={{ padding: '8px 16px', background: '#e5e7eb', color: '#1f2937', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>HỦY</button>
                                <button type="submit" className="btn btn-primary" style={{ padding: '8px 16px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>GỬI YÊU CẦU</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

export default OrderDetail;
