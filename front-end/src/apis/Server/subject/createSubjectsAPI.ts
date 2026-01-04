import { request } from '../../axios';

const createSubjectsAPI = async (data:{
    subjectName: string;
    subjectCode: string;
    gradeLevel: number;
    status: number;
}) => {    //如果需要其他请求参数，在这里输入
    return request("/api/subject",{
        method: "POST",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        data: data
    })
}

export default createSubjectsAPI