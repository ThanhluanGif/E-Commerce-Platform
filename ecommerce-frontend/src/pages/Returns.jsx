import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import UserLayout from '../components/UserLayout';
import returnService from '../services/returnService';
import { IconHeart, IconWarning } from '../utils/icons';

function Returns() {
    const [returns, setReturns] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        returnService.getMyReturns()
            .then(res => {
                if (res && res.success && Array.isArray(res.data)) {
                    setReturns(res.data);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setError('Lỗi khi tải danh sách yêu cầu hoàn hàng');
                setLoading(false);
            });
    }, []);

    const getStatusText = (status) => {
        switch (status) {
            case 'PENDING': return 'Chờ shop duyệt';
            case 'APPROVED': return 'Chờ admin hoàn tiền';
            case 'REJECTED': return 'Bị từ chối';
            case 'REFUNDED': return 'Đã hoàn tiền';
            case 'CLOSED': return 'Đã đóng';
            default: return status;
        }
    };

    const getStatusBadgeClass = (status) => {
        switch (status) {
            case 'PENDING': return 'badge-warning';
            case 'APPROVED': return 'badge-info';
            case 'REJECTED': return 'badge-danger';
            case 'REFUNDED': return 'badge-success';
            default: return 'badge-secondary';
        }
    };

    if (loading) {
        return (
            <UserLayout activeTab="returns">
                <div className="loading-center">
                    <div className="spinner spinner-lg" />
                </div>
            </UserLayout>
        );
    }

    return (
        <UserLayout activeTab="returns">
            <h3 className="user-content-title">Yêu cầu hoàn trả</h3>
            <p className="user-content-subtitle">Danh sách các đơn hàng yêu cầu trả hàng / hoàn tiền</p>

            {error && (
                <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3)', marginBottom: '15px' }}>
                    <IconWarning size={14} /> {error}
                </div>
            )}

            {returns.length === 0 ? (
                <div className="empty-state">
                    <div className="empty-state-icon"><IconHeart /></div>
                    <h3 className="empty-state-title">Chưa có yêu cầu hoàn trả nào</h3>
                    <p className="empty-state-text">Bạn chỉ có thể yêu cầu hoàn trả cho các đơn hàng đã được giao thành công.</p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    {returns.map(ret => (
                        <div key={ret.id} style={{ background: 'white', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', boxShadow: 'var(--shadow-sm)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px', flexWrap: 'wrap', gap: '10px' }}>
                                <span style={{ fontWeight: 'bold', fontSize: '15px' }}>
                                    Mã đơn hàng: <Link to={`/orders/${ret.orderId}`} style={{ color: '#3643ba', textDecoration: 'none' }}>#{ret.orderCode}</Link>
                                </span>
                                <span className={`badge ${getStatusBadgeClass(ret.status)}`}>
                                    {getStatusText(ret.status)}
                                </span>
                            </div>
                            <div style={{ fontSize: '14px', color: '#4b5563', marginBottom: '8px' }}>
                                <strong>Lý do hoàn hàng:</strong> {ret.reason}
                            </div>
                            {ret.imagesUrl && (
                                <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                                    <img src={ret.imagesUrl} alt="evidence" style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '4px', border: '1px solid #e5e7eb' }} onError={(e) => { e.target.style.display = 'none'; }} />
                                </div>
                            )}
                            <div style={{ fontSize: '12px', color: '#9ca3af' }}>
                                Gửi ngày: {new Date(ret.createdAt).toLocaleString('vi-VN')}
                            </div>
                            {ret.sellerNote && (
                                <div style={{ fontSize: '13px', color: '#d97706', marginTop: '10px', background: '#fffbeb', padding: '10px', borderRadius: '6px', borderLeft: '3px solid #d97706' }}>
                                    <strong>Phản hồi từ shop:</strong> {ret.sellerNote}
                                </div>
                            )}
                            {ret.adminNote && (
                                <div style={{ fontSize: '13px', color: '#059669', marginTop: '10px', background: '#ecfdf5', padding: '10px', borderRadius: '6px', borderLeft: '3px solid #059669' }}>
                                    <strong>Phản hồi từ Admin:</strong> {ret.adminNote}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </UserLayout>
    );
}

export default Returns;
