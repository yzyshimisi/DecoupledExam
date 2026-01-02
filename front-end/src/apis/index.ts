import loginAPI from "./Server/loginAPI";
import getQuestionTypeAPI from "./Server/questions/getQuestionTypeAPI";
import getSubjectAPI from "./Server/getSubjectAPI";
import getQuestionsAPI from "./Server/questions/getQuestionsAPI";
import loginFaceAPI from "./Server/loginFaceAPI";
import {
    createExamAPI,
    getExamListAPI,
    getExamDetailAPI,
    getExamRecordAPI,
    updateExamAPI,
    deleteExamAPI,
    addStudentsToExamAPI,
    removeStudentsFromExamAPI,
    getExamStudentsAPI,
    exportExamDataAPI,
    publishExamToCourseAPI,
    unpublishExamFromCourseAPI,
    getExamPublishedCoursesAPI,
    getCourseExamsAPI,
    getStudentExamsAPI,
    getStudentNotificationsAPI,
    getExamNotificationsAPI,
    initExamNotificationsAPI
} from "./Server/examAPI";
import {
    getAllStudentsAPI,
    getStudentByIdAPI,
    searchStudentsAPI,
    getStudentsNotInExamAPI
} from "./Server/studentAPI";
import addQuestionsAPI from "./Server/addQuestionsAPI";
import importQuestionsAPI from "./Server/importQuestionsAPI";
import addQuestionsAPI from "./Server/questions/addQuestionsAPI";
import importQuestionsAPI from "./Server/questions/importQuestionsAPI";
import registerAPI  from "./Server/registerAPI";
import teacherRegisterAPI from "./Server/teacherRegisterAPI";
import userAPI from "./Server/userAPI";
import getSubjectsAPI from "./Server/subject/getSubjectsAPI";
import createSubjectsAPI from "./Server/subject/createSubjectsAPI"
import deleteSubjectsAPI from "./Server/subject/deleteSubjectsAPI"
import importSubjectsAPI from "./Server/subject/importSubjectsAPI";
import deleteQuestionsAPI from "./Server/questions/deleteQuestionsAPI";
import getQuestionTagsAPI from "./Server/questionTags/getQuestionTagsAPI";
import addQuestionTagsAPI from "./Server/questionTags/addQuestionTagsAPI";
import deleteQuestionTagsAPI from "./Server/questionTags/deleteQuestionTagsAPI";
import modifyQuestionTagsAPI from "./Server/questionTags/modifyQuestionTagsAPI";
import modifyQuestionsAPI from "./Server/questions/modifyQuestionsAPI";
import getExamPapersAPI from "./Server/examPaper/getExamPapersAPI";
import addExamPapersAPI from "./Server/examPaper/addExamPaperAPI";
import generateExamPapersAPI from "./Server/examPaper/generateExamPaperAPI";
import deleteExamPapersAPI from "./Server/examPaper/deleteExamPaperAPI";
import getQuestionsByIdAPI from "./Server/questions/getQuestionByIdAPI";
import modifyExamPaperAPI from "./Server/examPaper/modifyExamPaperAPI";
import deleteExamPaperQuestionsAPI from "./Server/examPaper/deleteExamPaperQuestionsAPI";
import modifyExamPaperQuestionAPI from "./Server/examPaper/modifyExamPaperQuestionAPI";
import addExamPaperQuestionsAPI from "./Server/examPaper/addExamPaperQuestionsAPI";

export {
    loginAPI,
    getQuestionTypeAPI,
    getSubjectAPI,
    getQuestionsAPI,
    loginFaceAPI,
    createExamAPI,
    getExamListAPI,
    getExamDetailAPI,
    getExamRecordAPI,
    updateExamAPI,
    deleteExamAPI,
    addStudentsToExamAPI,
    removeStudentsFromExamAPI,
    getExamStudentsAPI,
    exportExamDataAPI,
    publishExamToCourseAPI,
    unpublishExamFromCourseAPI,
    getExamPublishedCoursesAPI,
    getCourseExamsAPI,
    getStudentExamsAPI,
    getStudentNotificationsAPI,
    getExamNotificationsAPI,
    initExamNotificationsAPI,
    getAllStudentsAPI,
    getStudentByIdAPI,
    searchStudentsAPI,
    getStudentsNotInExamAPI,
    addQuestionsAPI,
    importQuestionsAPI,
    registerAPI,
    teacherRegisterAPI,
    userAPI,
    getSubjectsAPI,
    createSubjectsAPI,
    deleteSubjectsAPI,
    importSubjectsAPI,
    deleteQuestionsAPI,
    getQuestionTagsAPI,
    addQuestionTagsAPI,
    deleteQuestionTagsAPI,
    modifyQuestionTagsAPI,
    modifyQuestionsAPI,
    getExamPapersAPI,
    addExamPapersAPI,
    generateExamPapersAPI,
    deleteExamPapersAPI,
    getQuestionsByIdAPI,
    modifyExamPaperAPI,
    deleteExamPaperQuestionsAPI,
    modifyExamPaperQuestionAPI,
    addExamPaperQuestionsAPI,
}