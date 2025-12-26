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

];
import { Login, Question } from "../views";

const routes : Array<RouteRecordRaw> = [
    {
        path : "/",
        redirect : "/hello"        // 实际情况，不会有单纯的根路由，所以在访问网址时，就需要重定向。
    },
    {
        path : "/login",           // 路由的URL
        name : "login",             // 路由的名字
        component : Login,    // 路由对应的组件
    },                           // 如果还有其他的路由，就继续添加
    {
        path: "/teacher/question",
        name: "question",
        component : Question,
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