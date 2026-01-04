import { request } from '../../axios';

const modifySealedStatusAPI = async (data: any[]) => {
    return request("/api/examPaper/seal", {
        method: "PUT",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        data: data
    });
};

export default modifySealedStatusAPI;
