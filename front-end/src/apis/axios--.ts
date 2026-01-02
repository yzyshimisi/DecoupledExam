import axios, { AxiosRequestConfig, AxiosResponse } from "axios";   // 导入模块

const axiosInstance = axios.create({        //创建实例
    baseURL:"",
    timeout: 10000,
})

const request = <T = unknown>(
    url: string,
    options?: AxiosRequestConfig,
): Promise<T> => {
    return new Promise((resolve, reject) => {
        axiosInstance({
            url,
            ...options,
        })
            .then((res: AxiosResponse) => {
                resolve(res.data);
            })
            .catch(err => reject(err))       //reject 同样由后续定义
    })
}

export { axiosInstance, request }