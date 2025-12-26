import { request } from '../axios';

const loginAPI = async (data:{
    username: string,
    password: string,
}) => {    //如果需要其他请求参数，在这里输入
    return request("/api/user/login",{
        method: "POST",
        headers: {"Content-Type":"application/json"},
        data: data
    })
}

export default loginAPI