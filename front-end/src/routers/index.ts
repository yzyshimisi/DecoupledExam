import { createRouter, createWebHistory, RouteRecordRaw  } from "vue-router";
import pinia from "../stores/createPinia";
import { useMainStore } from "../stores";
import { storeToRefs } from "pinia";

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
    history:createWebHistory(),    // 路由历史
    routes:routes                  // 编写的路由
})

router.beforeEach((to,_,next) => {
    const loginstore = useMainStore().useLoginStore(pinia);
    const { loginSession } = storeToRefs(loginstore);
    //loginSession 是登录的标记，true表示已经登录，false代表未登录

    if(loginSession.value === false){
        //解决无限重定向的问题
        if(to.path === "/login"){
            next();
        }else{
            next("/login");
        }
    }else{
        next();
    }
});

export default router;