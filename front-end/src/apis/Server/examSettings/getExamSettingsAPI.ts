import { request } from '../../axios';

const getExamSettingsAPI = async (examId: number) => {
    let url = `/api/exam/${examId}/exam-settings`
    return request(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
            "Exam-Token": localStorage.getItem("examToken")
        },
    });
};

export default getExamSettingsAPI;
