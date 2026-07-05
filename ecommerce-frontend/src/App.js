import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastProvider } from './utils/toast';
import { NotificationProvider } from './context/NotificationContext';
import ErrorBoundary from './components/common/ErrorBoundary';
import Home from './pages/Home';
import ProductList from './pages/ProductList';
import ProductDetail from './pages/ProductDetail';
import Categories from './pages/Categories';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import OrderSuccess from './pages/OrderSuccess';
import Orders from './pages/Orders';
import OrderDetail from './pages/OrderDetail';
import Profile from './pages/Profile';
import AdminDashboard from './pages/AdminDashboard';
import PaymentSimulation from './pages/PaymentSimulation';
import Login from './pages/Login';
import Register from './pages/Register';
import ShopPage from './pages/ShopPage';
import Wishlist from './pages/Wishlist';
import Messages from './pages/Messages';
import Addresses from './pages/Addresses';
import Returns from './pages/Returns';
import SellerDashboard from './pages/SellerDashboard';
import FlashSale from './pages/FlashSale';
import Notifications from './pages/Notifications';
import PaymentResult from './pages/PaymentResult';
import MainLayout from './components/MainLayout';
import AuthLayout from './components/AuthLayout';
import { PrivateRoute, AdminRoute } from './components/ProtectedRoute';

function App() {
    return (
        <ToastProvider>
            <NotificationProvider>
                <ErrorBoundary>
                    <Router>
                <Routes>
                    <Route element={<MainLayout />}>
                        <Route path="/" element={<Home />} />
                        <Route path="/products" element={<ProductList />} />
                        <Route path="/products/:id" element={<ProductDetail />} />
                        <Route path="/categories" element={<Categories />} />
                        <Route path="/cart" element={<Cart />} />
                        <Route path="/shop/:slug" element={<ShopPage />} />
                        <Route path="/flash-sale" element={<FlashSale />} />
                        
                        {/* Protected Routes */}
                        <Route element={<PrivateRoute />}>
                            <Route path="/checkout" element={<Checkout />} />
                            <Route path="/order-success" element={<OrderSuccess />} />
                            <Route path="/orders" element={<Orders />} />
                            <Route path="/orders/:id" element={<OrderDetail />} />
                            <Route path="/profile" element={<Profile />} />
                            <Route path="/payment-simulation" element={<PaymentSimulation />} />
                            <Route path="/payment-result" element={<PaymentResult />} />
                            <Route path="/wishlist" element={<Wishlist />} />
                            <Route path="/messages" element={<Messages />} />
                            <Route path="/addresses" element={<Addresses />} />
                            <Route path="/returns" element={<Returns />} />
                            <Route path="/seller" element={<SellerDashboard />} />
                            <Route path="/notifications" element={<Notifications />} />
                        </Route>

                        {/* Admin Routes */}
                        <Route element={<AdminRoute />}>
                            <Route path="/admin" element={<AdminDashboard />} />
                        </Route>
                    </Route>

                    <Route element={<AuthLayout />}>
                        <Route path="/login" element={<Login />} />
                        <Route path="/register" element={<Register />} />
                    </Route>
                </Routes>
                    </Router>
                </ErrorBoundary>
            </NotificationProvider>
        </ToastProvider>
    );
}

export default App;
