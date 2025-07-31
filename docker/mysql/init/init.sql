-- Create databases for each service
CREATE DATABASE IF NOT EXISTS monolith_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS materialized_view_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON monolith_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON materialized_view_db.* TO 'root'@'%';

FLUSH PRIVILEGES;
