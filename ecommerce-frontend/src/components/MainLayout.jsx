import React from 'react';
import Navbar from './Navbar'; // Thanh Navbar mặc định của bạn
import { Outlet } from 'react-router-dom';

const MainLayout = () => {
    return (
        <>
            <Navbar /> {/* Luôn hiển thị ở các trang con */}
            <main>
                <Outlet /> {/* Nơi chứa nội dung của các trang con (Home, Product...) */}
            </main>
        </>
    );
};

export default MainLayout;