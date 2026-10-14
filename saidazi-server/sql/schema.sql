-- =============================================
-- 赛搭子 数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS saidazi
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE saidazi;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `open_id`     VARCHAR(64)  NOT NULL UNIQUE COMMENT '微信 OpenID',
    `nickname`    VARCHAR(64)           COMMENT '微信昵称',
    `avatar_url`  VARCHAR(256)          COMMENT '微信头像 URL',
    `phone`       VARCHAR(20)           COMMENT '手机号',
    `college`     VARCHAR(64)           COMMENT '学院',
    `major`       VARCHAR(64)           COMMENT '专业',
    `grade`       VARCHAR(20)           COMMENT '年级：大一/大二/大三/大四',
    `skills`      VARCHAR(256)          COMMENT '技能标签，逗号分隔',
    `interests`   VARCHAR(256)          COMMENT '参赛兴趣标签',
    `role`        TINYINT      DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 赛事表
-- =============================================
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `title`           VARCHAR(128) NOT NULL COMMENT '赛事标题',
    `type`            TINYINT      NOT NULL COMMENT '赛事类型：0校内 1全国',
    `category`        VARCHAR(32)           COMMENT '分类',
    `difficulty`      TINYINT               COMMENT '难度：1入门 2进阶 3高阶',
    `college_tag`     VARCHAR(64)           COMMENT '学院标签',
    `organizer`       VARCHAR(128)          COMMENT '主办单位',
    `reg_start_time`  DATETIME              COMMENT '报名开始时间',
    `reg_end_time`    DATETIME              COMMENT '报名截止时间',
    `match_time`      DATETIME              COMMENT '比赛时间',
    `publish_time`    DATETIME              COMMENT '发布时间',
    `description`     TEXT                  COMMENT '赛事简介',
    `reg_info`        TEXT                  COMMENT '报名方式说明',
    `cover_url`       VARCHAR(256)          COMMENT '封面图',
    `source_url`      VARCHAR(512)          COMMENT '来源链接（爬虫抓取来源，用于去重）',
    `status`          TINYINT     DEFAULT 0 COMMENT '状态：0草稿 1已发布 2已结束',
    `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_type_status` (`type`, `status`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='赛事表';

-- =============================================
-- 3. 收藏表
-- =============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT    NOT NULL,
    `competition_id`  BIGINT    NOT NULL,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_comp` (`user_id`, `competition_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- =============================================
-- 4. 提醒表
-- =============================================
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT    NOT NULL,
    `competition_id`  BIGINT    NOT NULL,
    `remind_time`     DATETIME  NOT NULL COMMENT '提醒触发时间',
    `remind_type`     TINYINT   NOT NULL COMMENT '提醒类型：1开始前 2截止前7天 3截止前3天 4截止前1天 5自定义',
    `status`          TINYINT  DEFAULT 0  COMMENT '状态：0待触发 1已触发 2已取消',
    `is_pushed`       TINYINT  DEFAULT 0  COMMENT '是否已推送：0否 1是',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_status_time` (`status`, `is_pushed`, `remind_time`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒表';

-- =============================================
-- 5. 组队表
-- =============================================
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `competition_id`  BIGINT       NOT NULL COMMENT '关联赛事',
    `user_id`         BIGINT       NOT NULL COMMENT '发布人',
    `title`           VARCHAR(128) NOT NULL COMMENT '组队标题',
    `need_count`      INT          DEFAULT 1 COMMENT '需要人数',
    `skills_required` VARCHAR(256)          COMMENT '所需技能标签',
    `description`     TEXT                  COMMENT '项目简介',
    `contact`         VARCHAR(128)          COMMENT '联系方式',
    `require_resume`  TINYINT      DEFAULT 0 COMMENT '是否要求简历',
    `remark`          VARCHAR(256)          COMMENT '备注',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态：0招募中 1已满员 2已关闭',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_competition` (`competition_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组队表';

-- =============================================
-- 6. 组队申请表
-- =============================================
DROP TABLE IF EXISTS `team_application`;
CREATE TABLE `team_application` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `team_id`         BIGINT    NOT NULL,
    `applicant_id`    BIGINT    NOT NULL COMMENT '申请人',
    `resume_id`       BIGINT             COMMENT '简历ID',
    `match_score`     DECIMAL(5,2)       COMMENT '技能匹配度分数',
    `status`          TINYINT   DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已拒绝',
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_team_id` (`team_id`),
    INDEX `idx_applicant` (`applicant_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组队申请表';

-- =============================================
-- 7. 简历表
-- =============================================
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL UNIQUE,
    `name`            VARCHAR(32)  NOT NULL COMMENT '姓名',
    `college_major`   VARCHAR(128)          COMMENT '学院·专业',
    `grade`           VARCHAR(20)           COMMENT '年级',
    `skills`          VARCHAR(256)          COMMENT '技能标签',
    `experience`      TEXT                  COMMENT '参赛经历',
    `introduction`    TEXT                  COMMENT '个人简介',
    `attachment_url`  VARCHAR(256)          COMMENT '附件简历PDF URL',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态：0草稿 1已完成',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- =============================================
-- 8. 帖子表
-- =============================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `title`           VARCHAR(128) NOT NULL COMMENT '帖子标题',
    `category`        TINYINT      NOT NULL COMMENT '分类：1求助答疑 2备赛经验 3资料分享 4赛事闲聊',
    `content`         TEXT                  COMMENT '正文内容',
    `images`          VARCHAR(1024)         COMMENT '图片URL，逗号分隔',
    `allow_comment`   TINYINT      DEFAULT 1 COMMENT '是否允许评论',
    `like_count`      INT          DEFAULT 0 COMMENT '点赞数',
    `comment_count`   INT          DEFAULT 0 COMMENT '评论数',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态：0已下架 1正常',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- =============================================
-- 9. 评论表
-- =============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `post_id`         BIGINT    NOT NULL,
    `user_id`         BIGINT    NOT NULL,
    `content`         TEXT      NOT NULL COMMENT '评论内容',
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =============================================
-- 10. 点赞表
-- =============================================
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `post_id`         BIGINT    NOT NULL,
    `user_id`         BIGINT    NOT NULL,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- =============================================
-- 11. 往届成果表
-- =============================================
DROP TABLE IF EXISTS `achievement`;
CREATE TABLE `achievement` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `competition_id`  BIGINT       NOT NULL,
    `team_name`       VARCHAR(64)           COMMENT '获奖队伍名称',
    `award`           VARCHAR(64)           COMMENT '获奖等级',
    `introduction`    TEXT                  COMMENT '作品简介',
    `members`         VARCHAR(256)          COMMENT '队员姓名',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_competition` (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往届成果表';

-- =============================================
-- 12. 管理员表
-- =============================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(64)  NOT NULL UNIQUE,
    `password`    VARCHAR(256) NOT NULL COMMENT 'BCrypt 加密',
    `nickname`    VARCHAR(64),
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 测试用户
INSERT INTO `user` (`id`, `open_id`, `nickname`, `college`, `major`, `grade`, `skills`, `interests`, `role`) VALUES
(1, 'test_openid_001', '张三', '信息技术学院', '计算机科学与技术', '大三', '前端,后端,算法', 'ACM,数学建模', 0),
(2, 'test_openid_002', '李四', '信息技术学院', '软件工程', '大二', '算法,设计,文案', 'ACM,创新创业', 0),
(3, 'test_openid_003', '王五', '电子工程学院', '电子信息工程', '大三', '硬件,嵌入式,算法', '电子设计,智能车', 0);

-- 测试赛事
INSERT INTO `competition` (`id`, `title`, `type`, `category`, `difficulty`, `college_tag`, `organizer`, `reg_start_time`, `reg_end_time`, `match_time`, `status`) VALUES
(1, '2026 ACM-ICPC 亚洲区域赛', 1, '学科竞赛', 3, NULL, '中国计算机学会', '2026-09-01 00:00:00', '2026-10-15 23:59:59', '2026-11-15 00:00:00', 1),
(2, '2026 校园程序设计大赛', 0, '学科竞赛', 1, '信息技术学院', '信息技术学院', '2026-08-20 00:00:00', '2026-09-20 23:59:59', '2026-10-10 00:00:00', 1);

-- 测试组队
INSERT INTO `team` (`id`, `competition_id`, `user_id`, `title`, `need_count`, `skills_required`, `description`, `contact`, `require_resume`, `status`) VALUES
(1, 1, 1, '寻找算法搭子参加ACM', 2, '算法,前端', '寻找两位擅长算法和前端的小伙伴一起冲', 'wx: zhangsan', 0, 0),
(2, 2, 2, '程序设计大赛队友招募', 3, '算法,设计,后端', '校赛组队，已有后端，缺算法和设计', 'QQ: 123456', 1, 0);

-- 测试简历
INSERT INTO `resume` (`id`, `user_id`, `name`, `college_major`, `grade`, `skills`, `introduction`, `status`) VALUES
(1, 1, '张三', '信息技术学院·计科', '大三', '前端,后端,算法', '有ACM参赛经验', 1),
(2, 2, '李四', '信息技术学院·软工', '大二', '算法,设计,文案', '擅长算法和UI设计', 1);

-- 测试提醒
INSERT INTO `reminder` (`id`, `user_id`, `competition_id`, `remind_time`, `remind_type`, `status`, `is_pushed`) VALUES
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 1 MINUTE), 1, 0, 0);
