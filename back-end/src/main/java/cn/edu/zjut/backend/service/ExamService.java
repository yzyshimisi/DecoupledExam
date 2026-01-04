package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.*;
import cn.edu.zjut.backend.po.*;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service("examServ")
public class ExamService {

    @Autowired
    private ExamDAO examDAO;

    @Autowired
    private ExamSettingDAO examSettingDAO;

    @Autowired
    private ExamRecordDAO examRecordDAO;

    @Autowired
    private ExamNotificationService examNotificationService;

    @Autowired
    private ExamCourseDAO examCourseDAO;

    @Autowired
    private StudentCourseDAO studentCourseDAO;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 创建考试（核心修复：先保存Exam获取ID，再保存ExamSetting）
     */
    public Long createExam(Exam exam, ExamSetting setting) {
        Session session = null;
        Transaction tran = null;
        try {
            session = HibernateUtil.getSession();
            tran = session.beginTransaction();

            // 步骤1：保存Exam（生成自增examId）
            examDAO.setSession(session);
            session.save(exam);
            session.flush(); // 强制刷新以获取生成的ID
            Long examId = exam.getExamId(); // 获取自增ID

            // 步骤2：绑定examId到ExamSetting，补充默认值（匹配SQL）
            setting.setExamId(examId);
            // 补充SQL默认值（防止字段为空）
            if (setting.getAllowLateEnter() == null) setting.setAllowLateEnter((byte) 1);
            if (setting.getQuestionShuffle() == null) setting.setQuestionShuffle((byte) 1);
            if (setting.getOptionShuffle() == null) setting.setOptionShuffle((byte) 1);
            if (setting.getPreventScreenSwitch() == null) setting.setPreventScreenSwitch((byte) 0);
            if (setting.getAutoSubmit() == null) setting.setAutoSubmit((byte) 1);
            if (setting.getAllowViewPaper() == null) setting.setAllowViewPaper((byte) 1);
            if (setting.getAllowViewScore() == null) setting.setAllowViewScore((byte) 1);
            if (setting.getGenerateExamCode() == null) setting.setGenerateExamCode((byte) 0);
            if (setting.getPeerReview() == null) setting.setPeerReview((byte) 0);
            if (setting.getFillCaseSensitive() == null) setting.setFillCaseSensitive((byte) 0);
            if (setting.getFillIgnoreSymbols() == null) setting.setFillIgnoreSymbols((byte) 1);
            if (setting.getFillManualMark() == null) setting.setFillManualMark((byte) 0);
            if (setting.getMultiChoicePartialScore() == null) setting.setMultiChoicePartialScore((byte) 1);
            if (setting.getSortQuestionScorePerBlank() == null) setting.setSortQuestionScorePerBlank((byte) 1);

            // 步骤3：保存ExamSetting
            examSettingDAO.saveSetting(session, setting);

            // 步骤4：不再创建预考试通知，只在发布考试时发送通知
            // examNotificationService.createPreExamNotification(exam, session); // 已禁用定时通知

            tran.commit();
            return examId;
        } catch (Exception e) {
            if (tran != null) tran.rollback();
            e.printStackTrace();
            return null;
        } finally {
            // 不要在创建通知之前关闭session
            // Session将在HibernateUtil的ThreadLocal中管理
        }
    }

