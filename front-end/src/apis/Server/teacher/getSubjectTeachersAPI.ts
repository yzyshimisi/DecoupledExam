import { request } from '../../axios';

const getSubjectTeachersAPI = async (param: {subjectId: number}) => {
    return request("/api/teacher/subject/teachers", {
        method: "GET",
        headers: { "Content-Type": "application/json", "Authorization": `Bearer ${localStorage.getItem("token")}` },
        params: param
    });
};

export default getSubjectTeachersAPI;
