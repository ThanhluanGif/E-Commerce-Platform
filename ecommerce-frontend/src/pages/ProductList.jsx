import React from 'react';
import { Link } from 'react-router-dom';
import {useEffect, useState} from 'react'


function ProductList() {

    // 1. Tạo một State để lưu trữ danh sách sản phẩm từ Backend đổ về (ban đầu là mảng rỗng)
    const [products, setProducts] = useState([]);

    // State để lưu trạng thái đang tải dữ liệu hoặc lỗi nếu có
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 2. useEffect dùng để tự động chạy đoạn code gọi API ngay khi giao diện trang web vừa nạp xong
    useEffect(() => {
        // Hàm gọi API sang Backend Spring Boot
        fetch('http://localhost:8080/api/products') // Đường dẫn API của bạn bên Spring Boot
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Không thể kết nối đến Backend hoặc API bị lỗi!');
                }
                return response.json(); // Chuyển đổi dữ liệu nhận được từ dạng chuỗi sang JSON
            })
            .then((data) => {
                setProducts(data); // Nạp dữ liệu thật vào State 'products'
                setLoading(false); // Tắt trạng thái đang tải
            })
            .catch((err) => {
                setError(err.message); // Lưu lại thông báo lỗi nếu Backend chưa bật hoặc lỗi API
                setLoading(false);
            });
    }, []); // Mảng rỗng [] ở đây nghĩa là chỉ chạy duy nhất 1 lần khi mở trang lên

    // 3. Xử lý hiển thị các trạng thái giao diện thô
    if (loading) return <div style={{ padding: '20px' }}>⏳ Đang tải dữ liệu từ Backend Spring Boot...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>❌ Thông báo lỗi: {error}</div>;


    return (
        <div style={{ padding: '20px', border: '2px dashed green' }}>
            <p style={{ color: 'green', margin: 0 }}>[Trang danh sách sản phẩm - Dữ liệu động thực tế]</p>

            <h2>DANH SÁCH SẢN PHẨM TỪ DATABASE</h2>

            {/* Nếu mảng rỗng (chưa có sản phẩm nào dưới DB) */}
            {products.length === 0 ? (
                <p>Hiện tại không có sản phẩm nào trong cơ sở dữ liệu.</p>
            ) : (
                <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', marginTop: '20px' }}>
                    {/* Vòng lặp map dữ liệu thật từ Backend */}
                    {products.map((product) => (
                        <div key={product.id} style={{ border: '1px solid #000', padding: '15px', width: '220px' }}>
                            {/* Lưu ý: Các trường như .name, .price phải trùng khớp với thuộc tính trong Entity Java của bạn */}
                            <h4>{product.name}</h4>
                            <p>Giá: {product.price.toLocaleString()} đ</p>

                            <Link to={`/products/${product.id}`}>
                                <button style={{ cursor: 'pointer' }}>Xem chi tiết</button>
                            </Link>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
export default ProductList;