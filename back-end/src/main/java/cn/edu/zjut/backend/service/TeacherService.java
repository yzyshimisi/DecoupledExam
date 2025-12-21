package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.TeacherPositionDAO;
import cn.edu.zjut.backend.dao.TeacherSubjectDAO;
import cn.edu.zjut.backend.dao.UserDAO;
import cn.edu.zjut.backend.po.TeacherPosition;
import cn.edu.zjut.backend.po.TeacherSubject;
import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("teacherServ")
public class TeacherService {

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    /**
     * 设置教师职位
     * @param teacherId 教师ID
     * @param role 职位：0=任课老师，1=教务老师
     * @return 是否设置成功
     */
    public boolean setTeacherPosition(Long teacherId, Byte role) {
        Session session = getSession();
        TeacherPositionDAO positionDAO = new TeacherPositionDAO();
        UserDAO userDAO = new UserDAO();
        positionDAO.setSession(session);
        userDAO.setSession(session);
        Transaction tran = null;

        try {
            tran = session.beginTransaction();
            
            // 验证用户是否存在且为教师
            User user = userDAO.findById(teacherId);
            if (user == null || user.getUserType() != 1) { // 1表示教师
                return false;
            }
            
            // 查找是否已有职位记录
            TeacherPosition position = positionDAO.findByTeacherId(teacherId);
            if (position == null) {
                // 创建新记录
                position = new TeacherPosition();
                position.setTeacherId(teacherId);
                position.setRole(role);
                positionDAO.add(position);
            } else {
                // 更新现有记录
                position.setRole(role);
                positionDAO.update(position);
            }
            
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("set teacher position failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 获取教师职位
     * @param teacherId 教师ID
     * @return 教师职位信息，找不到返回null
     */
    public TeacherPosition getTeacherPosition(Long teacherId) {
        Session session = getSession();
        TeacherPositionDAO positionDAO = new TeacherPositionDAO();
        positionDAO.setSession(session);

        try {
            return positionDAO.findByTeacherId(teacherId);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 根据职位查询教师列表
     * @param role 职位：0=任课老师，1=教务老师
     * @return 符合条件的教师职位列表
     */
    public List<TeacherPosition> getTeachersByRole(Byte role) {
        Session session = getSession();
        TeacherPositionDAO positionDAO = new TeacherPositionDAO();
        positionDAO.setSession(session);

        try {
            return positionDAO.findByRole(role);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 绑定教师与学科
     * @param teacherId 教师ID
     * @param subjectId 学科ID
     * @param isMain 是否为主教学科：0=兼任，1=主讲
     * @return 是否绑定成功
     */
    public boolean bindTeacherSubject(Long teacherId, Integer subjectId, Byte isMain) {
        Session session = getSession();
        TeacherSubjectDAO subjectDAO = new TeacherSubjectDAO();
        UserDAO userDAO = new UserDAO();
        subjectDAO.setSession(session);
        userDAO.setSession(session);
        Transaction tran = null;

        try {
            tran = session.beginTransaction();
            
            // 验证用户是否存在且为教师
            User user = userDAO.findById(teacherId);
            if (user == null || user.getUserType() != 1) { // 1表示教师
                return false;
            }
            
            // 查找是否已有关联记录
            TeacherSubject teacherSubject = subjectDAO.findByTeacherIdAndSubjectId(teacherId, subjectId);
            if (teacherSubject == null) {
                // 创建新记录
                teacherSubject = new TeacherSubject();
                teacherSubject.setTeacherId(teacherId);
                teacherSubject.setSubjectId(subjectId);
                teacherSubject.setIsMain(isMain);
                subjectDAO.add(teacherSubject);
            } else {
                // 更新现有记录
                teacherSubject.setIsMain(isMain);
                subjectDAO.update(teacherSubject);
            }
            
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("bind teacher subject failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 获取教师的所有学科关联
     * @param teacherId 教师ID
     * @return 教师学科关联列表
     */
    public List<TeacherSubject> getTeacherSubjects(Long teacherId) {
        Session session = getSession();
        TeacherSubjectDAO subjectDAO = new TeacherSubjectDAO();
        subjectDAO.setSession(session);

        try {
            return subjectDAO.findByTeacherId(teacherId);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 获取学科的所有教师关联
     * @param subjectId 学科ID
     * @return 教师学科关联列表
     */
    public List<TeacherSubject> getSubjectTeachers(Integer subjectId) {
        Session session = getSession();
        TeacherSubjectDAO subjectDAO = new TeacherSubjectDAO();
        subjectDAO.setSession(session);

        try {
            return subjectDAO.findBySubjectId(subjectId);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 解绑教师与学科
     * @param teacherId 教师ID
     * @param subjectId 学科ID
     * @return 是否解绑成功
     */
    public boolean unbindTeacherSubject(Long teacherId, Integer subjectId) {
        Session session = getSession();
        TeacherSubjectDAO subjectDAO = new TeacherSubjectDAO();
        UserDAO userDAO = new UserDAO();
        subjectDAO.setSession(session);
        userDAO.setSession(session);
        Transaction tran = null;

        try {
            tran = session.beginTransaction();
            
            // 验证用户是否存在且为教师
            User user = userDAO.findById(teacherId);
            if (user == null || user.getUserType() != 1) { // 1表示教师
                tran.rollback();
                return false;
            }
            
            subjectDAO.deleteByTeacherIdAndSubjectId(teacherId, subjectId);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("unbind teacher subject failed " + e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    /**
     * 获取主教学科的教师列表
     * @return 主教学科的教师列表
     */
    public List<TeacherSubject> getMainTeachers() {
        Session session = getSession();
        TeacherSubjectDAO subjectDAO = new TeacherSubjectDAO();
        subjectDAO.setSession(session);

        try {
            return subjectDAO.findMainTeachers();
        } finally {
            HibernateUtil.closeSession();
        }
    }
}