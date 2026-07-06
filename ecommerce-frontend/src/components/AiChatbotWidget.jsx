import React, { useState, useEffect, useRef } from 'react';
import aiService from '../services/aiService';
import { IoChatbubbleEllipsesOutline, IoClose, IoSend } from 'react-icons/io5';

function AiChatbotWidget() {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { sender: 'bot', text: '🤖 Xin chào! Tôi là Trợ lý Ảo E-Shop AI. Tôi có thể giúp gì cho bạn hôm nay?' }
    ]);
    const [inputValue, setInputValue] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);

    const quickReplies = [
        { label: '📦 Tra cứu đơn hàng', text: 'Tôi muốn tra cứu đơn hàng gần đây' },
        { label: '🔄 Hướng dẫn đổi trả', text: 'Chính sách đổi trả hàng như thế nào?' },
        { label: '🚚 Phí & Thời gian ship', text: 'Thời gian giao hàng mất bao lâu?' },
        { label: '🧑‍💼 Gặp hỗ trợ viên', text: 'Gặp nhân viên hỗ trợ trực tuyến' }
    ];

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isLoading]);

    const handleSendMessage = async (textToSend) => {
        const text = textToSend || inputValue;
        if (!text.trim()) return;

        // Thêm tin nhắn của user vào chat
        setMessages(prev => [...prev, { sender: 'user', text }]);
        if (!textToSend) setInputValue('');
        setIsLoading(true);

        // Gọi API Chatbot
        const res = await aiService.askChatbot(text);
        setIsLoading(false);

        if (res && res.success && res.data) {
            setMessages(prev => [...prev, { sender: 'bot', text: res.data.response }]);
        } else {
            setMessages(prev => [...prev, { sender: 'bot', text: '🤖 Rất tiếc, hệ thống đang bận. Bạn vui lòng thử lại sau hoặc gõ "gặp nhân viên" để được kết nối!' }]);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSendMessage();
        }
    };

    return (
        <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 1000, fontFamily: "'Inter', sans-serif" }}>
            {/* FAB Button */}
            {!isOpen && (
                <button
                    onClick={() => setIsOpen(true)}
                    style={{
                        width: '60px',
                        height: '60px',
                        borderRadius: '50%',
                        background: 'linear-gradient(135deg, #3b82f6, #1d4ed8)',
                        color: 'white',
                        border: 'none',
                        boxShadow: '0 4px 20px rgba(29, 78, 216, 0.4)',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '28px',
                        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        outline: 'none'
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.transform = 'scale(1.1) rotate(5deg)';
                        e.currentTarget.style.boxShadow = '0 6px 24px rgba(29, 78, 216, 0.5)';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.transform = 'scale(1) rotate(0deg)';
                        e.currentTarget.style.boxShadow = '0 4px 20px rgba(29, 78, 216, 0.4)';
                    }}
                >
                    <IoChatbubbleEllipsesOutline />
                </button>
            )}

            {/* Chat Window */}
            {isOpen && (
                <div
                    style={{
                        width: '380px',
                        height: '520px',
                        background: 'rgba(255, 255, 255, 0.95)',
                        backdropFilter: 'blur(10px)',
                        borderRadius: '20px',
                        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.15)',
                        display: 'flex',
                        flexDirection: 'column',
                        overflow: 'hidden',
                        border: '1px solid rgba(226, 232, 240, 0.8)',
                        animation: 'fadeInUp 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
                    }}
                >
                    {/* Header */}
                    <div
                        style={{
                            background: 'linear-gradient(135deg, #1e3a8a, #3b82f6)',
                            color: 'white',
                            padding: '16px 20px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between'
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <div style={{ position: 'relative' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '20px' }}>
                                    🤖
                                </div>
                                <span style={{ position: 'absolute', bottom: '1px', right: '1px', width: '10px', height: '10px', backgroundColor: '#10b981', border: '2px solid #1e3a8a', borderRadius: '50%' }}></span>
                            </div>
                            <div>
                                <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 600 }}>Trợ lý ảo E-Shop AI</h4>
                                <span style={{ fontSize: '11px', opacity: 0.8 }}>Thường trả lời ngay lập tức</span>
                            </div>
                        </div>
                        <button
                            onClick={() => setIsOpen(false)}
                            style={{ background: 'none', border: 'none', color: 'white', fontSize: '20px', cursor: 'pointer', opacity: 0.8, transition: 'opacity 0.2s' }}
                            onMouseEnter={(e) => e.currentTarget.style.opacity = 1}
                            onMouseLeave={(e) => e.currentTarget.style.opacity = 0.8}
                        >
                            <IoClose />
                        </button>
                    </div>

                    {/* Chat Area */}
                    <div
                        style={{
                            flex: 1,
                            padding: '20px',
                            overflowY: 'auto',
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '12px',
                            backgroundColor: '#f8fafc'
                        }}
                    >
                        {messages.map((msg, idx) => (
                            <div
                                key={idx}
                                style={{
                                    alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                                    maxWidth: '80%',
                                    padding: '10px 14px',
                                    borderRadius: msg.sender === 'user' ? '16px 16px 2px 16px' : '16px 16px 16px 2px',
                                    backgroundColor: msg.sender === 'user' ? '#3b82f6' : 'white',
                                    color: msg.sender === 'user' ? 'white' : '#1e293b',
                                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.04)',
                                    fontSize: '13.5px',
                                    lineHeight: '1.5',
                                    whiteSpace: 'pre-line'
                                }}
                            >
                                {msg.text}
                            </div>
                        ))}
                        {isLoading && (
                            <div
                                style={{
                                    alignSelf: 'flex-start',
                                    padding: '10px 16px',
                                    borderRadius: '16px 16px 16px 2px',
                                    backgroundColor: 'white',
                                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.04)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '4px'
                                }}
                            >
                                <span style={{ width: '6px', height: '6px', backgroundColor: '#94a3b8', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out' }}></span>
                                <span style={{ width: '6px', height: '6px', backgroundColor: '#94a3b8', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out 0.2s' }}></span>
                                <span style={{ width: '6px', height: '6px', backgroundColor: '#94a3b8', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out 0.4s' }}></span>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Quick Replies */}
                    <div
                        style={{
                            padding: '8px 12px',
                            backgroundColor: '#f1f5f9',
                            display: 'flex',
                            flexWrap: 'nowrap',
                            gap: '8px',
                            overflowX: 'auto',
                            borderTop: '1px solid #e2e8f0',
                            scrollbarWidth: 'none'
                        }}
                    >
                        {quickReplies.map((reply, idx) => (
                            <button
                                key={idx}
                                onClick={() => handleSendMessage(reply.text)}
                                style={{
                                    flexShrink: 0,
                                    padding: '6px 12px',
                                    borderRadius: '14px',
                                    border: '1px solid #cbd5e1',
                                    backgroundColor: 'white',
                                    color: '#475569',
                                    fontSize: '12px',
                                    cursor: 'pointer',
                                    fontWeight: 500,
                                    transition: 'all 0.2s ease'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = '#3b82f6';
                                    e.currentTarget.style.color = '#3b82f6';
                                    e.currentTarget.style.backgroundColor = '#eff6ff';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = '#cbd5e1';
                                    e.currentTarget.style.color = '#475569';
                                    e.currentTarget.style.backgroundColor = 'white';
                                }}
                            >
                                {reply.label}
                            </button>
                        ))}
                    </div>

                    {/* Input Panel */}
                    <div
                        style={{
                            padding: '12px 16px',
                            backgroundColor: 'white',
                            borderTop: '1px solid #e2e8f0',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '10px'
                        }}
                    >
                        <input
                            type="text"
                            placeholder="Nhập tin nhắn..."
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyDown={handleKeyPress}
                            style={{
                                flex: 1,
                                border: '1px solid #cbd5e1',
                                borderRadius: '24px',
                                padding: '10px 16px',
                                fontSize: '13px',
                                outline: 'none',
                                transition: 'border-color 0.2s'
                            }}
                            onFocus={(e) => e.currentTarget.style.borderColor = '#3b82f6'}
                            onBlur={(e) => e.currentTarget.style.borderColor = '#cbd5e1'}
                        />
                        <button
                            onClick={() => handleSendMessage()}
                            style={{
                                width: '36px',
                                height: '36px',
                                borderRadius: '50%',
                                backgroundColor: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyCenter: 'center',
                                fontSize: '16px',
                                transition: 'background-color 0.2s',
                                outline: 'none',
                                justifyContent: 'center'
                            }}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#1d4ed8'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = '#3b82f6'}
                        >
                            <IoSend />
                        </button>
                    </div>
                </div>
            )}
            
            {/* Embedded bounce animation style */}
            <style dangerouslySetInnerHTML={{__html: `
                @keyframes fadeInUp {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
                @keyframes bounce {
                    0%, 80%, 100% { transform: scale(0); }
                    40% { transform: scale(1.0); }
                }
            `}} />
        </div>
    );
}

export default AiChatbotWidget;
