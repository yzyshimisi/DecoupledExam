import { request } from '../../axios';

const modifyExamPaperAPI = async (data) => {
    return request("/api/examPaper", {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default modifyExamPaperAPI;
