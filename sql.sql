DROP DATABASE IF EXISTS `DecoupledExam`;
CREATE DATABASE `DecoupledExam`;
SET NAMES utf8mb4;
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `DecoupledExam`;

-- 学科表
CREATE TABLE `subject` (
  `subject_id`   INT(11)      NOT NULL AUTO_INCREMENT COMMENT '学科ID',
  `subject_name` VARCHAR(50)  NOT NULL COMMENT '学科名称（如：语文、数学、英语、物理）',
  `subject_code` VARCHAR(20)  NOT NULL COMMENT '学科编码（如：CHINESE、MATH、ENGLISH、PHYSICS）',
  `grade_level`  TINYINT(2)   NOT NULL COMMENT '适用年级段：1小学 2初中 3高中 4大学 9通用',
  `sort_order`   INT(11)      NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_code` (`subject_code`),
  UNIQUE KEY `uk_name_grade` (`subject_name`,`grade_level`),
  KEY `idx_grade` (`grade_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- 1. 用户表
CREATE TABLE `user` (
  `user_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '加密密码',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `user_type`   INT(4)       NOT NULL DEFAULT 2 COMMENT '用户类型：0=管理员(admin), 1=教师(teacher), 2=学生(student)',
  `face_img`    VARCHAR(255) DEFAULT NULL COMMENT '人脸识别基准照片URL',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 课程表
CREATE TABLE `edu_course` (
  `course_id`   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `teacher_id`  BIGINT(20)   NOT NULL COMMENT '创建教师ID',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0开设 1结课)',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`course_id`),
  KEY `idx_teacher` (`teacher_id`),
  FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 5. 教师职位表（独立表，仅 id、教师ID、职位）
-- =============================================
CREATE TABLE teacher_position (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '职位记录ID',
  teacher_id BIGINT(20) NOT NULL COMMENT '教师ID',
  role TINYINT(1) NOT NULL DEFAULT 0 COMMENT '职位：0任课老师 1教务老师',
  PRIMARY KEY (id),
  UNIQUE KEY uk_teacher (teacher_id),
  KEY idx_role (role),
  CONSTRAINT fk_tp_teacher FOREIGN KEY (teacher_id) REFERENCES user(user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师职位表';

-- =============================================
-- 6. 教师职能表（教师 学科 多对多）
-- =============================================
CREATE TABLE teacher_subject (
  teacher_id BIGINT(20) NOT NULL COMMENT '教师ID',
  subject_id INT(11) NOT NULL COMMENT '学科ID',
  is_main TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主讲学科：1是 0兼任',
  PRIMARY KEY (teacher_id, subject_id),
  KEY idx_subject (subject_id),
  CONSTRAINT fk_ts_teacher FOREIGN KEY (teacher_id) REFERENCES user(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ts_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师职能表';

-- =============================================
-- 题型表（最终精简版：仅 3 个字段）
-- =============================================
DROP TABLE IF EXISTS `question_type`;
CREATE TABLE `question_type` (
  `type_id`    INT(11)      NOT NULL AUTO_INCREMENT COMMENT '题型ID（主键，自增）',
  `type_code`  VARCHAR(30)  NOT NULL COMMENT '题型编码（唯一，如 SINGLE、READING）',
  `type_name`  VARCHAR(30)  NOT NULL COMMENT '题型名称（如：单选题、阅读理解）',
  PRIMARY KEY (`type_id`),
  UNIQUE KEY `uk_code` (`type_code`),
  UNIQUE KEY `uk_name` (`type_name`),
  KEY `idx_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题型表';

INSERT INTO `question_type` (`type_code`, `type_name`) VALUES
('SINGLE',       '单选题'),
('MULTIPLE',     '多选题'),
('TRUE_FALSE',   '判断题'),
('FILL',         '填空题'),
('SHORT_ANS',    '简答题'),
('ESSAY',        '论述题'),
('CALC',         '计算题'),
('JOURNAL',      '分录题'),
('MATCHING',     '连线题'),
('SORTING',      '排序题'),
('CLOZE',        '完形填空'),
('READING',      '阅读理解'),
('LISTENING',    '听力题'),
('PROGRAM',      '程序题'),
('ORAL',         '口语题'),
('COMPREHENSIVE','综合题'),
('POLL',         '投票题');

-- =============================================
-- 2. 题目主表（全新建表，已使用 type_id 外键）
-- =============================================
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `type_id`        INT(11)     NOT NULL COMMENT '题型ID',
  `title`          TEXT         DEFAULT NULL COMMENT '题干（阅读理解文章放这里）',
  `difficulty`     TINYINT(4)   NOT NULL DEFAULT 1 COMMENT '难度 1-5',
  `subject_id`     INT(11)        DEFAULT NULL COMMENT '所属学科ID',
  `creator_id`     BIGINT(20)   NOT NULL COMMENT '出题人ID',
  `review_status`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '审核状态：0待审 1通过 2驳回',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_review` (`review_status`),
  CONSTRAINT `fk_questions_type`     FOREIGN KEY (`type_id`)     REFERENCES `question_type`(`type_id`)     ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_questions_subject`  FOREIGN KEY (`subject_id`)  REFERENCES `subject`(`subject_id`)         ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_questions_creator`  FOREIGN KEY (`creator_id`)  REFERENCES `user`(`user_id`)              ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目主表';

-- =============================================
-- 3. 子题表（全新建表，已使用 type_id 外键）
-- =============================================
DROP TABLE IF EXISTS `question_items`;
CREATE TABLE `question_items` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '子题ID',
  `question_id`  BIGINT(20) DEFAULT NULL COMMENT '所属主题目ID',
  `sequence`     INT(11)    NOT NULL COMMENT '子题顺序',
  `type_id`      INT(11)    NOT NULL COMMENT '子题题型ID',
  `content`      TEXT       DEFAULT NULL COMMENT '子题题干',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_type` (`type_id`),
  CONSTRAINT `fk_item_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_item_type`     FOREIGN KEY (`type_id`)     REFERENCES `question_type`(`type_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子题表';


-- 4. 题目组件表（核心）
CREATE TABLE `question_components` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id`    BIGINT(20)   DEFAULT NULL COMMENT '关联主题目（可空）',
  `item_id`        BIGINT(20)   DEFAULT NULL COMMENT '关联子题（可空）',
  `component_type` VARCHAR(20)  NOT NULL COMMENT '组件类型：stem/option/answer/analysis/audio/code等',
  `content`        JSON         DEFAULT NULL COMMENT '组件内容',
  `meta`           JSON         DEFAULT NULL COMMENT '扩展信息',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_item` (`item_id`),
  KEY `idx_type` (`component_type`),
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`item_id`) REFERENCES `question_items`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目组件表';

-- 6. 题目标签表
CREATE TABLE `question_tags` (
  `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id` BIGINT(20)  NOT NULL COMMENT '题目ID',
  `tag_name`    VARCHAR(30) NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_tag` (`question_id`,`tag_name`),
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- 7. 试卷表
CREATE TABLE `exam_paper` (
  `paper_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  `paper_name`   VARCHAR(100) NOT NULL COMMENT '试卷名称',
  `course_id`    BIGINT(20)   DEFAULT NULL COMMENT '所属课程ID',
  `total_score`  INT(11)      NOT NULL DEFAULT 100 COMMENT '试卷总分',
  `compose_type` CHAR(1)      NOT NULL COMMENT '组卷方式(1手动 2智能)',
  `is_sealed`    CHAR(1)      NOT NULL DEFAULT '0' COMMENT '是否封存(0否 1是)',
  `creator_id`   BIGINT(20)   NOT NULL COMMENT '创建人ID',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`paper_id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_creator` (`creator_id`),
  FOREIGN KEY (`course_id`) REFERENCES `edu_course`(`course_id`) ON DELETE SET NULL,
  FOREIGN KEY (`creator_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 8. 试卷题目关联表
CREATE TABLE `exam_paper_question` (
  `paper_id`     BIGINT(20)   NOT NULL COMMENT '试卷ID',
  `question_id`  BIGINT(20)   NOT NULL COMMENT '题目ID',
  `score`        DECIMAL(5,1) NOT NULL COMMENT '本题分值',
  `sort_order`   INT(11)      NOT NULL COMMENT '题目顺序',
  PRIMARY KEY (`paper_id`,`question_id`),
  KEY `idx_question` (`question_id`),
  FOREIGN KEY (`paper_id`)    REFERENCES `exam_paper`(`paper_id`)   ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`)         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- 9. 考试表
CREATE TABLE `exam` (
  `exam_id`    BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `title`      VARCHAR(100) NOT NULL COMMENT '考试标题',
  `paper_id`   BIGINT(20)   NOT NULL COMMENT '使用试卷ID',
  `teacher_id` BIGINT(20)   NOT NULL COMMENT '创建老师ID',
  `start_time` DATETIME     NOT NULL COMMENT '开始时间',
  `end_time`   DATETIME     NOT NULL COMMENT '结束时间',
  `exam_code`  VARCHAR(20)  DEFAULT NULL COMMENT '考试码',
  `status`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
  PRIMARY KEY (`exam_id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_teacher` (`teacher_id`),
  FOREIGN KEY (`paper_id`)   REFERENCES `exam_paper`(`paper_id`) ON DELETE RESTRICT,
  FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`)       ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 10. 考试设置表
CREATE TABLE `exam_setting` (
  `setting_id`                   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id`                      BIGINT(20)   NOT NULL COMMENT '考试ID',
  `duration_minute`              INT(11)      DEFAULT NULL COMMENT '考试时长(分钟)',
  `allow_late_enter`             TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许迟到进入',
  `question_shuffle`             TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '题目是否乱序',
  `option_shuffle`               TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '选项是否乱序',
  `prevent_screen_switch`        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否切屏监控',
  `passing_score`                DECIMAL(5,1) DEFAULT NULL COMMENT '及格分数',
  `auto_submit`                  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否超时自动提交',
  `allow_view_paper`             TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许考后查看试卷',
  `allow_view_score`             TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许查看成绩',
  `generate_exam_code`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否生成考试码',
  `peer_review`                  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否生生互评',
  `fill_case_sensitive`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '填空题是否区分大小写',
  `fill_ignore_symbols`          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '填空题是否忽略符号',
  `fill_manual_mark`             TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '填空题是否需要人工批改',
  `multi_choice_partial_score`   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '多选未选全评分方式',
  `multi_choice_partial_ratio`   DECIMAL(4,2) DEFAULT NULL COMMENT '多选未选全得分比例',
  `sort_question_score_per_blank` TINYINT(1)  NOT NULL DEFAULT 1 COMMENT '排序题是否按空给分',
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `uk_exam` (`exam_id`),
  FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试设置表';

-- 11. 考试记录表
CREATE TABLE `exam_record` (
  `record_id`        BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `exam_id`          BIGINT(20)   NOT NULL COMMENT '考试ID',
  `student_id`       BIGINT(20)   NOT NULL COMMENT '学生ID',
  `objective_score`  DECIMAL(5,1) DEFAULT NULL COMMENT '客观题得分',
  `subjective_score` DECIMAL(5,1) DEFAULT NULL COMMENT '主观题得分',
  `total_score`      DECIMAL(5,1) DEFAULT NULL COMMENT '总成绩',
  `ai_comment`       TEXT         DEFAULT NULL COMMENT 'AI学习建议',
  `status`           CHAR(1)      NOT NULL DEFAULT '0' COMMENT '0未考 1已交 2已阅',
  `submit_time`      DATETIME     DEFAULT NULL COMMENT '交卷时间',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_exam_student` (`exam_id`,`student_id`),
  KEY `idx_student` (`student_id`),
  FOREIGN KEY (`exam_id`)    REFERENCES `exam`(`exam_id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 12. 答题详情表
CREATE TABLE `exam_answer` (
  `answer_id`   BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '答题ID',
  `record_id`   BIGINT(20)   NOT NULL COMMENT '考试记录ID',
  `question_id` BIGINT(20)   NOT NULL COMMENT '题目ID',
  `my_answer`   TEXT         DEFAULT NULL COMMENT '学生答案',
  `score`       DECIMAL(5,1) DEFAULT NULL COMMENT '得分',
  `marker_id`   BIGINT(20)   DEFAULT NULL COMMENT '阅卷人ID',
  `mark_type`   CHAR(1)      NOT NULL COMMENT '1机评 2师评 3互评',
  PRIMARY KEY (`answer_id`),
  KEY `idx_record` (`record_id`),
  KEY `idx_question` (`question_id`),
  FOREIGN KEY (`record_id`)   REFERENCES `exam_record`(`record_id`) ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`)          ON DELETE CASCADE,
  FOREIGN KEY (`marker_id`)   REFERENCES `user`(`user_id`)          ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情表';

-- 13. 错题本表
CREATE TABLE `exam_wrong_book` (
  `id`              BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id`      BIGINT(20) NOT NULL COMMENT '学生ID',
  `question_id`     BIGINT(20) NOT NULL COMMENT '题目ID',
  `error_count`     INT(11)    NOT NULL DEFAULT 1 COMMENT '错误次数',
  `create_time`     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次加入时间',
  `last_error_time` DATETIME   DEFAULT NULL COMMENT '最后一次错题时间',
  `is_deleted`      CHAR(1)    NOT NULL DEFAULT '0' COMMENT '0正常 1已移除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_question` (`student_id`,`question_id`,`is_deleted`),
  FOREIGN KEY (`student_id`)  REFERENCES `user`(`user_id`)      ON DELETE CASCADE,
  FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`)      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- 14. 系统操作日志表
CREATE TABLE `system_operation_log` (
  `log_id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
  `log_time`          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志发生时间',
  `user_id`           BIGINT(20)   NOT NULL COMMENT '操作用户ID',
  `user_role`         VARCHAR(20)  NOT NULL COMMENT '用户角色',
  `module`            VARCHAR(50)  NOT NULL COMMENT '功能模块',
  `action`            VARCHAR(100) NOT NULL COMMENT '具体操作',
  `target_type`       VARCHAR(50)  DEFAULT NULL COMMENT '操作对象类型',
  `target_id`         VARCHAR(50)  DEFAULT NULL COMMENT '操作对象ID',
  `description`       VARCHAR(500) NOT NULL COMMENT '日志描述',
  `ip_address`        VARCHAR(45)  DEFAULT NULL COMMENT '操作来源IP',
  `status`            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '操作状态 1成功 购0失败',
  `execution_time_ms` INT(11)      DEFAULT NULL COMMENT '操作耗时(毫秒)',
  `extra_data`        JSON         DEFAULT NULL COMMENT '额外结构化数据',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_module` (`module`),
  KEY `idx_target` (`target_type`,`target_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- 15. 用户登录日志表
CREATE TABLE `user_login_log` (
  `login_id`       BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
  `user_id`        BIGINT(20)   DEFAULT NULL COMMENT '登录用户ID（尝试登录时可能为空）',
  `username`       VARCHAR(50)  NOT NULL COMMENT '登录用户名',
  `login_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `ip_address`     VARCHAR(45)  NOT NULL COMMENT '登录IP',
  `user_agent`     TEXT         DEFAULT NULL COMMENT '浏览器信息',
  `login_status`   TINYINT(1)   NOT NULL COMMENT '1成功 0失败',
  `failure_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `session_id`     VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
  PRIMARY KEY (`login_id`),
  KEY `idx_username_time` (`username`,`login_time`),
  KEY `idx_status_time` (`login_status`,`login_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录日志表';

-- 16. 安全事件日志表
CREATE TABLE `security_event_log` (
  `event_id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '安全事件ID',
  `event_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件发生时间',
  `user_id`          BIGINT(20)   DEFAULT NULL COMMENT '关联用户ID',
  `ip_address`       VARCHAR(45)  NOT NULL COMMENT '事件来源IP',
  `event_type`       VARCHAR(50)  NOT NULL COMMENT '事件类型',
  `risk_level`       TINYINT(1)   NOT NULL COMMENT '风险等级 1低 2中 3高 4紧急',
  `description`      VARCHAR(500) NOT NULL COMMENT '事件描述',
  `is_resolved`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已处理',
  `resolution_detail`TEXT        DEFAULT NULL COMMENT '处理详情',
  `extra_data`       JSON         DEFAULT NULL COMMENT '额外数据',
  PRIMARY KEY (`event_id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全事件日志表';

ALTER TABLE `edu_course` 
  ADD COLUMN `subject_id` INT(11) DEFAULT NULL COMMENT '学科ID' AFTER `course_name`,
  ADD KEY `idx_subject` (`subject_id`),
  ADD CONSTRAINT `fk_course_subject` 
  FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE SET NULL;

  -- =============================================
-- 22. 考试通知表
-- =============================================
CREATE TABLE `exam_notification` (
    `notification_id`  BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `exam_id`          BIGINT(20)   NOT NULL COMMENT '考试ID',
    `student_id`       BIGINT(20)   DEFAULT NULL COMMENT '学生ID（NULL表示发送给所有考生）',
    `title`            VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`          VARCHAR(500) NOT NULL COMMENT '通知内容',
    `send_time`        DATETIME     DEFAULT NULL COMMENT '计划发送时间',
    `is_sent`          TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已发送',
    `send_attempts`    INT(11)      NOT NULL DEFAULT 0 COMMENT '发送尝试次数',
    PRIMARY KEY (`notification_id`),
    KEY `idx_exam` (`exam_id`),
    KEY `idx_student` (`student_id`),
    KEY `idx_send_time` (`send_time`),
    CONSTRAINT `fk_notification_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_notification_student` FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试通知表';