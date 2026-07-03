import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

function Register() {
    // 1. Khai báo các state để quản lý dữ liệu ô nhập
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [isLanguageOpen, setIsLanguageOpen] = useState(false);

    const navigate = useNavigate();

    // 2. Hàm xử lý khi bấm nút Đăng Ký
    const handleRegister = (e) => {
        e.preventDefault(); // Chặn tải lại trang

        // Cấu trúc JSON trùng khớp hoàn toàn với Backend Spring Boot
        const registerData = {
            username: username,
            password: password, // Trùng khớp RequestBody Java
            email: email,
            role: "CUSTOMER" // Mặc định gán quyền CUSTOMER viết hoa
        };

        // 3. Gọi API Đăng ký sang Backend
        fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        })
            .then(async (res) => {
                const data = await res.json();
                if (!res.ok) {
                    throw new Error(data.message || 'Đăng ký thất bại!');
                }
                return data;
            })
            .then((data) => {
                setMessage('✅ Đăng ký tài khoản thành công!');
                const luonMuonChuyenTrang = window.confirm('Đăng ký thành công! Bạn có muốn chuyển sang trang Đăng nhập ngay không?');

                if (luonMuonChuyenTrang) {
                    navigate('/login');
                } else {
                    setUsername('');
                    setPassword('');
                    setEmail('');
                }
            })
            .catch((err) => {
                setMessage(`❌ Lỗi: ${err.message}`);
            });
    };

    return (
        <>
            <div style={{ fontFamily: 'Arial, sans-serif', maxWidth: '450px', margin: '40px auto', padding: '20px', color: '#1a1a1a' }}>

                <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '20px' }}>Tạo tài khoản</h2>

                {/* Form Đăng ký */}
                <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', fontWeight: '500' }}>Tên đăng nhập</label>
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
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', fontWeight: '500' }}>Địa chỉ Email</label>
                        <input
                            type="email"
                            placeholder="Email address"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            style={{ width: '100%', padding: '12px', border: '1px solid #767676', borderRadius: '2px', fontSize: '15px', boxSizing: 'border-box' }}
                        />
                    </div>

                    <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '14px', fontWeight: '500' }}>Mật khẩu</label>
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
                        ĐĂNG KÝ NGAY
                    </button>
                </form>

                {/* Khối hiển thị thông báo kết quả */}
                {message && <p style={{ marginTop: '15px', textAlign: 'center', fontWeight: 'bold', color: message.startsWith('❌') ? 'red' : 'green' }}>{message}</p>}

                {/* Đường gạch ngang phân cách khối */}
                <div style={{ display: 'flex', alignItems: 'center', margin: '25px 0', color: '#767676', fontSize: '13px' }}>
                    <div style={{ flex: 1, height: '1px', background: '#e5e5e5' }}></div>
                    <span style={{ padding: '0 10px' }}>HOẶC ĐĂNG KÝ VỚI</span>
                    <div style={{ flex: 1, height: '1px', background: '#e5e5e5' }}></div>
                </div>

                {/* Khối Đăng ký nhanh qua Mạng xã hội */}
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

                {/* Khu vực chuyển đổi luồng tài khoản quay ngược lại Login */}
                <div style={{ marginTop: '30px', borderTop: '1px solid #e5e5e5', paddingTop: '20px' }}>
                    <p style={{ margin: '0 0 5px 0', fontSize: '14px', fontWeight: 'bold' }}>Bạn đã có tài khoản TechStore?</p>
                    <Link to="/login" className="hover-underline-link">
                        Đăng nhập ngay
                    </Link>
                </div>

                {/* Khối thông tin đặc quyền thành viên khi Đăng ký */}
                <div style={{ marginTop: '20px', fontSize: '14px', color: '#1a1a1a', lineHeight: '1.8' }}>
                    <p style={{ margin: '0 0 12px 0', fontWeight: 'bold' }}>Đặc quyền hấp dẫn dành riêng cho thành viên TechStore:</p>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>Tích điểm không giới hạn, quy đổi voucher mua sắm miễn phí</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>Nhận thông báo quà tặng sinh nhật và ưu đãi bí mật hàng tháng</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <span style={{ color: '#1a1a1a', fontWeight: 'bold', fontSize: '16px' }}>✓</span>
                            <span>Ưu tiên xử lý đơn hàng nhanh và miễn phí vận chuyển toàn quốc</span>
                        </div>
                    </div>
                </div>

                {/* Các liên kết hỗ trợ */}
                <div style={{ display: 'flex', gap: '20px', marginTop: '35px', marginBottom: '20px', fontSize: '14px', fontWeight: '500' }}>
                    <a href="#support" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Hỗ trợ</a>
                    <a href="#privacy" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Bảo mật</a>
                    <a href="#about" style={{ color: '#1a1a1a', textDecoration: 'none' }}>Về TechStore</a>
                </div>

                {/* NÚT CHỌN NGÔN NGỮ */}
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

            {/* MODAL CHỌN NGÔN NGỮ */}
            {isLanguageOpen && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex',
                    justifyContent: 'center', alignItems: 'center', zIndex: 9999
                }}>
                    <div style={{
                        background: 'white', width: '400px', padding: '25px',
                        borderRadius: '4px', boxShadow: '0 4px 20px rgba(0,0,0,0.15)', position: 'relative'
                    }}>
                        <button
                            type="button"
                            onClick={() => setIsLanguageOpen(false)}
                            style={{
                                position: 'absolute', top: '20px', left: '20px', background: 'none',
                                border: 'none', fontSize: '22px', cursor: 'pointer', fontWeight: '300', color: '#1a1a1a'
                            }}
                        >
                            ✕
                        </button>

                        <div style={{ marginTop: '50px', display: 'flex', flexDirection: 'column' }}>
                            <div
                                onClick={() => { alert('Đã chọn Tiếng Việt'); setIsLanguageOpen(false); }}
                                style={{
                                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                                    padding: '15px 10px', backgroundColor: '#eefaf5', cursor: 'pointer', borderRadius: '2px'
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
                                style={{ display: 'flex', alignItems: 'center', padding: '15px 10px', cursor: 'pointer', gap: '15px' }}
                            >
                                <img src="https://flagcdn.com/w20/us.png" alt="US" style={{ width: '24px' }} />
                                <span style={{ fontSize: '15px', fontWeight: '400' }}>United States (English)</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

export default Register;