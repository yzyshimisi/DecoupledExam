import { request } from '../../axios';

const addExamPaperQuestionsAPI = async (data) => {
    return request("/api/examPaper/question", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default addExamPaperQuestionsAPI;
