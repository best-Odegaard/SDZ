-- =============================================
-- 赛搭子 H2 内存数据库初始化脚本
-- 兼容 H2 + MySQL MODE
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `open_id`     VARCHAR(64)  NOT NULL,
    `nickname`    VARCHAR(64),
    `avatar_url`  VARCHAR(256),
    `phone`       VARCHAR(20),
    `college`     VARCHAR(64),
    `major`       VARCHAR(64),
    `grade`       VARCHAR(20),
    `skills`      VARCHAR(256),
    `interests`   VARCHAR(256),
    `role`        TINYINT      DEFAULT 0,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 简历表
CREATE TABLE IF NOT EXISTS `resume` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `name`            VARCHAR(32)  NOT NULL,
    `college_major`   VARCHAR(128),
    `grade`           VARCHAR(20),
    `skills`          VARCHAR(256),
    `experience`      TEXT,
    `introduction`    TEXT,
    `attachment_url`  VARCHAR(256),
    `status`          TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 赛事表
CREATE TABLE IF NOT EXISTS `competition` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `title`           VARCHAR(128) NOT NULL,
    `type`            TINYINT      NOT NULL,
    `category`        VARCHAR(32),
    `difficulty`      TINYINT,
    `college_tag`     VARCHAR(64),
    `organizer`       VARCHAR(128),
    `reg_start_time`  DATETIME,
    `reg_end_time`    DATETIME,
    `match_time`      DATETIME,
    `publish_time`    DATETIME,
    `description`     TEXT,
    `reg_info`        TEXT,
    `cover_url`       VARCHAR(256),
    `source_url`      VARCHAR(512),
    `status`          TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT    NOT NULL,
    `competition_id`  BIGINT    NOT NULL,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_comp` (`user_id`, `competition_id`)
);

-- 提醒表
CREATE TABLE IF NOT EXISTS `reminder` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT    NOT NULL,
    `competition_id`  BIGINT    NOT NULL,
    `remind_time`     DATETIME  NOT NULL,
    `remind_type`     TINYINT   NOT NULL,
    `status`          TINYINT   DEFAULT 0,
    `is_pushed`       TINYINT   DEFAULT 0,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP
);

-- 组队表
CREATE TABLE IF NOT EXISTS `team` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `competition_id`  BIGINT       NOT NULL,
    `user_id`         BIGINT       NOT NULL,
    `title`           VARCHAR(128) NOT NULL,
    `need_count`      INT          DEFAULT 1,
    `skills_required` VARCHAR(256),
    `description`     TEXT,
    `contact`         VARCHAR(128),
    `require_resume`  TINYINT      DEFAULT 0,
    `remark`          VARCHAR(256),
    `status`          TINYINT      DEFAULT 0,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 组队申请表
CREATE TABLE IF NOT EXISTS `team_application` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `team_id`         BIGINT    NOT NULL,
    `applicant_id`    BIGINT    NOT NULL,
    `resume_id`       BIGINT,
    `match_score`     DECIMAL(5,2),
    `status`          TINYINT   DEFAULT 0,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP
);

-- 帖子表
CREATE TABLE IF NOT EXISTS `post` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `title`           VARCHAR(128) NOT NULL,
    `category`        TINYINT      NOT NULL,
    `content`         TEXT,
    `images`          VARCHAR(1024),
    `allow_comment`   TINYINT      DEFAULT 1,
    `like_count`      INT          DEFAULT 0,
    `comment_count`   INT          DEFAULT 0,
    `status`          TINYINT      DEFAULT 1,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `post_id`         BIGINT    NOT NULL,
    `user_id`         BIGINT    NOT NULL,
    `content`         TEXT      NOT NULL,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP
);

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    `post_id`         BIGINT    NOT NULL,
    `user_id`         BIGINT    NOT NULL,
    `create_time`     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
);

-- 往届成果表
CREATE TABLE IF NOT EXISTS `achievement` (
    `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `competition_id`  BIGINT       NOT NULL,
    `team_name`       VARCHAR(64),
    `award`           VARCHAR(64),
    `introduction`    TEXT,
    `members`         VARCHAR(256),
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP
);

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `username`    VARCHAR(64)  NOT NULL UNIQUE,
    `password`    VARCHAR(256) NOT NULL,
    `nickname`    VARCHAR(64),
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO `user` (`id`, `open_id`, `nickname`, `college`, `major`, `grade`, `skills`, `interests`, `role`) VALUES
(1, 'wx_openid_001', '张三', '信息技术学院', '计算机科学与技术', '大三', '前端,后端,算法', 'ACM,数学建模', 0),
(2, 'wx_openid_002', '李四', '信息技术学院', '软件工程', '大二', '算法,设计,文案', 'ACM,创新创业', 0),
(3, 'wx_openid_003', '王五', '电子工程学院', '电子信息工程', '大三', '硬件,嵌入式,算法', '电子设计,智能车', 0);

