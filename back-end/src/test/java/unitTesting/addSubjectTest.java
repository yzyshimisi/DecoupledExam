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

public class addSubjectTest {

    @Spy private SubjectService subjectService = new SubjectService();

    @Mock private Session mockSession;  // 模拟 Session
    @Mock private Transaction mockTransaction; // 模拟事务

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    /* ---------------- 成功路径 ---------------- */
    @Test
    void addSuccess() throws Exception {
        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"C++ 编程\",\n" +
                "    \"subjectCode\": \"CP101\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 0\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockSession).save(subject);
        verify(mockTransaction).commit();
    }

    /* ---------------- 必填字段为空 ---------------- */
    @Test
    void emptyParam() throws Exception {
        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"\",\n" +
                "    \"subjectCode\": \"HIS\",\n" +
                "    \"gradeLevel\": 1,\n" +
                "    \"sortOrder\": 1,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertFalse(result);
    }

    /* ---------------- gradeLevel 非法 ---------------- */
    @Test
    void illegalGradeLevel() throws Exception {
        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"历史\",\n" +
                "    \"subjectCode\": \"HIS\",\n" +
                "    \"gradeLevel\": 0,\n" +
                "    \"sortOrder\": 1,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertFalse(result);
    }

    /* ---------------- status 非法 ---------------- */
    @Test
    void illegalStatus() throws Exception {
        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"体育\",\n" +
                "    \"subjectCode\": \"PE\",\n" +
                "    \"gradeLevel\": 1,\n" +
                "    \"sortOrder\": 1,\n" +
                "    \"status\": 2\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertFalse(result);
    }
}
