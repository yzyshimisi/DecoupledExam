import { request } from '../../axios';

const sendPrepareExamPaperMailAPI = async (data: {
    mail: string,
    subject: string,
    content: string,
}) => {
    let url = `/api/exam/sendPrepareExamPaperMail`
    return request(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
        data: data
    });
};

export default sendPrepareExamPaperMailAPI;
