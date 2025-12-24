import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import pinia from "../stores/createPinia";
import { useMainStore } from "../stores";
import { storeToRefs } from "pinia";

import { 
  Login, 
  Register, 
  TeacherRegister,
  ProfileManagement
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
      }
    ]
  }
]

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
    
    if (adminPaths.some(path => to.path.startsWith(path))) {
      // 这里可以添加检查管理员权限的逻辑
      // 暂时允许所有已登录用户访问（实际应用中需要验证权限）
      next();
    } else {
      next();
    }
  }
});

export default router;