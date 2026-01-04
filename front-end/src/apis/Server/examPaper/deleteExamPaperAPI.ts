import { request } from '../../axios';

const deleteExamPapersAPI = async (data) => {
    return request("/api/examPaper", {
        method: "DELETE",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default deleteExamPapersAPI;
