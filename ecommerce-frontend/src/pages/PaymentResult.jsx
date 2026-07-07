import React from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { IconCheckCircle, IconWarning } from '../utils/icons';
import './PaymentResult.css';

function PaymentResult() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const status = searchParams.get('status');
  const orderCode = searchParams.get('orderCode');

  const isSuccess = status === 'success';

  return (
    <div className="payment-result-page container">
      <div className="payment-result-card">
        <div className={`result-icon-container ${isSuccess ? 'success' : 'fail'}`}>
          {isSuccess ? (
            <IconCheckCircle size={64} className="icon-success" />
          ) : (
            <IconWarning size={64} className="icon-fail" />
          )}
        </div>

        <h2 className="result-title">
          {isSuccess ? 'Thanh toán thành công!' : 'Thanh toán thất bại!'}
        </h2>

        <p className="result-message">
          {isSuccess 
            ? `Cảm ơn bạn đã mua hàng. Giao dịch thanh toán cho đơn hàng #${orderCode} đã hoàn tất thành công.`
            : `Giao dịch thanh toán cho đơn hàng #${orderCode} đã bị hủy hoặc gặp sự cố. Vui lòng thanh toán lại hoặc liên hệ hỗ trợ.`
          }
        </p>

        <div className="order-details-mini">
          <div className="detail-row">
            <span>Mã đơn hàng:</span>
            <strong>#{orderCode}</strong>
          </div>
          <div className="detail-row">
            <span>Phương thức:</span>
            <strong>Cổng thanh toán VNPay</strong>
          </div>
          <div className="detail-row">
            <span>Trạng thái:</span>
            <span className={`status-badge ${isSuccess ? 'success' : 'fail'}`}>
              {isSuccess ? 'Đã thanh toán' : 'Thất bại'}
            </span>
          </div>
        </div>

        <div className="action-buttons">
          <button 
            className="btn btn-primary" 
            onClick={() => navigate('/orders')}
          >
            Quản lý đơn hàng
          </button>
          <button 
            className="btn btn-secondary" 
            onClick={() => navigate('/')}
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    </div>
  );
}

export default PaymentResult;
