import { request } from '../../axios';

const importQuestionsAPI = async (data:{
    file: string
}) => {
    return request("/api/question/file", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default importQuestionsAPI;
