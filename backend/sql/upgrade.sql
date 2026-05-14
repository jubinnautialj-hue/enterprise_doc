USE enterprise_doc;

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` TINYINT DEFAULT 0 COMMENT '类型 0:菜单 1:按钮 2:数据',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `icon` VARCHAR(100) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系表';

CREATE TABLE IF NOT EXISTS `doc_directory_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型 0:文库 1:文件夹',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `subject_type` TINYINT NOT NULL COMMENT '主体类型 0:用户 1:角色',
    `subject_id` BIGINT NOT NULL COMMENT '主体ID',
    `permission_type` TINYINT DEFAULT 1 COMMENT '权限类型 1:只读 2:读写 4:管理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_target_subject` (`target_type`, `target_id`, `subject_type`, `subject_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_subject` (`subject_type`, `subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目录权限表';

INSERT INTO `sys_role` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'ADMIN', '拥有系统所有权限', 1),
('普通用户', 'USER', '普通用户，只能访问分配的权限', 1);

INSERT INTO `sys_permission` (`name`, `code`, `type`, `parent_id`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('系统管理', 'system', 0, 0, '/system', '', 'Setting', 1, 1),
('用户管理', 'system:user', 0, 1, '/system/user', 'system/UserManage', 'User', 1, 1),
('用户列表', 'system:user:list', 1, 2, '', '', '', 1, 1),
('用户新增', 'system:user:add', 1, 2, '', '', '', 2, 1),
('用户编辑', 'system:user:edit', 1, 2, '', '', '', 3, 1),
('用户删除', 'system:user:delete', 1, 2, '', '', '', 4, 1),
('角色管理', 'system:role', 0, 1, '/system/role', 'system/RoleManage', 'UserFilled', 2, 1),
('角色列表', 'system:role:list', 1, 7, '', '', '', 1, 1),
('角色新增', 'system:role:add', 1, 7, '', '', '', 2, 1),
('角色编辑', 'system:role:edit', 1, 7, '', '', '', 3, 1),
('角色删除', 'system:role:delete', 1, 7, '', '', '', 4, 1),
('权限管理', 'system:permission', 0, 1, '/system/permission', 'system/PermissionManage', 'Key', 3, 1),
('权限列表', 'system:permission:list', 1, 12, '', '', '', 1, 1),
('权限新增', 'system:permission:add', 1, 12, '', '', '', 2, 1),
('权限编辑', 'system:permission:edit', 1, 12, '', '', '', 3, 1),
('权限删除', 'system:permission:delete', 1, 12, '', '', '', 4, 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16);
