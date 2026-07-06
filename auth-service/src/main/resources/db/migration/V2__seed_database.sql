-- Seed default security roles
INSERT INTO auth.roles (id, name, description) VALUES
(1, 'ROLE_USER', 'Standard customer role'),
(2, 'ROLE_ADMIN', 'Administrator role'),
(3, 'ROLE_EMPLOYEE', 'Employee/Staff role')
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for roles
SELECT setval('auth.roles_id_seq', (SELECT COALESCE(MAX(id), 1) FROM auth.roles));

-- Seed sample users
INSERT INTO auth.users (id, username, email, phone, password_hash, full_name, status, created_at) VALUES
(1001, 'hoangnam', 'nam@gmail.com', '0987654321', '$2a$12$L8qgBfQGjRtfZ/t4q.F4.ux1Xp6V3yU12WkC7d4a2Xh48eD4v3mFa', 'Nguyễn Hoàng Nam', 'ACTIVE', '2026-07-06 08:00:00'),
(1002, 'minhthanh', 'thanh@gmail.com', '0912345678', '$2a$12$K1haBtP2/xN68nZ5l/hU0.89u2vRz3y12WgC7d4a2Xh48eD4v3mFa', 'Trần Minh Thành', 'ACTIVE', '2026-07-06 09:15:30')
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for users
SELECT setval('auth.users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM auth.users));

-- Link users with roles
INSERT INTO auth.user_roles (user_id, role_id) VALUES
(1001, 1),
(1002, 1)
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Seed user addresses
INSERT INTO auth.user_addresses (id, user_id, recipient_name, recipient_phone, address_line, province_city, district, ward, is_default) VALUES
(501, 1001, 'Nguyễn Hoàng Nam', '0987654321', 'Số 12 Ngõ 34 Cầu Giấy', 'Hà Nội', 'Cầu Giấy', 'Dịch Vọng', true),
(502, 1002, 'Trần Minh Thành', '0912345678', '88/9 Nguyễn Trãi, Q.5', 'TP Hồ Chí Minh', 'Quận 5', 'Phường 2', true)
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for user_addresses
SELECT setval('auth.user_addresses_id_seq', (SELECT COALESCE(MAX(id), 1) FROM auth.user_addresses));
