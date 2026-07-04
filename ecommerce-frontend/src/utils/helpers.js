/**
 * formatPrice — Format VND currency
 * @param {number} amount
 * @returns {string} formatted price string e.g. "₫1.234.567"
 */
export function formatPrice(amount) {
  if (amount == null || isNaN(amount)) return '₫0';
  return '₫' + new Intl.NumberFormat('vi-VN').format(amount);
}

/**
 * formatCompactNumber — Format large numbers e.g. 12500 → "12,5k"
 * @param {number} num
 * @returns {string}
 */
export function formatCompactNumber(num) {
  if (num == null || isNaN(num)) return '0';
  if (num >= 1000000) return (num / 1000000).toFixed(1).replace('.0', '') + 'tr';
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k';
  return num.toString();
}

/**
 * getDiscountPercent — Calculate discount percentage
 * @param {number} original
 * @param {number} sale
 * @returns {number} percentage
 */
export function getDiscountPercent(original, sale) {
  if (!original || !sale || sale >= original) return 0;
  return Math.round(((original - sale) / original) * 100);
}

/**
 * getProductImage — Get product image URL with fallback
 * @param {string} imageUrl
 * @returns {string}
 */
export function getProductImage(imageUrl) {
  if (!imageUrl) return '/no-image.png';
  if (imageUrl.startsWith('http')) return imageUrl;
  return `http://localhost:8080${imageUrl}`;
}

/**
 * timeAgo — Convert ISO date to relative time string
 * @param {string} dateString
 * @returns {string}
 */
export function timeAgo(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now - date) / 1000);

  if (seconds < 60) return 'Vừa xong';
  if (seconds < 3600) return Math.floor(seconds / 60) + ' phút trước';
  if (seconds < 86400) return Math.floor(seconds / 3600) + ' giờ trước';
  if (seconds < 2592000) return Math.floor(seconds / 86400) + ' ngày trước';
  if (seconds < 31536000) return Math.floor(seconds / 2592000) + ' tháng trước';
  return Math.floor(seconds / 31536000) + ' năm trước';
}

/**
 * truncateText — Truncate text with ellipsis
 * @param {string} text
 * @param {number} maxLength
 * @returns {string}
 */
export function truncateText(text, maxLength = 50) {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
}

/**
 * getOrderStatusLabel — Map order status to Vietnamese label + color
 * @param {string} status
 * @returns {{ label: string, color: string }}
 */
export function getOrderStatusLabel(status) {
  const map = {
    'PENDING': { label: 'Chờ xác nhận', className: 'badge-warning' },
    'CONFIRMED': { label: 'Đã xác nhận', className: 'badge-info' },
    'SHIPPING': { label: 'Đang giao', className: 'badge-info' },
    'DELIVERED': { label: 'Đã giao', className: 'badge-success' },
    'COMPLETED': { label: 'Hoàn thành', className: 'badge-success' },
    'CANCELLED': { label: 'Đã hủy', className: 'badge-danger' },
    'RETURNED': { label: 'Trả hàng', className: 'badge-danger' },
  };
  return map[status] || { label: status, className: 'badge-info' };
}
