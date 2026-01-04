import { request } from '../../axios';

const deleteSubjectsAPI = async (data:number[]) => {    //如果需要其他请求参数，在这里输入
    return request("/api/subject",{
        method: "DELETE",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        data: data
    })
}

export default deleteSubjectsAPI