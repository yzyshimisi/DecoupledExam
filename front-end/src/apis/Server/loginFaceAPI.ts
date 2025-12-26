import { request } from '../axios';

const loginFaceAPI = async (data:{ video: string }) => {    //如果需要其他请求参数，在这里输入
    return request("/api/user/login-face",{
        method: "POST",
        headers: {"Content-Type":"application/json"},
        data: data
    })
}

export default loginFaceAPI