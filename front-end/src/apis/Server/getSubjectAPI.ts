import { request } from '../axios';

const getSubjectAPI = async () => {    //如果需要其他请求参数，在这里输入
    return request("/api/subject",{
        method: "GET",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
    })
}

export default getSubjectAPI