import { request } from '../../axios';

const getQuestionsByIdAPI = async (params:{
    questionId: number
}) => {
    return request("/api/question-id", {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        params: params
    });
};

export default getQuestionsByIdAPI;
