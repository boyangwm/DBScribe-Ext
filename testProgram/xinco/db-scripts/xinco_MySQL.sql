SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `xinco` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `xinco`;

-- -----------------------------------------------------
-- Table `xinco`.`xinco_add_attribute_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_add_attribute_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_add_attribute_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_data_id` INT(10) UNSIGNED NOT NULL ,
  `attribute_id` INT(10) UNSIGNED NOT NULL ,
  `attrib_int` INT(11) NOT NULL ,
  `attrib_unsignedint` INT(10) UNSIGNED NOT NULL ,
  `attrib_double` DOUBLE NOT NULL ,
  `attrib_varchar` VARCHAR(255) NOT NULL ,
  `attrib_text` TEXT NOT NULL ,
  `attrib_datetime` DATETIME NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_ace_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_ace_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_ace_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_user_id` INT(10) UNSIGNED NULL ,
  `xinco_core_group_id` INT(10) UNSIGNED NULL ,
  `xinco_core_node_id` INT(10) UNSIGNED NULL ,
  `xinco_core_data_id` INT(10) UNSIGNED NULL ,
  `read_permission` TINYINT(1) NOT NULL ,
  `write_permission` TINYINT(1) NOT NULL ,
  `execute_permission` TINYINT(1) NOT NULL ,
  `admin_permission` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_node_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_language_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_data_type_id` INT(10) UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT(10) UNSIGNED NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data_type_attribute_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data_type_attribute_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data_type_attribute_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_data_type_id` INT(10) UNSIGNED NOT NULL ,
  `attribute_id` INT(10) UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `data_type` VARCHAR(255) NOT NULL ,
  `attr_size` INT(10) UNSIGNED NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data_type_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data_type_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data_type_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `description` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_group_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_group_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_group_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT(10) UNSIGNED NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_language_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_language_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_language_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `sign` VARCHAR(255) NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_node_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_node_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_node_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_node_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_language_id` INT(10) UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT(10) UNSIGNED NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user` (
  `id` INT UNSIGNED NOT NULL ,
  `username` VARCHAR(255) NOT NULL ,
  `userpassword` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `firstname` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  `attempts` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `last_modified` DATE NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_user_index_username` (`username` ASC) ,
  INDEX `xinco_core_user_index_status` (`status_number` ASC) ,
  UNIQUE INDEX `unique id` (`username` ASC) )
