-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 07, 2026 at 04:17 PM
-- Server version: 8.0.42
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `weekend_holiday`
--

-- --------------------------------------------------------

--
-- Table structure for table `itinerary_items`
--

CREATE TABLE `itinerary_items` (
  `id` int NOT NULL,
  `trip_id` int NOT NULL,
  `day_number` int NOT NULL,
  `item_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `item_type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `parent_spot_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `itinerary_items`
--

INSERT INTO `itinerary_items` (`id`, `trip_id`, `day_number`, `item_name`, `item_type`, `parent_spot_name`) VALUES
(5, 3, 1, 'Golden Temple', 'spot', NULL),
(6, 3, 2, 'Jallainwala Bagh', 'spot', NULL),
(10, 5, 1, 'Charminar', 'spot', NULL),
(11, 5, 1, 'iqbal hotel', 'attraction', 'Charminar'),
(12, 5, 2, 'Golconda Fort', 'spot', NULL),
(13, 5, 2, 'Durgam Cheruvu', 'attraction', 'Golconda Fort'),
(14, 5, 2, 'Sai Shruthi Guest House', 'attraction', 'Golconda Fort'),
(15, 6, 1, 'Golden Temple', 'spot', NULL),
(16, 6, 1, 'Hotel Golden Tower', 'attraction', 'Golden Temple'),
(17, 6, 1, 'Maharaja Ranjit Singh', 'attraction', 'Golden Temple'),
(18, 6, 2, 'Wagah Border Ceremony', 'spot', NULL),
(19, 6, 2, 'Bharawan Da Dhaba', 'attraction', 'Wagah Border Ceremony'),
(20, 7, 1, 'Golconda Fort', 'spot', NULL),
(21, 7, 1, 'Sai Shruthi Guest House', 'attraction', 'Golconda Fort'),
(22, 7, 2, 'Ramoji Film City', 'spot', NULL),
(23, 8, 1, 'Meenakshi Amman Temple', 'spot', NULL),
(24, 8, 1, 'Ana Menakshree Restaurant', 'attraction', 'Meenakshi Amman Temple'),
(25, 8, 2, 'Gandhi Memorial Museum', 'spot', NULL),
(26, 9, 1, 'Gateway Of India', 'spot', NULL),
(27, 9, 2, 'Elephanta Caves', 'spot', NULL),
(28, 10, 1, 'Golden Temple', 'spot', NULL),
(29, 10, 2, 'Wagah Border Ceremony', 'spot', NULL),
(30, 11, 1, 'Meenakshi Amman Temple', 'spot', NULL),
(31, 12, 1, 'Meenakshi Amman Temple', 'spot', NULL),
(32, 12, 2, 'Gandhi Memorial Museum', 'spot', NULL),
(33, 13, 1, 'Wagah Border Ceremony', 'spot', NULL),
(34, 13, 2, 'Jallainwala Bagh', 'spot', NULL),
(35, 14, 1, 'Golden Temple', 'spot', NULL),
(36, 14, 2, 'Wagah Border Ceremony', 'spot', NULL),
(37, 14, 2, 'Café Kebab', 'attraction', 'Wagah Border Ceremony'),
(38, 14, 2, 'Maharaja Ranjit Singh', 'attraction', 'Wagah Border Ceremony');

-- --------------------------------------------------------

--
-- Table structure for table `itinerary_spots`
--

CREATE TABLE `itinerary_spots` (
  `id` int NOT NULL,
  `trip_id` int NOT NULL,
  `day_number` int NOT NULL,
  `spot_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `password_reset`
--

CREATE TABLE `password_reset` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `otp` varchar(6) COLLATE utf8mb4_general_ci NOT NULL,
  `expiry` datetime NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `places`
--

CREATE TABLE `places` (
  `id` int NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `suitable_months` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_monsoon_destination` tinyint(1) NOT NULL DEFAULT '0',
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `toll_cost` decimal(10,2) DEFAULT '0.00',
  `parking_cost` decimal(10,2) DEFAULT '0.00',
  `avg_budget` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `local_language` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `hotel_high_cost` decimal(10,2) DEFAULT '0.00',
  `hotel_std_cost` decimal(10,2) DEFAULT '0.00',
  `hotel_low_cost` decimal(10,2) DEFAULT '0.00',
  `food_high_veg` decimal(10,2) DEFAULT '0.00',
  `food_high_nonveg` decimal(10,2) DEFAULT '0.00',
  `food_high_combo` decimal(10,2) DEFAULT '0.00',
  `food_std_veg` decimal(10,2) DEFAULT '0.00',
  `food_std_nonveg` decimal(10,2) DEFAULT '0.00',
  `food_std_combo` decimal(10,2) DEFAULT '0.00',
  `food_low_veg` decimal(10,2) DEFAULT '0.00',
  `food_low_nonveg` decimal(10,2) DEFAULT '0.00',
  `food_low_combo` decimal(10,2) DEFAULT '0.00',
  `flight_example` text COLLATE utf8mb4_general_ci,
  `train_example` text COLLATE utf8mb4_general_ci,
  `bus_example` text COLLATE utf8mb4_general_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `places`
--

INSERT INTO `places` (`id`, `name`, `location`, `suitable_months`, `is_monsoon_destination`, `latitude`, `longitude`, `toll_cost`, `parking_cost`, `avg_budget`, `local_language`, `hotel_high_cost`, `hotel_std_cost`, `hotel_low_cost`, `food_high_veg`, `food_high_nonveg`, `food_high_combo`, `food_std_veg`, `food_std_nonveg`, `food_std_combo`, `food_low_veg`, `food_low_nonveg`, `food_low_combo`, `flight_example`, `train_example`, `bus_example`, `created_at`) VALUES
(27, 'Amritsar', 'Punjab, India', 'Jan,Feb,Oct,Nov,Dec', 0, 31.635698622997168, 74.87869262695312, 800.00, 150.00, 'Avg budget (per person/day): Rs.2500-Rs.4500', 'Language:Punjabi,Urdu,Hindi', 8000.00, 3500.00, 1200.00, 1500.00, 2000.00, 1800.00, 700.00, 900.00, 800.00, 300.00, 450.00, 350.00, 'IndiGo 6E-2024, Vistara UK-691 (from Delhi)', '12013 New Delhi-Amritsar Shatabdi Express', 'Punjab Roadways Volvo, PRTC Bus (from Delhi/Chandigarh)', '2025-10-08 10:20:19'),
(28, 'Madurai', 'Tamil Nadu, India', 'Jan,Feb,Mar,Oct,Nov,Dec', 0, 9.926157734001194, 78.11408042907715, 750.00, 120.00, 'Avg budget (per person/day): Rs.2200-Rs.3200', 'Language:Tamil', 7500.00, 3000.00, 1000.00, 1200.00, 1600.00, 1400.00, 600.00, 800.00, 700.00, 250.00, 400.00, 300.00, 'IndiGo 6E-7188 (from Chennai)', '12635 Vaigai Superfast Express (from Chennai)', 'SETC Ultra Deluxe, KPN Travels (from Chennai/Bengaluru)', '2025-10-08 16:37:20'),
(29, 'Mumbai', 'Maharashtra, India', 'Jan,Feb,Mar,Oct,Nov,Dec', 1, 19.055059930787763, 72.86913871765137, 400.00, 400.00, 'Avg budget (per person/day): Rs.4000-Rs.5500', 'Marathi (Hindi and English are widely spoken)', 15000.00, 5500.00, 2000.00, 3000.00, 4000.00, 3500.00, 1000.00, 1400.00, 1200.00, 400.00, 550.00, 450.00, 'Vistara UK-981 (from Delhi)', '12951 Mumbai Rajdhani Express (from New Delhi)', 'MSRTC Shivneri Volvo, Neeta Bus (from Pune)', '2025-10-08 16:51:40'),
(30, 'Hyderabad', 'Telangana, India', 'Jan,Feb,Mar,Oct,Nov,Dec', 1, 17.360632976393333, 78.47405433654785, 850.00, 200.00, 'Avg budget (per person/day): Rs.2800-Rs.4000', 'Telugu (Urdu, Hindi, and English are also widely used)', 9000.00, 3500.00, 1200.00, 1800.00, 2500.00, 2200.00, 800.00, 1100.00, 900.00, 350.00, 500.00, 400.00, 'IndiGo 6E-6018 (from Bengaluru)', '12026 Secunderabad-Pune Shatabdi Express', 'TSRTC Garuda Plus, Orange Tours & Travels (from Vijayawada/Bengaluru)', '2025-10-08 17:07:30');

-- --------------------------------------------------------

--
-- Table structure for table `place_images`
--

CREATE TABLE `place_images` (
  `id` int NOT NULL,
  `place_id` int NOT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `place_images`
--

INSERT INTO `place_images` (`id`, `place_id`, `image_url`) VALUES
(51, 27, 'uploads/img_68e63ae3eb3152.84179435_image0.jpg'),
(52, 27, 'uploads/img_68e63ae3eb9505.59974665_image1.jpg'),
(53, 27, 'uploads/img_68e63ae3ebbf40.63052530_image2.jpg'),
(54, 28, 'uploads/img_68e693404417a5.75839838_image0.jpg'),
(55, 28, 'uploads/img_68e69340444cc4.86811287_image1.jpg'),
(56, 28, 'uploads/img_68e69340447340.58671481_image2.jpg'),
(57, 29, 'uploads/img_68e6969ce9b8b4.45262379_image0.jpg'),
(58, 29, 'uploads/img_68e6969ce9e103.89726082_image1.jpg'),
(59, 29, 'uploads/img_68e6969cea11c3.73525129_image2.jpg'),
(60, 30, 'uploads/img_68e69a52822780.17981024_image0.jpg'),
(61, 30, 'uploads/img_68e69a52825479.87458236_image1.jpg'),
(62, 30, 'uploads/img_68e69a52828a12.00287728_image2.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `place_id` int NOT NULL,
  `trip_id` int NOT NULL,
  `category` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `rating` float NOT NULL,
  `review_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reviews`
--

INSERT INTO `reviews` (`id`, `user_id`, `place_id`, `trip_id`, `category`, `rating`, `review_text`, `created_at`) VALUES
(1, 12, 28, 8, 'Place', 4.5, '', '2025-10-17 04:36:24'),
(2, 12, 28, 8, 'Hotel', 4, '', '2025-10-17 04:36:24'),
(3, 12, 30, 5, 'Place', 3, '', '2025-10-17 04:59:45'),
(4, 12, 30, 7, 'Place', 5, '', '2025-10-17 14:52:41'),
(5, 12, 30, 7, 'Hotel', 5, '', '2025-10-17 14:52:41'),
(6, 12, 27, 3, 'Trip', 1, 'e', '2025-10-18 12:10:26'),
(7, 12, 29, 9, 'Trip', 3, 'good place', '2025-10-18 14:48:06');

-- --------------------------------------------------------

--
-- Table structure for table `saved_trips`
--

CREATE TABLE `saved_trips` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `place_id` int NOT NULL,
  `saved_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `test_table`
--

CREATE TABLE `test_table` (
  `id` int NOT NULL,
  `test_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `test_table`
--

INSERT INTO `test_table` (`id`, `test_name`) VALUES
(1, 'Hello World'),
(2, 'Hello World');

-- --------------------------------------------------------

--
-- Table structure for table `top_spots`
--

CREATE TABLE `top_spots` (
  `id` int NOT NULL,
  `place_id` int NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `top_spots`
--

INSERT INTO `top_spots` (`id`, `place_id`, `name`, `description`, `latitude`, `longitude`) VALUES
(4, 27, 'Golden Temple', 'The spiritual and cultural center for the Sikh religion. This stunning golden gurdwara is surrounded by the Amrit Sarovar, a holy tank, offering a serene and powerful experience 24/7.', 31.62656364024963, 74.87714767456055),
(5, 27, 'Wagah Border Ceremony', 'Attari wagah border crossing and Experience the \"Beating Retreat\" ceremony at the India-Pakistan border. This daily military practice is a powerful display of national pride, filled with elaborate drills and a vibrant, patriotic atmosphere.', 31.62371334202922, 74.87929344177246),
(6, 27, 'Jallainwala Bagh', 'A public garden and memorial of national importance, preserving the memory of those wounded and killed in the tragic 1919 massacre. A solemn and historically significant site.', 31.620862956492942, 74.88006591796875),
(7, 28, 'Meenakshi Amman Temple', 'Heart and soul of madurai ', 9.919563090067058, 78.11880111694336),
(8, 28, 'Thirumalai Nayakkar Palace', '17th century palace showcasing a fusion of Dravidian', 9.507984402072921, 77.6323127746582),
(9, 28, 'Gandhi Memorial Museum', 'This museum is one of the india\'s five national gandhi museums', 9.929962275855871, 78.13854217529297),
(10, 29, 'Gateway Of India', 'This colossal arch was built to commemorate the visit of King George V and Queen Mary', 18.92203857475812, 72.83454895019531),
(11, 29, 'Marine Drive', 'Queen\'s Necklace , it\'s the perfect place to witness a stunning sunset', 18.941523628362603, 72.82382011413574),
(12, 29, 'Elephanta Caves', 'These ancient rock-cut caves are dedicated to the Hindu god Shiva and feature magnificent sculptures and reliefs, including the famous Trimurti Sadashiva.', 18.96311690600804, 72.93188095092773);

-- --------------------------------------------------------

--
-- Table structure for table `transport_options`
--

CREATE TABLE `transport_options` (
  `id` int NOT NULL,
  `place_id` int NOT NULL,
  `icon` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `info` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_options`
--

INSERT INTO `transport_options` (`id`, `place_id`, `icon`, `type`, `info`) VALUES
(4, 27, 'ic_flight', 'Flight', 'Well-connected via Sri Guru Ram Dass Jee International Airport (ATQ) with daily flights from major Indian and international cities.'),
(5, 27, 'ic_train', 'Train', 'Amritsar Junction (ASR) is a major railway station with excellent connectivity, including high-speed Shatabdi Express services to Delhi.'),
(6, 27, 'ic_bus', 'Bus', 'Robust network of government and private buses connecting to cities like Delhi, Chandigarh, and destinations in Himachal and J&K.'),
(7, 28, 'ic_flight', 'Flight', 'IndiGo 6E-7188 (from Chennai)'),
(8, 28, 'ic_train', 'Train', '12635 Vaigai Superfast Express (from Chennai)'),
(9, 28, 'ic_bus', 'Bus', 'SETC Ultra Deluxe, KPN Travels (from Chennai/Bengaluru)'),
(10, 29, 'ic_flight', 'Flight', 'Vistara UK-981 (from Delhi)'),
(11, 29, 'ic_bus', 'Bus', 'MSRTC Shivneri Volvo, Neeta Bus (from Pune)'),
(12, 29, 'ic_train', 'Train', '12951 Mumbai Rajdhani Express (from New Delhi)'),
(18, 30, 'ic_flight', 'Flight', 'IndiGo 6E-6018 (from Bengaluru)'),
(19, 30, 'ic_train', 'Train', '12026 Secunderabad-Pune Shatabdi Express'),
(20, 30, 'ic_bus', 'Bus', 'TSRTC Garuda Plus, Orange Tours & Travels (from Vijayawada/Bengaluru)');

-- --------------------------------------------------------

--
-- Table structure for table `travel_tips`
--

CREATE TABLE `travel_tips` (
  `id` int NOT NULL,
  `tip_title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `tip_content` text COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `travel_tips`
--

INSERT INTO `travel_tips` (`id`, `tip_title`, `tip_content`, `created_at`) VALUES
(2, 'Plan', 'Plan ahead of time', '2025-10-19 12:15:42');

-- --------------------------------------------------------

--
-- Table structure for table `trips`
--

CREATE TABLE `trips` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `place_id` int NOT NULL,
  `place_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `place_location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `num_people` int NOT NULL,
  `num_days` int NOT NULL,
  `transport_cost` decimal(10,2) DEFAULT '0.00',
  `food_cost` decimal(10,2) DEFAULT '0.00',
  `hotel_cost` decimal(10,2) DEFAULT '0.00',
  `other_cost` decimal(10,2) DEFAULT '0.00',
  `total_budget` decimal(10,2) DEFAULT '0.00',
  `status` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'future',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `media_folder` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trips`
--

INSERT INTO `trips` (`id`, `user_id`, `place_id`, `place_name`, `place_location`, `start_date`, `end_date`, `num_people`, `num_days`, `transport_cost`, `food_cost`, `hotel_cost`, `other_cost`, `total_budget`, `status`, `created_at`, `media_folder`) VALUES
(3, 12, 27, 'Amritsar', 'Punjab, India', '2025-10-14', '2025-10-15', 2, 2, 4400.00, 7200.00, 8000.00, 1600.00, 21200.00, 'finished', '2025-10-14 15:54:06', NULL),
(5, 12, 30, 'Hyderabad', 'Telangana, India', '2025-10-15', '2025-10-16', 2, 2, 4600.00, 8800.00, 9000.00, 1600.00, 24000.00, 'finished', '2025-10-15 02:29:07', 'Hyderabad'),
(6, 12, 27, 'Amritsar', 'Punjab, India', '2025-10-15', '2025-10-16', 1, 2, 4400.00, 3600.00, 8000.00, 1600.00, 17600.00, 'finished', '2025-10-15 06:25:02', 'AmritsarA'),
(7, 12, 30, 'Hyderabad', 'Telangana, India', '2025-10-15', '2025-10-16', 2, 2, 6000.00, 8800.00, 9000.00, 1600.00, 25400.00, 'finished', '2025-10-15 08:27:34', 'Hyderabad_7'),
(8, 12, 28, 'Madurai', 'Tamil Nadu, India', '2025-10-15', '2025-10-16', 2, 2, 4240.00, 5600.00, 7500.00, 1600.00, 18940.00, 'finished', '2025-10-15 08:47:11', 'Madurai_8'),
(9, 12, 29, 'Mumbai', 'Maharashtra, India', '2025-10-16', '2025-10-17', 1, 2, 40.00, 7000.00, 15000.00, 1510.00, 23550.00, 'Finished', '2025-10-15 10:49:31', 'Mumbai_9'),
(10, 12, 27, 'Amritsar', 'Punjab, India', '2025-10-17', '2025-10-18', 2, 2, 3983.33, 7200.00, 8000.00, 1510.00, 20693.33, 'cancelled', '2025-10-17 03:21:30', NULL),
(11, 12, 28, 'Madurai', 'Tamil Nadu, India', '2025-10-18', '2025-10-18', 3, 1, 4120.00, 3600.00, 0.00, 1600.00, 9320.00, 'finished', '2025-10-18 12:11:21', 'Madurai_11'),
(12, 12, 28, 'Madurai', 'Tamil Nadu, India', '2025-10-18', '2025-10-19', 2, 2, 4240.00, 5600.00, 7500.00, 1600.00, 18940.00, 'Finished', '2025-10-18 12:12:22', NULL),
(13, 12, 27, 'Amritsar', 'Punjab, India', '2025-10-19', '2025-10-20', 2, 2, 11900.00, 7200.00, 8000.00, 0.00, 27100.00, 'Finished', '2025-10-18 12:13:33', NULL),
(14, 12, 27, 'Amritsar', 'Punjab, India', '2025-10-24', '2025-10-25', 2, 2, 4400.00, 7200.00, 8000.00, 1600.00, 21200.00, 'active', '2025-10-24 12:16:51', 'Amritsar_14');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `fullname` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `session_token` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('active','blocked') COLLATE utf8mb4_general_ci DEFAULT 'active',
  `otp` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `otp_expires_at` datetime DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `profile_image` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `fullname`, `username`, `email`, `password`, `session_token`, `created_at`, `status`, `otp`, `otp_expires_at`, `phone`, `profile_image`) VALUES
(1, 'srimathi', 'srimathi1', 'srimathi@gmail.com', '$2y$10$5bCnOoymZYUxOFqbqdMc9.yE4Qwpp8ASz.DhkL84LY3ZxMhMrR6v2', NULL, '2025-07-18 03:07:02', 'active', NULL, NULL, NULL, NULL),
(2, 'santhosh', 'santhosh2', 'santhosh@gmail.com', '$2y$10$pHTg1ycwvgyGJMrRgBN95ehX4VZGnIwuIFHWbiZa8Dgp/vN1Y/DKu', NULL, '2025-07-18 03:07:25', 'active', NULL, NULL, NULL, NULL),
(4, 'Ranjithkumar', 'Ranjithkumar4', 'ranjithkumarsse@gmail.com', '$2y$10$zKizRWMnvfVTkCdJkdLwtOfMgUIQYYO/.r0At85I6Jx0JoXJ6KihC', '4102d9963f0557d9ba689e8ee29c1eb25242f11f1b66dbbb43f0aac370936561', '2025-09-10 11:28:34', 'active', '109625', '2025-09-15 08:50:48', NULL, NULL),
(5, 'ranjithkumar', 'ranjithkumar5', 'ranjithkumarr0687.sse@saveetha.com', '$2y$10$nTvAEeCHA38YDYVER1PpruOXKcbXXMHRkqT7bWBRkhHs1RIj39mK6', '823c352888721adecbb82528740b429dad1b6920a136f73f99ce519e40ffb8b2', '2025-09-11 04:57:11', 'blocked', NULL, NULL, NULL, NULL),
(6, 'Sri', 'Sri6', 'sri09@gmail.com', '$2y$10$75A44r8MkjH/JePGYuUgu.xv0QWIAuaMQbO5I9ooD71XFvIVX3TLC', '3de344325cfacddb431dee8bcc517e7be4af07d69c4025395036b9d43c84e6fa', '2025-09-11 07:19:21', 'active', NULL, NULL, NULL, NULL),
(7, 'Srimathi', 'Srimathi7', 'srimathisivan09@gmail.com', '$2y$10$fcn0XlowZXq7J7X4VPdxZuCGcEeN6xAWtYjcpSFfhmsHrfdCi1EXK', 'd948c36e81d2a6e8edd394851809d66fce8bec393ee3c4f2513cea7adaeb57c7', '2025-09-11 08:13:22', 'active', '847355', '2025-10-20 09:27:29', NULL, NULL),
(8, 'Admin', 'Admin8', 'admin123@gmail.com', '$2y$10$yovQ02mvfW77NIlyWkjpdO4x2wjRRMcSzSH.Hdi/dHrfuSUxEEV0W', '99424b504f1e98431172ef5e11aba642ef45de43722217bf829bb2d15db5b471', '2025-09-11 10:39:01', 'active', NULL, NULL, '9876543120', 'uploads/profiles/user_8_68f4998c4d84f8.17176394.jpg'),
(9, 'Srimathi sivan', 'Srimathisivan9', 'srimathisivan1148.sse@saveetha.com', '$2y$10$sp6WvxBg92xttJ/pSVTMhef.75mjqu3SZaKF9tCl.EK3lHatCiFv2', '224b45912b5a0a6bd96c1e9d148d41b0d349a8e261226a159af2a36a14c6db97', '2025-09-15 05:21:30', 'active', '955970', '2025-09-15 08:47:35', NULL, NULL),
(10, 'Sabarish R', 'SabarishR10', 'sabarishr4287.sse@saveetha.com', '$2y$10$Q3H0b9rIsGUJ4KS4qNIrQurINk6ToKFb/mG5kvSlWH1bp5YH5zqYW', 'a726105dead6efe1acbc10a8a252d94829faa085649700d47daf9c626356b74e', '2025-09-25 09:54:12', 'active', NULL, NULL, NULL, NULL),
(11, 'Ranjithkumar', 'Ranjithkumar11', 'ranjithsuriya12345@gmail.com', '$2y$10$L9kwe8U2NsmxLVA9GG1DG.SYpPmE2bcYw7ols2Y0XpxCRCyapkcP6', 'fbf96b6c6cd5bb14e34219b3df35e0a6194e9279da58e38c69a707a99fa33ff2', '2025-09-26 15:11:42', 'active', NULL, NULL, NULL, NULL),
(12, 'Rani', 'Rani12', 'rani@gmail.com', '$2y$10$Zo6BorSE2.hNdbT2BCKn1unqA.dZkzPTH7cTaJDE5A9S/.GF7d0X.', 'd0cc319fb21a378028fbd9ff9980b18320864fe3467cac1330b81b3ea14d316a', '2025-10-04 16:28:36', 'active', NULL, NULL, '9638527410', 'uploads/profiles/user_12_68f498a7b91237.13693449.jpg'),
(14, 'Kaviya s', 'kaviya123', 'kaviya@gmail.com', '$2y$10$7MAPpMTZkyUvXCvzlHLrwuxtSvFhAt6ttN0YsUQeUF/C4bsNpS53m', 'f02e9cd530b4e5175e56b277520b5ecfb15359d02f4d67d5cb524d5972d3572b', '2025-10-19 09:07:46', 'active', NULL, NULL, '9876543211', 'uploads/profiles/user_14_68f4aaf0190094.11896318.jpg'),
(15, 'Ranjith Kumar', 'rkrajasekar005', 'rkrajasekar005@gmail.com', '$2y$10$dOpBvamj2J16zP.kNErhvudg2NqU0qJUaOI4Ijoam4Eo4C1RY0vya', '7bf7459b0e718da113c189a906669f6ee7bd3043810b4d0289cfc80898852ffa', '2025-10-19 18:45:29', 'active', NULL, NULL, NULL, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `itinerary_items`
--
ALTER TABLE `itinerary_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `trip_id` (`trip_id`);

--
-- Indexes for table `itinerary_spots`
--
ALTER TABLE `itinerary_spots`
  ADD PRIMARY KEY (`id`),
  ADD KEY `trip_id` (`trip_id`);

--
-- Indexes for table `password_reset`
--
ALTER TABLE `password_reset`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id_index` (`user_id`);

--
-- Indexes for table `places`
--
ALTER TABLE `places`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `place_images`
--
ALTER TABLE `place_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `place_id` (`place_id`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `saved_trips`
--
ALTER TABLE `saved_trips`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_place` (`user_id`,`place_id`),
  ADD KEY `place_id` (`place_id`);

--
-- Indexes for table `test_table`
--
ALTER TABLE `test_table`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `top_spots`
--
ALTER TABLE `top_spots`
  ADD PRIMARY KEY (`id`),
  ADD KEY `place_id` (`place_id`);

--
-- Indexes for table `transport_options`
--
ALTER TABLE `transport_options`
  ADD PRIMARY KEY (`id`),
  ADD KEY `place_id` (`place_id`);

--
-- Indexes for table `travel_tips`
--
ALTER TABLE `travel_tips`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `trips`
--
ALTER TABLE `trips`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `itinerary_items`
--
ALTER TABLE `itinerary_items`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT for table `itinerary_spots`
--
ALTER TABLE `itinerary_spots`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `password_reset`
--
ALTER TABLE `password_reset`
  MODIFY `id` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `places`
--
ALTER TABLE `places`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `place_images`
--
ALTER TABLE `place_images`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=64;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `saved_trips`
--
ALTER TABLE `saved_trips`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `test_table`
--
ALTER TABLE `test_table`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `top_spots`
--
ALTER TABLE `top_spots`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `transport_options`
--
ALTER TABLE `transport_options`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `travel_tips`
--
ALTER TABLE `travel_tips`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `trips`
--
ALTER TABLE `trips`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `itinerary_spots`
--
ALTER TABLE `itinerary_spots`
  ADD CONSTRAINT `itinerary_spots_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `place_images`
--
ALTER TABLE `place_images`
  ADD CONSTRAINT `place_images_ibfk_1` FOREIGN KEY (`place_id`) REFERENCES `places` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `saved_trips`
--
ALTER TABLE `saved_trips`
  ADD CONSTRAINT `saved_trips_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `saved_trips_ibfk_2` FOREIGN KEY (`place_id`) REFERENCES `places` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `top_spots`
--
ALTER TABLE `top_spots`
  ADD CONSTRAINT `top_spots_ibfk_1` FOREIGN KEY (`place_id`) REFERENCES `places` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `transport_options`
--
ALTER TABLE `transport_options`
  ADD CONSTRAINT `transport_options_ibfk_1` FOREIGN KEY (`place_id`) REFERENCES `places` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
