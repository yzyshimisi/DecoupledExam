import { request } from '../../axios';

const getExamWrongBookAPI = async () => {
    let url = `/api/examWrongBook`
    return request(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("token")}`,
        },
    });
};

export default getExamWrongBookAPI;
