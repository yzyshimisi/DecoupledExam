DROP DATABASE IF EXISTS `DecoupledExam`;
CREATE DATABASE `DecoupledExam`;
SET NAMES utf8mb4;
USE `DecoupledExam`;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE `user` (
  `user_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '加密密码',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
`avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '用户头像照片URL',
  `user_type`   INT(4)       NOT NULL DEFAULT 2 COMMENT '用户类型：0=管理员(admin), 1=教师(teacher), 2=学生(student)',
  `face_img`    VARCHAR(255) DEFAULT NULL COMMENT '人脸识别基准照片URL',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_avatar` (`avatar_url`),
  KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建一个管理员（123456）
INSERT INTO `user` (
  `username`, `password`, `real_name`, `user_type`, `status`, `create_time`
) VALUES (
  'admin',
  'GgxzUO1hxmTvWtkKlS2g1w==',
  '系统管理员',
  0,  -- 0=管理员
  '0',  -- 状态正常
  NOW()
);

-- =============================================
-- 2. 学科表
-- =============================================
CREATE TABLE `subject` (
  `subject_id`   INT(11)      NOT NULL AUTO_INCREMENT COMMENT '学科ID',
  `subject_name` VARCHAR(50)  NOT NULL COMMENT '学科名称',
  `subject_code` VARCHAR(20)  NOT NULL COMMENT '学科编码（唯一）',
  `grade_level`  TINYINT(2)   NOT NULL DEFAULT 9 COMMENT '适用学段：1小学 2初中 3高中 9通用',
  `sort_order`   INT(11)      NOT NULL DEFAULT 0 COMMENT '排序值',
  `status`       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_code` (`subject_code`),
  UNIQUE KEY `uk_name_grade` (`subject_name`,`grade_level`),
  KEY `idx_grade` (`grade_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- =============================================
-- 3. 课程表（已添加课程邀请码字段）
-- =============================================
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course` (
  `course_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name`   VARCHAR(100) NOT NULL COMMENT '课程名称',
  `invite_code`   VARCHAR(20)  DEFAULT NULL COMMENT '课程邀请码（学生加入课程时使用）',
  `subject_id`    INT(11)      DEFAULT NULL COMMENT '所属学科ID',
  `teacher_id`    BIGINT(20)   NOT NULL COMMENT '创建教师ID',
  `description`   VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
  `status`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0开设 1结课)',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),        -- 邀请码全局唯一
  KEY `idx_subject` (`subject_id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_invite_code` (`invite_code`),              -- 方便按邀请码快速查找课程
  CONSTRAINT `fk_course_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- =============================================
-- 4. 选课表（学生与课程多对多）
-- =============================================
CREATE TABLE `student_course` (
  `student_id` BIGINT(20) NOT NULL COMMENT '学生ID',
  `course_id`  BIGINT(20) NOT NULL COMMENT '课程ID',
  PRIMARY KEY (`student_id`, `course_id`),
  KEY `idx_course` (`course_id`),
  CONSTRAINT `fk_sc_student` FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sc_course`  FOREIGN KEY (`course_id`)  REFERENCES `edu_course`(`course_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课表';

-- =============================================
-- 5. 教师职位表（独立表，仅 id、教师ID、职位）
-- =============================================
CREATE TABLE `teacher_position` (
  `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '职位记录ID',
  `teacher_id`  BIGINT(20)  NOT NULL COMMENT '教师ID',
  `role`        TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '职位：0任课老师 1教务老师',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher` (`teacher_id`),
  KEY `idx_role` (`role`),
  CONSTRAINT `fk_tp_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师职位表';

-- =============================================
-- 6. 教师职能表（教师 ↔ 学科 多对多）
-- =============================================
CREATE TABLE `teacher_subject` (
  `teacher_id`  BIGINT(20) NOT NULL COMMENT '教师ID',
  `subject_id`  INT(11)    NOT NULL COMMENT '学科ID',
  `is_main`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主讲学科：1是 0兼任',
  PRIMARY KEY (`teacher_id`, `subject_id`),
  KEY `idx_subject` (`subject_id`),
  CONSTRAINT `fk_ts_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ts_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师职能表';

-- =============================================
-- 7. 题型表
-- =============================================
CREATE TABLE `question_type` (
  `type_id`    INT(11)      NOT NULL AUTO_INCREMENT COMMENT '题型ID',
  `type_code`  VARCHAR(30)  NOT NULL COMMENT '题型编码',
  `type_name`  VARCHAR(30)  NOT NULL COMMENT '题型名称',
  PRIMARY KEY (`type_id`),
  UNIQUE KEY `uk_code` (`type_code`),
  UNIQUE KEY `uk_name` (`type_name`),
  KEY `idx_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题型表';

INSERT INTO `question_type` (`type_code`, `type_name`) VALUES
('SINGLE', '单选题'),
('MULTIPLE', '多选题'),
('TRUE_FALSE', '判断题'),
('FILL', '填空题'),
('NOUN_PARSING', '名词解析'),
('ESSAY', '论述题'),
('CALC', '计算题'),
('JOURNAL', '分录题'),
('MATCHING', '连线题'),
('SORTING', '排序题'),
('CLOZE', '完形填空'),
('READING', '阅读理解'),
('LISTENING', '听力题'),
('PROGRAM', '程序题'),
('ORAL', '口语题'),
('COMPREHENSIVE', '综合题'),
('POLL', '投票题');

-- =============================================
-- 8. 题目主表
-- =============================================
CREATE TABLE `questions` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `type_id`        INT(11)      NOT NULL COMMENT '题型ID',
  `title`          TEXT         DEFAULT NULL COMMENT '题干',
  `difficulty`     TINYINT(4)   NOT NULL DEFAULT 1 COMMENT '难度 1-5',
  `subject_id`     INT(11)      DEFAULT NULL COMMENT '所属学科ID',
  `creator_id`     BIGINT(20)   NOT NULL COMMENT '出题人ID',
  `review_status`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '审核状态：0待审 1通过 2驳回',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_review` (`review_status`),
  CONSTRAINT `fk_questions_type` FOREIGN KEY (`type_id`) REFERENCES `question_type`(`type_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_questions_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_questions_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目主表';

-- =============================================
-- 9. 子题表
-- =============================================
CREATE TABLE `question_items` (
  `id`           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '子题ID',
  `question_id`  BIGINT(20)  DEFAULT NULL COMMENT '所属主题目ID',
  `sequence`     INT(11)    NOT NULL COMMENT '子题顺序',
  `type_id`      INT(11)    NOT NULL COMMENT '子题题型ID',
  `content`      TEXT       DEFAULT NULL COMMENT '子题题干',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_type` (`type_id`),
  CONSTRAINT `fk_item_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_item_type` FOREIGN KEY (`type_id`) REFERENCES `question_type`(`type_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子题表';

-- =============================================
-- 10. 题目组件表
-- =============================================
CREATE TABLE `question_components` (
  `id`               BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id`      BIGINT(20)   DEFAULT NULL COMMENT '关联主题目',
  `item_id`          BIGINT(20)   DEFAULT NULL COMMENT '关联子题',
  `component_type`   VARCHAR(20)  NOT NULL COMMENT '组件类型',
  `content`          JSON         DEFAULT NULL COMMENT '组件内容',
  `meta`             JSON         DEFAULT NULL COMMENT '扩展信息',
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`),
  KEY `idx_item` (`item_id`),
  KEY `idx_type` (`component_type`),
  CONSTRAINT `fk_comp_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comp_item` FOREIGN KEY (`item_id`) REFERENCES `question_items`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目组件表';

-- =============================================
-- 11. 题目标签表
-- =============================================
CREATE TABLE `question_tags` (
  `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `question_id` BIGINT(20)  NOT NULL COMMENT '题目ID',
  `tag_name`    VARCHAR(50) NOT NULL COMMENT '标签名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_tag` (`question_id`,`tag_name`),
  CONSTRAINT `fk_tag_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- 12. 试卷表（修改：试卷绑定学科而非课程）
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper` (
  `paper_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  `paper_name` VARCHAR(100) NOT NULL COMMENT '试卷名称',
  `subject_id` INT(11) NOT NULL COMMENT '所属学科ID（教考分离后试卷属于学科）',
  `total_score` INT(11) NOT NULL DEFAULT 100 COMMENT '试卷总分',
  `compose_type` CHAR(1) NOT NULL COMMENT '组卷方式(1手动 2智能)',
  `is_sealed` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否封存(0否 1是)',
  `creator_id` BIGINT(20) NOT NULL COMMENT '组卷教师ID（出卷人）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`paper_id`),
  KEY `idx_subject` (`subject_id`),
  KEY `idx_creator` (`creator_id`),
  CONSTRAINT `fk_paper_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_paper_creator` FOREIGN KEY (`creator_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表（教考分离：试卷属于学科）';

-- =============================================
-- 13. 试卷题目关联表
-- =============================================
CREATE TABLE `exam_paper_question` (
  `paper_id`     BIGINT(20)   NOT NULL COMMENT '试卷ID',
  `question_id`  BIGINT(20)   NOT NULL COMMENT '题目ID',
  `score`        DECIMAL(5,1) NOT NULL COMMENT '本题分值',
  `sort_order`   INT(11)      NOT NULL COMMENT '题目顺序',
  PRIMARY KEY (`paper_id`,`question_id`),
  KEY `idx_question` (`question_id`),
  CONSTRAINT `fk_pq_paper` FOREIGN KEY (`paper_id`) REFERENCES `exam_paper`(`paper_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pq_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- =============================================
-- 14. 考试表
-- =============================================
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
  CONSTRAINT `fk_exam_paper` FOREIGN KEY (`paper_id`) REFERENCES `exam_paper`(`paper_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_exam_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- =============================================
-- 15. 考试设置表
-- =============================================
CREATE TABLE `exam_setting` (
  `setting_id`                    BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id`                       BIGINT(20)   NOT NULL COMMENT '考试ID',
  `duration_minute`               INT(11)      DEFAULT NULL COMMENT '考试时长(分钟)',
  `allow_late_enter`              TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许迟到进入',
  `question_shuffle`              TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '题目是否乱序',
  `option_shuffle`                TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '选项是否乱序',
  `prevent_screen_switch`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否切屏监控',
  `passing_score`                 DECIMAL(5,1) DEFAULT NULL COMMENT '及格分数',
  `auto_submit`                   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否超时自动提交',
  `allow_view_paper`              TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许考后查看试卷',
  `allow_view_score`              TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否允许查看成绩',
  `generate_exam_code`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否生成考试码',
  `peer_review`                   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否生生互评',
  `fill_case_sensitive`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '填空题是否区分大小写',
  `fill_ignore_symbols`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '填空题是否忽略符号',
  `fill_manual_mark`              TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '填空题是否需要人工批改',
  `multi_choice_partial_score`    TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '多选未选全评分方式',
  `multi_choice_partial_ratio`    DECIMAL(4,2) DEFAULT NULL COMMENT '多选未选全得分比例',
  `sort_question_score_per_blank` TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '排序题是否按空给分',
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `uk_exam` (`exam_id`),
  CONSTRAINT `fk_setting_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试设置表';

-- =============================================
-- 16. 考试记录表
-- =============================================
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
  CONSTRAINT `fk_record_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_record_student` FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- =============================================
-- 17. 答题详情表
-- =============================================
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
  CONSTRAINT `fk_answer_record` FOREIGN KEY (`record_id`) REFERENCES `exam_record`(`record_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_marker` FOREIGN KEY (`marker_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情表';

-- =============================================
-- 18. 错题本表
-- =============================================
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
  CONSTRAINT `fk_wrong_student` FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wrong_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- =============================================
-- 19. 系统操作日志表
-- =============================================
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
  `status`            TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '操作状态 1成功 0失败',
  `execution_time_ms` INT(11)      DEFAULT NULL COMMENT '操作耗时(毫秒)',
  `extra_data`        JSON         DEFAULT NULL COMMENT '额外结构化数据',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_module` (`module`),
  KEY `idx_target` (`target_type`,`target_id`),
  CONSTRAINT `fk_oplog_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- =============================================
-- 20. 用户登录日志表
-- =============================================
CREATE TABLE `user_login_log` (
  `login_id`       BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '登录日志ID',
  `user_id`        BIGINT(20)   DEFAULT NULL COMMENT '登录用户ID',
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

-- =============================================
-- 21. 安全事件日志表
-- =============================================
CREATE TABLE `security_event_log` (
  `event_id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '安全事件ID',
  `event_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '事件发生时间',
  `user_id`          BIGINT(20)   DEFAULT NULL COMMENT '关联用户ID',
  `ip_address`       VARCHAR(45)  NOT NULL COMMENT '事件来源IP',
  `event_type`       VARCHAR(50)  NOT NULL COMMENT '事件类型',
  `risk_level`       TINYINT(1)   NOT NULL COMMENT '风险等级 1低 2中 3高 4紧急',
  `description`      VARCHAR(500) NOT NULL COMMENT '事件描述',
  `is_resolved`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已处理',
  `resolution_detail` TEXT        DEFAULT NULL COMMENT '处理详情',
  `extra_data`       JSON         DEFAULT NULL COMMENT '额外数据',
  PRIMARY KEY (`event_id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全事件日志表';

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
-- 23. 学生成绩总表
CREATE TABLE `student_grade` (
  `grade_id`        BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '成绩记录ID',
  `student_id`      BIGINT(20)   NOT NULL COMMENT '学生ID，关联 user 表',
  `course_id`       BIGINT(20)   DEFAULT NULL COMMENT '所属课程ID（可选）',
  `subject_id`      INT(11)      DEFAULT NULL COMMENT '所属学科ID（可选）',
  `grade_type`      VARCHAR(30)  NOT NULL COMMENT '成绩类型：ONLINE_EXAM, OFFLINE_EXAM, QUIZ, HOMEWORK, PROJECT, EXPERIMENT, ATTENDANCE 等',
  `source_id`       BIGINT(20)   DEFAULT NULL COMMENT '来源ID（如线上考试可填 exam_id；线下可留空或自定义编号）',
  `grade_name`      VARCHAR(100) NOT NULL COMMENT '成绩名称，如“期中考试”、“Python作业1”',
  `score`           DECIMAL(5,1) NOT NULL COMMENT '实际得分',
  `full_score`      DECIMAL(5,1) NOT NULL DEFAULT 100.0 COMMENT '满分值',
  `teacher_id`      BIGINT(20)   NOT NULL COMMENT '成绩录入教师ID',
  `record_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成绩录入时间',
  `remark`          VARCHAR(255) DEFAULT NULL COMMENT '备注（如扣分原因、评语等）',
  PRIMARY KEY (`grade_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_grade_type` (`grade_type`),
  KEY `idx_record_time` (`record_time`),
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_grade_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_grade_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course`(`course_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_grade_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject`(`subject_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生成绩总表（兼容线上线下各类成绩）';

-- 24、考试与课程多对多关系
CREATE TABLE `exam_course` (
  `exam_id` BIGINT(20) NOT NULL COMMENT '考试ID',
  `course_id` BIGINT(20) NOT NULL COMMENT '课程ID（班级）',
  `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布到该课程的时间',
  `publisher_id` BIGINT(20) NOT NULL COMMENT '发布人ID（任课老师或教务老师）',
  PRIMARY KEY (`exam_id`, `course_id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_publisher` (`publisher_id`),
  CONSTRAINT `fk_ec_exam` FOREIGN KEY (`exam_id`) REFERENCES `exam`(`exam_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ec_course` FOREIGN KEY (`course_id`) REFERENCES `edu_course`(`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ec_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user`(`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试发布到课程（班级）关系表，支持多对多';

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '数据库建表完成！所有表已成功创建，外键约束已建立。' AS `状态`;