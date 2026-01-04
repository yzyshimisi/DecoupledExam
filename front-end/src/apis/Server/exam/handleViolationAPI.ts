import { request } from '../../axios';

const handleViolationAPI = async () => {
    let url = `/api/exam/violation`
    return request(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
            "Exam-Token": localStorage.getItem("examToken")
        },
    });
};

export default handleViolationAPI;
