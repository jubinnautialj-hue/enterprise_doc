CREATE DATABASE IF NOT EXISTS enterprise_doc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE enterprise_doc;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `doc_library` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '文库名称',
    `description` VARCHAR(500) COMMENT '文库描述',
    `cover` VARCHAR(255) COMMENT '封面图片',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `owner_name` VARCHAR(50) COMMENT '所有者名称',
    `type` TINYINT DEFAULT 0 COMMENT '文库类型 0:公共文库 1:个人文库',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文库表';

CREATE TABLE IF NOT EXISTS `doc_folder` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `library_id` BIGINT NOT NULL COMMENT '文库ID',
    `name` VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父文件夹ID',
    `path` VARCHAR(500) COMMENT '路径',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人名称',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_library_id` (`library_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

CREATE TABLE IF NOT EXISTS `doc_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `library_id` BIGINT NOT NULL COMMENT '文库ID',
    `folder_id` BIGINT DEFAULT 0 COMMENT '文件夹ID',
    `name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `file_extension` VARCHAR(20) COMMENT '文件扩展名',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小',
    `file_url` VARCHAR(500) COMMENT '文件访问URL',
    `preview_url` VARCHAR(500) COMMENT '预览URL',
    `thumbnail` VARCHAR(500) COMMENT '缩略图',
    `content_type` VARCHAR(100) COMMENT '内容类型',
    `storage_path` VARCHAR(500) COMMENT '存储路径',
    `version` BIGINT DEFAULT 1 COMMENT '当前版本',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人名称',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `update_user_name` VARCHAR(50) COMMENT '更新人名称',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_library_id` (`library_id`),
    KEY `idx_folder_id` (`folder_id`),
    KEY `idx_create_user_id` (`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

CREATE TABLE IF NOT EXISTS `doc_document_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `version` BIGINT NOT NULL COMMENT '版本号',
    `file_url` VARCHAR(500) COMMENT '文件访问URL',
    `storage_path` VARCHAR(500) COMMENT '存储路径',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小',
    `change_log` VARCHAR(500) COMMENT '变更说明',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

CREATE TABLE IF NOT EXISTS `doc_library_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `library_id` BIGINT NOT NULL COMMENT '文库ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(50) COMMENT '用户名称',
    `role` TINYINT DEFAULT 0 COMMENT '角色 0:只读 1:编辑 2:管理员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_library_id` (`library_id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_library_user` (`library_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文库成员表';

CREATE TABLE IF NOT EXISTS `doc_share` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `share_type` TINYINT NOT NULL COMMENT '分享类型 0:文库 1:文件夹 2:文档',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `share_code` VARCHAR(50) NOT NULL COMMENT '分享码',
    `share_url` VARCHAR(500) COMMENT '分享链接',
    `password` VARCHAR(50) COMMENT '访问密码',
    `expire_type` TINYINT DEFAULT 0 COMMENT '过期类型 0:永久 1:1天 2:7天 3:30天',
    `expire_time` DATETIME COMMENT '过期时间',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览次数',
    `download_count` BIGINT DEFAULT 0 COMMENT '下载次数',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `create_user_name` VARCHAR(50) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_share_code` (`share_code`),
    KEY `idx_create_user_id` (`create_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享表';

INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@enterprise.com', 1);
