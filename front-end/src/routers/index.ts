import { createRouter, createWebHistory, RouteRecordRaw  } from "vue-router";
import pinia from "../stores/createPinia";
import { useMainStore } from "../stores";
import { storeToRefs } from "pinia";

import { Admin, Login, Register, TeacherRegister, ProfileManagement, Question, Subject, ExamPaper, StudentDashboard, ExamPage, LogManagement, StudentGrade, StudentGrades } from "../views";

import ExamList from "../views/exam/ExamList.vue";
import ExamDetail from "../views/exam/ExamDetail.vue";
import CreateExam from "../views/exam/CreateExam.vue";
import ExamResult from "../views/exam/ExamResult.vue";
import ExamStudents from "../views/exam/ExamStudents.vue";
import ExamNotifications from "../views/student/ExamNotifications.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/login"
  },
  {
    path: "/login",
    name: "login",
    component: Login,
  },
  {
    path: "/register",
    name: "register",
    component: Register,
  },
  {
    path: "/profile",
    name: "profile",
    component: ProfileManagement,
  },
  {
    path: "/admin/teacher/register",        
    name: "teacherRegister",
    component: TeacherRegister,
  },
  {
    path: "/teacher/question",
    name: "question",
    component: Question,
  },
  {
    path: "/teacher/exam-paper",
    name: "examPaper",
    component: ExamPaper,
  },
  {
    path: "/admin/subject",
    name: "subject",
    component: Subject,
  },
  {
      path: "/admin/logs",
      name: "logManagement",
      component: LogManagement,
  },
  {
    path: "/admin",
    component: Admin,
    children: [
      {
        path: "",
        name: "adminDashboard",
        component: () => import('../views/admin/dashboard/index.vue'),
      },
      {
        path: "profile",
        name: "profileManagement",
        component: ProfileManagement,
      }
    ]
  },
  {
    path: "/student",
    component: () => import('../views/student/index.vue'),
    children: [
      {
        path: "",
        name: "studentDashboard",
        component: StudentDashboard,
      },
      {
        path: "courses",
        name: "studentCourses",
        component: () => import('../views/student/courses/index.vue'),
      },
      {
        path: "courses/join",
        name: "joinCourse",
        component: () => import('../views/student/courses/joinCourse.vue'),
      }
    ]
  },
  {
    path: "/teacher",
    component: () => import('../views/teacher/index.vue'),
    children: [
      {
        path: "",
        name: "teacherDashboard",
        component: () => import('../views/teacher/dashboard/index.vue'),
      },
      {
        path: "courses",
        name: "teacherCourses",
        component: () => import('../views/teacher/courses/index.vue'),
      }
    ]
  },
  // 考试相关路由
  {
    path: "/exam",
    name: "exam-list",
    component: ExamList,
  },
  {
    path: "/exam/:id",
    name: "exam-detail",
    component: ExamDetail,
    props: true
  },
  {
    path: "/exam/:id/edit",
    name: "exam-edit",
    component: CreateExam,
    props: true
  },
  {
    path: "/exam/create",
    name: "exam-create",
    component: CreateExam
  },
  {
    path: "/exam/:id/result",
    name: "exam-result",
    component: ExamResult,
    props: true
  },
  {
    path: "/exam/:id/students",
    name: "exam-students",
    component: ExamStudents,
    props: true
  },
  {
    path: "/student/notifications",
    name: "exam-notifications",
    component: ExamNotifications
  },
  {
    path: "/teacher/student-grade",
    name: "studentGrade",
    component: StudentGrade,
  },
  {
    path: "/student/grades",
    name: "studentGrades",
    component: StudentGrades,
  },
  {
    path: "/student/exam-page/:examId",
    name: "exam-page",
    component: ExamPage,
  }
];

const router = createRouter({
    history: createWebHistory(),    // 路由历史
    routes: routes                  // 编写的路由
});

router.beforeEach((to, _, next) => {
    const loginstore = useMainStore().useLoginStore(pinia);
    const { loginSession } = storeToRefs(loginstore);
    //loginSession 是登录的标记，true表示已经登录，false代表未登录

    if (loginSession.value === false) {
        //解决无限重定向的问题
        if (to.path === "/login") {
            next();
        } else {
            next("/login");
        }
    } else {
        // 根据用户类型重定向到相应的考试页面
        const userType = localStorage.getItem('userType');
        if (userType === '0' && to.path === '/') {
            // 教务老师 -> 考试管理
            next('/exam');
        } else if (userType === '1' && to.path === '/') {
            // 普通老师 -> 考试管理
            next('/exam');
        } else if (userType === '2' && to.path === '/') {
            // 学生 -> 我的考试
            next('/exam');
        } else {
            // 检查是否需要管理员权限
            const adminPaths = [
                '/admin', 
                '/admin/simulate-login', 
                '/admin/user-management', 
                '/admin/user-management/students', 
                '/admin/user-management/teachers', 
                '/admin/profile',
                '/admin/teacher/register'
            ];
            
            // 检查是否需要学生权限
            const studentPaths = [
                '/student',
                '/student/courses',
                '/student/notifications',
                '/student/exams',
                '/student/grades',
                '/student/mistakes'
            ];
            
            if (adminPaths.some(path => to.path.startsWith(path))) {
                // 这里可以添加检查管理员权限的逻辑
                // 暂时允许所有已登录用户访问（实际应用中需要验证权限）
                next();
            } else if (studentPaths.some(path => to.path.startsWith(path))) {
                // 检查是否为学生用户
                const userType = localStorage.getItem('userType');
                if (userType === '2') { // 2 代表学生
                    next();
                } else {
                    // 非学生用户重定向到登录页或其他页面
                    next('/login');
                }
            } else {
                // 检查是否为教师路径，学生不允许访问教师页面
                const teacherPaths = ['/teacher'];
                if (teacherPaths.some(path => to.path.startsWith(path))) {
                    const userType = localStorage.getItem('userType');
                    if (userType === '1') { // 1 代表教师
                        next();
                    } else {
                        // 非教师用户重定向到登录页或其他页面
                        next('/login');
                    }
                } else {
                    next();
                }
            }
        }
    }
});

export default router;