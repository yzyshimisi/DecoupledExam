import { request } from '../../axios';

const getQuestionsAPI = async (query: {
    mineOnly?: boolean;
    stemKeyword?: string;
    selectedTypes?: string[];
    selectedSubjects?: string[];
    maxDifficulty?: number;
    selectedStatuses?: ('0' | '1' | '2')[];
    authorQuery?: string;
}) => {
    return request("/api/question", {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        params: query
    });
};

export default getQuestionsAPI;
