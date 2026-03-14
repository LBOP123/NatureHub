-- 生物识别记录表
CREATE TABLE IF NOT EXISTS `bio_recognition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `recognition_result` varchar(200) DEFAULT NULL COMMENT '识别结果（物种名称）',
  `recognition_type` varchar(50) DEFAULT NULL COMMENT '识别类型（plant/animal/other）',
  `confidence` decimal(5,2) DEFAULT NULL COMMENT '置信度（0-100）',
  `status` varchar(20) DEFAULT 'success' COMMENT '识别状态（success/fail）',
  `baike_info` text COMMENT '百科信息（JSON格式）',
  `all_results` text COMMENT '所有识别结果（JSON格式）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_recognition_result` (`recognition_result`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生物识别记录表';
