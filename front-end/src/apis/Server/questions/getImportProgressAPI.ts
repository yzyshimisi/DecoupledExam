import { request } from '../../axios';

const getImportProgressAPI = async (taskId: string) => {
    let url = `/api/taskProgress/${taskId}`
    return request(url, {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
    });
};

export default getImportProgressAPI;
