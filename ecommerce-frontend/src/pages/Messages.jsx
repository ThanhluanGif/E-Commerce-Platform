import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import UserLayout from '../components/UserLayout';
import { useToast } from '../utils/toast';
import { getProductImage } from '../utils/helpers';
import { IconMessage, IconWarning } from '../utils/icons';
import chatService from '../services/chatService';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import './Messages.css';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Messages() {
    const [searchParams] = useSearchParams();
    const targetConvId = searchParams.get('convId');
    const toast = useToast();

    const [conversations, setConversations] = useState([]);
    const [activeConv, setActiveConv] = useState(null);
    const [messages, setMessages] = useState([]);
    const [typedMsg, setTypedMsg] = useState('');
    const [isSellerMode, setIsSellerMode] = useState(false);
    const [loadingConvs, setLoadingConvs] = useState(true);
    const [loadingMsgs, setLoadingMsgs] = useState(false);

    const token = localStorage.getItem('jwtToken') || localStorage.getItem('token');
    const currentUser = {
        id: Number(localStorage.getItem('userId')),
        username: localStorage.getItem('username'),
        role: localStorage.getItem('userRole')
    };

    const messagesEndRef = useRef(null);
    const stompClientRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    // Load conversations list
    const fetchConversations = () => {
        if (!token) return;
        setLoadingConvs(true);
        chatService.getConversations(isSellerMode)
        .then(res => {
            if (res && res.success) {
                const list = res.data || [];
                setConversations(list);
                
                // If we have a query parameter convId, set it active
                if (targetConvId) {
                    const match = list.find(c => c.id.toString() === targetConvId);
                    if (match) setActiveConv(match);
                } else if (list.length > 0 && !activeConv) {
                    setActiveConv(list[0]);
                }
            }
            setLoadingConvs(false);
        })
        .catch(err => {
            console.error(err);
            setLoadingConvs(false);
        });
    };

    // Load messages of active conversation
    const fetchMessages = () => {
        if (!token || !activeConv) return Promise.resolve();
        return chatService.getMessages(activeConv.id)
        .then(res => {
            if (res && res.success) {
                setMessages(res.data || []);
            }
        })
        .catch(err => console.error(err));
    };

    useEffect(() => {
        fetchConversations();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token, isSellerMode]);

    // WebSocket STOMP Connection handler
    useEffect(() => {
        if (!activeConv || !token) return;

        setLoadingMsgs(true);
        fetchMessages().finally(() => setLoadingMsgs(false));

        // Connect to WebSocket STOMP broker
        const socket = new SockJS(`${API_BASE_URL}/ws`);
        const stompClient = Stomp.over(socket);
        stompClient.debug = () => {}; // Mute console logging

        stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
            stompClientRef.current = stompClient;
            stompClient.subscribe(`/topic/chat/${activeConv.id}`, (messageOutput) => {
                const messageObj = JSON.parse(messageOutput.body);
                setMessages(prev => {
                    // Check for duplicate messages to avoid adding twice when sending
                    if (prev.some(m => m.id === messageObj.id)) return prev;
                    return [...prev, messageObj];
                });
            });
        }, (error) => {
            console.error('WebSocket connection error:', error);
        });

        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.disconnect();
                stompClientRef.current = null;
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeConv, token]);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!typedMsg.trim() || !activeConv) return;

        const content = typedMsg;
        setTypedMsg('');

        chatService.sendMessage(activeConv.id, content)
        .then(res => {
            if (res && res.success) {
                setMessages(prev => [...prev, res.data]);
                // Refresh conversations list to update last message
                chatService.getConversations(isSellerMode)
                .then(r => {
                    if (r && r.success) setConversations(r.data || []);
                });
            }
        })
        .catch(err => {
            console.error(err);
            toast.error("Lỗi khi gửi tin nhắn!");
        });
    };

    if (!token) {
        return (
            <div className="container" style={{ padding: 'var(--space-8) 0' }}>
                <div className="badge badge-danger" style={{ display: 'flex', gap: 6, padding: 'var(--space-4)', width: '100%' }}>
                    <IconWarning size={14} /> Vui lòng đăng nhập để sử dụng tính năng Chat!
                </div>
            </div>
        );
    }

    return (
        <UserLayout activeTab="messages">
            <h3 className="user-content-title">Tin nhắn trò chuyện</h3>
            <p className="user-content-subtitle" style={{ marginBottom: 'var(--space-4)' }}>Liên hệ trực tiếp với người bán hoặc người mua</p>

            <div className="chat-wrapper">
                {/* Conversations list sidebar */}
                <div className="chat-sidebar">
                    <div className="chat-sidebar-header">
                        <h4 style={{ margin: 0, fontSize: 'var(--font-size-md)', fontWeight: 700 }}>Danh sách chat</h4>
                        {currentUser?.role === 'SELLER' && (
                            <button 
                                onClick={() => {
                                    setIsSellerMode(!isSellerMode);
                                    setActiveConv(null);
                                }} 
                                className="chat-mode-toggle btn"
                                style={{
                                    height: 'auto',
                                    background: isSellerMode ? 'var(--color-success)' : 'var(--color-info)'
                                }}
                            >
                                {isSellerMode ? 'Shop' : 'Mua'}
                            </button>
                        )}
                    </div>

                    <div className="conversations-scroll">
                        {loadingConvs ? (
                            <div className="loading-center" style={{ padding: 'var(--space-5)' }}>
                                <div className="spinner" />
                            </div>
                        ) : conversations.length === 0 ? (
                            <div style={{ padding: 'var(--space-5)', textAlign: 'center', fontSize: 'var(--font-size-sm)', color: 'var(--color-gray-500)' }}>
                                Chưa có cuộc hội thoại nào.
                            </div>
                        ) : (
                            conversations.map(c => {
                                const isActive = activeConv && activeConv.id === c.id;
                                const title = isSellerMode ? c.buyerUsername : c.shopName;
                                const avatar = isSellerMode ? c.buyerAvatarUrl : c.shopLogoUrl;
                                
                                return (
                                    <div 
                                        key={c.id} 
                                        onClick={() => setActiveConv(c)} 
                                        className={`conv-item ${isActive ? 'active' : ''}`}
                                    >
                                        <div className="conv-avatar">
                                            <img src={getProductImage(avatar)} alt={title} onError={(e) => { e.target.src = "https://img.icons8.com/color/96/user-male-circle.png"; }} />
                                        </div>
                                        <div className="conv-title-row">
                                            <h4 className="conv-name">{title}</h4>
                                            <p className="conv-last-msg">{c.lastMessage}</p>
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>

                {/* Chat Box window */}
                <div className="chat-main-window">
                    {activeConv ? (
                        <>
                            {/* Chat Box Header */}
                            <div className="chat-header">
                                <div className="chat-header-avatar">
                                    <img src={getProductImage(isSellerMode ? activeConv.buyerAvatarUrl : activeConv.shopLogoUrl)} alt="Active" onError={(e) => { e.target.src = "https://img.icons8.com/color/96/user-male-circle.png"; }} />
                                </div>
                                <h4 style={{ margin: 0, fontSize: 'var(--font-size-base)', fontWeight: 700 }}>
                                    {isSellerMode ? activeConv.buyerUsername : activeConv.shopName}
                                </h4>
                            </div>

                            {/* Messages Area */}
                            <div className="chat-messages-area">
                                {loadingMsgs ? (
                                    <div className="loading-center">
                                        <div className="spinner" />
                                    </div>
                                ) : (
                                    messages.map(m => {
                                        const isMe = currentUser && m.senderId === currentUser.id;
                                        return (
                                            <div 
                                                key={m.id} 
                                                className={`msg-bubble-wrap ${isMe ? 'me' : 'other'}`}
                                            >
                                                <div className="msg-bubble">
                                                    {m.content}
                                                </div>
                                                <span className="msg-timestamp">
                                                    {new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                </span>
                                            </div>
                                        );
                                    })
                                )}
                                <div ref={messagesEndRef} />
                            </div>

                            {/* Message Input Box */}
                            <form onSubmit={handleSend} className="chat-input-form">
                                <input
                                    type="text"
                                    className="form-input"
                                    placeholder="Nhập tin nhắn..."
                                    value={typedMsg}
                                    onChange={(e) => setTypedMsg(e.target.value)}
                                />
                                <button type="submit" className="btn btn-primary" style={{ padding: '0 var(--space-6)' }}>
                                    Gửi
                                </button>
                            </form>
                        </>
                    ) : (
                        <div className="empty-state" style={{ flex: 1 }}>
                            <div className="empty-state-icon"><IconMessage size={48} /></div>
                            <p className="empty-state-text" style={{ margin: 0 }}>Chọn một cuộc hội thoại từ danh sách bên trái để bắt đầu nhắn tin.</p>
                        </div>
                    )}
                </div>
            </div>
        </UserLayout>
    );
}

export default Messages;
