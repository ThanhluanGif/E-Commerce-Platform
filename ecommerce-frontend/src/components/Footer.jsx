import React from 'react';
import { Link } from 'react-router-dom';
import { 
  IconShield, IconTruck, IconRefresh, IconCreditCard,
  IconPhone, IconMail, IconMapPin,
  IconFacebook, IconGlobe
} from '../utils/icons';
import './Footer.css';

function Footer() {
  return (
    <footer className="footer">
      {/* Guarantees Bar */}
      <div className="footer-guarantees">
        <div className="container footer-guarantees-inner">
          <div className="footer-guarantee-item">
            <div className="footer-guarantee-icon"><IconTruck size={20} /></div>
            <div>
              <div style={{ fontWeight: 600, color: 'var(--color-white)', fontSize: 'var(--font-size-sm)' }}>Miễn phí vận chuyển</div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Cho đơn từ 500.000₫</div>
            </div>
          </div>
          <div className="footer-guarantee-item">
            <div className="footer-guarantee-icon"><IconShield size={20} /></div>
            <div>
              <div style={{ fontWeight: 600, color: 'var(--color-white)', fontSize: 'var(--font-size-sm)' }}>Hàng chính hãng</div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Cam kết 100% authentic</div>
            </div>
          </div>
          <div className="footer-guarantee-item">
            <div className="footer-guarantee-icon"><IconRefresh size={20} /></div>
            <div>
              <div style={{ fontWeight: 600, color: 'var(--color-white)', fontSize: 'var(--font-size-sm)' }}>Đổi trả 30 ngày</div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Hoàn tiền nếu không hài lòng</div>
            </div>
          </div>
          <div className="footer-guarantee-item">
            <div className="footer-guarantee-icon"><IconCreditCard size={20} /></div>
            <div>
              <div style={{ fontWeight: 600, color: 'var(--color-white)', fontSize: 'var(--font-size-sm)' }}>Thanh toán an toàn</div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-gray-500)' }}>Bảo mật SSL 256-bit</div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Footer Content */}
      <div className="footer-main">
        <div className="container">
          <div className="footer-grid">
            {/* Column 1: About */}
            <div>
              <h3 className="footer-col-title">E-Commerce</h3>
              <p className="footer-about-text">
                Nền tảng thương mại điện tử hàng đầu Việt Nam, kết nối hàng triệu người mua và người bán.
                Cam kết mang đến trải nghiệm mua sắm trực tuyến tốt nhất.
              </p>
              <div className="footer-social">
                <a href="#/" className="footer-social-icon" aria-label="Facebook"><IconFacebook size={18} /></a>
                <a href="#/" className="footer-social-icon" aria-label="Website"><IconGlobe size={18} /></a>
                <a href="#/" className="footer-social-icon" aria-label="Email"><IconMail size={18} /></a>
              </div>
            </div>

            {/* Column 2: Customer Service */}
            <div>
              <h4 className="footer-col-title">Chăm Sóc Khách Hàng</h4>
              <div className="footer-links">
                <Link to="/help" className="footer-link">Trung tâm trợ giúp</Link>
                <Link to="/help" className="footer-link">Hướng dẫn mua hàng</Link>
                <Link to="/help" className="footer-link">Hướng dẫn bán hàng</Link>
                <Link to="/help" className="footer-link">Vận chuyển & Giao nhận</Link>
                <Link to="/help" className="footer-link">Trả hàng & Hoàn tiền</Link>
                <Link to="/messages" className="footer-link">Liên hệ chúng tôi</Link>
              </div>
            </div>

            {/* Column 3: About Us */}
            <div>
              <h4 className="footer-col-title">Về Chúng Tôi</h4>
              <div className="footer-links">
                <Link to="/about" className="footer-link">Giới thiệu</Link>
                <Link to="/careers" className="footer-link">Tuyển dụng</Link>
                <Link to="/terms" className="footer-link">Điều khoản sử dụng</Link>
                <Link to="/privacy" className="footer-link">Chính sách bảo mật</Link>
                <Link to="/seller" className="footer-link">Kênh Người Bán</Link>
                <Link to="/flash-sale" className="footer-link">Flash Sale</Link>
              </div>
            </div>

            {/* Column 4: Payment & Shipping */}
            <div>
              <h4 className="footer-col-title">Thanh Toán</h4>
              <div className="footer-payment-icons">
                <span className="footer-icon-badge">VISA</span>
                <span className="footer-icon-badge">MasterCard</span>
                <span className="footer-icon-badge">MoMo</span>
                <span className="footer-icon-badge">VNPay</span>
                <span className="footer-icon-badge">COD</span>
              </div>
              <h4 className="footer-col-title" style={{ marginTop: 'var(--space-6)' }}>Vận Chuyển</h4>
              <div className="footer-shipping-icons">
                <span className="footer-icon-badge">GHN</span>
                <span className="footer-icon-badge">GHTK</span>
                <span className="footer-icon-badge">J&T</span>
                <span className="footer-icon-badge">Viettel Post</span>
              </div>
            </div>

            {/* Column 5: Contact & Newsletter */}
            <div>
              <h4 className="footer-col-title">Liên Hệ</h4>
              <div className="footer-links" style={{ marginBottom: 'var(--space-4)' }}>
                <span className="footer-link"><IconMapPin size={14} /> 123 Nguyễn Huệ, Q.1, TP.HCM</span>
                <span className="footer-link"><IconPhone size={14} /> 1900 1234 56</span>
                <span className="footer-link"><IconMail size={14} /> support@ecommerce.vn</span>
              </div>
              <h4 className="footer-col-title">Đăng ký nhận tin</h4>
              <div className="footer-newsletter">
                <input type="email" className="footer-newsletter-input" placeholder="Email của bạn..." />
                <button className="footer-newsletter-btn">Đăng ký</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="footer-bottom">
        <div className="container footer-bottom-inner">
          <p className="footer-copyright">© 2026 E-Commerce. Tất cả quyền được bảo lưu.</p>
          <div className="footer-bottom-links">
            <Link to="/privacy" className="footer-bottom-link">Chính sách bảo mật</Link>
            <Link to="/terms" className="footer-bottom-link">Điều khoản dịch vụ</Link>
            <Link to="/shipping-policy" className="footer-bottom-link">Chính sách vận chuyển</Link>
            <Link to="/return-policy" className="footer-bottom-link">Chính sách đổi trả</Link>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
