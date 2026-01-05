import { request } from '../../axios';

const uploadInvigilationVideoAPI = async (data:{ video: string}) => {
    let url = `/api/exam/invigilation`
    return request(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
            "Exam-Token": localStorage.getItem("examToken")
        },
        data: data
    });
};

export default uploadInvigilationVideoAPI;
