-- MSA Commerce Lab Database Initialization
CREATE USER IF NOT EXISTS 'app_rw' @'%' IDENTIFIED BY '1q2w3e4r!';

CREATE USER IF NOT EXISTS 'app_ro' @'%' IDENTIFIED BY '1q2w3e4r@';

CREATE USER IF NOT EXISTS 'app_flyway' @'%' IDENTIFIED BY '1q2w3e4r!@';

CREATE USER IF NOT EXISTS 'admin_commerce' @'%' IDENTIFIED BY '1q2w3e4r';

-- Grant permissions for each database (corrected database name)
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_platform`.* TO 'app_rw' @'%';

GRANT SELECT ON `db_platform`.* TO 'app_ro' @'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_order`.* TO 'app_rw' @'%';

GRANT SELECT ON `db_order`.* TO 'app_ro' @'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_payment`.* TO 'app_rw' @'%';

GRANT SELECT ON `db_payment`.* TO 'app_ro' @'%';

GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON `db_materialized_view`.* TO 'app_rw' @'%';

GRANT SELECT ON `db_materialized_view`.* TO 'app_ro' @'%';

-- Flyway (DDL 전용)
GRANT ALL PRIVILEGES ON *.* TO 'app_flyway' @'%';

-- Admin permissions for all databases (corrected database name)
GRANT ALL PRIVILEGES ON *.* TO 'admin_commerce' @'%';

FLUSH PRIVILEGES;
