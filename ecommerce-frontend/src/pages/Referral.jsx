import React, { useEffect, useState } from 'react';
import referralService from '../services/referralService';
import { useToast } from '../utils/toast';

function Referral() {
    const toast = useToast();
    const [referralCode, setReferralCode] = useState('');
    const [referrals, setReferrals] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const codeRes = await referralService.getReferralCode();
                if (codeRes && codeRes.success) {
                    setReferralCode(codeRes.data.referralCode);
                }

                const referralsRes = await referralService.getMyReferrals();
                if (referralsRes && referralsRes.success) {
                    setReferrals(referralsRes.data || []);
                }
            } catch (err) {
                console.error("Lỗi khi tải dữ liệu tiếp thị liên kết: ", err);
                toast.error("Không thể tải thông tin tiếp thị liên kết.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [toast]);

    const getReferralLink = () => {
        return `${window.location.origin}/register?ref=${referralCode}`;
    };

    const handleCopyCode = () => {
        navigator.clipboard.writeText(referralCode);
        toast.success("Đã sao chép mã giới thiệu!");
    };

    const handleCopyLink = () => {
        navigator.clipboard.writeText(getReferralLink());
        toast.success("Đã sao chép đường dẫn giới thiệu!");
    };

    const formatDateTime = (dateStr) => {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    };

    if (loading) {
        return <div style={{ padding: '50px', textAlign: 'center' }}>Đang tải thông tin tiếp thị...</div>;
    }

    const completedReferrals = referrals.filter(r => r.status === 'COMPLETED').length;
    const totalPointsEarned = completedReferrals * 100;

    return (
        <div style={{ fontFamily: 'system-ui, sans-serif', maxWidth: '1000px', margin: '0 auto', padding: '20px' }}>
            <h1 style={{ fontSize: '26px', fontWeight: 'bold', marginBottom: '25px', color: '#2d3748' }}>🤝 Chương Trình Tiếp Thị Liên Kết</h1>
            
            {/* Cards Overview */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                {/* Referral Link Card */}
                <div style={{ background: 'linear-gradient(135deg, #4f46e5, #3b82f6)', borderRadius: '12px', padding: '25px', color: 'white', boxShadow: '0 4px 15px rgba(59, 130, 246, 0.2)' }}>
                    <h3 style={{ margin: '0 0 10px 0', fontSize: '18px', fontWeight: '600' }}>Mã Giới Thiệu Của Bạn</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', background: 'rgba(255, 255, 255, 0.15)', padding: '10px 15px', borderRadius: '8px', margin: '15px 0' }}>
                        <span style={{ fontSize: '24px', fontWeight: '800', letterSpacing: '1px' }}>{referralCode || 'CHƯA CÓ'}</span>
                        <button 
                            onClick={handleCopyCode}
                            style={{ marginLeft: 'auto', background: 'white', color: '#4f46e5', border: 'none', padding: '6px 12px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer', fontSize: '13px' }}
                        >
                            Sao chép
                        </button>
                    </div>
                    <p style={{ margin: '0 0 12px 0', fontSize: '13px', opacity: 0.9 }}>Chia sẻ đường dẫn dưới đây cho bạn bè đăng ký tài khoản:</p>
                    <button 
                        onClick={handleCopyLink}
                        style={{ width: '100%', padding: '10px', background: 'white', color: '#3b82f6', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer', fontSize: '14px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}
                    >
                        🔗 Sao chép Link Giới thiệu
                    </button>
                </div>

                {/* Stats Card */}
                <div style={{ background: 'white', borderRadius: '12px', padding: '25px', border: '1px solid #e2e8f0', boxShadow: '0 2px 10px rgba(0,0,0,0.02)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                    <div>
                        <h3 style={{ margin: '0 0 15px 0', fontSize: '18px', color: '#4a5568', fontWeight: '600' }}>Hiệu Quả Giới Thiệu</h3>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                            <span style={{ color: '#718096' }}>Tổng số người đã đăng ký:</span>
                            <strong style={{ color: '#2d3748', fontSize: '16px' }}>{referrals.length} thành viên</strong>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                            <span style={{ color: '#718096' }}>Mua hàng thành công (hoàn thành):</span>
                            <strong style={{ color: '#10b981', fontSize: '16px' }}>{completedReferrals} thành viên</strong>
                        </div>
                    </div>
                    <div style={{ borderTop: '1px solid #edf2f7', paddingTop: '15px', marginTop: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ color: '#4a5568', fontWeight: 'bold' }}>Loyalty Points đã tích lũy:</span>
                        <span style={{ fontSize: '20px', fontWeight: '800', color: '#ff8a00' }}>+{totalPointsEarned} đt</span>
                    </div>
                </div>
            </div>

            {/* How it works Banner */}
            <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '20px', marginBottom: '35px' }}>
                <h4 style={{ margin: '0 0 10px 0', color: '#334155', fontWeight: '600' }}>💡 Quy định nhận thưởng:</h4>
                <ol style={{ margin: 0, paddingLeft: '20px', fontSize: '13px', color: '#475569', lineHeight: '1.6' }}>
                    <li>Bạn gửi <strong>Link giới thiệu</strong> hoặc <strong>Mã giới thiệu</strong> cho người thân, bạn bè.</li>
                    <li>Họ đăng ký tài khoản thành công tại E-Shop và mua sắm đơn hàng đầu tiên.</li>
                    <li>Ngay khi đơn hàng của họ chuyển sang trạng thái <strong>Đã giao hàng thành công (DELIVERED)</strong>, bạn sẽ nhận được <strong>100 điểm thưởng Loyalty</strong> cộng thẳng vào ví.</li>
                </ol>
            </div>

            {/* Table Referrals List */}
            <div style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden', boxShadow: '0 2px 10px rgba(0,0,0,0.02)' }}>
                <h3 style={{ margin: 0, padding: '20px', fontSize: '16px', fontWeight: 'bold', borderBottom: '1px solid #edf2f7', background: '#f8fafc', color: '#2d3748' }}>📋 Bạn bè đã giới thiệu</h3>
                {referrals.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#a0aec0' }}>
                        Bạn chưa giới thiệu bạn bè nào đăng ký tài khoản. Hãy bắt đầu chia sẻ link để nhận điểm thưởng nhé!
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                                <tr style={{ background: '#f8fafc', borderBottom: '1px solid #edf2f7' }}>
                                    <th style={{ padding: '15px 20px', fontSize: '13px', color: '#718096', fontWeight: 'bold' }}>Tài khoản bạn bè</th>
                                    <th style={{ padding: '15px', fontSize: '13px', color: '#718096', fontWeight: 'bold' }}>Email</th>
                                    <th style={{ padding: '15px', fontSize: '13px', color: '#718096', fontWeight: 'bold' }}>Ngày tham gia</th>
                                    <th style={{ padding: '15px', fontSize: '13px', color: '#718096', fontWeight: 'bold' }}>Trạng thái đơn hàng đầu</th>
                                    <th style={{ padding: '15px 20px', fontSize: '13px', color: '#718096', fontWeight: 'bold', textAlign: 'right' }}>Điểm thưởng nhận được</th>
                                </tr>
                            </thead>
                            <tbody>
                                {referrals.map((ref) => (
                                    <tr key={ref.id} style={{ borderBottom: '1px solid #edf2f7', transition: 'background-color 0.1s' }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#fcfdfe'} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                                        <td style={{ padding: '15px 20px', fontSize: '14px', color: '#2d3748', fontWeight: '500' }}>{ref.refereeUsername}</td>
                                        <td style={{ padding: '15px', fontSize: '14px', color: '#4a5568' }}>{ref.refereeEmail}</td>
                                        <td style={{ padding: '15px', fontSize: '14px', color: '#718096' }}>{formatDateTime(ref.createdAt)}</td>
                                        <td style={{ padding: '15px', fontSize: '14px' }}>
                                            <span style={{ 
                                                display: 'inline-block',
                                                padding: '4px 10px',
                                                borderRadius: '20px',
                                                fontSize: '12px',
                                                fontWeight: 'bold',
                                                backgroundColor: ref.status === 'COMPLETED' ? '#e6fffa' : '#fffaf0',
                                                color: ref.status === 'COMPLETED' ? '#00c38c' : '#dd6b20'
                                            }}>
                                                {ref.status === 'COMPLETED' ? 'Đã hoàn thành' : 'Chờ mua hàng'}
                                            </span>
                                        </td>
                                        <td style={{ padding: '15px 20px', fontSize: '14px', fontWeight: 'bold', color: ref.rewarded ? '#10b981' : '#718096', textAlign: 'right' }}>
                                            {ref.rewarded ? '+100 đt' : '0 đt'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Referral;
