

INSERT INTO employees (id, name, email, password_hash, role, manager_id) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Ana Admin', 'admin@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'ADMIN', NULL);

INSERT INTO employees (id, name, email, password_hash, role, manager_id) VALUES
  ('22222222-2222-2222-2222-222222222222', 'Marcos Manager', 'manager1@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'MANAGER', NULL),
  ('33333333-3333-3333-3333-333333333333', 'Marta Manager', 'manager2@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'MANAGER', NULL);

INSERT INTO employees (id, name, email, password_hash, role, manager_id) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Carla Colab', 'carla@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'COLLABORATOR',
   '22222222-2222-2222-2222-222222222222'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Caio Colab', 'caio@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'COLLABORATOR',
   '22222222-2222-2222-2222-222222222222'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Cris Colab', 'cris@taskflow.com',
   '$2a$10$ZZun4DPv1GZ7q5us0QM7g.3uXajxCEMSLyPHOW1Uj0qXtjXEYKNMa', 'COLLABORATOR',
   '33333333-3333-3333-3333-333333333333');