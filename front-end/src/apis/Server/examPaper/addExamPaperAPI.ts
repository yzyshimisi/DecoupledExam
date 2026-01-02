import { request } from '../../axios';

const addExamPapersAPI = async (data) => {
    return request("/api/examPaper", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default addExamPapersAPI;
