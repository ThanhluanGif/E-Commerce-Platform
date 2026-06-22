import React, { useState } from "react";
import { Link, useNavigate } from 'react-router-dom';

function Login() {
    // 1. Khai báo state để quản lý dữ liệu ô nhập (Form Input)
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [isLanguageOpen, setIsLanguageOpen] = useState(false); // Đã thêm biến này để tránh lỗi chưa định nghĩa
    const navigate = useNavigate();

    // 2. Hàm xử lý khi bấm nút Đăng Nhập
    const handleLogin = (e) => {
        e.preventDefault(); // Ngăn trang web tải lại mặc định của thẻ <form>

        const loginData = {
            username: username,
            password: password
        };

        // 3. Gọi API Login sang Backend Spring Boot
        fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Sai tài khoản hoặc mật khẩu.');
                }
                return res.json();
            })
            .then((data) => {
                if (data.token) {
                    localStorage.setItem('jwtToken', data.token);
                    setMessage('Đăng nhập thành công!');
                    setTimeout(() => navigate('/'), 1000);
                }
            })
            .catch((err) => {
                setMessage(`❌ Lỗi: ${err.message}`);
            });
    };

    return (
        <> {/* Thẻ bọc ngoài cùng bắt buộc của React */}
            <div style={{ fontFamily: 'Arial, sans-serif', maxWidth: '450px', margin: '40px auto', padding: '20px', color: '#1a1a1a' }}>

                {/* Nút Quay lại phía trên góc trái */}
                <div style={{ marginBottom: '20px' }}>
                    <Link to="/" style={{ textDecoration: 'none', color: '#1a1a1a', display: 'flex', alignItems: 'center', gap: '5px', fontSize: '14px' }}>
                        🏠 Quay lại
                    </Link>
                </div>

                {/* Logo Thương hiệu */}
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <h1 style={{ color: '#3643ba', fontStyle: 'italic', letterSpacing: '1px', margin: 0, fontSize: '32px', fontWeight: 'bold' }}>
                        TECHSTORE
                    </h1>
                </div>

                <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '20px' }}>Đăng nhập</h2>

                {/* Form Đăng nhập chính */}
                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', fontWeight: '500' }}>Nhập tên đăng nhập</label>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            style={{ width: '100%', padding: '12px', border: '1px solid #767676', borderRadius: '2px', fontSize: '15px', boxSizing: 'border-box' }}
                        />
                    </div>

                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', fontWeight: '500' }}>Nhập mật khẩu</label>
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            style={{ width: '100%', padding: '12px', border: '1px solid #767676', borderRadius: '2px', fontSize: '15px', boxSizing: 'border-box' }}
                        />
                    </div>

                    <button type="submit" style={{ width: '100%', padding: '14px', background: '#3643ba', color: 'white', border: 'none', borderRadius: '2px', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer', marginTop: '10px' }}>
                        TIẾP TỤC
                    </button>
                </form>

                {message && <p style={{ marginTop: '15px', textAlign: 'center', fontWeight: 'bold' }}>{message}</p>}

                {/* Đường gạch ngang phân cách khối */}
                <div style={{ display: 'flex', alignItems: 'center', margin: '25px 0', color: '#767676', fontSize: '13px' }}>
                    <div style={{ flex: 1, height: '1px', background: '#e5e5e5' }}></div>
                    <span style={{ padding: '0 10px' }}>HOẶC</span>
                    <div style={{ flex: 1, height: '1px', background: '#e5e5e5' }}></div>
                </div>

                {/* Khối Đăng nhập Mạng xã hội */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    <button type="button" style={{ width: '100%', padding: '12px', background: 'white', border: '1px solid #ccd0d5', borderRadius: '2px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', fontSize: '14px', fontWeight: '500' }}>
                        <span style={{ color: '#1877f2', fontSize: '18px', fontWeight: 'bold' }}>f</span> Continue with Facebook
                    </button>
                    <button type="button" style={{ width: '100%', padding: '12px', background: 'white', border: '1px solid #ccd0d5', borderRadius: '2px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', fontSize: '14px', fontWeight: '500' }}>
                        <span style={{ color: '#ea4335', fontSize: '16px', fontWeight: 'bold' }}>G</span> Continue with Google
                    </button>
                    <button type="button" style={{ width: '100%', padding: '12px', background: 'white', border: '1px solid #ccd0d5', borderRadius: '2px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', fontSize: '14px', fontWeight: '500' }}>
                        <span style={{ color: 'black', fontSize: '16px', fontWeight: 'bold' }}></span> Continue with Apple
                    </button>
                </div>

                {/* Khu vực chuyển đổi luồng tài khoản */}
                <div style={{ marginTop: '30px', borderTop: '1px solid #e5e5e5', paddingTop: '20px' }}>
                    <p style={{ margin: '0 0 5px 0', fontSize: '14px', fontWeight: 'bold' }}>Bạn chưa có tài khoản TechStore? Đăng ký ngay!</p>
                    <Link to="/register" style={{ color: '#3643ba', textDecoration: 'none', fontSize: '14px', fontWeight: '500' }}>
                        Tạo tài khoản
                    </Link>
                </div>

                {/* Khối thông tin cam kết và tích điểm */}
                <div style={{ marginTop: '20px', fontSize: '14px', color: '#1a1a1a', lineHeight: '1.8' }}>
                    <p style={{ margin: '0 0 12px 0', fontWeight: 'bold' }}>Đăng nhập để luôn nắm bắt thông tin mới nhất từ TechStore</p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>Tham gia Chương trình tích điểm miễn phí</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>Chương trình giảm giá và ưu đãi độc quyền</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>365 ngày đổi trả miễn phí với sản phẩm TechStore.</span>
                        </div>
                    </div>
                </div>

                {/* Các liên kết hỗ trợ */}
                <div style={{ display: 'flex', gap: '20px', marginTop: '35px', marginBottom: '20px', fontSize: '14px', fontWeight: '500' }}>
                    <a href="#support" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Hỗ trợ</a>
                    <a href="#privacy" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Bảo mật</a>
                    <a href="#about" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Về TechStore</a>
                </div>

                {/* NÚT BẤM HIỂN THỊ CHÍNH CHỌN NGÔN NGỮ */}
                <div
                    onClick={() => setIsLanguageOpen(true)}
                    style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '30px', cursor: 'pointer', width: 'fit-content' }}
                >
                    <img
                        src="https://flagcdn.com/w20/vn.png"
                        alt="Vietnam Flag"
                        style={{ width: '20px', height: 'auto', border: '1px solid #e5e5e5' }}
                    />
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>Tiếng Việt (Việt Nam)</span>
                    <span style={{ fontSize: '10px', color: '#767676' }}>▼</span>
                </div>

                {/* Bản quyền reCaptcha bảo mật */}
                <div style={{ fontSize: '12px', color: '#767676', lineHeight: '1.5', borderTop: '1px solid #e5e5e5', paddingTop: '15px' }}>
                    Trang này được bảo vệ bởi reCaptcha. <a href="#policy" style={{ color: '#767676', textDecoration: 'underline' }}>Chính Sách Bảo Mật của Google</a> áp dụng cùng <a href="#terms" style={{ color: '#767676', textDecoration: 'underline' }}>Điều khoản</a>
                </div>
            </div>

            {/* 3. BẢNG MODAL CHỌN NGÔN NGỮ */}
            {isLanguageOpen && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100vw',
                    height: '100vh',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 9999
                }}>
                    <div style={{
                        background: 'white',
                        width: '400px',
                        padding: '25px',
                        borderRadius: '4px',
                        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
                        position: 'relative'
                    }}>

                        <button
                            type="button"
                            onClick={() => setIsLanguageOpen(false)}
                            style={{
                                position: 'absolute',
                                top: '20px',
                                left: '20px',
                                background: 'none',
                                border: 'none',
                                fontSize: '22px',
                                cursor: 'pointer',
                                fontWeight: '300',
                                color: '#1a1a1a'
                            }}
                        >
                            ✕
                        </button>

                        <div style={{ marginTop: '50px', display: 'flex', flexDirection: 'column' }}>
                            <div
                                onClick={() => { alert('Đã chọn Tiếng Việt'); setIsLanguageOpen(false); }}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'space-between',
                                    padding: '15px 10px',
                                    backgroundColor: '#eefaf5',
                                    cursor: 'pointer',
                                    borderRadius: '2px'
                                }}
                            >
                                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                                    <img src="https://flagcdn.com/w20/vn.png" alt="VN" style={{ width: '24px' }} />
                                    <span style={{ fontSize: '15px', fontWeight: '500' }}>Việt Nam (Tiếng Việt)</span>
                                </div>
                                <span style={{ color: '#2ecc71', fontWeight: 'bold' }}>✓</span>
                            </div>

                            <div
                                onClick={() => { alert('Đã chọn English'); setIsLanguageOpen(false); }}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    padding: '15px 10px',
                                    cursor: 'pointer',
                                    gap: '15px'
                                }}
                            >
                                <img src="https://flagcdn.com/w20/vn.png" alt="VN" style={{ width: '24px' }} />
                                <span style={{ fontSize: '15px', fontWeight: '400' }}>Vietnam (English)</span>
                            </div>
                        </div>
                    </div>
                </div>
            )} {/* Đã sửa dấu đóng logic chuẩn xác ở đây */}
        </>
    );
}

export default Login;