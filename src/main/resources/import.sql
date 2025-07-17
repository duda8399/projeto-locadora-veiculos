-- Insert: user admin
INSERT INTO client (name, email, password, phone, address, city, role, created_at, updated_at) VALUES('Usuário administrador', 'admin', '$2a$10$MYWChDsHdPTUSWvqQ2mxZ.pTpm654bnw/qQypDij3e6UjkB5Qz3GG', '33999786543', 'Rua A, 123', 'Formiga','ADMIN', NOW(), NOW());
INSERT INTO client (name, email, password, phone, address, city, role, created_at, updated_at) VALUES('João Silva', 'joao.silva@example.com', '$2a$10$abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234', '11988888888', 'Rua B, 456', 'Rio de Janeiro', 'CLIENT', NOW(), NOW());
INSERT INTO client (name, email, password, phone, address, city, role, created_at, updated_at) VALUES ('Maria Oliveira', 'maria.oliveira@example.com', '$2a$10$abcd5678abcd5678abcd5678abcd5678abcd5678abcd5678abcd5678', '11977777777', 'Rua C, 789', 'Belo Horizonte', 'CLIENT', NOW(), NOW());

-- Insert vehicles
INSERT INTO vehicle (plate, brand, model, year, color, description, img_url, daily_value, created_at, updated_at) VALUES ('ABC-1234', 'Toyota', 'Corolla', '2020', 'Azul', 'Sedan confortável para uso diário', 'https://exemplo.com/imagens/carro_corolla_2020.jpg', 150.75, NOW(), NOW());
INSERT INTO vehicle (plate, brand, model, year, color, description, img_url, daily_value, created_at, updated_at) VALUES ('XYZ-9876', 'Honda', 'Civic', '2021', 'Preto', 'Carro esportivo, com ótimo desempenho e design moderno', 'https://exemplo.com/imagens/carro_civic_2021.jpg', 180.50, NOW(), NOW());
INSERT INTO vehicle (plate, brand, model, year, color, description, img_url, daily_value, created_at, updated_at) VALUES ('DEF-5678', 'Ford', 'Fiesta', '2019', 'Branco', 'Carro luxuoso, com excelente acessibilidade', 'https://exemplo.com/imagens/carro_fiesta_2019.jpg', 120.00, NOW(), NOW());

-- Insert reservations
INSERT INTO reservation (client_id, vehicle_id, start_date, end_date, created_at, updated_at) VALUES (2, 1, '2025-07-01 10:00:00', '2025-07-05 10:00:00', NOW(), NOW());
INSERT INTO reservation (client_id, vehicle_id, start_date, end_date, created_at, updated_at) VALUES (3, 2, '2025-07-10 14:00:00', '2025-07-18 14:00:00', NOW(), NOW());
INSERT INTO reservation (client_id, vehicle_id, start_date, end_date, created_at, updated_at) VALUES(2, 3, '2025-08-01 08:00:00', '2025-08-03 08:00:00', NOW(), NOW());
