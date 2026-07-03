import React, { useState, useEffect } from 'react';
import userService from '../services/userService';
import api from '../services/api';

function Profile() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Profile form states
    const [isEditMode, setIsEditMode] = useState(false);
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [address, setAddress] = useState('');
    const [avatarUrl, setAvatarUrl] = useState('');
    const [uploading, setUploading] = useState(false);
    const [profileSuccessMsg, setProfileSuccessMsg] = useState(null);

    // Password change states
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState(null);
    const [passwordSuccessMsg, setPasswordSuccessMsg] = useState(null);

    const fetchUserProfile = () => {
        setLoading(true);
        userService.getProfile()
            .then(res => {
                if (res && res.success && res.data) {
                    const u = res.data;
                    setUser(u);
                    setEmail(u.email || '');
                    setPhone(u.phone || '');
                    setAddress(u.address || '');
                    setAvatarUrl(u.avatarUrl || '');
                }
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchUserProfile();
    }, []);

    // Handle avatar image file upload
    const handleAvatarUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);
        setUploading(true);
        setProfileSuccessMsg(null);

        try {
            const res = await api.post('/api/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            if (res.data && res.data.success) {
                setAvatarUrl(res.data.data);
                setProfileSuccessMsg("Tải ảnh đại diện lên thành công! Đừng quên lưu lại thông tin cá nhân của bạn.");
            }
        } catch (err) {
            console.error("Avatar upload error:", err);
            alert("Upload ảnh thất bại: " + (err.response?.data?.message || err.message));
        } finally {
            setUploading(false);
        }
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setProfileSuccessMsg(null);
        setError(null);

        try {
            const res = await userService.updateProfile({
                email,
                phone,
                address,
                avatarUrl
            });
            if (res && res.success && res.data) {
                setUser(res.data);
                setIsEditMode(false);
                setProfileSuccessMsg("Cập nhật thông tin cá nhân thành công!");
            }
        } catch (err) {
            setError(err.response?.data?.message || err.message);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setPasswordError(null);
        setPasswordSuccessMsg(null);

        if (newPassword !== confirmPassword) {
            setPasswordError("Xác nhận mật khẩu mới không khớp!");
            return;
        }

        try {
            const res = await userService.changePassword({
                oldPassword,
                newPassword
            });
            if (res && res.success) {
                setPasswordSuccessMsg("Thay đổi mật khẩu thành công!");
                setOldPassword('');
                setNewPassword('');
                setConfirmPassword('');
            }
        } catch (err) {
            setPasswordError(err.response?.data?.message || err.message);
        }
    };

    if (loading) return <div style={{ padding: '40px', textAlign: 'center', fontSize: '18px', color: '#6b7280' }}>⏳ Đang tải thông tin tài khoản...</div>;
    if (error && !user) return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center', fontSize: '18px' }}>❌ Lỗi: {error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '1000px', margin: '0 auto', fontFamily: 'system-ui, -apple-system, sans-serif' }}>
            <h2 style={{ fontSize: '28px', fontWeight: '800', color: '#1a1a1a', marginBottom: '25px', borderBottom: '2px solid #f3f4f6', paddingBottom: '10px' }}>
                Quản Lý Tài Khoản
            </h2>

            <div style={{ display: 'flex', gap: '40px', flexWrap: 'wrap' }}>
                {/* 1. THÔNG TIN CÁ NHÂN (Bên trái) */}
                <div style={{ flex: '2 1 500px', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '25px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', margin: 0 }}>Thông tin cá nhân</h3>
                        {!isEditMode && (
                            <button 
                                onClick={() => setIsEditMode(true)}
                                style={{ padding: '6px 15px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' }}
                            >
                                Chỉnh sửa
                            </button>
                        )}
                    </div>

                    {profileSuccessMsg && (
                        <div style={{ padding: '12px', background: '#ecfdf5', border: '1px solid #a7f3d0', color: '#047857', borderRadius: '6px', marginBottom: '20px', fontSize: '14px' }}>
                            ✓ {profileSuccessMsg}
                        </div>
                    )}
                    {error && (
                        <div style={{ padding: '12px', background: '#fef2f2', border: '1px solid #fca5a5', color: '#b91c1c', borderRadius: '6px', marginBottom: '20px', fontSize: '14px' }}>
                            ⚠️ {error}
                        </div>
                    )}

                    {/* Profile Form */}
                    <form onSubmit={handleProfileSubmit}>
                        <div style={{ display: 'flex', gap: '20px', alignItems: 'center', marginBottom: '25px', flexWrap: 'wrap' }}>
                            <div style={{ width: '90px', height: '90px', borderRadius: '50%', overflow: 'hidden', background: '#f3f4f6', border: '1px solid #e5e7eb', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <img src={avatarUrl || "https://img.icons8.com/color/96/user-male-circle.png"} alt="Avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            </div>
                            {isEditMode && (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                                    <label style={{ cursor: 'pointer', background: 'white', border: '1px solid #d1d5db', padding: '6px 15px', borderRadius: '4px', fontSize: '13px', fontWeight: '600', color: '#374151' }}>
                                        {uploading ? 'Đang tải lên...' : 'Chọn ảnh mới'}
                                        <input type="file" accept="image/*" onChange={handleAvatarUpload} style={{ display: 'none' }} disabled={uploading} />
                                    </label>
                                    <span style={{ fontSize: '11px', color: '#6b7280' }}>Chấp nhận các file định dạng ảnh</span>
                                </div>
                            )}
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            <div>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Tên đăng nhập</label>
                                <input type="text" value={user?.username || ''} disabled style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #e5e7eb', background: '#f9fafb', color: '#6b7280', fontSize: '14px' }} />
                            </div>

                            <div>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Email *</label>
                                <input 
                                    type="email" 
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={!isEditMode} 
                                    style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', background: isEditMode ? 'white' : '#f9fafb', fontSize: '14px', boxSizing: 'border-box' }}
                                    required 
                                />
                            </div>

                            <div>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Số điện thoại</label>
                                <input 
                                    type="text" 
                                    value={phone}
                                    onChange={(e) => setPhone(e.target.value)}
                                    disabled={!isEditMode} 
                                    style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', background: isEditMode ? 'white' : '#f9fafb', fontSize: '14px', boxSizing: 'border-box' }}
                                />
                            </div>

                            <div>
                                <label style={{ display: 'block', fontSize: '14px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Địa chỉ giao hàng mặc định</label>
                                <textarea 
                                    rows="2"
                                    value={address}
                                    onChange={(e) => setAddress(e.target.value)}
                                    disabled={!isEditMode} 
                                    style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', background: isEditMode ? 'white' : '#f9fafb', fontSize: '14px', boxSizing: 'border-box' }}
                                />
                            </div>

                            {isEditMode && (
                                <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                                    <button type="submit" style={{ padding: '10px 20px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' }}>
                                        Lưu thay đổi
                                    </button>
                                    <button 
                                        type="button" 
                                        onClick={() => { setIsEditMode(false); fetchUserProfile(); }} 
                                        style={{ padding: '10px 20px', background: 'white', border: '1px solid #d1d5db', color: '#374151', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold' }}
                                    >
                                        Hủy
                                    </button>
                                </div>
                            )}
                        </div>
                    </form>
                </div>

                {/* 2. ĐỔI MẬT KHẨU (Bên phải) */}
                <div style={{ flex: '1 1 350px', background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', padding: '25px', height: 'fit-content' }}>
                    <h3 style={{ fontSize: '18px', fontWeight: 'bold', color: '#1f2937', marginBottom: '20px', borderBottom: '1px solid #f3f4f6', paddingBottom: '10px' }}>
                        Đổi mật khẩu
                    </h3>

                    {passwordSuccessMsg && (
                        <div style={{ padding: '10px', background: '#ecfdf5', border: '1px solid #a7f3d0', color: '#047857', borderRadius: '6px', marginBottom: '15px', fontSize: '13px' }}>
                            ✓ {passwordSuccessMsg}
                        </div>
                    )}
                    {passwordError && (
                        <div style={{ padding: '10px', background: '#fef2f2', border: '1px solid #fca5a5', color: '#b91c1c', borderRadius: '6px', marginBottom: '15px', fontSize: '13px' }}>
                            ⚠️ {passwordError}
                        </div>
                    )}

                    <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        <div>
                            <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Mật khẩu hiện tại *</label>
                            <input 
                                type="password" 
                                value={oldPassword}
                                onChange={(e) => setOldPassword(e.target.value)}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', boxSizing: 'border-box' }}
                                required 
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Mật khẩu mới *</label>
                            <input 
                                type="password" 
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', boxSizing: 'border-box' }}
                                required 
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', fontSize: '13px', fontWeight: '600', color: '#4b5563', marginBottom: '5px' }}>Xác nhận mật khẩu mới *</label>
                            <input 
                                type="password" 
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #d1d5db', fontSize: '14px', boxSizing: 'border-box' }}
                                required 
                            />
                        </div>

                        <button type="submit" style={{ padding: '12px 0', background: '#2c3e50', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', fontWeight: 'bold', transition: 'background 0.2s' }}
                                onMouseEnter={(e) => e.target.style.background = '#1a252f'}
                                onMouseLeave={(e) => e.target.style.background = '#2c3e50'}>
                            Cập nhật mật khẩu
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default Profile;
