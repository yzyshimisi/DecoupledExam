package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.po.QuestionComponents;
import cn.edu.zjut.backend.po.QuestionItems;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.service.QuestionService;
import cn.edu.zjut.backend.service.SubjectService;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        // 1. 创建题目主表
        Questions question = new Questions();
        question.setTypeId(1); // 直接赋值题型ID（无需关联QuestionType对象）
        question.setTitle("2+2等于几？");
        question.setDifficulty((byte) 1);
        question.setSubjectId(1);
        question.setCreatorId(2L);

        // 2. 创建子题
        QuestionItems item1 = new QuestionItems();
        item1.setSequence(1);
        item1.setTypeId(1); // 直接赋值子题题型ID
        item1.setContent("本题的答案是？");

        // 3. 创建组件（选项）
        QuestionComponents option1 = new QuestionComponents();
        option1.setComponentType("option");
        option1.setContent("{\"label\":\"A\",\"value\":\"4\"}");

//        question.setQuestionItems(List.of(item1)); // 主题目知道它有这个子题了
//        item1.setQuestion(question);             // 子题知道它属于这个主题目了
//
//        item1.setQuestionComponents(List.of(option1)); // 子题知道它有这个组件了
//        option1.setItem(item1);                     // 组件知道它属于这个子题了
//
//
//        QuestionService questionService = new QuestionService();
//        questionService.addQuestion(question);
    }
}
