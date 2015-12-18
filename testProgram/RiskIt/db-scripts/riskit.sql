SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `riskit` ;
CREATE SCHEMA IF NOT EXISTS `riskit` DEFAULT CHARACTER SET latin1 ;
USE `riskit` ;

-- -----------------------------------------------------
-- Table `riskit`.`userrecord`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`userrecord` ;

CREATE TABLE IF NOT EXISTS `riskit`.`userrecord` (
  `NAME` CHAR(50) NULL DEFAULT NULL,
  `ZIP` CHAR(5) NULL DEFAULT NULL,
  `SSN` INT(11) NOT NULL,
  `AGE` INT(11) NULL DEFAULT NULL,
  `SEX` CHAR(50) NULL DEFAULT NULL,
  `MARITAL` CHAR(50) NULL DEFAULT NULL,
  `RACE` CHAR(50) NULL DEFAULT NULL,
  `TAXSTAT` CHAR(50) NULL DEFAULT NULL,
  `DETAIL` CHAR(100) NULL DEFAULT NULL,
  `HOUSEHOLDDETAIL` CHAR(100) NULL DEFAULT NULL,
  `FATHERORIGIN` CHAR(50) NULL DEFAULT NULL,
  `MOTHERORIGIN` CHAR(50) NULL DEFAULT NULL,
  `BIRTHCOUNTRY` CHAR(50) NULL DEFAULT NULL,
  `CITIZENSHIP` CHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`education`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`education` ;

CREATE TABLE IF NOT EXISTS `riskit`.`education` (
  `SSN` INT(11) NOT NULL,
  `EDUCATION` CHAR(50) NULL DEFAULT NULL,
  `EDUENROLL` CHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  CONSTRAINT `education_ibfk_1`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`employmentstat`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`employmentstat` ;

CREATE TABLE IF NOT EXISTS `riskit`.`employmentstat` (
  `SSN` INT(11) NOT NULL,
  `UNEMPLOYMENTREASON` CHAR(50) NULL DEFAULT NULL,
  `EMPLOYMENTSTAT` CHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  CONSTRAINT `employmentstat_ibfk_1`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`geo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`geo` ;

CREATE TABLE IF NOT EXISTS `riskit`.`geo` (
  `REGION` CHAR(50) NOT NULL,
  `RESSTATE` CHAR(50) NOT NULL,
  PRIMARY KEY (`RESSTATE`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`industry`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`industry` ;

CREATE TABLE IF NOT EXISTS `riskit`.`industry` (
  `INDUSTRYCODE` INT(11) NOT NULL,
  `INDUSTRY` CHAR(50) NULL DEFAULT NULL,
  `STABILITY` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`INDUSTRYCODE`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`investment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`investment` ;

CREATE TABLE IF NOT EXISTS `riskit`.`investment` (
  `SSN` INT(11) NOT NULL,
  `CAPITALGAINS` INT(11) NULL DEFAULT NULL,
  `CAPITALLOSSES` INT(11) NULL DEFAULT NULL,
  `STOCKDIVIDENDS` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  CONSTRAINT `investment_ibfk_1`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`occupation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`occupation` ;

CREATE TABLE IF NOT EXISTS `riskit`.`occupation` (
  `OCCUPATIONCODE` INT(11) NOT NULL,
  `OCCUPATION` CHAR(50) NULL DEFAULT NULL,
  `STABILITY` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`OCCUPATIONCODE`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`job`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`job` ;

CREATE TABLE IF NOT EXISTS `riskit`.`job` (
  `SSN` INT(11) NOT NULL,
  `WORKCLASS` CHAR(50) NULL DEFAULT NULL,
  `INDUSTRYCODE` INT(11) NULL DEFAULT NULL,
  `OCCUPATIONCODE` INT(11) NULL DEFAULT NULL,
  `UNIONMEMBER` CHAR(50) NULL DEFAULT NULL,
  `EMPLOYERSIZE` INT(11) NULL DEFAULT NULL,
  `WEEKWAGE` INT(11) NULL DEFAULT NULL,
  `SELFEMPLOYED` SMALLINT(6) NULL DEFAULT NULL,
  `WORKWEEKS` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  INDEX `OCCUPATIONCODE` (`OCCUPATIONCODE` ASC),
  INDEX `INDUSTRYCODE` (`INDUSTRYCODE` ASC),
  CONSTRAINT `job_ibfk_1`
    FOREIGN KEY (`OCCUPATIONCODE`)
    REFERENCES `riskit`.`occupation` (`OCCUPATIONCODE`),
  CONSTRAINT `job_ibfk_2`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`),
  CONSTRAINT `job_ibfk_3`
    FOREIGN KEY (`INDUSTRYCODE`)
    REFERENCES `riskit`.`industry` (`INDUSTRYCODE`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`migration`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`migration` ;

CREATE TABLE IF NOT EXISTS `riskit`.`migration` (
  `SSN` INT(11) NOT NULL,
  `MIGRATIONCODE` CHAR(50) NULL DEFAULT NULL,
  `MIGRATIONDISTANCE` CHAR(50) NULL DEFAULT NULL,
  `MIGRATIONMOVE` CHAR(50) NULL DEFAULT NULL,
  `MIGRATIONFROMSUNBELT` CHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  CONSTRAINT `migration_ibfk_1`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`stateabbv`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`stateabbv` ;

CREATE TABLE IF NOT EXISTS `riskit`.`stateabbv` (
  `ABBV` CHAR(2) NOT NULL,
  `NAME` CHAR(50) NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`wage`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`wage` ;

CREATE TABLE IF NOT EXISTS `riskit`.`wage` (
  `INDUSTRYCODE` INT(11) NOT NULL,
  `OCCUPATIONCODE` INT(11) NOT NULL,
  `MEANWEEKWAGE` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`INDUSTRYCODE`, `OCCUPATIONCODE`),
  INDEX `OCCUPATIONCODE` (`OCCUPATIONCODE` ASC),
  CONSTRAINT `wage_ibfk_1`
    FOREIGN KEY (`INDUSTRYCODE`)
    REFERENCES `riskit`.`industry` (`INDUSTRYCODE`),
  CONSTRAINT `wage_ibfk_2`
    FOREIGN KEY (`OCCUPATIONCODE`)
    REFERENCES `riskit`.`occupation` (`OCCUPATIONCODE`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`youth`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`youth` ;

CREATE TABLE IF NOT EXISTS `riskit`.`youth` (
  `SSN` INT(11) NOT NULL,
  `PARENTS` CHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`SSN`),
  CONSTRAINT `youth_ibfk_1`
    FOREIGN KEY (`SSN`)
    REFERENCES `riskit`.`userrecord` (`SSN`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `riskit`.`ziptable`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `riskit`.`ziptable` ;

CREATE TABLE IF NOT EXISTS `riskit`.`ziptable` (
  `ZIP` CHAR(5) NULL DEFAULT NULL,
  `CITY` CHAR(20) NULL DEFAULT NULL,
  `STATENAME` CHAR(20) NULL DEFAULT NULL,
  `COUNTY` CHAR(20) NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
