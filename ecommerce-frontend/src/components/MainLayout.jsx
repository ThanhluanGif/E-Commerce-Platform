import React from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import AiChatbotWidget from './AiChatbotWidget';
import { Outlet } from 'react-router-dom';

const MainLayout = () => {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <Navbar />
            <main className="page-content">
                <Outlet />
            </main>
            <Footer />
            <AiChatbotWidget />
        </div>
    );
};

export default MainLayout;