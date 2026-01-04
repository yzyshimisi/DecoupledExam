package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.*;
import cn.edu.zjut.backend.dto.ExamGenerationRequest;
import cn.edu.zjut.backend.dto.ExamPaperDTO;
import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.*;
import cn.edu.zjut.backend.util.ChatGLM_N;
import cn.edu.zjut.backend.util.HibernateUtil;
import cn.edu.zjut.backend.util.JedisConnectionFactory;
import cn.edu.zjut.backend.util.UserContext;
import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service("examPaperServ")
public class ExamPaperService {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TASK_PREFIX = "task:";

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    // 添加试卷（手动组卷）
    public boolean addExamPaper(ExamPaperDTO examPaperDTO) {

        ExamPaper examPaper = new ExamPaper();
        BeanUtil.copyProperties(examPaperDTO, examPaper);

        Session session = this.getSession();
        ExamPaperDAO examPaperDAO = new ExamPaperDAO();
        ExamPaperQuestionDAO questionDAO = new ExamPaperQuestionDAO();
        examPaperDAO.setSession(session);
        questionDAO.setSession(session);

        Transaction tran = null;
        try {
            tran = session.beginTransaction();

            examPaper.setComposeType("1");  // 设置为手动组件
            examPaper.setCreatorId(UserContext.getUserId());
            examPaperDAO.add(examPaper);
            if(examPaperDTO.getQuestions() != null){
                for(ExamPaperDTO.QuestionInfoDTO questionInfoDTO : examPaperDTO.getQuestions()){
                    ExamPaperQuestionId examPaperQuestionId = new ExamPaperQuestionId();
                    ExamPaperQuestion examPaperQuestion = new ExamPaperQuestion();

                    examPaperQuestionId.setPaperId(examPaper.getPaperId());
                    examPaperQuestionId.setQuestionId(questionInfoDTO.getQuestionId());
                    BeanUtil.copyProperties(questionInfoDTO, examPaperQuestion);
                    examPaperQuestion.setId(examPaperQuestionId);

                    questionDAO.add(examPaperQuestion);
                }
            }

            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("save examPaper failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 智能组卷
    public boolean generateExamPaperAsync(ExamGenerationRequest examGenerationRequest, String taskId) {
//        exportQuestionTagsToCsv("C:\\Users\\31986\\Desktop\\questionTags.csv");

        updateTaskStatus(taskId, "running", 10, "开始获取题目...");   // 记录任务进度

        Session session = this.getSession();
        QuestionDAO dao = new QuestionDAO();
        ExamPaperDAO examPaperDAO = new ExamPaperDAO();
        ExamPaperQuestionDAO questionDAO = new ExamPaperQuestionDAO();
        dao.setSession(session);
        examPaperDAO.setSession(session);
        questionDAO.setSession(session);

        Transaction tran = null;

        try {
            tran = session.beginTransaction();

            ExamPaper examPaper = new ExamPaper();
            BeanUtil.copyProperties(examGenerationRequest, examPaper);
            examPaper.setComposeType("2");
            System.out.println(UserContext.getUserId());
            examPaper.setCreatorId(UserContext.getUserId());

            examPaperDAO.add(examPaper);

            updateTaskStatus(taskId, "running", 25, "试卷框架已创建"); // 记录进度

            List<ExamGenerationRequest.QuestionTypeRequirement> questionTypeRequirements = examGenerationRequest.getQuestionTypes();

            // 获取指定学科的题目
            QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
            questionQueryDTO.setSelectedSubjects(List.of(examGenerationRequest.getSubjectId()));
            List<Questions> questions = dao.query(questionQueryDTO);

            updateTaskStatus(taskId, "running", 40, "共加载 " + questions.size() + " 道备选题");   // 记录进度

            // 遍历每一个题型需求
            int totalTypes = questionTypeRequirements.size();
            for(int i = 0; i < totalTypes; i++) {
                ExamGenerationRequest.QuestionTypeRequirement questionTypeRequirement = questionTypeRequirements.get(i);

                // 动态计算进度：从 40% 到 90%
                int progress = 40 + (int) Math.ceil(50.0 * (i + 1) / totalTypes);
                updateTaskStatus(taskId, "running", progress, "正在处理题型: " + questionTypeRequirement.getTypeId());

                // 过滤指定题型的Questions对象
                List<Questions> questions1  = questions.stream()
                        .filter(question -> questionTypeRequirement.getTypeId().equals(question.getTypeId())) // 筛选条件
                        .collect(Collectors.toList());  // 将结果收集到新的List中

                // 从用户描述中提取标签
                String tagsJson = extractTags(questionTypeRequirement, questions);
                Gson gson = new Gson();
                List<String> tags = gson.fromJson(tagsJson, new TypeToken<List<String>>() {}.getType());
                System.out.println("大模型提取的标签：" + tags);

                // 获取备选的题目对象列表
                List<Questions> candidateQue = chooseQuestion(questions1, tags);

                List<Long> ids = new ArrayList<>();  // 选择的题目ID列表

                if(candidateQue.size() < questionTypeRequirement.getCount()){   // 如果候选数量不够，目前是随机选择
                    for(Questions question : candidateQue){
                        ids.add(question.getId());
                    }
                    List<Questions> l = getRandomSupplement(questions1,candidateQue, questionTypeRequirement.getCount()-candidateQue.size());
                    for(Questions question : l){
                        ids.add(question.getId());
                    }

                    updateTaskStatus(taskId, "running", progress, "题型 " + questionTypeRequirement.getTypeId() + " 候选不足，已随机补充");

                }else{
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Questions question : candidateQue) {
                        stringBuilder.append(question.getId()).append(" ");
                    }
                    System.out.println(stringBuilder);
                    String prompt = buildSelectionPrompt(questionTypeRequirement, candidateQue);
    //            System.out.println("================================\n" + prompt + "================================\n");
                    String res = ChatGLM_N.inquire(prompt, false);
                    ids = gson.fromJson(res, new TypeToken<List<Long>>() {}.getType());

                    updateTaskStatus(taskId, "running", progress, "题型 " + questionTypeRequirement.getTypeId() + " 已通过大模型选出题目");

                }

                // 遍历每一道题目，与试卷建立关联，插入数据库
                for(Long id : ids){
                    ExamPaperQuestion examPaperQuestion = new ExamPaperQuestion();
                    ExamPaperQuestionId examPaperQuestionId = new ExamPaperQuestionId();

                    examPaperQuestionId.setQuestionId(id);
                    examPaperQuestionId.setPaperId(examPaper.getPaperId());

                    examPaperQuestion.setId(examPaperQuestionId);
                    examPaperQuestion.setScore(new BigDecimal(6));
                    examPaperQuestion.setSortOrder(1);

                    questionDAO.add(examPaperQuestion);
                }
            }

            tran.commit();
            updateTaskStatus(taskId, "completed", 100, "试卷生成成功！", examPaper.getPaperId());
            return true;
        } catch (Exception e) {
            System.out.println("save examPaper failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            updateTaskStatus(taskId, "failed", 0, "组卷失败: " + e.getMessage());
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 查询试卷
    public List<ExamPaperDTO> queryExamPaper(Long creatorId) {

        List<ExamPaperDTO> examPaperDTOList = new ArrayList<ExamPaperDTO>();

        // DAO查询
        Session session = this.getSession();
        ExamPaperDAO examPaperDAO = new ExamPaperDAO();
        ExamPaperQuestionDAO questionDAO = new ExamPaperQuestionDAO();
        examPaperDAO.setSession(session);
        questionDAO.setSession(session);

        // 查询所有的试卷
        List<ExamPaper> examPapers = examPaperDAO.query(creatorId);
        if(examPapers != null && !examPapers.isEmpty()){
            for(ExamPaper examPaper : examPapers){

                ExamPaperDTO examPaperDTO = new ExamPaperDTO();     // 试卷DTO
                BeanUtil.copyProperties(examPaper, examPaperDTO);

                List<ExamPaperQuestion> examPaperQuestions = questionDAO.query(examPaper.getPaperId());
                for(ExamPaperQuestion examPaperQuestion : examPaperQuestions){
                    ExamPaperDTO.QuestionInfoDTO questionInfoDTO = new ExamPaperDTO.QuestionInfoDTO();
                    questionInfoDTO.setQuestionId(examPaperQuestion.getId().getQuestionId());   // 提取题目的ID
                    BeanUtil.copyProperties(examPaperQuestion, questionInfoDTO);

                    examPaperDTO.getQuestions().add(questionInfoDTO);
                }

                examPaperDTOList.add(examPaperDTO);
            }
        }

        return examPaperDTOList;
    }

    // 删除试卷
    public boolean deleteExamPaper(List<Long> paperIds) {
        Session session = this.getSession();
        ExamPaperDAO dao = new ExamPaperDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for(Long paperId : paperIds){
                dao.delete(paperId);
            }
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("save customer failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 修改试卷
    public boolean updateExamPaper(ExamPaper examPaper) {
        examPaper.setUpdatedAt(new Date());
        examPaper.setCreatorId(UserContext.getUserId());

        Session session = this.getSession();
        ExamPaperDAO dao = new ExamPaperDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            dao.update(examPaper);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("update examPaper failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 添加试卷题目
    public boolean addExamPaperQuestion(List<ExamPaperQuestion> examPaperQuestions) {
        Session session = this.getSession();
        ExamPaperQuestionDAO dao = new ExamPaperQuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for(ExamPaperQuestion examPaperQuestion : examPaperQuestions){
                dao.add(examPaperQuestion);
            }
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("save examPaperQuestion failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 删除试卷题目
    public boolean deleteExamPaperQuestion(List<ExamPaperQuestionId> examPaperQuestionIds) {
        Session session = this.getSession();
        ExamPaperQuestionDAO dao = new ExamPaperQuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();

            for(ExamPaperQuestionId examPaperQuestionId : examPaperQuestionIds){
                ExamPaperQuestion examPaperQuestion = dao.query(examPaperQuestionId);
                if(examPaperQuestion != null){
                    dao.delete(examPaperQuestion);
                }else{
                    throw new Exception();
                }
            }

            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("delete examPaperQuestion failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 修改试卷题目
    public boolean updateExamPaperQuestion(List<ExamPaperQuestion> examPaperQuestions) {
        Session session = this.getSession();
        ExamPaperQuestionDAO dao = new ExamPaperQuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for(ExamPaperQuestion examPaperQuestion : examPaperQuestions){
                dao.update(examPaperQuestion);
            }
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("update examPaperQuestion failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public boolean modifySealedStatus(List<Long> paperIds){
        Session session = this.getSession();
        ExamPaperDAO dao = new ExamPaperDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();

            for(Long paperId : paperIds){
                ExamPaper examPaper = dao.queryById(paperId);
                if(examPaper != null){
                    String isSealed = examPaper.getIsSealed();
                    if (isSealed.equals("0")) {
                        examPaper.setIsSealed("1");
                    } else {
                        examPaper.setIsSealed("0");
                    }

                    examPaper.setUpdatedAt(new Date());

                    dao.update(examPaper);
                }
            }

            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("update examPaper failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 随机选择题目
    public List<Questions> getRandomSupplement(
            List<Questions> questions1,
            List<Questions> candidateQue,
            int count) {

        // 1. 提取 candidateQue 中所有题目的 ID（假设 Questions 有 getId()）
        Set<Long> excludedIds = candidateQue.stream()
                .map(Questions::getId)
                .collect(Collectors.toSet());

        // 2. 从 questions1 中筛选出不在 candidateQue 中的题目
        List<Questions> available = questions1.stream()
                .filter(q -> !excludedIds.contains(q.getId()))
                .collect(Collectors.toList());

        // 3. 随机打乱顺序
        Collections.shuffle(available, new Random()); // 可选：传入固定种子用于测试

        // 4. 取前 count 个（如果不足则返回全部）
        int actualCount = Math.min(count, available.size());
        return available.subList(0, actualCount);
    }

    // 从用户输入中提取标签（大模型）
    private String extractTags(
            ExamGenerationRequest.QuestionTypeRequirement questionTypeRequirement,
            List<Questions> questions){

        Session session = this.getSession();
        QuestionTagsDAO dao = new QuestionTagsDAO();
        dao.setSession(session);

        List<String> userTags = questionTypeRequirement.getTags();          // 用户提供的标签（可能为空）
        String description = questionTypeRequirement.getDescription();

        // 合并用户标签和描述作为输入
        StringBuilder userInput = new StringBuilder();
        if (userTags != null && !userTags.isEmpty()) {
            userInput.append(String.join("、", userTags));
            if (description != null && !description.trim().isEmpty()) {
                userInput.append("。").append(description);
            }
        } else if (description != null && !description.trim().isEmpty()) {
            userInput.append(description);
        }

        String finalUserInput = userInput.toString().trim();

        if (finalUserInput.isEmpty()) {
            finalUserInput = "（无明确输入）";
        }


        List<QuestionTags> questionTags = new ArrayList<>();
        for(Questions question : questions){
            List<QuestionTags> l = dao.query(question.getId());
            questionTags.addAll(l);
        }

        String knowledgeBase = questionTags.stream()
                .map(QuestionTags::getTagName)          // 提取 tagName
                .filter(Objects::nonNull)               // 过滤 null
                .map(String::trim)                      // 去除前后空格
                .filter(tag -> !tag.isEmpty())          // 过滤空字符串
                .distinct()                             // 去重
                .collect(Collectors.joining("、"));      // 用中文顿号连接

        // 构造 Prompt
        String prompt = String.format(
                "请根据用户的输入，从以下【知识库】中选择相关的标签。\n\n" +
                        "【知识库】\n%s\n\n" +
                        "【用户输入】\n%s\n\n" +
                        "【要求】\n" +
                        "- 只能从上述【知识库】中选择，不要编造新词\n" +
                        "- 如果知识库中没有完全匹配的，可返回语义相近的；如果实在没有相关项，请返回空列表 []\n" +
                        "- 不要解释，不要输出任何其他文字\n" +
                        "- 输出格式必须是 JSON 数组，例如：[\"标签1\", \"标签2\"]（不要```json代码框）\n" +
                        "- 把与用户输入关键词、用户描述相关的知识点全部返回（宁多勿漏）\n\n" +
                        "【输出】",
                knowledgeBase,
                finalUserInput
        );

        return ChatGLM_N.inquire(prompt, false);
    }

    // 通过标签选择Questions对象
    private List<Questions> chooseQuestion(List<Questions> questions, List<String> tags){

        Session session = this.getSession();
        QuestionTagsDAO dao = new QuestionTagsDAO();
        dao.setSession(session);

        List<Questions> candidateQue = new ArrayList<>();

        for(Questions question : questions){
            List<QuestionTags> questionTagsList = dao.query(question.getId());
            for(QuestionTags questionTags : questionTagsList){
                if(tags.contains(questionTags.getTagName())){
                    candidateQue.add(question);
                    break;
                }
            }
        }

        return candidateQue;
    }

    // 工具方法：将 Questions 转为简洁描述（避免泄露答案/选项）
    private String serializeQuestions(List<Questions> questions) {
        return questions.stream()
                .map(q -> String.format(
                        "- 题目ID: %d, 难度: %s, 题干: %s",
                        q.getId(),
                        q.getDifficulty() != null ? q.getDifficulty() : "未知",
                        truncateText(q.getTitle(), 150) // 截断题干，节省 token
                ))
                .collect(Collectors.joining("\n"));
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }


    // 智能构建选题的Prompt
    private String buildSelectionPrompt(ExamGenerationRequest.QuestionTypeRequirement req, List<Questions> candidateQue) {

        // 构建数量要求
        int requiredCount = req.getCount() != null ? req.getCount() : 1;

        String questionList = serializeQuestions(candidateQue);
        String userDesc = Optional.ofNullable(req.getDescription()).orElse("无特殊要求");

        // 处理 tags：转为逗号分隔字符串
        String tagsStr = "无指定关键词";
        if (req.getTags() != null && !req.getTags().isEmpty()) {
            tagsStr = String.join(", ", req.getTags());
        }

        // 构建难度要求描述
        // 难度策略描述（适配 1~5）
        String difficultyInstruction;
        if (req.getDifficulty() != null) { // 假设字段名是 difficultyLevel (Integer)
            int target = req.getDifficulty();
            if (target < 1 || target > 5) {
                difficultyInstruction = "难度不限（指定难度超出1~5范围）";
            } else {
                List<Integer> fallbackLevels = new ArrayList<>();
                fallbackLevels.add(target); // 第一优先级

                // 添加相邻难度（按距离排序）
                if (target - 1 >= 1) fallbackLevels.add(target - 1);
                if (target + 1 <= 5) fallbackLevels.add(target + 1);
                if (target - 2 >= 1) fallbackLevels.add(target - 2);
                if (target + 2 <= 5) fallbackLevels.add(target + 2);

                // 去重并格式化
                String levelsStr = fallbackLevels.stream().map(String::valueOf).collect(Collectors.joining(", "));
                difficultyInstruction = String.format(
                        "优先选择难度 %d 的题目；若不足，按以下顺序尝试相近难度：%s",
                        target, levelsStr
                );
            }
        } else {
            difficultyInstruction = "难度不限，可从1~5中任意选择";
        }

        return String.format(
                "你是一个智能出题助手，请从以下【候选题目】中严格选出 %d 道最符合要求的题目。\n\n" +

                        "【选题要求】\n" +
                        "- 题目数量：%d\n" +
                        "- 难度策略：%s\n" +
                        "- 关键词要求：优先包含以下标签或关键词：%s\n" +
                        "- 语义要求：题干内容应与以下描述高度契合：“%s”\n\n" +

                        "【候选题目】（共 %d 道）\n" +
                        "%s\n\n" +

                        "【输出规则】\n" +
                        "1. 只能选择上述题目，禁止编造ID\n" +
                        "2. 在满足数量的前提下，优先匹配关键词和语义，其次考虑难度弹性\n" +
                        "3. 输出必须是 JSON 数组，仅包含题目ID，例如：[101, 205]\n" +
                        "4. 不要任何解释、注释或额外文字\n" +
                        "5. 如果候选题目总数不足 %d 道，返回所有可用题目ID\n\n" +

                        "【输出】",
                requiredCount,
                requiredCount,
                difficultyInstruction,
                tagsStr,
                userDesc,
                candidateQue.size(),
                questionList,
                requiredCount
        );
    }

    // 更新任务状态（Redis）
    private void updateTaskStatus(String taskId, String status, int progress, String message) {
        updateTaskStatus(taskId, status, progress, message, null);
    }

    private void updateTaskStatus(String taskId, String status, int progress, String message, Long paperId) {
        try (Jedis jedis = JedisConnectionFactory.getJedis()) {
            String key = TASK_PREFIX + taskId;
            jedis.hset(key, "status", status);
            jedis.hset(key, "progress", String.valueOf(progress));
            jedis.hset(key, "message", message);
            jedis.hset(key, "updateTime", dateFormatter.format(new Date()));
            if (paperId != null) {
                jedis.hset(key, "paperId", String.valueOf(paperId));
            }
            jedis.expire(key, 3600); // 刷新过期时间
        }
    }

    // 查询任务进度（Redis）
    public Map<String, String> getGenerateProgress(String taskId) {
        try (Jedis jedis = JedisConnectionFactory.getJedis()) {
            String key = TASK_PREFIX + taskId;
            Map<String, String> hash = jedis.hgetAll(key);
            if (hash.isEmpty()) {
                hash.put("status", "not_found");
                hash.put("message", "任务不存在或已过期");
            }
            return hash;
        }
    }

    // 将候选题目转为紧凑格式（节省 token，提升模型注意力）
    private String buildQuestionContext(List<Questions> candidates, int maxCharsPerQuestion) {
        StringBuilder sb = new StringBuilder();
        for (Questions q : candidates) {
            String content = Optional.ofNullable(q.getTitle()).orElse("");
            // 截断题干，保留核心
            if (content.length() > maxCharsPerQuestion) {
                content = content.substring(0, maxCharsPerQuestion) + "...";
            }
            sb.append(String.format(
                    "【题目ID: %d】[难度: %s] %s\n",
                    q.getId(),
                    q.getDifficulty() != null ? q.getDifficulty() : "未知",
                    content.trim()
            ));
        }
        return sb.toString().trim();
    }

    public void exportQuestionTagsToCsv(String filePath) {

        Session session = this.getSession();
        QuestionTagsDAO tagDao = new QuestionTagsDAO();

        tagDao.setSession(session);

        try (FileWriter writer = new FileWriter(filePath)) {

            // 1. 写入CSV表头（根据 QuestionTags 表的字段来定）
            writer.write("tag_name\n");

            // 2. 直接查询所有标签
            List<QuestionTags> allTags = tagDao.queryAll();

            // 3. 遍历并写入数据
            for (QuestionTags tag : allTags) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(tag.getTagName()));

                line.append("\n");
                writer.write(line.toString());
            }

            System.out.println("QuestionTags 表导出完成，共导出 " + allTags.size() + " 条记录到: " + filePath);

        } catch (Exception e) {
            System.err.println("导出 QuestionTags 表失败: " + e.getMessage());
            e.printStackTrace(); // 建议打印堆栈，方便定位问题
            // 删除已创建的损坏文件
            new File(filePath).delete();
        } finally {
            // 记得关闭 session，如果你的框架不自动管理的话
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 导出题目和标签数据到合并的CSV文件
     */
    public void exportQuestionsWithTagsToCsv(String filePath) {

        Session session = this.getSession();
        QuestionDAO questionDao = new QuestionDAO();
        QuestionTagsDAO tagDao = new QuestionTagsDAO();
        QuestionTypeDAO typeDao = new QuestionTypeDAO();

        questionDao.setSession(session);
        tagDao.setSession(session);
        typeDao.setSession(session);

        try (FileWriter writer = new FileWriter(filePath)) {

            // 写入CSV表头
            writer.write("id,type_name,title,difficulty,tags\n");

            // 查询所有题目
            List<Questions> questions = questionDao.query(new QuestionQueryDTO());
            // 查询所有标签
            List<QuestionTags> allTags = tagDao.queryAll();

            // 查询所有题型映射
            List<QuestionType> types = typeDao.queryAll();
            Map<Integer, String> typeMap = types.stream()
                    .collect(Collectors.toMap(QuestionType::getTypeId, QuestionType::getTypeName));

            // 写入数据
            for (Questions q : questions) {

                StringBuilder line = new StringBuilder();
                line.append(q.getId()).append(",");

                String typeName = typeMap.getOrDefault(q.getTypeId(), String.valueOf(q.getTypeId()));
                line.append(escapeCsv(typeName)).append(",");
                line.append(escapeCsv(q.getTitle())).append(",");
                line.append(q.getDifficulty()).append(",");

                // 获取题目的标签
                List<QuestionTags> tags = tagDao.query(q.getId());
                List<String> tagNames = new ArrayList<>();

                for (QuestionTags tag : tags) {
                    if (tag.getTagName() != null && !tag.getTagName().trim().isEmpty()) {
                        tagNames.add(tag.getTagName());
                    }
                }

                // 将标签列表转换为JSON数组格式
                String tagsStr;
                if (tagNames.isEmpty()) {
                    tagsStr = "[]";
                } else {
                    tagsStr = "[" + tagNames.stream().map(tag -> "\"" + tag + "\"").collect(Collectors.joining(", ")) + "]";
                }

                line.append(escapeCsv(tagsStr));

                line.append("\n");
                writer.write(line.toString());
            }

            System.out.println("题目和标签合并导出完成，共导出 " + questions.size() + " 条记录到: " + filePath);

        } catch (Exception e) {
            System.err.println("导出题目和标签失败: " + e.getMessage());
            // 删除已创建的损坏文件
            new File(filePath).delete();
        }
    }

    /**
     * CSV字段转义，处理包含逗号、引号、换行符的情况
     */
    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }

        // 如果包含逗号、引号或换行符，需要用引号包围，并且内部的引号要转义
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }
}
