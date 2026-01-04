import { request } from '../../axios';

const deleteQuestionsAPI = async (data:any[]) => {
    return request("/api/question", {
        method: "DELETE",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default deleteQuestionsAPI;
