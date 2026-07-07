import React, { createContext, useState, useEffect } from 'react';

export const AppContext = createContext();

const translations = {
  vi: {
    home: "Trang Chủ",
    products: "Sản phẩm",
    cart: "Giỏ hàng",
    loyalty: "Thành viên VIP",
    seller_center: "Kênh Người Bán",
    admin_dashboard: "Trang Quản Trị",
    logout: "Đăng xuất",
    login: "Đăng nhập",
    register: "Đăng ký",
    search_placeholder: "Tìm kiếm sản phẩm...",
    add_to_cart: "Thêm Vào Giỏ Hàng",
    buy_now: "Mua Ngay",
    trending: "Sản Phẩm Xu Hướng",
    for_you: "Gợi ý cho bạn",
    similar_products: "Sản Phẩm Tương Tự",
    price: "Giá",
    quantity: "Số lượng",
    total: "Tổng cộng",
    checkout: "Thanh Toán",
    dark_mode: "Giao diện tối",
    light_mode: "Giao diện sáng",
    language: "Ngôn ngữ",
    empty_cart: "Giỏ hàng của bạn đang trống!",
    loading: "Đang tải dữ liệu...",
    recent_viewed: "Sản phẩm đã xem",
    member_points: "Điểm tích lũy",
    vip_benefits: "Quyền lợi VIP",
    order_history: "Lịch sử đơn hàng"
  },
  en: {
    home: "Home",
    products: "Products",
    cart: "Cart",
    loyalty: "VIP Loyalty",
    seller_center: "Seller Center",
    admin_dashboard: "Admin Dashboard",
    logout: "Logout",
    login: "Login",
    register: "Register",
    search_placeholder: "Search products...",
    add_to_cart: "Add To Cart",
    buy_now: "Buy Now",
    trending: "Trending Products",
    for_you: "For You",
    similar_products: "Similar Products",
    price: "Price",
    quantity: "Quantity",
    total: "Total",
    checkout: "Checkout",
    dark_mode: "Dark Theme",
    light_mode: "Light Theme",
    language: "Language",
    empty_cart: "Your cart is empty!",
    loading: "Loading...",
    recent_viewed: "Recently Viewed",
    member_points: "Member Points",
    vip_benefits: "VIP Benefits",
    order_history: "Order History"
  }
};

export const AppProvider = ({ children }) => {
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const [lang, setLang] = useState(localStorage.getItem('lang') || 'vi');

  // Sync theme to DOM
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }, [theme]);

  // Sync lang to localStorage
  useEffect(() => {
    localStorage.setItem('lang', lang);
  }, [lang]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  const t = (key) => {
    return translations[lang]?.[key] || key;
  };

  return (
    <AppContext.Provider value={{ theme, setTheme, toggleTheme, lang, setLang, t }}>
      {children}
    </AppContext.Provider>
  );
};
