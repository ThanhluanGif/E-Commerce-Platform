import React, { useState, useEffect } from 'react';
import userService from '../services/userService';
import api from '../services/api';
import UserLayout from '../components/UserLayout';
import { useToast } from '../utils/toast';
import { getProductImage } from '../utils/helpers';
import { IconUpload, IconLock, IconCheck, IconWarning } from '../utils/icons';
import './Profile.css';

function Profile() {
    const toast = useToast();
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
                toast.success("Tải ảnh đại diện lên thành công! Đừng quên lưu lại thông tin cá nhân.");
            }
        } catch (err) {
            console.error("Avatar upload error:", err);
            toast.error("Upload ảnh thất bại: " + (err.response?.data?.message || err.message));
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
                toast.success("Cập nhật thông tin cá nhân thành công!");
                setProfileSuccessMsg("Cập nhật thông tin cá nhân thành công!");
            }
        } catch (err) {
            setError(err.response?.data?.message || err.message);
            toast.error(err.response?.data?.message || err.message);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setPasswordError(null);
        setPasswordSuccessMsg(null);

        if (newPassword !== confirmPassword) {
            setPasswordError("Xác nhận mật khẩu mới không khớp!");
            toast.error("Xác nhận mật khẩu mới không khớp!");
            return;
        }

        try {
            const res = await userService.changePassword({
                oldPassword,
                newPassword
            });
            if (res && res.success) {
                toast.success("Thay đổi mật khẩu thành công!");
                setPasswordSuccessMsg("Thay đổi mật khẩu thành công!");
                setOldPassword('');
                setNewPassword('');
                setConfirmPassword('');
            }
        } catch (err) {
            setPasswordError(err.response?.data?.message || err.message);
            toast.error(err.response?.data?.message || err.message);
        }
    };

    if (loading) {
        return (
            <UserLayout activeTab="profile">
                <div className="loading-center">
                    <div className="spinner spinner-lg" />
                </div>
            </UserLayout>
        );
    }

    return (
        <UserLayout activeTab="profile">
            <div className="profile-layout">
                {/* 1. THÔNG TIN CÁ NHÂN */}
                <div className="profile-card">
                    <div className="profile-title-bar">
                        <h3 className="user-content-title" style={{ margin: 0 }}>Hồ sơ của tôi</h3>
                        {!isEditMode && (
                            <button 
                                onClick={() => setIsEditMode(true)}
                                className="btn btn-primary btn-sm"
                            >
                                Chỉnh sửa
                            </button>
                        )}
                    </div>

                    <p className="user-content-subtitle" style={{ marginTop: '-15px' }}>
                        Quản lý thông tin hồ sơ để bảo mật tài khoản
                    </p>

                    {profileSuccessMsg && (
                        <div className="badge badge-success" style={{ width: '100%', padding: 'var(--space-3)', marginBottom: 'var(--space-4)', display: 'flex', gap: 6, alignItems: 'center' }}>
                            <IconCheck size={14} /> {profileSuccessMsg}
                        </div>
                    )}
                    {error && (
                        <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3)', marginBottom: 'var(--space-4)', display: 'flex', gap: 6, alignItems: 'center' }}>
                            <IconWarning size={14} /> {error}
                        </div>
                    )}

                    <form onSubmit={handleProfileSubmit}>
                        {/* Avatar upload */}
                        <div className="avatar-upload-row">
                            <div className="avatar-preview">
                                <img src={getProductImage(avatarUrl) || "https://img.icons8.com/color/96/user-male-circle.png"} alt="Avatar" onError={(e) => { e.target.src = "https://img.icons8.com/color/96/user-male-circle.png"; }} />
                            </div>
                            {isEditMode && (
                                <div className="avatar-upload-btn-col">
                                    <label className="avatar-file-label">
                                        <IconUpload size={14} style={{ marginRight: 4 }} />
                                        {uploading ? 'Đang tải...' : 'Chọn ảnh đại diện'}
                                        <input type="file" accept="image/*" onChange={handleAvatarUpload} style={{ display: 'none' }} disabled={uploading} />
                                    </label>
                                    <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Dung lượng file tối đa 1MB. Định dạng: JPG, PNG</span>
                                </div>
                            )}
                        </div>

                        {/* Text fields */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
                            <div className="form-group">
                                <label className="form-label">Tên đăng nhập</label>
                                <input type="text" className="form-input" value={user?.username || ''} disabled style={{ background: 'var(--color-gray-100)', color: 'var(--color-gray-500)' }} />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Email *</label>
                                <input 
                                    type="email" 
                                    className="form-input"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={!isEditMode} 
                                    required 
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Số điện thoại</label>
                                <input 
                                    type="text" 
                                    className="form-input"
                                    value={phone}
                                    onChange={(e) => setPhone(e.target.value)}
                                    disabled={!isEditMode} 
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Địa chỉ giao hàng mặc định</label>
                                <textarea 
                                    className="form-textarea"
                                    rows="2"
                                    value={address}
                                    onChange={(e) => setAddress(e.target.value)}
                                    disabled={!isEditMode} 
                                />
                            </div>

                            {isEditMode && (
                                <div style={{ display: 'flex', gap: '10px', marginTop: 'var(--space-2)' }}>
                                    <button type="submit" className="btn btn-primary">
                                        Lưu hồ sơ
                                    </button>
                                    <button 
                                        type="button" 
                                        onClick={() => { setIsEditMode(false); fetchUserProfile(); }} 
                                        className="btn btn-secondary"
                                    >
                                        Hủy
                                    </button>
                                </div>
                            )}
                        </div>
                    </form>
                </div>

                {/* 2. ĐỔI MẬT KHẨU */}
                <div className="password-card">
                    <h3 className="user-content-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <IconLock size={18} /> Đổi mật khẩu
                    </h3>
                    <p className="user-content-subtitle">Bảo mật tài khoản của bạn</p>

                    {passwordSuccessMsg && (
                        <div className="badge badge-success" style={{ width: '100%', padding: 'var(--space-3)', marginBottom: 'var(--space-4)', display: 'flex', gap: 6, alignItems: 'center' }}>
                            <IconCheck size={14} /> {passwordSuccessMsg}
                        </div>
                    )}
                    {passwordError && (
                        <div className="badge badge-danger" style={{ width: '100%', padding: 'var(--space-3)', marginBottom: 'var(--space-4)', display: 'flex', gap: 6, alignItems: 'center' }}>
                            <IconWarning size={14} /> {passwordError}
                        </div>
                    )}

                    <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        <div className="form-group">
                            <label className="form-label">Mật khẩu hiện tại *</label>
                            <input 
                                type="password" 
                                className="form-input"
                                value={oldPassword}
                                onChange={(e) => setOldPassword(e.target.value)}
                                required 
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Mật khẩu mới *</label>
                            <input 
                                type="password" 
                                className="form-input"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                required 
                            />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Xác nhận mật khẩu mới *</label>
                            <input 
                                type="password" 
                                className="form-input"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required 
                            />
                        </div>

                        <button type="submit" className="btn btn-primary btn-block" style={{ background: 'var(--color-gray-800)' }}>
                            Cập nhật mật khẩu
                        </button>
                    </form>
                </div>
            </div>
        </UserLayout>
    );
}

export default Profile;
