CREATE TABLE `cost_0` (
   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
   `ent_code` varchar(20) DEFAULT NULL,
   `money` int DEFAULT NULL,
   `create_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
   `update_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
 CREATE TABLE `cost_2` (
   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
   `ent_code` varchar(20) DEFAULT NULL,
   `money` int DEFAULT NULL,
   `create_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
   `update_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `t_user` (
   `id` bigint unsigned NOT NULL AUTO_INCREMENT,
   `name` varchar(20) DEFAULT NULL,
   `create_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
   `birthday` datetime(6) DEFAULT CURRENT_TIMESTAMP(6),
   `update_time` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6),
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;