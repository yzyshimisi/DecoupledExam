import { request } from '../../axios';

const deleteQuestionTagsAPI = async (data: any[]) => {    //如果需要其他请求参数，在这里输入
    return request("/api/question/tags",{
        method: "DELETE",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        data: data
    })
}

export default deleteQuestionTagsAPI