import React, { useEffect, useState } from 'react';
import UserLayout from '../components/UserLayout';
import addressService from '../services/addressService';
import { useToast } from '../utils/toast';
import { IconMapPin, IconPlus, IconEdit, IconTrash, IconClose } from '../utils/icons';
import './Addresses.css';

function Addresses() {
  const toast = useToast();
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modal states
  const [showModal, setShowModal] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  
  // Form fields
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [city, setCity] = useState('');
  const [district, setDistrict] = useState('');
  const [ward, setWard] = useState('');
  const [street, setStreet] = useState('');
  const [isDefault, setIsDefault] = useState(false);

  const fetchAddresses = () => {
    setLoading(true);
    addressService.getAllAddresses()
      .then(res => {
        if (res && res.success) {
          setAddresses(res.data || []);
        }
        setLoading(false);
      })
      .catch(err => {
        console.error(err);
        toast.error("Không thể tải danh sách địa chỉ!");
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchAddresses();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openAddModal = () => {
    setEditingAddress(null);
    setFullName('');
    setPhone('');
    setCity('');
    setDistrict('');
    setWard('');
    setStreet('');
    setIsDefault(false);
    setShowModal(true);
  };

  const openEditModal = (addr) => {
    setEditingAddress(addr);
    setFullName(addr.fullName || '');
    setPhone(addr.phone || '');
    setCity(addr.city || '');
    setDistrict(addr.district || '');
    setWard(addr.ward || '');
    setStreet(addr.street || '');
    setIsDefault(addr.isDefault || false);
    setShowModal(true);
  };

  const handleSave = (e) => {
    e.preventDefault();
    if (!fullName || !phone || !city || !district || !ward || !street) {
      toast.error("Vui lòng nhập đầy đủ các trường bắt buộc!");
      return;
    }

    const payload = {
      fullName,
      phone,
      city,
      district,
      ward,
      street,
      isDefault
    };

    const action = editingAddress
      ? addressService.updateAddress(editingAddress.id, payload)
      : addressService.createAddress(payload);

    action
      .then(res => {
        if (res && res.success) {
          toast.success(editingAddress ? "Cập nhật địa chỉ thành công!" : "Thêm địa chỉ mới thành công!");
          setShowModal(false);
          fetchAddresses();
        }
      })
      .catch(err => {
        console.error(err);
        toast.error("Lỗi khi lưu địa chỉ!");
      });
  };

  const handleDelete = (id, e) => {
    e.stopPropagation();
    if (!window.confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;

    addressService.deleteAddress(id)
      .then(res => {
        if (res && res.success) {
          toast.success("Xóa địa chỉ thành công!");
          setAddresses(prev => prev.filter(a => a.id !== id));
        }
      })
      .catch(err => {
        console.error(err);
        toast.error("Không thể xóa địa chỉ!");
      });
  };

  const handleSetDefault = (id, e) => {
    e.stopPropagation();
    addressService.setDefaultAddress(id)
      .then(res => {
        if (res && res.success) {
          toast.success("Đã thiết lập địa chỉ mặc định!");
          fetchAddresses();
        }
      })
      .catch(err => {
        console.error(err);
        toast.error("Không thể thay đổi địa chỉ mặc định!");
      });
  };

  return (
    <UserLayout activeTab="addresses">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid var(--color-gray-200)', paddingBottom: 'var(--space-3)' }}>
        <h3 className="user-content-title" style={{ margin: 0 }}>Địa chỉ của tôi</h3>
        <button className="btn btn-primary btn-sm" onClick={openAddModal} style={{ display: 'flex', gap: 4 }}>
          <IconPlus size={14} /> Thêm địa chỉ mới
        </button>
      </div>
      <p className="user-content-subtitle" style={{ marginTop: '-15px' }}>Quản lý địa chỉ giao nhận hàng của bạn</p>

      {loading ? (
        <div className="loading-center">
          <div className="spinner spinner-lg" />
        </div>
      ) : addresses.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><IconMapPin /></div>
          <h3 className="empty-state-title">Chưa có địa chỉ</h3>
          <p className="empty-state-text">Vui lòng thêm địa chỉ nhận hàng để thuận tiện khi mua sắm.</p>
        </div>
      ) : (
        <div className="address-card-list">
          {addresses.map(addr => (
            <div key={addr.id} className="address-item-card">
              <div className="address-info-left">
                <div className="address-row-header">
                  <span className="address-name">{addr.fullName}</span>
                  <span className="address-phone">{addr.phone}</span>
                </div>
                <div className="address-detail-text">
                  {addr.street}
                  <br />
                  {addr.ward}, {addr.district}, {addr.city}
                </div>
                {addr.isDefault && (
                  <span className="badge badge-success" style={{ alignSelf: 'flex-start', marginTop: 'var(--space-1)' }}>Mặc định</span>
                )}
              </div>

              <div className="address-actions-right">
                <div className="address-btn-row">
                  <button className="btn btn-ghost btn-sm" onClick={() => openEditModal(addr)} style={{ color: 'var(--color-info)', display: 'flex', gap: 4 }}>
                    <IconEdit size={14} /> Sửa
                  </button>
                  {!addr.isDefault && (
                    <button className="btn btn-ghost btn-sm" onClick={(e) => handleDelete(addr.id, e)} style={{ color: 'var(--color-danger)', display: 'flex', gap: 4 }}>
                      <IconTrash size={14} /> Xóa
                    </button>
                  )}
                </div>
                {!addr.isDefault && (
                  <button className="btn btn-secondary btn-sm" onClick={(e) => handleSetDefault(addr.id, e)} style={{ marginTop: 'var(--space-2)' }}>
                    Thiết lập mặc định
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* MODAL ADD/EDIT */}
      {showModal && (
        <div className="address-modal-overlay">
          <div className="address-modal-container">
            <div className="address-modal-header">
              <h3 style={{ margin: 0, fontSize: 'var(--font-size-md)' }}>
                {editingAddress ? "Cập Nhật Địa Chỉ" : "Địa Chỉ Mới"}
              </h3>
              <button className="btn btn-ghost btn-sm" onClick={() => setShowModal(false)}>
                <IconClose size={16} />
              </button>
            </div>
            <form onSubmit={handleSave}>
              <div className="address-modal-body">
                <div style={{ display: 'flex', gap: '15px' }}>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label className="form-label">Họ và tên *</label>
                    <input type="text" className="form-input" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ flex: 1 }}>
                    <label className="form-label">Số điện thoại *</label>
                    <input type="text" className="form-input" value={phone} onChange={(e) => setPhone(e.target.value)} required />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Tỉnh/Thành phố *</label>
                  <input type="text" className="form-input" value={city} onChange={(e) => setCity(e.target.value)} required placeholder="Ví dụ: Hà Nội" />
                </div>

                <div className="form-group">
                  <label className="form-label">Quận/Huyện *</label>
                  <input type="text" className="form-input" value={district} onChange={(e) => setDistrict(e.target.value)} required placeholder="Ví dụ: Quận Cầu Giấy" />
                </div>

                <div className="form-group">
                  <label className="form-label">Phường/Xã *</label>
                  <input type="text" className="form-input" value={ward} onChange={(e) => setWard(e.target.value)} required placeholder="Ví dụ: Phường Dịch Vọng" />
                </div>

                <div className="form-group">
                  <label className="form-label">Địa chỉ cụ thể *</label>
                  <input type="text" className="form-input" value={street} onChange={(e) => setStreet(e.target.value)} required placeholder="Ví dụ: Số 25, ngõ 102 Trần Duy Hưng" />
                </div>

                <label className="form-checkbox-label" style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: 'var(--font-size-sm)', marginTop: 'var(--space-2)' }}>
                  <input type="checkbox" checked={isDefault} onChange={(e) => setIsDefault(e.target.checked)} />
                  Đặt làm địa chỉ mặc định
                </label>
              </div>
              <div className="address-modal-footer">
                <button type="submit" className="btn btn-primary">
                  Hoàn thành
                </button>
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                  Trở lại
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </UserLayout>
  );
}

export default Addresses;
