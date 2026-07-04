import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';
import UserLayout from '../components/UserLayout';
import { useToast } from '../utils/toast';
import { getProductImage } from '../utils/helpers';
import { IconMessage, IconWarning } from '../utils/icons';
import './Messages.css';

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

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    // Load conversations list
    const fetchConversations = () => {
        if (!token) return;
        setLoadingConvs(true);
        axios.get(`http://localhost:8080/api/chat/conversations?isSeller=${isSellerMode}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                const list = res.data.data || [];
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
        if (!token || !activeConv) return;
        axios.get(`http://localhost:8080/api/chat/conversations/${activeConv.id}/messages`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setMessages(res.data.data || []);
            }
        })
        .catch(err => console.error(err));
    };

    useEffect(() => {
        fetchConversations();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [token, isSellerMode]);

    useEffect(() => {
        if (activeConv) {
            setLoadingMsgs(true);
            fetchMessages();
            setLoadingMsgs(false);
        } else {
            setMessages([]);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeConv]);

    // Simulated realtime polling every 3 seconds
    useEffect(() => {
        if (!activeConv) return;
        const interval = setInterval(() => {
            fetchMessages();
        }, 3000);
        return () => clearInterval(interval);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeConv]);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!typedMsg.trim() || !activeConv) return;

        const payload = { content: typedMsg };
        setTypedMsg('');

        axios.post(`http://localhost:8080/api/chat/conversations/${activeConv.id}/messages`, payload, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(res => {
            if (res.data && res.data.success) {
                setMessages(prev => [...prev, res.data.data]);
                // Refresh conversations list to update last message
                axios.get(`http://localhost:8080/api/chat/conversations?isSeller=${isSellerMode}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                })
                .then(r => {
                    if (r.data && r.data.success) setConversations(r.data.data || []);
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
