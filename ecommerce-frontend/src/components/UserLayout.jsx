import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { 
  IconUser, IconPackage, IconHeart, 
  IconMessage, IconEdit, IconLogout, IconMapPin 
} from '../utils/icons';
import './UserLayout.css';

function UserLayout({ children, activeTab = 'profile' }) {
  const { username, logout } = useContext(AuthContext);

  return (
    <div className="container user-layout">
      {/* 1. SIDEBAR */}
      <aside className="user-sidebar">
        {/* Profile info header */}
        <div className="user-sidebar-profile">
          <div className="user-sidebar-avatar">
            {username ? username.charAt(0).toUpperCase() : <IconUser />}
          </div>
          <div className="user-sidebar-info">
            <span className="user-sidebar-name">{username}</span>
            <Link to="/profile" className="user-sidebar-edit-link">
              <IconEdit size={10} /> Sửa hồ sơ
            </Link>
          </div>
        </div>

        {/* Navigation Menu */}
        <nav className="user-sidebar-menu">
          <Link 
            to="/profile" 
            className={`user-sidebar-menu-item ${activeTab === 'profile' ? 'active' : ''}`}
          >
            <IconUser size={18} />
            <span>Tài khoản của tôi</span>
          </Link>

          <Link 
            to="/addresses" 
            className={`user-sidebar-menu-item ${activeTab === 'addresses' ? 'active' : ''}`}
          >
            <IconMapPin size={18} />
            <span>Địa chỉ nhận hàng</span>
          </Link>
          
          <Link 
            to="/orders" 
            className={`user-sidebar-menu-item ${activeTab === 'orders' ? 'active' : ''}`}
          >
            <IconPackage size={18} />
            <span>Đơn Mua</span>
          </Link>
          
          <Link 
            to="/wishlist" 
            className={`user-sidebar-menu-item ${activeTab === 'wishlist' ? 'active' : ''}`}
          >
            <IconHeart size={18} />
            <span>Sản phẩm yêu thích</span>
          </Link>

          <Link 
            to="/returns" 
            className={`user-sidebar-menu-item ${activeTab === 'returns' ? 'active' : ''}`}
          >
            <IconPackage size={18} style={{ transform: 'rotate(180deg)' }} />
            <span>Yêu cầu trả hàng</span>
          </Link>
          
          <Link 
            to="/messages" 
            className={`user-sidebar-menu-item ${activeTab === 'messages' ? 'active' : ''}`}
          >
            <IconMessage size={18} />
            <span>Trò chuyện (Chat)</span>
          </Link>
          
          <button 
            onClick={logout} 
            className="user-sidebar-menu-item"
            style={{ width: '100%', textAlign: 'left', background: 'none', border: 'none', cursor: 'pointer' }}
          >
            <IconLogout size={18} />
            <span>Đăng xuất</span>
          </button>
        </nav>
      </aside>

      {/* 2. CONTENT AREA */}
      <main className="user-content-panel">
        {children}
      </main>
    </div>
  );
}

export default UserLayout;