COMMENT = 'Status: \nopen = 1 \nlocked = 2 \n'
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user_has_xinco_core_group_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user_has_xinco_core_group_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user_has_xinco_core_group_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_user_id` INT(10) UNSIGNED NOT NULL ,
  `xinco_core_group_id` INT(10) UNSIGNED NOT NULL ,
  `status_number` INT(10) UNSIGNED NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user_t`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user_t` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user_t` (
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `id` INT(10) UNSIGNED NOT NULL ,
  `username` VARCHAR(255) NOT NULL ,
  `userpassword` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `firstname` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `status_number` INT(10) UNSIGNED NOT NULL ,
  `attempts` INT(10) UNSIGNED NOT NULL ,
  `last_modified` DATE NOT NULL ,
  PRIMARY KEY (`record_id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user_modified_record`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user_modified_record` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user_modified_record` (
  `id` INT(10) UNSIGNED NOT NULL ,
  `record_id` INT(10) UNSIGNED NOT NULL ,
  `mod_Time` TIMESTAMP NOT NULL ,
  `mod_Reason` VARCHAR(255) NULL ,
  PRIMARY KEY (`id`, `record_id`) ,
  CONSTRAINT `fk_{66203500-79C5-4ABB-AF2B-546B0D7CD657}`
    FOREIGN KEY (`id` )
    REFERENCES `xinco`.`xinco_core_user` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_language`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_language` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_language` (
  `id` INT UNSIGNED NOT NULL ,
  `sign` VARCHAR(255) NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_node`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_node` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_node` (
  `id` INT UNSIGNED NOT NULL ,
  `xinco_core_node_id` INT UNSIGNED NULL ,
  `xinco_core_language_id` INT UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_node_FKIndex1` (`xinco_core_node_id` ASC) ,
  INDEX `xinco_core_node_FKIndex2` (`xinco_core_language_id` ASC) ,
  INDEX `xinco_core_node_index_designation` (`designation` ASC) ,
  INDEX `xinco_core_node_index_status` (`status_number` ASC) ,
  CONSTRAINT `fk_{52D94EF1-ED6A-43AD-9C52-7E883915CF11}`
    FOREIGN KEY (`xinco_core_node_id` )
    REFERENCES `xinco`.`xinco_core_node` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{E427E1BE-0EC7-40B2-9822-8756CE5FFF5C}`
    FOREIGN KEY (`xinco_core_language_id` )
    REFERENCES `xinco`.`xinco_core_language` (`id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
COMMENT = 'Status:\nopen = 1\nlocked = 2\narchived = 3\n'
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data_type` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data_type` (
  `id` INT UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `description` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data` (
  `id` INT UNSIGNED NOT NULL ,
  `xinco_core_node_id` INT UNSIGNED NOT NULL ,
  `xinco_core_language_id` INT UNSIGNED NOT NULL ,
  `xinco_core_data_type_id` INT UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_data_FKIndex1` (`xinco_core_node_id` ASC) ,
  INDEX `xinco_core_data_FKIndex2` (`xinco_core_language_id` ASC) ,
  INDEX `xinco_core_data_FKIndex5` (`xinco_core_data_type_id` ASC) ,
  INDEX `xinco_core_data_index_designation` (`designation` ASC) ,
  INDEX `xinco_core_data_index_status` (`status_number` ASC) ,
  CONSTRAINT `fk_{0FE42428-0C82-42FC-923F-7314A740D04F}`
    FOREIGN KEY (`xinco_core_node_id` )
    REFERENCES `xinco`.`xinco_core_node` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{249C3090-F2CF-4C6B-A528-C9DDF697E702}`
    FOREIGN KEY (`xinco_core_language_id` )
    REFERENCES `xinco`.`xinco_core_language` (`id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{592A6D87-0049-4758-9D7F-D4F421983CDB}`
    FOREIGN KEY (`xinco_core_data_type_id` )
    REFERENCES `xinco`.`xinco_core_data_type` (`id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user` (
  `id` INT UNSIGNED NOT NULL ,
  `username` VARCHAR(255) NOT NULL ,
  `userpassword` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `firstname` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  `attempts` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `last_modified` DATE NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_user_index_username` (`username` ASC) ,
  INDEX `xinco_core_user_index_status` (`status_number` ASC) ,
  UNIQUE INDEX `unique id` (`username` ASC) )
COMMENT = 'Status: \nopen = 1 \nlocked = 2 \n'
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_group` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_group` (
  `id` INT UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_group_index_status` (`status_number` ASC) ,
  UNIQUE INDEX `unique name` (`designation` ASC) )
COMMENT = 'Status:  \nopen = 1  \nlocked = 2  \n'
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_ace`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_ace` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_ace` (
  `id` INT UNSIGNED NOT NULL ,
  `xinco_core_user_id` INT UNSIGNED NULL ,
  `xinco_core_group_id` INT UNSIGNED NULL ,
  `xinco_core_node_id` INT UNSIGNED NULL ,
  `xinco_core_data_id` INT UNSIGNED NULL ,
  `read_permission` TINYINT(1) NOT NULL ,
  `write_permission` TINYINT(1) NOT NULL ,
  `execute_permission` TINYINT(1) NOT NULL ,
  `admin_permission` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_ace_FKIndex1` (`xinco_core_user_id` ASC) ,
  INDEX `xinco_core_ace_FKIndex2` (`xinco_core_group_id` ASC) ,
  INDEX `xinco_core_ace_FKIndex3` (`xinco_core_node_id` ASC) ,
  INDEX `xinco_core_ace_FKIndex4` (`xinco_core_data_id` ASC) ,
  CONSTRAINT `fk_{A5B19221-5358-4C98-82CC-581CD695C068}`
    FOREIGN KEY (`xinco_core_user_id` )
    REFERENCES `xinco`.`xinco_core_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{BE5919E0-4B49-4754-861D-E7834EB59238}`
    FOREIGN KEY (`xinco_core_group_id` )
    REFERENCES `xinco`.`xinco_core_group` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{8CCF024C-2E44-468D-B847-EF00897639E3}`
    FOREIGN KEY (`xinco_core_node_id` )
    REFERENCES `xinco`.`xinco_core_node` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{4F26567D-38D8-4210-86D6-524AE418D6EB}`
    FOREIGN KEY (`xinco_core_data_id` )
    REFERENCES `xinco`.`xinco_core_data` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_user_has_xinco_core_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_user_has_xinco_core_group` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_user_has_xinco_core_group` (
  `xinco_core_user_id` INT UNSIGNED NOT NULL ,
  `xinco_core_group_id` INT UNSIGNED NOT NULL ,
  `status_number` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`xinco_core_user_id`, `xinco_core_group_id`) ,
  INDEX `xinco_core_user_has_xinco_core_group_FKIndex1` (`xinco_core_user_id` ASC) ,
  INDEX `xinco_core_user_has_xinco_core_group_FKIndex2` (`xinco_core_group_id` ASC) ,
  INDEX `xinco_core_user_has_xinco_core_group_index_status` (`status_number` ASC) ,
  CONSTRAINT `fk_{E9CE4AC9-ECE3-48EF-9C4A-081FB0A9FD96}`
    FOREIGN KEY (`xinco_core_user_id` )
    REFERENCES `xinco`.`xinco_core_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{BB4D6DB4-7E25-4F88-801D-A715A8A28E33}`
    FOREIGN KEY (`xinco_core_group_id` )
    REFERENCES `xinco`.`xinco_core_group` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
COMMENT = 'Status:  \nopen = 1  \nlocked = 2  \n'
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_id`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_id` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_id` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `tablename` VARCHAR(255) NOT NULL ,
  `last_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) )
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_add_attribute`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_add_attribute` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_add_attribute` (
  `xinco_core_data_id` INT UNSIGNED NOT NULL ,
  `attribute_id` INT UNSIGNED NOT NULL ,
  `attrib_int` INT NULL ,
  `attrib_unsignedint` INT UNSIGNED NULL ,
  `attrib_double` DOUBLE NULL ,
  `attrib_varchar` VARCHAR(255) NULL ,
  `attrib_text` TEXT NULL ,
  `attrib_datetime` DATETIME NULL ,
  PRIMARY KEY (`xinco_core_data_id`, `attribute_id`) ,
  INDEX `xinco_add_attribute_FKIndex1` (`xinco_core_data_id` ASC) ,
  CONSTRAINT `fk_{DEA334D0-7F8F-4E6B-81EA-671A6C493FCC}`
    FOREIGN KEY (`xinco_core_data_id` )
    REFERENCES `xinco`.`xinco_core_data` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_data_type_attribute`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_data_type_attribute` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_data_type_attribute` (
  `xinco_core_data_type_id` INT UNSIGNED NOT NULL ,
  `attribute_id` INT UNSIGNED NOT NULL ,
  `designation` VARCHAR(255) NOT NULL ,
  `data_type` VARCHAR(255) NOT NULL ,
  `attr_size` INT UNSIGNED NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`xinco_core_data_type_id`, `attribute_id`) ,
  INDEX `xinco_core_data_type_attribute_FKIndex1` (`xinco_core_data_type_id` ASC) ,
  CONSTRAINT `fk_{E75392C0-42A1-47BE-8007-549B123B7775}`
    FOREIGN KEY (`xinco_core_data_type_id` )
    REFERENCES `xinco`.`xinco_core_data_type` (`id` )
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `xinco`.`xinco_core_log`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `xinco`.`xinco_core_log` ;

CREATE  TABLE IF NOT EXISTS `xinco`.`xinco_core_log` (
  `id` INT UNSIGNED NOT NULL ,
  `xinco_core_data_id` INT UNSIGNED NOT NULL ,
  `xinco_core_user_id` INT UNSIGNED NOT NULL ,
  `op_code` INT UNSIGNED NOT NULL ,
  `op_datetime` DATETIME NOT NULL ,
  `op_description` VARCHAR(255) NOT NULL ,
  `version_high` INT UNSIGNED NULL ,
  `version_mid` INT UNSIGNED NULL ,
  `version_low` INT UNSIGNED NULL ,
  `version_postfix` VARCHAR(255) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `xinco_core_log_FKIndex1` (`xinco_core_data_id` ASC) ,
  INDEX `xinco_core_log_FKIndex2` (`xinco_core_user_id` ASC) ,
  CONSTRAINT `fk_{E4C7D7F8-39B7-48D1-B9EE-BD9DE0D2CB4A}`
    FOREIGN KEY (`xinco_core_data_id` )
    REFERENCES `xinco`.`xinco_core_data` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{1B8A7333-846D-4DA1-A8B4-D730053B6871}`
    FOREIGN KEY (`xinco_core_user_id` )
    REFERENCES `xinco`.`xinco_core_user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_language`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (1, 'n/a', 'unknown');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (2, 'en', 'English');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (3, 'de', 'German');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (4, 'fr', 'French');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (5, 'it', 'Italian');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (6, 'es', 'Spanish');
INSERT INTO `xinco_core_language` (`id`, `sign`, `designation`) VALUES (7, 'ru', 'Russian');

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_node`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_node` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `designation`, `status_number`) VALUES (1, null, 1, 'xincoRoot', 1);
INSERT INTO `xinco_core_node` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `designation`, `status_number`) VALUES (2, 1, 1, 'Trash', 1);
INSERT INTO `xinco_core_node` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `designation`, `status_number`) VALUES (3, 1, 1, 'Temp', 1);
INSERT INTO `xinco_core_node` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `designation`, `status_number`) VALUES (4, 1, 1, 'News', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_data_type`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_data_type` (`id`, `designation`, `description`) VALUES (1, 'general.data.type.file', 'general.data.type.file.description');
INSERT INTO `xinco_core_data_type` (`id`, `designation`, `description`) VALUES (2, 'general.data.type.text', 'general.data.type.text.description');
INSERT INTO `xinco_core_data_type` (`id`, `designation`, `description`) VALUES (3, 'general.data.type.URL', 'general.data.type.URL.description');
INSERT INTO `xinco_core_data_type` (`id`, `designation`, `description`) VALUES (4, 'general.data.type.contact', 'general.data.type.contact.description');

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_data`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_data` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `xinco_core_data_type_id`, `designation`, `status_number`) VALUES (1, 1, 2, 3, 'Apache License 2.0', 5);
INSERT INTO `xinco_core_data` (`id`, `xinco_core_node_id`, `xinco_core_language_id`, `xinco_core_data_type_id`, `designation`, `status_number`) VALUES (2, 1, 2, 3, 'xinco.org', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_user`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_user` VALUES (1, 'admin', MD5('admin'), 'Administrator', 'Xinco', 'admin@xinco.org', 1, 0, now());
INSERT INTO `xinco_core_user` VALUES(2, 'user', MD5('user'), 'User', 'Default', 'user@xinco.org', 1, 0, now());
INSERT INTO `xinco_core_user` VALUES (3, 'bluecubs', MD5('bluecubs'), 'System', 'User', 'info@bluecubs.com', 1, 0, now());

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_group`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_group` (`id`, `designation`, `status_number`) VALUES (1, 'general.group.admin', 1);
INSERT INTO `xinco_core_group` (`id`, `designation`, `status_number`) VALUES (2, 'general.group.allusers', 1);
INSERT INTO `xinco_core_group` (`id`, `designation`, `status_number`) VALUES (3, 'general.group.public', 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_ace`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (1, 1, NULL, 1, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (2, 1, NULL, 2, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (3, 1, NULL, 3, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (4, NULL, 1, 1, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (5, NULL, 1, 2, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (6, NULL, 1, 3, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (7, NULL, 2, 1, NULL, 1, 1, 1, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (8, NULL, 2, 2, NULL, 1, 1, 1, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (9, NULL, 2, 3, NULL, 1, 1, 1, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (10, 1, NULL, NULL, 1, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (11, 1, NULL, NULL, 2, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (12, NULL, 1, NULL, 1, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (13, NULL, 1, NULL, 2, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (14, NULL, 2, NULL, 1, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (15, NULL, 2, NULL, 2, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (16, 1, NULL, 4, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (17, NULL, 1, 4, NULL, 1, 1, 1, 1);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (18, NULL, 2, 4, NULL, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (19, NULL, 3, 1, NULL, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (20, NULL, 3, 4, NULL, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (21, NULL, 3, NULL, 1, 1, 0, 0, 0);
INSERT INTO `xinco_core_ace` (`id`, `xinco_core_user_id`, `xinco_core_group_id`, `xinco_core_node_id`, `xinco_core_data_id`, `read_permission`, `write_permission`, `execute_permission`, `admin_permission`) VALUES (22, NULL, 3, NULL, 2, 1, 0, 0, 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_user_has_xinco_core_group`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_user_has_xinco_core_group` (`xinco_core_user_id`, `xinco_core_group_id`, `status_number`) VALUES (1, 1, 1);
INSERT INTO `xinco_core_user_has_xinco_core_group` (`xinco_core_user_id`, `xinco_core_group_id`, `status_number`) VALUES (1, 2, 1);
INSERT INTO `xinco_core_user_has_xinco_core_group` (`xinco_core_user_id`, `xinco_core_group_id`, `status_number`) VALUES (2, 2, 1);
INSERT INTO `xinco_core_user_has_xinco_core_group` (`xinco_core_user_id`, `xinco_core_group_id`, `status_number`) VALUES (3, 2, 1);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_id`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (1, 'xinco_core_language', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (2, 'xinco_core_data_type', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (3, 'xinco_core_user', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (4, 'xinco_core_group', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (5, 'xinco_core_node', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (6, 'xinco_core_data', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (7, 'xinco_core_ace', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (8, 'xinco_core_log', 1000);
INSERT INTO `xinco_id` (`id`, `tablename`, `last_id`) VALUES (9, 'xinco_core_user_modified_record', 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_add_attribute`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_add_attribute` (`xinco_core_data_id`, `attribute_id`, `attrib_int`, `attrib_unsignedint`, `attrib_double`, `attrib_varchar`, `attrib_text`, `attrib_datetime`) VALUES (2, 1, 0, 0, 0, 'http://www.xinco.org', '', 0);
INSERT INTO `xinco_add_attribute` (`xinco_core_data_id`, `attribute_id`, `attrib_int`, `attrib_unsignedint`, `attrib_double`, `attrib_varchar`, `attrib_text`, `attrib_datetime`) VALUES (1, 1, 0, 0, 0, 'http://www.apache.org/licenses/LICENSE-2.0.html', '', 0);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_data_type_attribute`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 1, 'File Name', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 2, 'Size', 'unsignedint', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 3, 'Checksum', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 4, 'Revision_Model', 'unsignedint', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 5, 'Archiving_Model', 'unsignedint', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 6, 'Archiving_Date', 'datetime', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 7, 'Archiving_Days', 'unsignedint', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 8, 'Archiving_Location', 'text', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 9, 'Description', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 10, 'Keyword_1', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 11, 'Keyword_2', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (1, 12, 'Keyword_3', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (2, 1, 'Text', 'text', 0);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (3, 1, 'URL', 'varchar', 255);
INSERT INTO `xinco_core_data_type_attribute` (`xinco_core_data_type_id`, `attribute_id`, `designation`, `data_type`, `attr_size`) VALUES (4, 1, 'Salutation', 'varchar', 255);

COMMIT;

-- -----------------------------------------------------
-- Data for table `xinco`.`xinco_core_log`
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
USE `xinco`;
INSERT INTO `xinco_core_log` values(1, 1, 1, 1, now(), 'Creation!', 1, 0, 0, '');
INSERT INTO `xinco_core_log` values(2, 2, 1, 1, now(), 'Creation!', 1, 0, 0, '');

COMMIT;
