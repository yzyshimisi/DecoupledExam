import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import pinia from "../stores/createPinia";
import { useMainStore } from "../stores";
import { storeToRefs } from "pinia";


import {
  Login,
  Register,
  TeacherRegister,
  ProfileManagement,
  Question,
  StudentDashboard
} from "../views";

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
    path: "/admin",
    component: () => import('../views/admin/index.vue'),
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
      },
      {
        path: "/teacher/question",
        name: "question",
        component : Question,
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
  }
  ,
  {
    path: "/teacher",
    component: () => import('../views/teacher/dashboard/index.vue'),
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
  }
  ,
  {
    path: "/teacher",
    name: "teacherDashboard",
    component: () => import('../views/teacher/dashboard/index.vue'),
  },
  {
    path: "/teacher/courses",
    name: "teacherCourses",
    component: () => import('../views/teacher/courses/index.vue'),
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes: routes
})

router.beforeEach((to, _, next) => {
  const loginstore = useMainStore().useLoginStore(pinia);
  const { loginSession } = storeToRefs(loginstore);

  if (loginSession.value === false) {
    if (to.path === "/login" || to.path === "/register") {
      next();
    } else {
      next("/login");
    }
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
});

export default router;