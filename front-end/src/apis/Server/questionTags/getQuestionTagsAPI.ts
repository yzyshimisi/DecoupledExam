import { request } from '../../axios';

const getQuestionTagsAPI = async (params:{questionId: number}) => {    //如果需要其他请求参数，在这里输入
    return request("/api/question/tags",{
        method: "GET",
        headers: {"Content-Type":"application/json", "Authorization": `Bearer ${localStorage.getItem("token")}`},
        params: params
    })
}

export default getQuestionTagsAPI