    /**
     * 查询考试列表
     * @param status 考试状态 (可选)
     * @param teacherId 教师ID (可选)
     * @return 考试列表
     */
    public List<Exam> getExamList(Byte status, Long teacherId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            return examDAO.queryByStatus(status, teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试列表失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 根据ID获取考试详情
     * @param examId 考试ID
     * @return 考试对象
     */
    public Exam getExamById(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            return examDAO.getById(examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试详情失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 更新考试信息
     * @param exam 考试对象
     * @return 是否更新成功
     */
    public boolean updateExam(Exam exam) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            examDAO.setSession(session);
            examDAO.updateExam(exam);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("更新考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 删除考试
     * @param examId 考试ID
     * @return 是否删除成功
     */
    public boolean deleteExam(Long examId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // 设置各DAO的session
            examDAO.setSession(session);
            examCourseDAO.setSession(session);
            examRecordDAO.setSession(session);

            // 1. 删除考试与课程的关联记录
            examCourseDAO.deleteByExamId(examId);

            // 2. 删除考试记录（考试成绩相关）
            // 注意：这里不能直接删除，因为exam_record表中有外键约束
            // 由于exam_record表的外键设置了ON DELETE CASCADE，删除exam时会自动删除相关记录
            // 但为了明确处理，我们可以先删除关联数据
            String hql = "DELETE FROM ExamRecord WHERE examId = :examId";
            org.hibernate.query.Query deleteRecordQuery = session.createQuery(hql);
            deleteRecordQuery.setParameter("examId", examId);
            deleteRecordQuery.executeUpdate();

            // 3. 删除考试设置（通过exam_setting表的外键级联删除会自动处理）
            // 由于exam_setting表设置了ON DELETE CASCADE，删除exam时会自动删除设置
            // 但为确保完整性，显式删除考试设置
            String deleteSettingHql = "DELETE FROM ExamSetting WHERE examId = :examId";
            org.hibernate.query.Query deleteSettingQuery = session.createQuery(deleteSettingHql);
            deleteSettingQuery.setParameter("examId", examId);
            deleteSettingQuery.executeUpdate();

            // 4. 删除考试通知（已移除相关方法，使用HQL直接删除）
            String deleteNotificationHql = "DELETE FROM ExamNotification WHERE examId = :examId";
            org.hibernate.query.Query deleteNotificationQuery = session.createQuery(deleteNotificationHql);
            deleteNotificationQuery.setParameter("examId", examId);
            deleteNotificationQuery.executeUpdate();

            // 5. 最后删除考试本身
            examDAO.deleteExam(examId);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("删除考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 为考试添加学生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否添加成功
     */
    public boolean addStudentsToExam(Long examId, List<Long> studentIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // 检查考试是否存在
            examDAO.setSession(session);
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }

            // 添加学生到考试
            examRecordDAO.addStudentsToExam(session, examId, studentIds);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("添加考生失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 从考试中移除学生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否移除成功
     */
    public boolean removeStudentsFromExam(Long examId, List<Long> studentIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // 检查考试是否存在
            examDAO.setSession(session);
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }

            // 从考试中移除学生
            examRecordDAO.removeStudentsFromExam(session, examId, studentIds);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("移除考生失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 获取考试的所有考生记录
     * @param examId 考试ID
     * @return 考试记录列表
     */
    public List<ExamRecord> getStudentsByExam(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            // 先检查考试是否存在
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }
            return examRecordDAO.getRecordsByExamId(session, examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考生列表失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 根据考试记录ID获取考试记录
     * @param recordId 考试记录ID
     * @return 考试记录
     */
    public ExamRecord getExamRecordById(Long recordId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            return examRecordDAO.getRecordById(session, recordId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试记录失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 检查教师是否为教务老师
     * @param teacherId 教师ID
     * @return 是否为教务老师
     */
    public boolean isAcademicAffairsTeacher(Long teacherId) {
        return teacherService.isAcademicAffairsTeacher(teacherId);
    }

    /**
     * 将考试发布到课程
     * @param examId 考试ID
     * @param courseIds 课程ID列表
     * @param publisherId 发布者ID
     * @return 是否发布成功
     */
    public boolean publishExamToCourses(Long examId, List<Long> courseIds, Long publisherId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            examCourseDAO.setSession(session);

            // 检查考试是否存在
            examDAO.setSession(session);
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }

            // 为每个课程添加考试发布记录
            for (Long courseId : courseIds) {
                // 检查课程是否存在
                Course course = courseService.findCourseById(courseId);
                if (course == null) {
                    throw new RuntimeException("课程不存在，ID: " + courseId);
                }

                // 检查考试是否已经发布到该课程
                if (examCourseDAO.isExamPublishedToCourse(examId, courseId)) {
                    continue; // 如果已发布，跳过
                }

                // 创建考试课程关联记录
                ExamCourse examCourse = new ExamCourse();
                ExamCoursePK id = new ExamCoursePK(examId, courseId);
                examCourse.setId(id);
                examCourse.setPublishTime(new java.util.Date());
                examCourse.setPublisherId(publisherId);

                examCourseDAO.addExamToCourse(examCourse);

                // 将该课程的所有学生添加到考试中
                addCourseStudentsToExam(session, examId, courseId);
            }

            // 为考试创建即时通知（发送给相关学生）
            examNotificationService.createImmediateNotificationForExam(exam, courseIds);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("发布考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 从课程中移除考试发布
     * @param examId 考试ID
     * @param courseIds 课程ID列表
     * @return 是否移除成功
     */
    public boolean removeExamFromCourses(Long examId, List<Long> courseIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            examCourseDAO.setSession(session);

            for (Long courseId : courseIds) {
                examCourseDAO.deleteByExamIdAndCourseId(examId, courseId);
            }

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("移除考试发布失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 获取考试已发布的所有课程
     * @param examId 考试ID
     * @return 课程ID列表
     */
    public List<Long> getCoursesByExam(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examCourseDAO.setSession(session);
            return examCourseDAO.getCoursesByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试发布的课程失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 获取课程中的所有考试
     * @param courseId 课程ID
     * @return 考试ID列表
     */
    public List<Long> getExamsByCourse(Long courseId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examCourseDAO.setSession(session);
            return examCourseDAO.getExamsByCourseId(courseId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询课程的考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 将课程的学生添加到考试中
     * @param session Hibernate会话
     * @param examId 考试ID
     * @param courseId 课程ID
     */
    private void addCourseStudentsToExam(Session session, Long examId, Long courseId) {
        studentCourseDAO.setSession(session);
        examRecordDAO.setSession(session);

        // 获取课程中的所有学生
        List<Long> studentIds = studentCourseDAO.getStudentsByCourseId(courseId);

        if (studentIds != null && !studentIds.isEmpty()) {
            // 将这些学生添加到考试中
            examRecordDAO.addStudentsToExam(session, examId, studentIds);
        }
    }

    /**
     * 获取学生参加的考试列表
     * @param studentId 学生ID
     * @return 考试列表
     */
    public List<Exam> getExamsByStudentId(Long studentId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            examRecordDAO.setSession(session);

            // 从exam_record表中获取学生参加的考试ID列表
            String hql = "SELECT r.examId FROM ExamRecord r WHERE r.studentId = :studentId";
            org.hibernate.query.Query<Long> examIdQuery = session.createQuery(hql, Long.class);
            examIdQuery.setParameter("studentId", studentId);
            List<Long> examIds = examIdQuery.getResultList();

            if (examIds.isEmpty()) {
                return new ArrayList<>();
            }

            // 根据考试ID列表查询考试详情
            String examHql = "FROM Exam e WHERE e.examId IN (:examIds) ORDER BY e.startTime DESC";
            org.hibernate.query.Query<Exam> examQuery = session.createQuery(examHql, Exam.class);
            examQuery.setParameter("examIds", examIds);
            List<Exam> exams = examQuery.getResultList();

            return exams;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询学生考试列表失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 为所有现有考试创建通知（用于初始化或修复通知）
     * @return 成功处理的考试数量
     */
    public int createNotificationsForAllExistingExams() {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);

            // 获取所有考试
            String hql = "FROM Exam ORDER BY examId";
            org.hibernate.query.Query<Exam> examQuery = session.createQuery(hql, Exam.class);
            List<Exam> exams = examQuery.getResultList();

            int processedCount = 0;
            for (Exam exam : exams) {
                // 为每个考试创建通知
                examNotificationService.createNotificationForExistingExam(exam);
                processedCount++;
            }

            System.out.println("已为 " + processedCount + " 个考试创建通知");
            return processedCount;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("为现有考试创建通知失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}