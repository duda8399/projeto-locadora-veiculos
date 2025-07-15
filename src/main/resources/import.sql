-- Insert: user admin
INSERT INTO client (name, email, password, phone, role, created_at, updated_at) VALUES('Usuário administrador', 'admin', '$2a$10$MYWChDsHdPTUSWvqQ2mxZ.pTpm654bnw/qQypDij3e6UjkB5Qz3GG', '', 'ADMIN', NOW(), NOW());

-- Insert vehicles
INSERT INTO vehicle (plate, brand, model, year, color, description, img_url, daily_value, created_at, updated_at) VALUES ('ABC-1234', 'Toyota', 'Corolla', '2020', 'Azul', 'Sedan confortável para uso diário', 'https://exemplo.com/imagens/carro_corolla_2020.jpg', 150.75, NOW(), NOW());
INSERT INTO vehicle (plate, brand, model, year, color, description, img_url, daily_value, created_at, updated_at) VALUES ('XYZ-9876', 'Honda', 'Civic', '2021', 'Preto', 'Carro esportivo, com ótimo desempenho e design moderno', 'https://exemplo.com/imagens/carro_civic_2021.jpg', 180.50, NOW(), NOW());
