import React from 'react';
import { Link } from 'react-router-dom';
import { IconStar } from '../utils/icons';
import { formatPrice, getDiscountPercent, getProductImage, formatCompactNumber } from '../utils/helpers';
import './ProductCard.css';

/**
 * ProductCard — Reusable product card component
 * Used on Home, ProductList, FlashSale, ShopPage, Wishlist, etc.
 */
function ProductCard({ product, showProgress = false }) {
  if (!product) return null;

  const {
    id,
    name,
    price,
    salePrice,
    imageUrl,
    averageRating,
    soldCount,
    totalQuantity,
  } = product;

  const discount = getDiscountPercent(price, salePrice);
  const displayPrice = salePrice && salePrice > 0 ? salePrice : price;
  const soldPercent = totalQuantity ? Math.min(Math.round((soldCount || 0) / totalQuantity * 100), 100) : 0;

  return (
    <Link to={`/products/${id}`} className="product-card">
      {/* Discount Badge */}
      {discount > 0 && (
        <div className="product-card-discount">
          <span>GIẢM</span>
          {discount}%
        </div>
      )}

      {/* Image */}
      <div className="product-card-image">
        <img
          src={getProductImage(imageUrl)}
          alt={name}
          loading="lazy"
          onError={(e) => { e.target.src = '/no-image.png'; }}
        />
      </div>

      {/* Body */}
      <div className="product-card-body">
        <div className="product-card-name">{name}</div>

        <div className="product-card-price-row">
          <span className="product-card-price">{formatPrice(displayPrice)}</span>
          {discount > 0 && (
            <span className="product-card-original-price">{formatPrice(price)}</span>
          )}
        </div>

        <div className="product-card-meta">
          {averageRating > 0 ? (
            <div className="product-card-rating">
              <IconStar size={10} fill="#ffc400" color="#ffc400" />
              <span>{averageRating.toFixed(1)}</span>
            </div>
          ) : (
            <div />
          )}
          {soldCount > 0 && (
            <span className="product-card-sold">Đã bán {formatCompactNumber(soldCount)}</span>
          )}
        </div>
      </div>

      {/* Flash Sale Progress Bar */}
      {showProgress && totalQuantity > 0 && (
        <div className="product-card-progress">
          <div className="product-card-progress-bar">
            <div
              className="product-card-progress-fill"
              style={{ width: `${soldPercent}%` }}
            />
            <span className="product-card-progress-text">
              {soldPercent >= 80 ? 'Sắp hết' : `Đã bán ${soldCount || 0}`}
            </span>
          </div>
        </div>
      )}
    </Link>
  );
}

/**
 * ProductCardSkeleton — Loading placeholder for product cards
 */
export function ProductCardSkeleton() {
  return (
    <div className="product-card product-card-skeleton">
      <div className="product-card-image" />
      <div className="product-card-body">
        <div className="skeleton-line" />
        <div className="skeleton-line skeleton-line-short" />
        <div className="skeleton-line-price skeleton-line" />
      </div>
    </div>
  );
}

export default ProductCard;
