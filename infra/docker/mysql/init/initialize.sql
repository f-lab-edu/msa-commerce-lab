CREATE DATABASE IF NOT EXISTS `db_order`    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS `db_payment`  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS `db_monolith`  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'app_rw'@'%' IDENTIFIED BY '1q2w3e4r!';
CREATE USER IF NOT EXISTS 'app_ro'@'%' IDENTIFIED BY '1q2w3e4r@';
CREATE USER IF NOT EXISTS 'admin_rw'@'%' IDENTIFIED BY '1q2w3e4r';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_order`.*   TO 'app_rw'@'%';
GRANT SELECT                                   ON `db_order`.*   TO 'app_ro'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_payment`.* TO 'app_rw'@'%';
GRANT SELECT                                   ON `db_payment`.* TO 'app_ro'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_monolith`.* TO 'app_rw'@'%';
GRANT SELECT                                   ON `db_monolith`.* TO 'app_ro'@'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_order`.*   TO 'admin_rw'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_payment`.* TO 'admin_rw'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_monolith`.* TO 'admin_rw'@'%';

FLUSH PRIVILEGES;