INSERT INTO `resume` (`id`, `user_id`, `name`, `college_major`, `grade`, `skills`, `introduction`, `status`) VALUES
(1, 1, '张三', '信息技术学院·计科', '大三', '前端,后端,算法', '有ACM参赛经验', 1),
(2, 2, '李四', '信息技术学院·软工', '大二', '算法,设计,文案', '擅长算法和UI设计', 1);

INSERT INTO `competition` (`id`, `title`, `type`, `category`, `difficulty`, `college_tag`, `organizer`, `reg_start_time`, `reg_end_time`, `match_time`, `description`, `reg_info`, `status`) VALUES
(1, 'ACM-ICPC 国际大学生程序设计竞赛', 1, '学科竞赛', 3, NULL, 'ACM', DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', 30, CURRENT_TIMESTAMP), DATEADD('DAY', 60, CURRENT_TIMESTAMP), '全球最具影响力的大学生程序设计竞赛', '登录全国大学生创业服务网在线报名，网址：cy.ncss.cn（长按复制网址）', 1),
(2, '校级程序设计大赛', 0, '学科竞赛', 1, '信息技术学院', '信息技术学院', DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 20, CURRENT_TIMESTAMP), DATEADD('DAY', 40, CURRENT_TIMESTAMP), '面向全院的程序设计入门赛事', '填写报名表提交至学院教务办公室，具体请留意班级群通知', 1),
(3, '挑战杯大学生课外学术科技作品竞赛', 1, '创新创业', 2, NULL, '共青团中央', DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('DAY', 60, CURRENT_TIMESTAMP), DATEADD('DAY', 90, CURRENT_TIMESTAMP), '全国大学生创新创业顶级赛事', '登录挑战杯官网在线报名，网址：www.tiaozhanbei.org（长按复制网址）', 1),
(4, '校园歌手大赛', 0, '文艺活动', 1, '艺术学院', '校团委', DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', 15, CURRENT_TIMESTAMP), DATEADD('DAY', 25, CURRENT_TIMESTAMP), '展示校园音乐风采', '扫码关注校团委公众号回复"歌手大赛"报名', 1);

INSERT INTO `team` (`id`, `competition_id`, `user_id`, `title`, `need_count`, `skills_required`, `description`, `contact`, `require_resume`, `status`) VALUES
(1, 1, 1, '寻找算法搭子参加ACM', 2, '算法,前端', '寻找两位擅长算法和前端的小伙伴', 'wx: zhangsan', 0, 0),
(2, 2, 2, '程序设计大赛队友招募', 3, '算法,设计,后端', '校赛组队，缺算法和设计', 'QQ: 123456', 1, 0);

INSERT INTO `reminder` (`id`, `user_id`, `competition_id`, `remind_time`, `remind_type`, `status`, `is_pushed`) VALUES
(1, 1, 1, DATEADD('MINUTE', 1, CURRENT_TIMESTAMP), 1, 0, 0);

INSERT INTO `post` (`id`, `user_id`, `title`, `category`, `content`, `images`, `allow_comment`, `like_count`, `comment_count`, `status`) VALUES
(1, 1, 'ACM备赛经验分享', 2, '分享一下去年ACM校赛的备赛心得，重点练习动态规划和图论。', NULL, 1, 5, 2, 1),
(2, 2, '数学建模求助：如何选择题目？', 1, '第一次参加数学建模，求问选题有什么技巧？', NULL, 1, 2, 1, 1),
(3, 3, '电子设计大赛资料分享', 3, '整理了一些往届电子设计大赛的优秀作品文档，希望对大家有帮助。', NULL, 1, 8, 0, 1);

INSERT INTO `comment` (`id`, `post_id`, `user_id`, `content`) VALUES
(1, 1, 2, '感谢分享！动态规划确实是重点。'),
(2, 1, 3, '请问有什么推荐的练习平台吗？'),
(3, 2, 1, '建议先看历年真题，根据团队擅长方向选。');

INSERT INTO `post_like` (`id`, `post_id`, `user_id`) VALUES
(1, 1, 2), (2, 1, 3),
(3, 2, 1), (4, 2, 3),
(5, 3, 1), (6, 3, 2);

INSERT INTO `achievement` (`id`, `competition_id`, `team_name`, `award`, `introduction`, `members`) VALUES
(1, 2, '代码先锋队', '一等奖', '基于深度学习的智能编程辅助系统', '张三、李四、王五'),
(2, 1, 'ACM-X', '银奖', '在第48届ICPC区域赛获得银奖', '赵六、钱七');

INSERT INTO `favorite` (`id`, `user_id`, `competition_id`) VALUES
(1, 1, 1), (2, 1, 3);

-- 管理员账号由 DataInitializer 在启动时通过 BCrypt 初始化（admin/admin123）
