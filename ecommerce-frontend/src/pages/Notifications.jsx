import React, { useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { NotificationContext } from '../context/NotificationContext';
import { timeAgo } from '../utils/helpers';
import { IconBell, IconCheckCircle } from '../utils/icons';
import './Notifications.css';

function Notifications() {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useContext(NotificationContext);
  const [filter, setFilter] = useState('ALL'); // ALL, UNREAD, READ
  const navigate = useNavigate();

  const handleNotificationClick = async (notif) => {
    if (!notif.read) {
      await markAsRead(notif.id);
    }
    if (notif.actionUrl) {
      navigate(notif.actionUrl);
    }
  };

  const filteredNotifications = notifications.filter(n => {
    if (filter === 'UNREAD') return !n.read;
    if (filter === 'READ') return n.read;
    return true; // ALL
  });

  return (
    <div className="notifications-page container">
      <div className="notifications-card">
        <div className="notifications-header">
          <div className="header-title-section">
            <IconBell size={24} className="icon-orange" />
            <h2>Thông báo của tôi</h2>
            {unreadCount > 0 && (
              <span className="unread-pill">{unreadCount} chưa đọc</span>
            )}
          </div>
          {unreadCount > 0 && (
            <button className="btn-mark-all" onClick={markAllAsRead}>
              <IconCheckCircle size={16} />
              Đánh dấu tất cả đã đọc
            </button>
          )}
        </div>

        <div className="notifications-tabs">
          <button 
            className={`tab-btn ${filter === 'ALL' ? 'active' : ''}`}
            onClick={() => setFilter('ALL')}
          >
            Tất cả
          </button>
          <button 
            className={`tab-btn ${filter === 'UNREAD' ? 'active' : ''}`}
            onClick={() => setFilter('UNREAD')}
          >
            Chưa đọc
          </button>
          <button 
            className={`tab-btn ${filter === 'READ' ? 'active' : ''}`}
            onClick={() => setFilter('READ')}
          >
            Đã đọc
          </button>
        </div>

        <div className="notifications-list">
          {filteredNotifications.length === 0 ? (
            <div className="notif-page-empty">
              <IconBell size={48} className="icon-gray" />
              <p>Không có thông báo nào trong mục này</p>
            </div>
          ) : (
            filteredNotifications.map((notif) => (
              <div 
                key={notif.id}
                className={`notif-page-item ${!notif.read ? 'unread' : ''}`}
                onClick={() => handleNotificationClick(notif)}
              >
                {notif.imageUrl && (
                  <img src={notif.imageUrl} alt="" className="notif-page-img" />
                )}
                <div className="notif-page-content">
                  <div className="notif-page-title">{notif.title}</div>
                  <div className="notif-page-body">{notif.body}</div>
                  <div className="notif-page-time">{timeAgo(notif.createdAt)}</div>
                </div>
                {!notif.read && (
                  <div className="unread-indicator" />
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default Notifications;
