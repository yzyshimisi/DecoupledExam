import { request } from '../../axios';

const getPaperGenerateProgressAPI = async (taskId: string) => {
    let url = `/api/examPaper/generate/progress/${taskId}`
    return request(url, {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    });
};

export default getPaperGenerateProgressAPI;
