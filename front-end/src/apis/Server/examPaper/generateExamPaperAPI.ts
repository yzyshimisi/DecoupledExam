import { request } from '../../axios';

const generateExamPapersAPI = async (data) => {
    return request("/api/examPaper/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data,
    });
};

export default generateExamPapersAPI;
