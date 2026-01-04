import { request } from '../../axios';

const getSubjectsAPI = async (params: {
    subjectName?: string
    subjectCode?: string
    gradeLevel?: number | null
    status?: number | null
}) => {    //如果需要其他请求参数，在这里输入
    return request("/api/subject",{
        method: "GET",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        params: params
    })
}

export default getSubjectsAPI