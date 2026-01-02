import { request } from '../../axios';

const modifyExamPaperQuestionAPI = async (data) => {
    return request("/api/examPaper/question", {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default modifyExamPaperQuestionAPI;
