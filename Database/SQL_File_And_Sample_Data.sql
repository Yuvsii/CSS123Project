CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

#Table Creation
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    price DOUBLE,
    quantity INT,
    description TEXT
);

# Inserted Products 
INSERT INTO products (id, name, price, quantity, description) VALUES 
(1, 'Gaming Mouse', 2500.00, 15, 'High precision 16000 DPI sensor with RGB lighting.'),
(2, 'Mechanical Keyboard', 4500.50, 10, 'Blue switches, clicky tactile feedback, TKL layout.'),
(3, 'Monitor 24 inch', 8000.00, 8, '144Hz Refresh Rate, IPS Panel, 1ms response time.'),
(4, 'Gaming Headset', 3200.00, 12, '7.1 Surround Sound with noise-canceling microphone.'),
(5, 'Webcam 1080p', 1850.00, 20, 'Full HD resolution with built-in dual microphones.');

# View Table
SELECT * FROM products;
