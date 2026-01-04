import { request } from '../../axios';

const getExamPapersAPI = async () => {
    return request("/api/examPaper", {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    });
};

export default getExamPapersAPI;
