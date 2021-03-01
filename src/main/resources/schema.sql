CREATE DATABASE `dssmoe` /*!40100 DEFAULT CHARACTER SET latin1 */;

use dssmoe;

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NULL,
  `first_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
   PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FKa68196081fvovjhkek5m97n3y` (`role_id`),
  CONSTRAINT `FK859n2jvi8ivhui0rl0esws6o` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKa68196081fvovjhkek5m97n3y` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `userapproval` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `active` int(11) DEFAULT NULL,
   `userId` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `validTo` Date NULL,
   `validFrom` Date NULL,
   
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;




CREATE TABLE `dssmoe`.`certificatefile` (
  `idcertificatefile` int(11) NOT NULL AUTO_INCREMENT,
 
  `id` INT NOT NULL ,

  `fileName` VARCHAR(255) NULL ,

  `fileType` VARCHAR(45) NULL ,

  `fileData` TEXT NULL ,
  
  `passwordP12` VARCHAR(45) NULL ,
   
  PRIMARY KEY (`idcertificatefile`),
  CONSTRAINT `id` FOREIGN KEY (`id`) REFERENCES `userapproval` (`id`),
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;


CREATE TABLE `dssmoe`.`systemsetting` (
  `idsetting` int(11) NOT NULL AUTO_INCREMENT,
 
 
  `emailSMTP` VARCHAR(255) NULL ,

  `emailPort` VARCHAR(255) NULL ,

  `emailId` VARCHAR(255) NULL ,
  
  `emailPassword` VARCHAR(45) NULL ,
   
  PRIMARY KEY (`idsetting`)
 
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `dssmoe`.`token` (
  `token_id` int(11) NOT NULL AUTO_INCREMENT,
 
 
  `token` TEXT NULL ,

  `status` VARCHAR(255) NULL ,
   
  PRIMARY KEY (`token_id`)
 
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;




