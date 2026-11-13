package unitTesting;

import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.service.SubjectService;
import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.*;

public class addSubjectWhiteBoxTest {

    @Spy private SubjectService subjectService = new SubjectService();

    @Mock private Session mockSession;  // 模拟 Session
    @Mock private Transaction mockTransaction; // 模拟事务

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);
    }

    /* =============== TC-01: 成功路径 =============== */
    @Test
    void addSubject_Success() {
        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 0\n" +
                "}", Subject.class);

        // 2. 执行
        boolean result = subjectService.addSubject(subject);

        // 3. 验证
        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockTransaction).commit();
        verify(mockTransaction, never()).rollback();
    }

    /* =============== TC-02: 年级无效 =============== */
    @Test
    void addSubject_InvalidGrade() {
        Gson gson = new Gson();
        // 1. 构造数据：修改 gradeLevel 为无效值
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 5,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 0\n" +
                "}", Subject.class);

        // 2. 执行
        boolean result = subjectService.addSubject(subject);

        // 3. 验证
        assertFalse(result);
        // 验证后续数据库操作未被执行
        verify(mockSession, never()).beginTransaction();
    }

    /* =============== TC-03: 名称为空 =============== */
    @Test
    void addSubject_EmptyName() {
        Gson gson = new Gson();
        // 1. 构造数据：修改 subjectName 为空字符串
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 0\n" +
                "}", Subject.class);

        // 2. 执行
        boolean result = subjectService.addSubject(subject);

        // 3. 验证
        assertFalse(result);
        verify(mockSession, never()).beginTransaction();
    }

    /* =============== TC-04: 状态无效 =============== */
    @Test
    void addSubject_InvalidStatus() {
        Gson gson = new Gson();
        // 1. 构造数据：修改 status 为无效值
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 2\n" +
                "}", Subject.class);

        // 2. 执行
        boolean result = subjectService.addSubject(subject);

        // 3. 验证
        assertFalse(result);
        verify(mockSession, never()).beginTransaction();
    }

    /* =============== TC-05: 状态为Null =============== */
    @Test
    @DisplayName("TC-05: 添加失败 - 状态为Null")
    void addSubject_NullStatus() {
        Gson gson = new Gson();
        // 1. 构造数据：修改 status 为 null
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": null\n" +
                "}", Subject.class);

        // 2. 执行
        boolean result = subjectService.addSubject(subject);

        // 3. 验证
        assertFalse(result);
        verify(mockSession, never()).beginTransaction();
    }

    /* =============== TC-06: 数据库异常 =============== */
    @Test
    @DisplayName("TC-06: 添加失败 - 数据库异常回滚")
    void addSubject_DbException() {
        Gson gson = new Gson();
        // 1. 构造有效数据
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 0\n" +
                "}", Subject.class);

        // 2. 模拟异常：假设 dao.add 内部调用了 session.save
        // 注意：如果被测代码中 dao.add 不依赖 session.save，需根据实际情况调整 Mock 点
        doThrow(new RuntimeException("DB Error")).when(mockSession).save(any(Subject.class));

        // 3. 执行
        boolean result = subjectService.addSubject(subject);

        // 4. 验证
        assertFalse(result);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
}
