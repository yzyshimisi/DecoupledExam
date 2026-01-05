import { request } from '../../axios';

const judgeEligibleAPI = async (examId: number) => {
    let url = `/api/exam/${examId}/eligible`
    return request(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
    });
};

export default judgeEligibleAPI;
