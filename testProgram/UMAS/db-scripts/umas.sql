SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `university` ;
CREATE SCHEMA IF NOT EXISTS `university` DEFAULT CHARACTER SET utf8 ;
USE `university` ;

-- -----------------------------------------------------
-- Table `university`.`gradingsystem`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`gradingsystem` ;

CREATE TABLE IF NOT EXISTS `university`.`gradingsystem` (
  `Grade` VARCHAR(2) NOT NULL,
  `GradeLevel` INT(2) NOT NULL,
  PRIMARY KEY (`Grade`),
  UNIQUE INDEX `GradeLevel_UNIQUE` (`GradeLevel` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`department`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`department` ;

CREATE TABLE IF NOT EXISTS `university`.`department` (
  `DepartmentID` INT(12) NOT NULL AUTO_INCREMENT,
  `DepartmentName` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`DepartmentID`, `DepartmentName`),
  UNIQUE INDEX `DepartmentID_UNIQUE` (`DepartmentID` ASC),
  UNIQUE INDEX `DepartmentName_UNIQUE` (`DepartmentName` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 27
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`courses`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`courses` ;

CREATE TABLE IF NOT EXISTS `university`.`courses` (
  `CourseID` INT(12) NOT NULL AUTO_INCREMENT,
  `CourseName` VARCHAR(45) NOT NULL,
  `DepartmentID` INT(12) NOT NULL,
  PRIMARY KEY (`CourseID`, `CourseName`),
  INDEX `Department_idx` (`DepartmentID` ASC),
  CONSTRAINT `DepartmentCourse`
    FOREIGN KEY (`DepartmentID`)
    REFERENCES `university`.`department` (`DepartmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 312
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`position`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`position` ;

CREATE TABLE IF NOT EXISTS `university`.`position` (
  `PositionID` INT(12) NOT NULL AUTO_INCREMENT,
  `PositionName` VARCHAR(45) NOT NULL,
  `PositionLevel` VARCHAR(3) NULL DEFAULT NULL,
  PRIMARY KEY (`PositionID`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`logindetails`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`logindetails` ;

CREATE TABLE IF NOT EXISTS `university`.`logindetails` (
  `Username` VARCHAR(20) NOT NULL,
  `Password` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Username`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`people`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`people` ;

CREATE TABLE IF NOT EXISTS `university`.`people` (
  `UIN` INT(12) NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(45) NOT NULL,
  `Username` VARCHAR(20) NOT NULL,
  `DepartmentID` INT(12) NOT NULL,
  `PositionID` INT(12) NOT NULL,
  PRIMARY KEY (`UIN`),
  INDEX `Username_idx` (`Username` ASC),
  INDEX `Department_idx` (`DepartmentID` ASC),
  INDEX `Position` (`PositionID` ASC),
  CONSTRAINT `Department`
    FOREIGN KEY (`DepartmentID`)
    REFERENCES `university`.`department` (`DepartmentID`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `Position`
    FOREIGN KEY (`PositionID`)
    REFERENCES `university`.`position` (`PositionID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Username`
    FOREIGN KEY (`Username`)
    REFERENCES `university`.`logindetails` (`Username`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 1000
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`semester`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`semester` ;

CREATE TABLE IF NOT EXISTS `university`.`semester` (
  `SemesterID` INT(12) NOT NULL AUTO_INCREMENT,
  `SemesterName` VARCHAR(45) NOT NULL,
  `SemesterYear` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `IsCurrent` INT(12) NOT NULL,
  PRIMARY KEY (`SemesterID`))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`coursesoffered`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`coursesoffered` ;

CREATE TABLE IF NOT EXISTS `university`.`coursesoffered` (
  `OfferID` INT(12) NOT NULL AUTO_INCREMENT,
  `CourseID` INT(12) NOT NULL,
  `SemesterID` INT(12) NOT NULL,
  `TotalCapacity` INT(11) NOT NULL,
  `SeatsFilled` INT(11) NOT NULL,
  `TaughtBy` INT(12) NOT NULL,
  PRIMARY KEY (`OfferID`),
  INDEX `CourseID_idx` (`CourseID` ASC),
  INDEX `PeopleID_idx` (`TaughtBy` ASC),
  INDEX `SemesterOffered_idx` (`SemesterID` ASC),
  CONSTRAINT `CourseID`
    FOREIGN KEY (`CourseID`)
    REFERENCES `university`.`courses` (`CourseID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Professor`
    FOREIGN KEY (`TaughtBy`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `Semester`
    FOREIGN KEY (`SemesterID`)
    REFERENCES `university`.`semester` (`SemesterID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 460
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`studentenrollment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`studentenrollment` ;

CREATE TABLE IF NOT EXISTS `university`.`studentenrollment` (
  `EnrollmentID` INT(12) NOT NULL AUTO_INCREMENT,
  `UIN` INT(12) NOT NULL,
  `OfferID` INT(12) NOT NULL,
  `Grade` VARCHAR(2) NOT NULL,
  PRIMARY KEY (`EnrollmentID`),
  INDEX `OfferID_idx` (`OfferID` ASC),
  INDEX `UIN_idx` (`UIN` ASC),
  INDEX `Grade_idx` (`Grade` ASC),
  CONSTRAINT `Grade`
    FOREIGN KEY (`Grade`)
    REFERENCES `university`.`gradingsystem` (`Grade`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `OfferID`
    FOREIGN KEY (`OfferID`)
    REFERENCES `university`.`coursesoffered` (`OfferID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `StudentUIN`
    FOREIGN KEY (`UIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 248
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`student`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`student` ;

CREATE TABLE IF NOT EXISTS `university`.`student` (
  `UIN` INT(12) NOT NULL,
  `GPA` DECIMAL(3,2) NOT NULL DEFAULT '4.00',
  `Level` VARCHAR(2) NOT NULL,
  PRIMARY KEY (`UIN`),
  INDEX `UIN_idx` (`UIN` ASC),
  CONSTRAINT `UIN`
    FOREIGN KEY (`UIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`acn6474221`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`acn6474221` ;

CREATE TABLE IF NOT EXISTS `university`.`acn6474221` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `ACN6474221studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ACN6474221studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`acn6474221structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`acn6474221structure` ;

CREATE TABLE IF NOT EXISTS `university`.`acn6474221structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`adc4724381`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`adc4724381` ;

CREATE TABLE IF NOT EXISTS `university`.`adc4724381` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `ADC4724381studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ADC4724381studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`adc4724381structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`adc4724381structure` ;

CREATE TABLE IF NOT EXISTS `university`.`adc4724381structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`aiy4034421`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`aiy4034421` ;

CREATE TABLE IF NOT EXISTS `university`.`aiy4034421` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `AIY4034421studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `AIY4034421studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`aiy4034421structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`aiy4034421structure` ;

CREATE TABLE IF NOT EXISTS `university`.`aiy4034421structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`aml4344431`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`aml4344431` ;

CREATE TABLE IF NOT EXISTS `university`.`aml4344431` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `AML4344431studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `AML4344431studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`aml4344431structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`aml4344431structure` ;

CREATE TABLE IF NOT EXISTS `university`.`aml4344431structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`applicationdetails`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`applicationdetails` ;

CREATE TABLE IF NOT EXISTS `university`.`applicationdetails` (
  `ApplicationID` INT(12) NOT NULL AUTO_INCREMENT,
  `ApplicantUIN` INT(12) NOT NULL,
  `WorkExperience` DOUBLE NULL DEFAULT '0',
  `Skillset1` VARCHAR(45) NULL DEFAULT NULL,
  `Skillset2` VARCHAR(45) NULL DEFAULT NULL,
  `Skillset3` VARCHAR(45) NULL DEFAULT NULL,
  `Skillset4` VARCHAR(45) NULL DEFAULT NULL,
  `Skillset5` VARCHAR(45) NULL DEFAULT NULL,
  `Scaledscore` DOUBLE NULL DEFAULT NULL,
  PRIMARY KEY (`ApplicationID`),
  UNIQUE INDEX `ApplicantUIN_UNIQUE` (`ApplicantUIN` ASC),
  INDEX `ApplicantUIN_idx` (`ApplicantUIN` ASC),
  CONSTRAINT `ApplicantUIN`
    FOREIGN KEY (`ApplicantUIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 168
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`classroom`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`classroom` ;

CREATE TABLE IF NOT EXISTS `university`.`classroom` (
  `ClassroomID` INT(12) NOT NULL AUTO_INCREMENT,
  `ClassroomName` VARCHAR(45) NOT NULL,
  `ClassroomLocation` VARCHAR(45) NOT NULL,
  `ClassroomCapacity` INT(12) NOT NULL,
  PRIMARY KEY (`ClassroomID`))
ENGINE = InnoDB
AUTO_INCREMENT = 59
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`timeslots`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`timeslots` ;

CREATE TABLE IF NOT EXISTS `university`.`timeslots` (
  `TimeSlotID` INT(12) NOT NULL AUTO_INCREMENT,
  `StartHour` INT(2) NOT NULL,
  `EndHour` INT(2) NOT NULL,
  `TimeslotType` INT(1) NOT NULL,
  PRIMARY KEY (`TimeSlotID`))
ENGINE = InnoDB
AUTO_INCREMENT = 40
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`courseschedule`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`courseschedule` ;

CREATE TABLE IF NOT EXISTS `university`.`courseschedule` (
  `OfferID` INT(12) NOT NULL,
  `TimeSlotID` INT(12) NOT NULL,
  `ClassroomID` INT(12) NOT NULL,
  PRIMARY KEY (`TimeSlotID`, `OfferID`, `ClassroomID`),
  UNIQUE INDEX `OfferID_UNIQUE` (`OfferID` ASC),
  INDEX `OfferID_idx` (`OfferID` ASC),
  INDEX `TimeID_idx` (`TimeSlotID` ASC),
  INDEX `ClassName_idx` (`ClassroomID` ASC),
  CONSTRAINT `ClassOfferID`
    FOREIGN KEY (`OfferID`)
    REFERENCES `university`.`coursesoffered` (`OfferID`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE,
  CONSTRAINT `ClassroomID`
    FOREIGN KEY (`ClassroomID`)
    REFERENCES `university`.`classroom` (`ClassroomID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `TimeID`
    FOREIGN KEY (`TimeSlotID`)
    REFERENCES `university`.`timeslots` (`TimeSlotID`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004101`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004101` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004101` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `Assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `Assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3004101studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3004101studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004101structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004101structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004101structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004171`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004171` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004171` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3004171studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3004171studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004171structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004171structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004171structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004471`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004471` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004471` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3004471studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3004471studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3004471structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3004471structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3004471structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014111`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014111` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014111` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3014111studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3014111studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014111structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014111structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014111structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014151`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014151` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014151` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3014151studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3014151studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014151structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014151structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014151structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014461`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014461` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014461` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS3014461studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS3014461studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs3014461structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs3014461structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs3014461structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs4804451`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs4804451` ;

CREATE TABLE IF NOT EXISTS `university`.`cs4804451` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS4804451studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS4804451studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs4804451structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs4804451structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs4804451structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs5024161`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs5024161` ;

CREATE TABLE IF NOT EXISTS `university`.`cs5024161` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CS5024161studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CS5024161studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`cs5024161structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`cs5024161structure` ;

CREATE TABLE IF NOT EXISTS `university`.`cs5024161structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`czd3164391`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`czd3164391` ;

CREATE TABLE IF NOT EXISTS `university`.`czd3164391` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `CZD3164391studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CZD3164391studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`czd3164391structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`czd3164391structure` ;

CREATE TABLE IF NOT EXISTS `university`.`czd3164391structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dcw2194321`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dcw2194321` ;

CREATE TABLE IF NOT EXISTS `university`.`dcw2194321` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `DCW2194321studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DCW2194321studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dcw2194321structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dcw2194321structure` ;

CREATE TABLE IF NOT EXISTS `university`.`dcw2194321structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dfk3694281`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dfk3694281` ;

CREATE TABLE IF NOT EXISTS `university`.`dfk3694281` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `DFK3694281studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DFK3694281studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dfk3694281structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dfk3694281structure` ;

CREATE TABLE IF NOT EXISTS `university`.`dfk3694281structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dtw2494571`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dtw2494571` ;

CREATE TABLE IF NOT EXISTS `university`.`dtw2494571` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `DTW2494571studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DTW2494571studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dtw2494571structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dtw2494571structure` ;

CREATE TABLE IF NOT EXISTS `university`.`dtw2494571structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dui5214261`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dui5214261` ;

CREATE TABLE IF NOT EXISTS `university`.`dui5214261` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `DUI5214261studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DUI5214261studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dui5214261structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dui5214261structure` ;

CREATE TABLE IF NOT EXISTS `university`.`dui5214261structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dui5214551`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dui5214551` ;

CREATE TABLE IF NOT EXISTS `university`.`dui5214551` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `DUI5214551studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `DUI5214551studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`dui5214551structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`dui5214551structure` ;

CREATE TABLE IF NOT EXISTS `university`.`dui5214551structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`eay2974511`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`eay2974511` ;

CREATE TABLE IF NOT EXISTS `university`.`eay2974511` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `EAY2974511studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `EAY2974511studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`eay2974511structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`eay2974511structure` ;

CREATE TABLE IF NOT EXISTS `university`.`eay2974511structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`eid5854491`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`eid5854491` ;

CREATE TABLE IF NOT EXISTS `university`.`eid5854491` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `EID5854491studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `EID5854491studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`eid5854491structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`eid5854491structure` ;

CREATE TABLE IF NOT EXISTS `university`.`eid5854491structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`emailedwaitlist`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`emailedwaitlist` ;

CREATE TABLE IF NOT EXISTS `university`.`emailedwaitlist` (
  `StudentUIN` INT(12) NOT NULL,
  `OfferID` INT(12) NOT NULL,
  `TimeEmailed` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`StudentUIN`, `OfferID`),
  INDEX `WaitCourseOfferID_idx` (`OfferID` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`employee`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`employee` ;

CREATE TABLE IF NOT EXISTS `university`.`employee` (
  `UIN` INT(12) NOT NULL,
  `Salary` DOUBLE NOT NULL,
  `OfficeAddress` VARCHAR(45) NULL DEFAULT NULL,
  `OfficeHours` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`UIN`),
  CONSTRAINT `EmpUIN`
    FOREIGN KEY (`UIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`etb5614531`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`etb5614531` ;

CREATE TABLE IF NOT EXISTS `university`.`etb5614531` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `ETB5614531studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `ETB5614531studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`etb5614531structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`etb5614531structure` ;

CREATE TABLE IF NOT EXISTS `university`.`etb5614531structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ffu6954351`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ffu6954351` ;

CREATE TABLE IF NOT EXISTS `university`.`ffu6954351` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `FFU6954351studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FFU6954351studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ffu6954351structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ffu6954351structure` ;

CREATE TABLE IF NOT EXISTS `university`.`ffu6954351structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`files` ;

CREATE TABLE IF NOT EXISTS `university`.`files` (
  `FileID` INT(12) NOT NULL AUTO_INCREMENT,
  `OfferID` INT(12) NOT NULL,
  `FileLocation` VARCHAR(100) NOT NULL,
  `FileName` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`FileID`),
  UNIQUE INDEX `FileName_UNIQUE` (`FileName` ASC),
  INDEX `OfferID_idx` (`OfferID` ASC),
  CONSTRAINT `FileOfferID`
    FOREIGN KEY (`OfferID`)
    REFERENCES `university`.`coursesoffered` (`OfferID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 50
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`fnv4824591`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`fnv4824591` ;

CREATE TABLE IF NOT EXISTS `university`.`fnv4824591` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `FNV4824591studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `FNV4824591studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`fnv4824591structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`fnv4824591structure` ;

CREATE TABLE IF NOT EXISTS `university`.`fnv4824591structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`gen3234271`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`gen3234271` ;

CREATE TABLE IF NOT EXISTS `university`.`gen3234271` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `GEN3234271studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `GEN3234271studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`gen3234271structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`gen3234271structure` ;

CREATE TABLE IF NOT EXISTS `university`.`gen3234271structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`gey2564341`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`gey2564341` ;

CREATE TABLE IF NOT EXISTS `university`.`gey2564341` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `GEY2564341studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `GEY2564341studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`gey2564341structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`gey2564341structure` ;

CREATE TABLE IF NOT EXISTS `university`.`gey2564341structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`hpy5794521`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`hpy5794521` ;

CREATE TABLE IF NOT EXISTS `university`.`hpy5794521` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `HPY5794521studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `HPY5794521studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`hpy5794521structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`hpy5794521structure` ;

CREATE TABLE IF NOT EXISTS `university`.`hpy5794521structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`hsb2264201`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`hsb2264201` ;

CREATE TABLE IF NOT EXISTS `university`.`hsb2264201` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `HSB2264201studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `HSB2264201studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`hsb2264201structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`hsb2264201structure` ;

CREATE TABLE IF NOT EXISTS `university`.`hsb2264201structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ipq5514561`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ipq5514561` ;

CREATE TABLE IF NOT EXISTS `university`.`ipq5514561` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `IPQ5514561studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `IPQ5514561studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ipq5514561structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ipq5514561structure` ;

CREATE TABLE IF NOT EXISTS `university`.`ipq5514561structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`iuv2764331`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`iuv2764331` ;

CREATE TABLE IF NOT EXISTS `university`.`iuv2764331` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `IUV2764331studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `IUV2764331studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`iuv2764331structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`iuv2764331structure` ;

CREATE TABLE IF NOT EXISTS `university`.`iuv2764331structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`jobpostings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`jobpostings` ;

CREATE TABLE IF NOT EXISTS `university`.`jobpostings` (
  `JobID` INT(12) NOT NULL AUTO_INCREMENT,
  `PostedByUIN` INT(12) NOT NULL,
  `JobInDepartment` INT(12) NOT NULL,
  `ReqdMinimumGPA` DOUBLE NULL DEFAULT NULL,
  `ReqdMinimumWorkExperience` DOUBLE NULL DEFAULT NULL,
  `ReqdSkillset1` VARCHAR(45) NULL DEFAULT NULL,
  `ReqdSkillset2` VARCHAR(45) NULL DEFAULT NULL,
  `ReqdSkillset3` VARCHAR(45) NULL DEFAULT NULL,
  `ReqdSkillset4` VARCHAR(45) NULL DEFAULT NULL,
  `ReqdSkillset5` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`JobID`),
  INDEX `PostedByUIN_idx` (`PostedByUIN` ASC),
  INDEX `JobInDepartment_idx` (`JobInDepartment` ASC),
  CONSTRAINT `JobInDepartment`
    FOREIGN KEY (`JobInDepartment`)
    REFERENCES `university`.`department` (`DepartmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `PostedByUIN`
    FOREIGN KEY (`PostedByUIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 91
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`jobroster`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`jobroster` ;

CREATE TABLE IF NOT EXISTS `university`.`jobroster` (
  `JobID` INT(11) NULL DEFAULT NULL,
  `UIN` INT(11) NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`kbv5814581`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`kbv5814581` ;

CREATE TABLE IF NOT EXISTS `university`.`kbv5814581` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `KBV5814581studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `KBV5814581studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`kbv5814581structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`kbv5814581structure` ;

CREATE TABLE IF NOT EXISTS `university`.`kbv5814581structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`knf9814231`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`knf9814231` ;

CREATE TABLE IF NOT EXISTS `university`.`knf9814231` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `KNF9814231studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `KNF9814231studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`knf9814231structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`knf9814231structure` ;

CREATE TABLE IF NOT EXISTS `university`.`knf9814231structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`lzc3794541`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`lzc3794541` ;

CREATE TABLE IF NOT EXISTS `university`.`lzc3794541` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `LZC3794541studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `LZC3794541studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`lzc3794541structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`lzc3794541structure` ;

CREATE TABLE IF NOT EXISTS `university`.`lzc3794541structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1014141`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1014141` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1014141` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `exam1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `MEC1014141studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `MEC1014141studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1014141structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1014141structure` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1014141structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1014191`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1014191` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1014191` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `MEC1014191studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `MEC1014191studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1014191structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1014191structure` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1014191structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1104181`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1104181` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1104181` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `MEC1104181studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `MEC1104181studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`mec1104181structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`mec1104181structure` ;

CREATE TABLE IF NOT EXISTS `university`.`mec1104181structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`msz3804501`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`msz3804501` ;

CREATE TABLE IF NOT EXISTS `university`.`msz3804501` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `MSZ3804501studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `MSZ3804501studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`msz3804501structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`msz3804501structure` ;

CREATE TABLE IF NOT EXISTS `university`.`msz3804501structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`names`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`names` ;

CREATE TABLE IF NOT EXISTS `university`.`names` (
  ` name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (` name`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`names1`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`names1` ;

CREATE TABLE IF NOT EXISTS `university`.`names1` (
  `name1` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`name1`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`names2`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`names2` ;

CREATE TABLE IF NOT EXISTS `university`.`names2` (
  `names2` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`names2`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`namesdept`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`namesdept` ;

CREATE TABLE IF NOT EXISTS `university`.`namesdept` (
  `names` VARCHAR(45) NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`nqs6184311`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`nqs6184311` ;

CREATE TABLE IF NOT EXISTS `university`.`nqs6184311` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `NQS6184311studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `NQS6184311studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`nqs6184311structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`nqs6184311structure` ;

CREATE TABLE IF NOT EXISTS `university`.`nqs6184311structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ocd3274401`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ocd3274401` ;

CREATE TABLE IF NOT EXISTS `university`.`ocd3274401` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `OCD3274401studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `OCD3274401studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ocd3274401structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ocd3274401structure` ;

CREATE TABLE IF NOT EXISTS `university`.`ocd3274401structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ogh9354411`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ogh9354411` ;

CREATE TABLE IF NOT EXISTS `university`.`ogh9354411` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `OGH9354411studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `OGH9354411studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`ogh9354411structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`ogh9354411structure` ;

CREATE TABLE IF NOT EXISTS `university`.`ogh9354411structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`oqa7284301`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`oqa7284301` ;

CREATE TABLE IF NOT EXISTS `university`.`oqa7284301` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `OQA7284301studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `OQA7284301studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`oqa7284301structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`oqa7284301structure` ;

CREATE TABLE IF NOT EXISTS `university`.`oqa7284301structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`pbi3524481`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`pbi3524481` ;

CREATE TABLE IF NOT EXISTS `university`.`pbi3524481` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `PBI3524481studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `PBI3524481studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`pbi3524481structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`pbi3524481structure` ;

CREATE TABLE IF NOT EXISTS `university`.`pbi3524481structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`qgz5264251`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`qgz5264251` ;

CREATE TABLE IF NOT EXISTS `university`.`qgz5264251` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `QGZ5264251studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `QGZ5264251studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`qgz5264251structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`qgz5264251structure` ;

CREATE TABLE IF NOT EXISTS `university`.`qgz5264251structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`sap9134211`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`sap9134211` ;

CREATE TABLE IF NOT EXISTS `university`.`sap9134211` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `SAP9134211studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `SAP9134211studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`sap9134211structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`sap9134211structure` ;

CREATE TABLE IF NOT EXISTS `university`.`sap9134211structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`skillsetlist`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`skillsetlist` ;

CREATE TABLE IF NOT EXISTS `university`.`skillsetlist` (
  `SkillsetID` INT(12) NOT NULL AUTO_INCREMENT,
  `SkillsetName` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`SkillsetID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`teachingassistant`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`teachingassistant` ;

CREATE TABLE IF NOT EXISTS `university`.`teachingassistant` (
  `TaUIN` INT(12) NOT NULL,
  `OfferID` INT(12) NOT NULL,
  `TaOfficeHours` VARCHAR(45) NOT NULL DEFAULT 'TBD',
  `TaOfficeLocation` VARCHAR(45) NOT NULL DEFAULT 'TBD',
  PRIMARY KEY (`TaUIN`, `OfferID`),
  INDEX `TaUIN_idx` (`TaUIN` ASC),
  INDEX `TaOfferID_idx` (`OfferID` ASC),
  CONSTRAINT `TaOfferID`
    FOREIGN KEY (`OfferID`)
    REFERENCES `university`.`coursesoffered` (`OfferID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `TaUIN`
    FOREIGN KEY (`TaUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`tiq2754371`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`tiq2754371` ;

CREATE TABLE IF NOT EXISTS `university`.`tiq2754371` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `TIQ2754371studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `TIQ2754371studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`tiq2754371structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`tiq2754371structure` ;

CREATE TABLE IF NOT EXISTS `university`.`tiq2754371structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`vsf1464291`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`vsf1464291` ;

CREATE TABLE IF NOT EXISTS `university`.`vsf1464291` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `VSF1464291studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `VSF1464291studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`vsf1464291structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`vsf1464291structure` ;

CREATE TABLE IF NOT EXISTS `university`.`vsf1464291structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`waitlist`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`waitlist` ;

CREATE TABLE IF NOT EXISTS `university`.`waitlist` (
  `UIN` INT(12) NOT NULL,
  `OfferID` INT(12) NOT NULL,
  `QueuePos` INT(12) NOT NULL,
  PRIMARY KEY (`UIN`, `OfferID`, `QueuePos`),
  INDEX `WaitOfferID_idx` (`OfferID` ASC),
  CONSTRAINT `WaitIUIN`
    FOREIGN KEY (`UIN`)
    REFERENCES `university`.`people` (`UIN`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `WaitOfferID`
    FOREIGN KEY (`OfferID`)
    REFERENCES `university`.`coursesoffered` (`OfferID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`whm1644361`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`whm1644361` ;

CREATE TABLE IF NOT EXISTS `university`.`whm1644361` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `WHM1644361studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `WHM1644361studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`whm1644361structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`whm1644361structure` ;

CREATE TABLE IF NOT EXISTS `university`.`whm1644361structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`wym8514441`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`wym8514441` ;

CREATE TABLE IF NOT EXISTS `university`.`wym8514441` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `WYM8514441studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `WYM8514441studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`wym8514441structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`wym8514441structure` ;

CREATE TABLE IF NOT EXISTS `university`.`wym8514441structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`yyn6874241`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`yyn6874241` ;

CREATE TABLE IF NOT EXISTS `university`.`yyn6874241` (
  `StudentUIN` INT(12) NOT NULL,
  `StudentEnrollmentID` INT(12) NOT NULL,
  `assign1` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign2` DECIMAL(4,1) NULL DEFAULT '0.0',
  `assign3` DECIMAL(4,1) NULL DEFAULT '0.0',
  PRIMARY KEY (`StudentUIN`),
  INDEX `StudentID_idx` (`StudentUIN` ASC),
  INDEX `StudentEnrollmentID_idx` (`StudentEnrollmentID` ASC),
  CONSTRAINT `YYN6874241studentEnrollmentID`
    FOREIGN KEY (`StudentEnrollmentID`)
    REFERENCES `university`.`studentenrollment` (`EnrollmentID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `YYN6874241studentID`
    FOREIGN KEY (`StudentUIN`)
    REFERENCES `university`.`student` (`UIN`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `university`.`yyn6874241structure`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `university`.`yyn6874241structure` ;

CREATE TABLE IF NOT EXISTS `university`.`yyn6874241structure` (
  `ExamName` VARCHAR(20) NOT NULL DEFAULT '',
  `TotalMarks` INT(12) NULL DEFAULT NULL,
  PRIMARY KEY (`ExamName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
