import { request } from '../../axios';

const addQuestionsAPI = async (data) => {
    return request("/api/question", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default addQuestionsAPI;
