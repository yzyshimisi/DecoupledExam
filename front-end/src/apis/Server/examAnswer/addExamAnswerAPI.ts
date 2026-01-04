import { request } from '../../axios';

const addExamAnswerAPI = async (data:{
    answers: any,
    examId: number,
}) => {
    return request("/api/examAnswer", {
        method: "POST",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default addExamAnswerAPI;
