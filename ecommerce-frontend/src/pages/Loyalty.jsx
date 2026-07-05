import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';
import './Loyalty.css';

function Loyalty() {
  const { isAuthenticated } = useContext(AuthContext);
  const [pointsData, setPointsData] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) return;

    setLoading(true);
    Promise.all([
      api.get('/api/loyalty/points'),
      api.get('/api/loyalty/history')
    ])
      .then(([pointsRes, historyRes]) => {
        if (pointsRes.data && pointsRes.data.success) {
          setPointsData(pointsRes.data.data);
        }
        if (historyRes.data && historyRes.data.success) {
          setHistory(historyRes.data.data || []);
        }
      })
      .catch(err => console.error("Error loading loyalty stats:", err))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <div className="loyalty-page container" style={{ textAlign: 'center', padding: '100px 0' }}>
        <h2>Vui lòng đăng nhập để xem thông tin thẻ thành viên và điểm thưởng!</h2>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="loyalty-page container" style={{ textAlign: 'center', padding: '100px 0' }}>
        <h2>⏳ Đang tải thông tin thành viên...</h2>
      </div>
    );
  }

  const currentPoints = pointsData?.points || 0;
  const currentTier = pointsData?.tier || 'BRONZE';

  const getTierDetails = (tier) => {
    switch (tier) {
      case 'DIAMOND':
        return { name: 'Kim Cương (Diamond)', next: 'Max Level', nextPoints: 0, discount: 15, class: 'tier-diamond' };
      case 'PLATINUM':
        return { name: 'Bạch Kim (Platinum)', next: 'Kim Cương', nextPoints: 5000, discount: 10, class: 'tier-platinum' };
      case 'GOLD':
        return { name: 'Vàng (Gold)', next: 'Bạch Kim', nextPoints: 2000, discount: 5, class: 'tier-gold' };
      case 'SILVER':
        return { name: 'Bạc (Silver)', next: 'Vàng', nextPoints: 500, discount: 2, class: 'tier-silver' };
      default:
        return { name: 'Đồng (Bronze)', next: 'Bạc', nextPoints: 100, discount: 0, class: 'tier-bronze' };
    }
  };

  const details = getTierDetails(currentTier);
  const percentToNext = details.nextPoints > 0 
    ? Math.min(100, Math.floor((currentPoints / details.nextPoints) * 100))
    : 100;

  return (
    <div className="loyalty-page container">
      {/* 1. MEMBER CARD */}
      <div className={`member-card-wrapper ${details.class}`}>
        <div className="card-logo">E-COMMERCE VIP</div>
        <div className="card-tier-name">{details.name}</div>
        <div className="card-points-label">Điểm thưởng hiện tại</div>
        <div className="card-points-val">{currentPoints} xu</div>
        <div className="card-benefits">
          Ưu đãi độc quyền: <strong>{details.discount}% Chiết khấu đơn hàng</strong>
        </div>
      </div>

      {/* 2. PROGRESS BLOCK */}
      {details.nextPoints > 0 && (
        <div className="loyalty-progress-card">
          <h3 className="section-title-loyalty">Tiến trình nâng hạng</h3>
          <p className="progress-info">
            Bạn cần thêm <strong>{details.nextPoints - currentPoints} xu</strong> để nâng hạng lên <strong>{details.next}</strong>.
          </p>
          <div className="progress-bar-bg">
            <div className="progress-bar-fill" style={{ width: `${percentToNext}%` }} />
          </div>
          <div className="progress-labels">
            <span>{currentPoints} xu</span>
            <span>{details.nextPoints} xu</span>
          </div>
        </div>
      )}

      {/* 3. HISTORY & BENEFITS */}
      <div className="loyalty-two-columns">
        {/* Left: Point Transactions */}
        <div className="loyalty-column-card">
          <h3 className="section-title-loyalty">Lịch sử điểm thưởng</h3>
          <div className="point-history-list">
            {history.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '30px 0', color: 'var(--color-gray-400)' }}>
                Chưa có giao dịch điểm nào.
              </div>
            ) : (
              history.map(item => (
                <div key={item.id} className="point-history-item">
                  <div className="hist-desc">
                    <strong>{item.reason}</strong>
                    <span className="hist-date">{new Date(item.createdAt).toLocaleString('vi-VN')}</span>
                  </div>
                  <span className={`hist-points ${item.points > 0 ? 'plus' : 'minus'}`}>
                    {item.points > 0 ? `+${item.points}` : item.points} xu
                  </span>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Right: Membership Benefits Table */}
        <div className="loyalty-column-card">
          <h3 className="section-title-loyalty">Quyền lợi thành viên</h3>
          <div className="tier-benefit-list">
            <div className="benefit-row active">
              <span>Đồng (Bronze)</span>
              <span>Hạng mặc định</span>
            </div>
            <div className="benefit-row active">
              <span>Bạc (Silver)</span>
              <span>100 xu - Chiết khấu 2% đơn hàng</span>
            </div>
            <div className="benefit-row active">
              <span>Vàng (Gold)</span>
              <span>500 xu - Chiết khấu 5% đơn hàng</span>
            </div>
            <div className="benefit-row active">
              <span>Bạch Kim (Platinum)</span>
              <span>2000 xu - Chiết khấu 10% đơn hàng</span>
            </div>
            <div className="benefit-row active">
              <span>Kim Cương (Diamond)</span>
              <span>5000 xu - Chiết khấu 15% đơn hàng + Free Ship toàn quốc</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Loyalty;
