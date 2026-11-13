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

public class addSubjectBoundaryTest {

    @Spy private SubjectService subjectService = new SubjectService();
    @Mock private Session mockSession;  // 模拟 Session
    @Mock private Transaction mockTransaction; // 模拟事务

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    /* =====================================================================
                   1. 下界：1 -> 小学
   ===================================================================== */
    @Test
    void gradeLevelLowerBoundary_1() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"语文\",\n" +
                "    \"subjectCode\": \"CHN\",\n" +
                "    \"gradeLevel\": 1,\n" +
                "    \"sortOrder\": 1,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockTransaction).commit();
    }

    /* =====================================================================
                   2. 中间值：2 -> 初中
   ===================================================================== */
    @Test
    void gradeLevelMiddle_2() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"数学\",\n" +
                "    \"subjectCode\": \"MATH\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 2,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockTransaction).commit();
    }

    /* =====================================================================
                   3. 局部上界：3 -> 高中
   ===================================================================== */
    @Test
    void gradeLevelMiddle_3() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"英语\",\n" +
                "    \"subjectCode\": \"ENG\",\n" +
                "    \"gradeLevel\": 3,\n" +
                "    \"sortOrder\": 3,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockTransaction).commit();
    }

    /* =====================================================================
                   4. 上界：9 -> 通用
   ===================================================================== */
    @Test
    void gradeLevelUpperBoundary_9() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"技术\",\n" +
                "    \"subjectCode\": \"TEC\",\n" +
                "    \"gradeLevel\": 9,\n" +
                "    \"sortOrder\": 4,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertTrue(result);
        verify(mockSession).beginTransaction();
        verify(mockTransaction).commit();
    }

    /* =====================================================================
               5. 越界：4（应该返回false）
    ===================================================================== */
    @Test
    void gradeLevelAboveUpperBoundary_4() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"测试\",\n" +
                "    \"subjectCode\": \"TEST\",\n" +
                "    \"gradeLevel\": 4,\n" +
                "    \"sortOrder\": 5,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertFalse(result);
    }

    /* =====================================================================
               6. 必填参数为空
    ===================================================================== */
    @Test
    void emptyRequiredParam() throws Exception {

        Gson gson = new Gson();
        Subject subject = gson.fromJson("{\n" +
                "    \"subjectName\": \"\",\n" +
                "    \"subjectCode\": \"YYY\",\n" +
                "    \"gradeLevel\": 2,\n" +
                "    \"sortOrder\": 6,\n" +
                "    \"status\": 1\n" +
                "}", Subject.class);

        doReturn(mockSession).when(subjectService).getSession();
        when(mockSession.beginTransaction()).thenReturn(mockTransaction);

        boolean result = subjectService.addSubject(subject);

        assertFalse(result);
    }
}
