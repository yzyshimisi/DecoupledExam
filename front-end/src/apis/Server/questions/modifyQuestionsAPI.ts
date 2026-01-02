import { request } from '../../axios';

const modifyQuestionsAPI = async (data:{}) => {
    return request("/api/question", {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default modifyQuestionsAPI;
