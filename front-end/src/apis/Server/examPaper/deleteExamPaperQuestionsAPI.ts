import { request } from '../../axios';

const deleteExamPaperQuestionsAPI = async (data) => {
    return request("/api/examPaper/question", {
        method: "DELETE",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default deleteExamPaperQuestionsAPI;
