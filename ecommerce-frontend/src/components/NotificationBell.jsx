import React, { useContext, useState, useRef, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { NotificationContext } from '../context/NotificationContext';
import { IconBell } from '../utils/icons';
import { timeAgo } from '../utils/helpers';
import './NotificationBell.css';

function NotificationBell() {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useContext(NotificationContext);
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  // Close dropdown on click outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNotificationClick = async (notif) => {
    setIsOpen(false);
    if (!notif.read) {
      await markAsRead(notif.id);
    }
    if (notif.actionUrl) {
      navigate(notif.actionUrl);
    }
  };

  const handleMarkAllRead = (e) => {
    e.stopPropagation();
    markAllAsRead();
  };

  return (
    <div className="notif-bell-container" ref={dropdownRef}>
      <button 
        className="notif-bell-btn" 
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Thông báo"
      >
        <IconBell size={16} />
        <span className="notif-text">Thông Báo</span>
        {unreadCount > 0 && (
          <span className="notif-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
        )}
      </button>

      {isOpen && (
        <div className="notif-dropdown">
          <div className="notif-dropdown-header">
            <span>Thông báo mới nhận</span>
            {unreadCount > 0 && (
              <button className="notif-mark-read-all" onClick={handleMarkAllRead}>
                Đánh dấu đã đọc tất cả
              </button>
            )}
          </div>

          <div className="notif-dropdown-body">
            {notifications.length === 0 ? (
              <div className="notif-empty">Không có thông báo nào</div>
            ) : (
              notifications.slice(0, 5).map((notif) => (
                <div 
                  key={notif.id} 
                  className={`notif-item ${!notif.read ? 'notif-unread' : ''}`}
                  onClick={() => handleNotificationClick(notif)}
                >
                  {notif.imageUrl && (
                    <img src={notif.imageUrl} alt="" className="notif-img" />
                  )}
                  <div className="notif-content">
                    <div className="notif-title">{notif.title}</div>
                    <div className="notif-body-text">{notif.body}</div>
                    <div className="notif-time">{timeAgo(notif.createdAt)}</div>
                  </div>
                  {!notif.read && <span className="notif-unread-dot" />}
                </div>
              ))
            )}
          </div>

          <div className="notif-dropdown-footer">
            <Link to="/notifications" onClick={() => setIsOpen(false)}>
              Xem tất cả thông báo
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

export default NotificationBell;
