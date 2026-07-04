import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';

function Messages() {
    const [searchParams] = useSearchParams();
    const targetConvId = searchParams.get('convId');

    const [conversations, setConversations] = useState([]);
    const [activeConv, setActiveConv] = useState(null);
    const [messages, setMessages] = useState([]);
    const [typedMsg, setTypedMsg] = useState('');
    const [isSellerMode, setIsSellerMode] = useState(false);
    const [loadingConvs, setLoadingConvs] = useState(true);
    const [loadingMsgs, setLoadingMsgs] = useState(false);

    const token = localStorage.getItem('token');
    const userJson = localStorage.getItem('user'); 
    const currentUser = userJson ? JSON.parse(userJson) : null;

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
    }, [token, isSellerMode]);

    useEffect(() => {
        if (activeConv) {
            setLoadingMsgs(true);
            fetchMessages();
            setLoadingMsgs(false);
        } else {
            setMessages([]);
        }
    }, [activeConv]);

    // Simulated realtime polling every 3 seconds
    useEffect(() => {
        if (!activeConv) return;
        const interval = setInterval(() => {
            fetchMessages();
        }, 3000);
        return () => clearInterval(interval);
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
            alert("Lỗi khi gửi tin nhắn!");
        });
    };

    if (!token) return <div style={{ padding: '40px', color: 'red', textAlign: 'center' }}>Vui lòng đăng nhập để sử dụng tính năng Chat!</div>;

    return (
        <div style={{ display: 'flex', height: '600px', background: 'white', borderRadius: '8px', border: '1px solid #e2e8f0', overflow: 'hidden', fontFamily: 'system-ui, sans-serif', boxShadow: 'var(--card-shadow)' }}>
            
            {/* Conversations list sidebar */}
            <div style={{ width: '280px', borderRight: '1px solid #e2e8f0', display: 'flex', flexDirection: 'column', background: '#f8fafc' }}>
                <div style={{ padding: '15px', borderBottom: '1px solid #e2e8f0', display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>Trò Chuyện</h3>
                    {currentUser?.role === 'SELLER' && (
                        <button onClick={() => {
                            setIsSellerMode(!isSellerMode);
                            setActiveConv(null);
                        }} style={{
                            marginLeft: 'auto',
                            padding: '4px 8px',
                            background: isSellerMode ? '#10b981' : '#3b82f6',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            fontSize: '11px',
                            fontWeight: 'bold',
                            cursor: 'pointer'
                        }}>
                            {isSellerMode ? 'Chế độ Shop' : 'Chế độ Mua'}
                        </button>
                    )}
                </div>

                <div style={{ flex: 1, overflowY: 'auto' }}>
                    {loadingConvs ? (
                        <div style={{ padding: '20px', textAlign: 'center', fontSize: '13px', color: '#666' }}>Đang tải hội thoại...</div>
                    ) : conversations.length === 0 ? (
                        <div style={{ padding: '20px', textAlign: 'center', fontSize: '13px', color: '#666' }}>Chưa có cuộc hội thoại nào.</div>
                    ) : (
                        conversations.map(c => {
                            const isActive = activeConv && activeConv.id === c.id;
                            const title = isSellerMode ? c.buyerUsername : c.shopName;
                            const avatar = isSellerMode ? c.buyerAvatarUrl : c.shopLogoUrl;
                            
                            return (
                                <div key={c.id} onClick={() => setActiveConv(c)} style={{
                                    padding: '12px 15px',
                                    borderBottom: '1px solid #f1f5f9',
                                    cursor: 'pointer',
                                    background: isActive ? '#ffe4de' : 'transparent',
                                    display: 'flex',
                                    gap: '12px',
                                    alignItems: 'center',
                                    transition: 'background 0.2s'
                                }}>
                                    <div style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#fff', border: '1px solid #e2e8f0', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                                        <img src={avatar || 'https://via.placeholder.com/80'} alt={title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                    </div>
                                    <div style={{ flex: 1, overflow: 'hidden' }}>
                                        <h4 style={{ margin: '0 0 4px 0', fontSize: '14px', fontWeight: 'bold', color: '#1e293b', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{title}</h4>
                                        <p style={{ margin: 0, fontSize: '12px', color: '#64748b', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{c.lastMessage}</p>
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            </div>

            {/* Chat Box window */}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#fff' }}>
                {activeConv ? (
                    <>
                        {/* Chat Box Header */}
                        <div style={{ padding: '15px', borderBottom: '1px solid #e2e8f0', display: 'flex', alignItems: 'center', gap: '12px' }}>
                            <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: '#f1f5f9', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <img src={(isSellerMode ? activeConv.buyerAvatarUrl : activeConv.shopLogoUrl) || 'https://via.placeholder.com/80'} alt="Active" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                            </div>
                            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 'bold', color: '#1e293b' }}>
                                {isSellerMode ? activeConv.buyerUsername : activeConv.shopName}
                            </h4>
                        </div>

                        {/* Messages Area */}
                        <div style={{ flex: 1, padding: '20px', overflowY: 'auto', background: '#f8fafc', display: 'flex', flexDirection: 'column', gap: '15px' }}>
                            {loadingMsgs ? (
                                <div style={{ textAlign: 'center', color: '#666', fontSize: '13px' }}>Đang tải tin nhắn...</div>
                            ) : (
                                messages.map(m => {
                                    const isMe = currentUser && m.senderId === currentUser.id;
                                    return (
                                        <div key={m.id} style={{
                                            alignSelf: isMe ? 'flex-end' : 'flex-start',
                                            maxWidth: '70%',
                                            display: 'flex',
                                            flexDirection: 'column'
                                        }}>
                                            <div style={{
                                                padding: '10px 14px',
                                                borderRadius: '12px',
                                                background: isMe ? '#f94e30' : '#e2e8f0',
                                                color: isMe ? 'white' : '#1e293b',
                                                fontSize: '14px',
                                                lineHeight: '1.4',
                                                boxShadow: '0 1px 2px rgba(0,0,0,0.05)'
                                            }}>
                                                {m.content}
                                            </div>
                                            <span style={{ fontSize: '10px', color: '#94a3b8', marginTop: '4px', alignSelf: isMe ? 'flex-end' : 'flex-start' }}>
                                                {new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                            </span>
                                        </div>
                                    );
                                })
                            )}
                            <div ref={messagesEndRef} />
                        </div>

                        {/* Message Input Box */}
                        <form onSubmit={handleSend} style={{ padding: '15px', borderTop: '1px solid #e2e8f0', display: 'flex', gap: '10px' }}>
                            <input
                                type="text"
                                placeholder="Nhập tin nhắn..."
                                value={typedMsg}
                                onChange={(e) => setTypedMsg(e.target.value)}
                                style={{
                                    flex: 1,
                                    padding: '10px 15px',
                                    border: '1px solid #cbd5e1',
                                    borderRadius: '6px',
                                    fontSize: '14px',
                                    outline: 'none'
                                }}
                            />
                            <button type="submit" style={{
                                padding: '10px 24px',
                                background: '#f94e30',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                fontWeight: 'bold',
                                cursor: 'pointer'
                            }}>
                                Gửi
                            </button>
                        </form>
                    </>
                ) : (
                    <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b', flexDirection: 'column', gap: '10px' }}>
                        <span style={{ fontSize: '48px' }}>💬</span>
                        <p style={{ margin: 0, fontSize: '14px' }}>Chọn một cuộc hội thoại để bắt đầu nhắn tin</p>
                    </div>
                )}
            </div>

        </div>
    );
}

export default Messages;
