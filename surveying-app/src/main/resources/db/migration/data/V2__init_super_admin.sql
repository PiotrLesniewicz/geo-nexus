-- COMPANY
INSERT INTO company (name, nip, active)
VALUES ('SYSTEM', '0000000000', true);

-- USER
INSERT INTO user_account (company_id, email, name, surname, role, active)
VALUES (1, 'super_admin@sp.sp', 'Super', 'Admin', 'SUPER_ADMIN', true);

-- AUTH
-- password: "Super_admin!"
INSERT INTO user_auth (user_id, password_hash)
VALUES (1, '$2a$10$q.gkqQlTl9R0w6SgNjlUjeXJLdnstRZ7qmgMcifLfCvCs2lwhtZ/C');