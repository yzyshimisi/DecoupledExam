import { request } from '../../axios';

const getExam_PapersAPI = async (examId: number) => {
    let url = `/api/exam-paper/${examId}`
    return request(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
    });
};

export default getExam_PapersAPI;
