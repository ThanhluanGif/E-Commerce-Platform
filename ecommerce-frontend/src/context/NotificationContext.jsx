import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';
import { useToast } from '../utils/toast';
import notificationService from '../services/notificationService';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const { userId, isAuthenticated } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const toast = useToast();

  const fetchNotifications = async () => {
    try {
      const res = await notificationService.getMyNotifications();
      if (res.success) {
        setNotifications(res.data);
      }
      const countRes = await notificationService.getUnreadCount();
      if (countRes.success) {
        setUnreadCount(countRes.data);
      }
    } catch (err) {
      console.error('Lỗi khi tải thông báo:', err);
    }
  };

  useEffect(() => {
    if (isAuthenticated && userId) {
      fetchNotifications();

      const wsUrl = (process.env.REACT_APP_API_URL || 'http://localhost:8080') + '/ws';
      const socket = new SockJS(wsUrl);
      const stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        onConnect: () => {
          console.log('Connected to WebSocket server');
          stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
            const newNotif = JSON.parse(message.body);
            setNotifications((prev) => [newNotif, ...prev]);
            setUnreadCount((prev) => prev + 1);
            toast.info(newNotif.body, { title: newNotif.title });
          });
        },
        onStompError: (frame) => {
          console.error('Stomp error: ', frame);
        }
      });

      stompClient.activate();

      return () => {
        stompClient.deactivate();
      };
    } else {
      setNotifications([]);
      setUnreadCount(0);
    }
  }, [isAuthenticated, userId, toast]);

  const markAllAsRead = async () => {
    try {
      const res = await notificationService.readAll();
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => ({ ...n, read: true }))
        );
        setUnreadCount(0);
      }
    } catch (err) {
      console.error('Lỗi khi đánh dấu đọc tất cả:', err);
    }
  };

  const markAsRead = async (id) => {
    try {
      const res = await notificationService.readOne(id);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.id === id ? { ...n, read: true } : n))
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (err) {
      console.error('Lỗi khi đánh dấu đọc thông báo:', err);
    }
  };

  return (
    <NotificationContext.Provider value={{ notifications, unreadCount, markAllAsRead, markAsRead, refreshNotifications: fetchNotifications }}>
      {children}
    </NotificationContext.Provider>
  );
};
