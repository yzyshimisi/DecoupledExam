import axios, { AxiosRequestConfig } from "axios";
import { useMainStore } from "../stores";
import router from "../routers";   // 导入模块
import { useRoute } from 'vue-router'

const axiosInstance = axios.create({        //创建实例
    baseURL:"",
    timeout: 20000,
})

axiosInstance.interceptors.response.use(
    response => {
        // 如果状态码是 200，说明接口正常，直接返回数据
        return response
    },
    error => {
        // 获取错误状态码
        if (error.response) {
            switch (error.response.code) {
                case 401:
                    // 这里是关键：401 代表未授权（Token 无效/过期）
                    handleTokenInvalid()
                    break
                // 还可以处理其他状态码，比如 500 等
                default:
                    return Promise.reject(error)
            }
        }
        return Promise.reject(error)
    }
)

// 请求拦截器，自动添加JWT Token
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器，处理通用错误
axiosInstance.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    console.error('API请求错误:', error);
    if (error.response?.code === 401) {
      // 如果是认证错误，跳转到登录页面
      localStorage.removeItem('token');
      localStorage.removeItem('userType');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

const request = <ResponseType = unknown>(  //创建request
    url: string,                             //上面如果给了baseURL，这里可以直接传入子路径
    options?: AxiosRequestConfig<unknown>,   //其他参数，包括请求头、请求方法、请求参数，由后续导入。
): Promise<ResponseType> => {
    return new Promise((resolve, reject) => {
        axiosInstance({
            url,
            ...options,
        })
            .then(res => {
                resolve(res.data)              //这里的 resolve 函数会在后面定义具体功能
            })
            .catch(err => reject(err))       //reject 同样由后续定义
    })
}

// 处理 Token 无效的函数
const handleTokenInvalid = () => {
    // 1. 清除 localStorage 中的 Token
    localStorage.removeItem('token')
    localStorage.removeItem('userType')

    // 2. 如果使用了 Vuex/Pinia，也需要清除 store 中的用户信息
    useMainStore().useLoginStore().setLogin(false)

    const route = router.currentRoute.value

    // 3. 跳转到登录页
    // 使用 replace 而不是 push，这样用户点击浏览器后退按钮不会回到刚才的页面
    router.replace({
        path: '/login',
        query: {
            // 可选：带上当前路由路径，登录成功后跳回
            redirect: route.fullPath
        }
    }).then(()=>{window.location.reload()})
}


export { axiosInstance, request }