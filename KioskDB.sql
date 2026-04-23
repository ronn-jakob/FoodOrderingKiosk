CREATE DATABASE  IF NOT EXISTS `kioskedp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `kioskedp`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: kioskedp
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `add_ons`
--

DROP TABLE IF EXISTS `add_ons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `add_ons` (
  `addonId` int NOT NULL AUTO_INCREMENT,
  `addonName` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `imagePath` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`addonId`),
  UNIQUE KEY `addonName` (`addonName`)
) ENGINE=InnoDB AUTO_INCREMENT=4005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `add_ons`
--

LOCK TABLES `add_ons` WRITE;
/*!40000 ALTER TABLE `add_ons` DISABLE KEYS */;
INSERT INTO `add_ons` VALUES (4001,'Extra Rice',20.00,'images/add1.png'),(4002,'Sundae',60.00,'images/des1.png'),(4003,'Apple Pie',50.00,'images/des2.png'),(4004,'Brownie',55.00,'images/des3.png');
/*!40000 ALTER TABLE `add_ons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `categoryId` int NOT NULL AUTO_INCREMENT,
  `categoryName` varchar(50) NOT NULL,
  PRIMARY KEY (`categoryId`),
  UNIQUE KEY `categoryName` (`categoryName`)
) ENGINE=InnoDB AUTO_INCREMENT=1006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1004,'Burgers'),(1002,'Chicken'),(1005,'Desserts'),(1003,'Pasta'),(1001,'What\'s New');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drinks`
--

DROP TABLE IF EXISTS `drinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drinks` (
  `drinkID` int NOT NULL AUTO_INCREMENT,
  `drinkName` varchar(100) NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `image_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`drinkID`),
  UNIQUE KEY `drinkName` (`drinkName`)
) ENGINE=InnoDB AUTO_INCREMENT=3005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drinks`
--

LOCK TABLES `drinks` WRITE;
/*!40000 ALTER TABLE `drinks` DISABLE KEYS */;
INSERT INTO `drinks` VALUES (3001,'Coke',30.00,'images/d1.png'),(3002,'Sprite',30.00,'images/d2.png'),(3003,'Iced Tea',25.00,'images/d3.png');
/*!40000 ALTER TABLE `drinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_items`
--

DROP TABLE IF EXISTS `menu_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_items` (
  `itemID` int NOT NULL AUTO_INCREMENT,
  `itemName` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `imagePath` varchar(500) DEFAULT NULL,
  `categoryId` int NOT NULL,
  PRIMARY KEY (`itemID`),
  UNIQUE KEY `itemName` (`itemName`),
  KEY `idx_category` (`categoryId`),
  CONSTRAINT `menu_items_ibfk_1` FOREIGN KEY (`categoryId`) REFERENCES `categories` (`categoryId`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2024 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_items`
--

LOCK TABLES `menu_items` WRITE;
/*!40000 ALTER TABLE `menu_items` DISABLE KEYS */;
INSERT INTO `menu_items` VALUES (2001,'Spicy Chicken Sandwich',65.00,'images/wn1.png',1001),(2002,'Truffle Mushroom Pasta',90.00,'images/wn2.png',1001),(2003,'Ube Sundae',35.00,'images/wn3.png',1001),(2004,'1 pc Chicken',99.00,'images/chicken1.png',1002),(2005,'1 pc Spicy Chicken',110.00,'images/chicken2.png',1002),(2006,'2 pc Chicken',140.00,'images/chicken3.png',1002),(2007,'2 pc Spicy Chicken',140.00,'images/chicken4.png',1002),(2008,'Spaghetti',60.00,'images/p1.png',1003),(2009,'Carbonara',50.00,'images/p2.png',1003),(2010,'Lasagna',55.00,'images/p3.png',1003),(2011,'Classic Burger',85.00,'images/b1.png',1004),(2012,'Cheese Burger',95.00,'images/b2.png',1004),(2013,'Bacon Burger',110.00,'images/b3.png',1004),(2014,'Double Patty Burger',130.00,'images/b4.png',1004),(2015,'Sundae Cup',60.00,'images/des1.png',1005),(2016,'Apple Pie',50.00,'images/des2.png',1005),(2017,'Brownie',55.00,'images/des3.png',1005);
/*!40000 ALTER TABLE `menu_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item_addons`
--

DROP TABLE IF EXISTS `order_item_addons`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item_addons` (
  `orderItemAddonId` int NOT NULL AUTO_INCREMENT,
  `orderItemId` int NOT NULL,
  `addonId` int DEFAULT NULL,
  PRIMARY KEY (`orderItemAddonId`),
  KEY `orderItemId` (`orderItemId`),
  KEY `addonId` (`addonId`),
  CONSTRAINT `order_item_addons_ibfk_1` FOREIGN KEY (`orderItemId`) REFERENCES `order_items` (`orderItemId`) ON DELETE CASCADE,
  CONSTRAINT `order_item_addons_ibfk_2` FOREIGN KEY (`addonId`) REFERENCES `add_ons` (`addonId`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item_addons`
--

LOCK TABLES `order_item_addons` WRITE;
/*!40000 ALTER TABLE `order_item_addons` DISABLE KEYS */;
INSERT INTO `order_item_addons` VALUES (1,1,4002),(2,2,4003),(3,2,4002),(4,4,4003),(5,4,4002),(7,6,4003),(8,6,4002),(9,7,4002),(10,8,4003),(11,10,4003),(12,31,4002),(14,36,4003),(15,44,4003),(16,48,4003),(17,49,4003),(18,50,4002),(19,52,4003),(21,60,4003),(22,61,4003),(23,62,4003),(24,65,4003),(25,66,4002),(26,66,4003),(28,75,4003),(29,76,4003),(30,77,4003);
/*!40000 ALTER TABLE `order_item_addons` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item_drinks`
--

DROP TABLE IF EXISTS `order_item_drinks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item_drinks` (
  `orderItemDrinkId` int NOT NULL AUTO_INCREMENT,
  `orderItemId` int NOT NULL,
  `drinkId` int DEFAULT NULL,
  PRIMARY KEY (`orderItemDrinkId`),
  KEY `orderItemId` (`orderItemId`),
  KEY `drinkId` (`drinkId`),
  CONSTRAINT `order_item_drinks_ibfk_1` FOREIGN KEY (`orderItemId`) REFERENCES `order_items` (`orderItemId`) ON DELETE CASCADE,
  CONSTRAINT `order_item_drinks_ibfk_2` FOREIGN KEY (`drinkId`) REFERENCES `drinks` (`drinkID`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item_drinks`
--

LOCK TABLES `order_item_drinks` WRITE;
/*!40000 ALTER TABLE `order_item_drinks` DISABLE KEYS */;
INSERT INTO `order_item_drinks` VALUES (1,2,3003),(2,3,3002),(3,4,3002),(4,6,3003),(5,8,3003),(6,10,3003),(7,20,3001),(8,21,3002),(9,22,3003),(10,23,3003),(12,52,3003),(14,60,3002),(15,58,3003),(16,61,3003),(17,66,3002),(19,74,3002),(20,75,3002);
/*!40000 ALTER TABLE `order_item_drinks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `orderItemId` int NOT NULL AUTO_INCREMENT,
  `orderId` int NOT NULL,
  `menuItemId` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `pricePerItem` decimal(10,2) NOT NULL,
  `itemTotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`orderItemId`),
  KEY `orderId` (`orderId`),
  KEY `menuItemId` (`menuItemId`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`menuItemId`) REFERENCES `menu_items` (`itemID`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,2003,3,95.00,285.00),(2,2,2001,1,200.00,200.00),(3,3,2002,1,120.00,120.00),(4,4,2001,2,205.00,410.00),(6,5,2002,1,225.00,225.00),(7,6,2003,1,95.00,95.00),(8,7,2003,1,110.00,110.00),(9,7,2003,3,35.00,105.00),(10,7,2005,2,185.00,370.00),(12,8,2001,2,65.00,130.00),(18,12,2002,3,90.00,270.00),(19,12,2003,3,35.00,105.00),(20,13,NULL,3,30.00,90.00),(21,13,NULL,2,30.00,60.00),(22,13,NULL,2,25.00,50.00),(23,14,NULL,1,25.00,25.00),(24,15,2001,3,65.00,195.00),(25,15,2002,4,90.00,360.00),(26,15,2003,1,35.00,35.00),(27,16,2001,2,65.00,130.00),(28,17,2003,1,35.00,35.00),(29,17,2002,1,90.00,90.00),(30,17,2001,1,65.00,65.00),(31,17,NULL,1,60.00,60.00),(32,18,2001,1,65.00,65.00),(33,18,2002,2,90.00,180.00),(34,18,2003,5,35.00,175.00),(36,20,2001,2,115.00,230.00),(37,21,2002,3,90.00,270.00),(38,21,2001,1,65.00,65.00),(39,21,2003,2,35.00,70.00),(40,22,2003,1,35.00,35.00),(41,23,2003,1,35.00,35.00),(42,24,2001,1,65.00,65.00),(43,25,2003,1,35.00,35.00),(44,26,2003,1,85.00,85.00),(45,27,2013,1,110.00,110.00),(46,27,2013,1,110.00,110.00),(48,29,2013,1,160.00,160.00),(49,30,2003,2,85.00,170.00),(50,31,2003,1,95.00,95.00),(51,31,2003,1,35.00,35.00),(52,32,2001,1,140.00,140.00),(53,32,2013,1,110.00,110.00),(55,33,2003,1,35.00,35.00),(56,3,2002,1,90.00,90.00),(57,3,2002,5,90.00,450.00),(58,3,2004,2,124.00,248.00),(60,3,2016,13,130.00,1690.00),(61,34,2003,1,110.00,110.00),(62,34,2001,1,115.00,115.00),(65,37,2003,1,85.00,85.00),(66,37,2015,1,200.00,200.00),(73,43,2016,1,50.00,50.00),(74,44,NULL,1,30.00,30.00),(75,45,2001,1,145.00,145.00),(76,45,2011,2,135.00,270.00),(77,45,2011,2,135.00,270.00),(78,46,2002,2,90.00,180.00),(79,47,2003,1,35.00,35.00),(80,47,2002,2,90.00,180.00),(81,48,2001,2,65.00,130.00),(82,49,2003,1,35.00,35.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `orderId` int NOT NULL AUTO_INCREMENT,
  `orderNumber` int NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `discountRate` decimal(5,2) DEFAULT '0.00',
  `discountAmount` decimal(10,2) DEFAULT '0.00',
  `finalTotal` decimal(10,2) NOT NULL,
  `orderStatus` enum('pending','completed','cancelled') DEFAULT 'pending',
  `createdAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `completedAt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`orderId`),
  UNIQUE KEY `orderNumber` (`orderNumber`),
  KEY `idx_order_number` (`orderNumber`),
  KEY `idx_order_status` (`orderStatus`),
  KEY `idx_order_date` (`createdAt`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,20001,285.00,0.00,0.00,285.00,'completed','2026-03-30 12:11:57',NULL),(2,20002,200.00,0.00,0.00,200.00,'cancelled','2026-03-30 12:42:05',NULL),(3,20003,2598.00,0.20,519.60,2078.40,'completed','2026-03-30 12:42:51',NULL),(4,20004,410.00,0.00,0.00,410.00,'completed','2026-03-30 12:54:22',NULL),(5,20005,225.00,0.00,0.00,225.00,'cancelled','2026-03-30 12:56:12',NULL),(6,20006,95.00,0.00,0.00,95.00,'completed','2026-03-30 12:57:22',NULL),(7,20007,585.00,0.00,0.00,585.00,'cancelled','2026-03-30 13:09:59',NULL),(8,20008,130.00,0.00,0.00,130.00,'completed','2026-03-31 11:59:25',NULL),(12,20009,375.00,0.00,0.00,375.00,'completed','2026-03-31 14:49:51',NULL),(13,20010,200.00,0.00,0.00,200.00,'cancelled','2026-03-31 14:52:15',NULL),(14,20011,25.00,0.00,0.00,25.00,'cancelled','2026-03-31 14:52:27',NULL),(15,20012,590.00,0.00,0.00,590.00,'cancelled','2026-03-31 14:57:11',NULL),(16,20013,130.00,0.20,26.00,104.00,'completed','2026-03-31 15:01:42',NULL),(17,20014,250.00,0.00,0.00,250.00,'completed','2026-04-04 12:25:28',NULL),(18,20015,420.00,0.00,0.00,420.00,'completed','2026-04-04 16:53:03',NULL),(19,20016,0.00,0.00,0.00,145.00,'cancelled','2026-04-04 16:54:34',NULL),(20,20017,230.00,0.00,0.00,230.00,'cancelled','2026-04-04 16:54:48',NULL),(21,20018,405.00,0.00,0.00,405.00,'completed','2026-04-04 16:55:22',NULL),(22,20019,35.00,0.00,0.00,35.00,'cancelled','2026-04-05 18:25:16',NULL),(23,20020,35.00,0.00,0.00,35.00,'cancelled','2026-04-05 19:01:35',NULL),(24,20021,65.00,0.00,0.00,65.00,'cancelled','2026-04-05 19:02:14',NULL),(25,20022,35.00,0.00,0.00,35.00,'cancelled','2026-04-05 19:03:46',NULL),(26,20023,85.00,0.00,0.00,85.00,'cancelled','2026-04-05 19:05:17',NULL),(27,20024,220.00,0.00,0.00,220.00,'cancelled','2026-04-05 19:05:47',NULL),(28,20025,0.00,0.00,0.00,35.00,'cancelled','2026-04-05 19:09:24',NULL),(29,20026,160.00,0.00,0.00,160.00,'completed','2026-04-05 19:09:59',NULL),(30,20027,170.00,0.00,0.00,170.00,'cancelled','2026-04-06 05:22:15',NULL),(31,20028,130.00,0.00,0.00,130.00,'cancelled','2026-04-06 05:42:33',NULL),(32,20029,250.00,0.20,50.00,200.00,'completed','2026-04-06 06:03:43',NULL),(33,20030,35.00,0.00,0.00,35.00,'completed','2026-04-06 06:18:21',NULL),(34,20031,225.00,0.00,0.00,225.00,'cancelled','2026-04-14 13:35:38',NULL),(37,20032,285.00,0.00,0.00,285.00,'cancelled','2026-04-14 13:38:50',NULL),(43,20033,50.00,0.00,0.00,50.00,'cancelled','2026-04-14 14:12:17',NULL),(44,20034,30.00,0.00,0.00,30.00,'cancelled','2026-04-14 14:15:20',NULL),(45,20035,685.00,0.00,0.00,685.00,'cancelled','2026-04-14 14:15:47',NULL),(46,20036,180.00,0.00,0.00,180.00,'completed','2026-04-14 16:46:32',NULL),(47,20037,215.00,0.00,0.00,215.00,'cancelled','2026-04-14 16:53:05',NULL),(48,20038,130.00,0.00,0.00,130.00,'completed','2026-04-14 17:29:26',NULL),(49,20039,35.00,0.00,0.00,35.00,'pending','2026-04-23 13:17:03',NULL);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `paymentId` int NOT NULL AUTO_INCREMENT,
  `orderId` int NOT NULL,
  `amountReceived` decimal(10,2) NOT NULL,
  `changeAmount` decimal(10,2) NOT NULL,
  `paymentDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`paymentId`),
  KEY `orderId` (`orderId`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`orderId`) REFERENCES `orders` (`orderId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,1,95.00,0.00,'2026-03-30 12:11:57'),(2,2,200.00,0.00,'2026-03-30 12:42:05'),(3,3,120.00,0.00,'2026-03-30 12:42:51'),(4,4,580.00,0.00,'2026-03-30 12:54:22'),(5,5,225.00,0.00,'2026-03-30 12:56:12'),(6,6,95.00,0.00,'2026-03-30 12:57:22'),(7,7,585.00,0.00,'2026-03-30 13:09:59'),(8,1,20000.00,19715.00,'2026-03-30 14:15:05'),(9,4,500.00,90.00,'2026-03-30 15:42:05'),(10,8,130.00,0.00,'2026-03-31 11:59:25'),(11,8,200.00,70.00,'2026-03-31 12:00:49'),(12,12,500.00,125.00,'2026-03-31 14:49:51'),(13,16,120.00,16.00,'2026-03-31 15:01:42'),(14,17,500.00,250.00,'2026-04-04 12:25:28'),(15,18,500.00,80.00,'2026-04-04 16:53:03'),(16,19,145.00,0.00,'2026-04-04 16:54:34'),(17,20,230.00,0.00,'2026-04-04 16:54:48'),(18,21,5000.00,4595.00,'2026-04-04 16:55:42'),(19,22,35.00,0.00,'2026-04-05 18:25:16'),(20,23,35.00,0.00,'2026-04-05 19:01:35'),(21,24,65.00,0.00,'2026-04-05 19:02:14'),(22,25,35.00,0.00,'2026-04-05 19:03:46'),(23,26,85.00,0.00,'2026-04-05 19:05:17'),(24,27,220.00,0.00,'2026-04-05 19:05:47'),(25,28,35.00,0.00,'2026-04-05 19:09:24'),(26,29,160.00,0.00,'2026-04-05 19:09:59'),(27,30,170.00,0.00,'2026-04-06 05:22:15'),(28,31,130.00,0.00,'2026-04-06 05:42:33'),(29,32,250.00,0.00,'2026-04-06 06:03:43'),(30,32,300.00,100.00,'2026-04-06 06:09:52'),(31,33,50.00,15.00,'2026-04-06 06:18:21'),(32,3,5000.00,2921.60,'2026-04-14 03:54:44'),(33,34,225.00,0.00,'2026-04-14 13:35:38'),(34,37,285.00,0.00,'2026-04-14 13:38:50'),(35,43,50.00,0.00,'2026-04-14 14:12:17'),(36,44,30.00,0.00,'2026-04-14 14:15:20'),(37,45,685.00,0.00,'2026-04-14 14:15:47'),(38,6,100.00,5.00,'2026-04-14 16:44:32'),(39,46,200.00,20.00,'2026-04-14 16:46:32'),(40,29,200.00,40.00,'2026-04-14 16:46:56'),(41,48,1000.00,870.00,'2026-04-14 17:29:26'),(42,49,35.00,0.00,'2026-04-23 13:17:03');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `userId` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','cashier') NOT NULL,
  `createdAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`userId`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'john','12345678','admin','2026-03-28 15:35:17'),(2,'asd','12345678','cashier','2026-03-30 15:51:05'),(3,'admin','qwertyuiop','admin','2026-03-30 15:55:29');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-23 21:48:42
