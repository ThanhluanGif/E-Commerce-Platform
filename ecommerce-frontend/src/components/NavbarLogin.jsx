import React from 'react';
import { useNavigate } from 'react-router-dom'; // Dùng để chuyển trang khi click Quay lại
import { FaHome } from 'react-icons/fa'; // Hoặc icon bất kỳ bạn muốn dùng
import './NavbarLogin.css'; // File CSS riêng cho Navbar này

const NavbarLogin = ({ title }) => {
    const navigate = useNavigate();

    const handleBack = () => {
        navigate('/'); // Quay về trang chủ, hoặc navigate(-1) để quay lại trang trước đó
    };

    return (
        <nav className="navbar-login">
            {/* Khối bên trái: Nút quay lại */}
            <div className="nav-left">
                <button onClick={handleBack} className="back-button">
                    <FaHome className="back-icon" />
                    <span className="back-text">Quay lại</span>
                </button>
            </div>

            {/* Khối ở giữa: Tiêu đề động nhận từ props */}
            <div className="nav-center">
                <h1 className="navbar-title" style={{ color: '#3643ba', fontStyle: 'italic', letterSpacing: '1px', margin: 0, fontSize: '32px', fontWeight: 'bold' }}>{title}</h1>
            </div>

            {/* Khối bên phải: Giữ rỗng để căn giữa tuyệt đối */}
            <div className="nav-right"></div>
        </nav>
    );
};

export default NavbarLogin;