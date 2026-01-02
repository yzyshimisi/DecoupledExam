import { request } from '../../axios';

const addQuestionTagsAPI = async (data: any[]) => {    //如果需要其他请求参数，在这里输入
    return request("/api/question/tags",{
        method: "POST",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        data: data
    })
}

export default addQuestionTagsAPI