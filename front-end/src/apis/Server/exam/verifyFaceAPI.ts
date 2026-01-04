import { request } from '../../axios';

const verifyFaceAPI = async (examId: number, data: {video: string}) => {
    let url = `/api/exam/${examId}/verify-face`
    return request(url, {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default verifyFaceAPI;
