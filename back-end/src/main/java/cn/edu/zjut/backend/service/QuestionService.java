package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.QuestionDAO;
import cn.edu.zjut.backend.dao.QuestionTagsDAO;
import cn.edu.zjut.backend.dao.SubjectDAO;
import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.dto.SubjectQueryDTO;
import cn.edu.zjut.backend.po.*;
import cn.edu.zjut.backend.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.*;
import org.hibernate.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("questionServ")
public class QuestionService {

    private final Gson gson = new Gson();
    private static final Map<String, String> QUESTION_JSON = new HashMap<String, String>() {{
        // 单选题
        put("单选题", "{\n" +
                "  \"typeId\": 1, \n" +
                "  \"title\": \"以下关于Java中ArrayList的描述，错误的是？\",\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"option\",\n" +
                "      \"content\": \"{\\\"options\\\":[{\\\"label\\\":\\\"A\\\",\\\"value\\\":\\\"ArrayList底层基于数组实现\\\",\\\"isCorrect\\\":false},{\\\"label\\\":\\\"B\\\",\\\"value\\\":\\\"ArrayList扩容时默认扩容为原容量的1.5倍\\\",\\\"isCorrect\\\":false},{\\\"label\\\":\\\"C\\\",\\\"value\\\":\\\"ArrayList支持快速的插入/删除操作\\\",\\\"isCorrect\\\":true},{\\\"label\\\":\\\"D\\\",\\\"value\\\":\\\"ArrayList是非线程安全的\\\",\\\"isCorrect\\\":false}],\\\"allowMultiple\\\":false}\",\n" +
                "      \"meta\": \"{\\\"questionType\\\":\\\"singleChoice\\\",\\\"totalOptions\\\":4}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"correctOption\\\":\\\"C\\\",\\\"answerDesc\\\":\\\"ArrayList基于数组实现，插入/删除需移动元素，效率低；LinkedList更适合频繁增删场景。\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\", \n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. ArrayList随机访问效率O(1)，增删O(n)；2. 默认初始容量10，扩容1.5倍；3. 非线程安全，多线程需用CopyOnWriteArrayList。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 多选题
        put("多选题", "{\n" +
                "  \"typeId\": 2,\n" +
                "  \"title\": \"以下属于Java集合框架中线程安全的类有哪些？\",\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"option\",\n" +
                "      \"content\": \"{\\\"options\\\":[{\\\"label\\\":\\\"A\\\",\\\"value\\\":\\\"Vector\\\",\\\"isCorrect\\\":true},{\\\"label\\\":\\\"B\\\",\\\"value\\\":\\\"ArrayList\\\",\\\"isCorrect\\\":false},{\\\"label\\\":\\\"C\\\",\\\"value\\\":\\\"Hashtable\\\",\\\"isCorrect\\\":true},{\\\"label\\\":\\\"D\\\",\\\"value\\\":\\\"ConcurrentHashMap\\\",\\\"isCorrect\\\":true},{\\\"label\\\":\\\"E\\\",\\\"value\\\":\\\"HashMap\\\",\\\"isCorrect\\\":false}],\\\"allowMultiple\\\":true}\",\n" +
                "      \"meta\": \"{\\\"questionType\\\":\\\"multipleChoice\\\",\\\"totalOptions\\\":5}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"correctOption\\\":\\\"A,C,D\\\",\\\"answerDesc\\\":\\\"Vector、Hashtable是传统线程安全类，ConcurrentHashMap是JUC包下高效线程安全实现；ArrayList、HashMap非线程安全。\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. Vector/Hashtable通过synchronized实现线程安全，性能较低；2. ConcurrentHashMap采用分段锁/CAS机制，性能更高；3. 日常开发中优先使用ConcurrentHashMap而非Hashtable。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 填空题
        put("填空题", "{\n" +
                "  \"typeId\": 4,\n" +
                "  \"title\": \"Java中声明抽象方法的关键字是______，定义接口的关键字是______。\",\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"blank\",\n" +
                "      \"content\": \"{\\\"blanks\\\":[{\\\"label\\\":\\\"1\\\",\\\"placeholder\\\":\\\"______\\\",\\\"position\\\":\\\"第1空\\\",\\\"answer\\\":\\\"abstract\\\",\\\"sort\\\":1},{\\\"label\\\":\\\"2\\\",\\\"placeholder\\\":\\\"______\\\",\\\"position\\\":\\\"第2空\\\",\\\"answer\\\":\\\"interface\\\",\\\"sort\\\":2}]}\",\n" +
                "      \"meta\": \"{\\\"totalScore\\\":4}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"answers\\\":[{\\\"blankLabel\\\":\\\"1\\\",\\\"correctAnswer\\\":\\\"abstract\\\"},{\\\"blankLabel\\\":\\\"2\\\",\\\"correctAnswer\\\":\\\"interface\\\"}],\\\"tip\\\":\\\"注意关键字拼写，区分大小写\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. abstract关键字用于声明抽象方法/抽象类，抽象方法无方法体；2. interface关键字用于定义接口，接口中方法默认是public abstract，变量默认是public static final。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 判断题
        put("判断题", "{\n" +
                "  \"typeId\": 3,\n" +
                "  \"title\": \"Java中的String类是不可变的。\",\n" +
                "  \"difficulty\": 1,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"correctResult\\\":true,\\\"answerDesc\\\":\\\"String类被final修饰，其内部字符数组也被final修饰，一旦创建无法修改，因此是不可变类。\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. String不可变的核心原因是底层char[]数组被private final修饰，且无对外修改的方法；2. 看似修改String的操作（如substring、concat）实际是创建新的String对象；3. 如需可变字符串，可使用StringBuffer或StringBuilder。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 名词解析
        put("简答题", "{\n" +
                "  \"typeId\": 5,\n" +
                "  \"title\": \"请解析Java中的多态性\",\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"completeAnswer\\\":\\\"多态性分为编译时多态（方法重载）和运行时多态（方法重写）：1. 编译时多态：编译器根据参数列表不同确定调用的方法；2. 运行时多态：JVM在运行时根据实际对象类型确定调用的方法，需满足继承、重写、父类引用指向子类对象三个条件。\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"多态性的优势在于提高代码的扩展性和复用性，降低耦合度；典型应用场景包括抽象类、接口的使用，以及工厂模式、策略模式等设计模式的实现。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 论述题
        put("论述题", "{\n" +
                "  \"typeId\": 6,\n" +
                "  \"title\": \"论述Java中异常处理机制的设计思想及实际应用场景\",\n" +
                "  \"difficulty\": 4,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"completeAnswer\\\":\\\"1. 设计思想：Java异常处理基于‘异常即对象’的核心思想，将错误处理逻辑与核心业务逻辑解耦，通过抛出-捕获的异常处理流程，脱离传统线性执行逻辑，避免大量if-else错误判断代码，提升程序可读性；2. 异常体系：分为Checked Exception（受检异常，如IOException，强制捕获处理）和Unchecked Exception（非受检异常，如NullPointerException，可选择性处理）；3. 核心应用场景：① IO操作中捕获IOException，保证流资源正常关闭；② 数据库操作中捕获SQLException，处理连接失败/查询异常；③ 自定义业务异常，标识参数非法、权限不足等业务错误；4. 最佳实践：避免捕获Exception父类、按需使用finally释放资源、合理设计自定义异常体系。\\\"}\",\n" +
                "      \"meta\": \"{\\\"score\\\":10}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"异常处理的核心价值是提升程序容错性和可维护性，但滥用会导致问题排查困难（如捕获异常后仅打印日志不处理）；实际开发中需平衡健壮性与开发效率：受检异常适用于可预见的外部错误（如文件不存在），非受检异常适用于程序内部逻辑错误（如空指针）；自定义异常需遵循‘精准标识错误类型’原则，便于定位问题。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 计算题
        put("计算题", "{\n" +
                "  \"typeId\": 7,\n" +
                "  \"title\": \"已知Java程序中int a = 10, b = 5; 计算a++ + ++b + a * b的结果，并写出详细解题步骤\",\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"finalAnswer\\\":71,\\\"steps\\\":[\\\"1. 初始值：a=10，b=5;\\\",\\\"2. 计算a++：先取值10参与运算，运算后a自增为11;\\\",\\\"3. 计算++b：先自增b为6，再取值6参与运算;\\\",\\\"4. 计算a*b：此时a=11，b=6，结果为11*6=66;\\\",\\\"5. 汇总运算：10 + 6 + 66 = 71;\\\"]}\",\n" +
                "      \"meta\": \"{\\\"score\\\":8,\\\"stepScore\\\":[2,2,2,2]}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. 核心考点：自增运算符（++）的前置/后置区别——后置++先取值后自增，前置++先自增后取值；2. 运算优先级：自增运算符优先级高于算术运算符，乘法优先级高于加法；3. 易错点：容易忽略a++运算后a的值已更新，导致后续a*b计算错误。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 分录题
        put("分录题", "{\n" +
                "  \"typeId\": 8,\n" +
                "  \"title\": \"企业从银行取得期限为6个月的短期借款200000元，款项已存入银行账户\",\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 5,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"entryList\\\":[{\\\"debitAccount\\\":\\\"银行存款\\\",\\\"debitAmount\\\":200000.00,\\\"creditAccount\\\":\\\"短期借款\\\",\\\"creditAmount\\\":200000.00}],\\\"entryDesc\\\":\\\"该业务导致企业资产（银行存款）增加，负债（短期借款）等额增加，符合借贷记账法‘有借必有贷，借贷必相等’的记账规则。\\\"}\",\n" +
                "      \"meta\": \"{\\\"score\\\":10,\\\"debitScore\\\":5,\\\"creditScore\\\":5}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. 业务性质：属于筹资活动中的短期借款业务；2. 账户性质：银行存款属于资产类账户，借方记增加；短期借款属于负债类账户，贷方记增加；3. 易错点：容易混淆‘短期借款’与‘长期借款’的账户归属，区分关键在于借款期限是否超过1年。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 连线题
        put("连线题", "{\n" +
                "  \"typeId\": 9,\n" +
                "  \"title\": \"将下列Java集合类与其对应的底层实现结构进行连线匹配\",\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"match\",\n" +
                "      \"content\": \"{\\\"leftList\\\":[{\\\"id\\\":\\\"L1\\\",\\\"content\\\":\\\"ArrayList\\\"},{\\\"id\\\":\\\"L2\\\",\\\"content\\\":\\\"LinkedList\\\"},{\\\"id\\\":\\\"L3\\\",\\\"content\\\":\\\"HashMap\\\"}],\\\"rightList\\\":[{\\\"id\\\":\\\"R1\\\",\\\"content\\\":\\\"数组+链表/红黑树\\\"},{\\\"id\\\":\\\"R2\\\",\\\"content\\\":\\\"动态数组\\\"},{\\\"id\\\":\\\"R3\\\",\\\"content\\\":\\\"双向链表\\\"}]}\",\n" +
                "      \"meta\": \"{\\\"renderType\\\":\\\"vertical\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"matchPairs\\\":[{\\\"leftId\\\":\\\"L1\\\",\\\"rightId\\\":\\\"R2\\\"},{\\\"leftId\\\":\\\"L2\\\",\\\"rightId\\\":\\\"R3\\\"},{\\\"leftId\\\":\\\"L3\\\",\\\"rightId\\\":\\\"R1\\\"}],\\\"ruleDesc\\\":\\\"左侧集合类与右侧底层实现结构一一对应，每个匹配项唯一且不重复\\\"}\",\n" +
                "      \"meta\": \"{\\\"score\\\":6,\\\"singleScore\\\":2}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"1. ArrayList底层基于动态数组，LinkedList底层基于双向链表，HashMap JDK1.8后底层为数组+链表+红黑树。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 排序题
        put("排序题", "{\n" +
                "  \"typeId\": 10,\n" +
                "  \"title\": \"请将下列Java代码执行流程按正确顺序排列：定义主类Main, 编写main方法作为程序入口, 声明并初始化变量, 调用方法执行业务逻辑, 编译代码生成class文件, 运行class文件输出结果\",\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 4,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"sort\",\n" +
                "      \"content\": \"{\\\"itemList\\\":[{\\\"id\\\":\\\"S1\\\",\\\"content\\\":\\\"定义主类Main\\\"},{\\\"id\\\":\\\"S2\\\",\\\"content\\\":\\\"编写main方法作为程序入口\\\"},{\\\"id\\\":\\\"S3\\\",\\\"content\\\":\\\"声明并初始化变量\\\"},{\\\"id\\\":\\\"S4\\\",\\\"content\\\":\\\"调用方法执行业务逻辑\\\"},{\\\"id\\\":\\\"S5\\\",\\\"content\\\":\\\"编译代码生成class文件\\\"},{\\\"id\\\":\\\"S6\\\",\\\"content\\\":\\\"运行class文件输出结果\\\"}]}\",\n" +
                "      \"meta\": \"{\\\"sortType\\\":\\\"sequence\\\",\\\"tip\\\":\\\"按代码开发到运行的流程排序\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"correctOrder\\\":[\\\"S1\\\",\\\"S2\\\",\\\"S3\\\",\\\"S4\\\",\\\"S5\\\",\\\"S6\\\"],\\\"orderDesc\\\":\\\"遵循‘定义类→编写入口→实现逻辑→编译→运行’的Java程序执行规范\\\"}\",\n" +
                "      \"meta\": \"{\\\"score\\\":10,\\\"singleItemScore\\\":1.67}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysisDesc\\\":\\\"核心考点为Java程序的开发与执行流程，注意区分‘编译’与‘运行’的顺序。\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 完形填空
        put("完形填空", "{\n" +
                "  \"typeId\": 11,\n" +
                "  \"title\": \"English is one of the most widely used languages in the world. It __1__ by nearly every country. If you want to travel abroad, you will find __2__ important to master English. It is also a bridge between different cultures. With English, we can communicate with people from all over the world __3__.\",\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 2,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionItems\": [\n" +
                "    {\n" +
                "      \"sequence\": 1,\n" +
                "      \"content\": \"\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"options\\\":[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"speaks\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"is spoken\\\"},{\\\"key\\\":\\\"C\\\",\\\"value\\\":\\\"spoke\\\"},{\\\"key\\\":\\\"D\\\",\\\"value\\\":\\\"is speaking\\\"}]}\",\n" +
                "          \"meta\": \"{\\\"optionType\\\":\\\"singleChoice\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"correctKey\\\":\\\"B\\\",\\\"answerDesc\\\":\\\"本题考查一般现在时的被动语态\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"analysisDesc\\\":\\\"主语It指代English，与speak是逻辑上的被动关系，因此需用被动语态；一般现在时的被动语态结构为am/is/are + 过去分词，speak的过去分词为spoken，故答案为B。\\\"}\",\n" +
                "          \"meta\": \"{}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 2,\n" +
                "      \"content\": \"\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"options\\\":[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"it\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"this\\\"},{\\\"key\\\":\\\"C\\\",\\\"value\\\":\\\"that\\\"},{\\\"key\\\":\\\"D\\\",\\\"value\\\":\\\"its\\\"}]}\",\n" +
                "          \"meta\": \"{\\\"optionType\\\":\\\"singleChoice\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"correctKey\\\":\\\"A\\\",\\\"answerDesc\\\":\\\"本题考查it作形式宾语的用法\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"analysisDesc\\\":\\\"固定句型find it + adj. + to do sth.中，it作形式宾语，真正的宾语是后面的不定式短语to master English，this/that/its均无法充当形式宾语，故答案为A。\\\"}\",\n" +
                "          \"meta\": \"{}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 3,\n" +
                "      \"content\": \"\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"options\\\":[{\\\"key\\\":\\\"A\\\",\\\"value\\\":\\\"easy\\\"},{\\\"key\\\":\\\"B\\\",\\\"value\\\":\\\"easier\\\"},{\\\"key\\\":\\\"C\\\",\\\"value\\\":\\\"easily\\\"},{\\\"key\\\":\\\"D\\\",\\\"value\\\":\\\"easiest\\\"}]}\",\n" +
                "          \"meta\": \"{\\\"optionType\\\":\\\"singleChoice\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"correctKey\\\":\\\"C\\\",\\\"answerDesc\\\":\\\"本题考查副词修饰动词的用法\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"analysisDesc\\\":\\\"空格处需修饰动词communicate，修饰动词需用副词；easy是形容词，easier是比较级，easiest是最高级，easily是副词形式，故答案为C。\\\"}\",\n" +
                "          \"meta\": \"{}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 阅读理解
        put("阅读理解", "{\n" +
                "  \"title\": \"A new study published in Nature shows that artificial intelligence (AI) has gradually changed people's daily life. From smart home devices to personalized medical care, AI is making life more convenient. However, experts warn that over-reliance on AI may reduce people's independent thinking ability. The study surveyed 10,000 people across 5 countries and found that 78% of respondents use AI tools at least once a day, while only 32% still keep the habit of manual problem-solving.\",\n" +
                "  \"typeId\": 12,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 1,\n" +
                "  \"questionItems\": [\n" +
                "    {\n" +
                "      \"sequence\": 1,\n" +
                "      \"content\": \"What is the main topic of the passage?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. The history of AI development<br/>B. The influence of AI on daily life<br/>C. The disadvantages of AI technology<br/>D. The global survey of AI usage\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C|D\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"B\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"The passage mainly discusses how AI changes people's daily life, including convenience and potential problems. Option A is not mentioned, Option C is only a small part, Option D is just a supporting detail.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"主旨理解需抓住全文核心，而非局部信息\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 2,\n" +
                "      \"content\": \"According to the survey, what percentage of people use AI tools daily?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. 32%<br/>B. 78%<br/>C. 50%<br/>D. 100%\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C|D\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"B\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"The passage clearly states: '78% of respondents use AI tools at least once a day', so the answer is B. Option A refers to the percentage of people who keep manual problem-solving habits.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"细节题需精准定位原文数据\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 3,\n" +
                "      \"content\": \"What do experts warn about AI?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. AI will replace human jobs completely<br/>B. Over-reliance on AI may reduce independent thinking<br/>C. AI is not suitable for medical care<br/>D. AI tools are too expensive for daily use\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C|D\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"B\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"Experts' warning is mentioned in the sentence: 'over-reliance on AI may reduce people's independent thinking ability'. Option A is an extreme statement not supported by the passage, Option C and D are not mentioned.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"推理题需基于原文，避免过度推断\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"global_analysis\",\n" +
                "      \"content\": \"{\\\"text\\\":\\\"This passage is a typical expository text about AI's influence. It combines factual data and expert opinions, testing students' ability to understand main ideas, locate details, and make reasonable inferences. Key vocabulary: artificial intelligence, independent thinking, respondent.\\\"}\",\n" +
                "      \"meta\": \"{\\\"totalScore\\\":6, \\\"difficulty\\\":\\\"medium-hard\\\", \\\"coreVocab\\\":\\\"artificial intelligence, independent thinking\\\"}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 听力题
        put("听力题", "{\n" +
                "  \"title\": \"请听对话，完成下列题目\",\n" +
                "  \"typeId\": 13,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"difficulty\": 3,\n" +
                "  \"subjectId\": 1,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"audio\",\n" +
                "      \"content\": \"{\\\"url\\\":\\\"null\\\", \\\"duration\\\":\\\"1分45秒\\\", \\\"size\\\":\\\"2.5MB\\\"}\",\n" +
                "      \"meta\": \"{\\\"audioFormat\\\":\\\"mp3\\\", \\\"bitRate\\\":\\\"128kbps\\\", \\\"source\\\":\\\"高考真题改编\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"listening_text\",\n" +
                "      \"content\": \"{\\\"text\\\":\\\"W: Good morning, sir. Can I help you?\\\\nM: Yes, I'd like to book a train ticket to Beijing tomorrow.\\\\nW: What time do you want to leave? We have trains at 8:00, 10:30 and 14:15.\\\\nM: The 10:30 one, please.\\\\nW: Single or return ticket?\\\\nM: Single, and a first-class seat if possible.\\\\nW: Let me check... Yes, there are first-class seats available for the 10:30 train.\\\\nM: Great, how much is it?\\\\nW: 380 yuan.\\\\nM: Here's the money.\\\\nW: Thank you. Your ticket is ready. Have a nice trip!\\\"}\",\n" +
                "      \"meta\": \"{\\\"language\\\":\\\"English\\\", \\\"level\\\":\\\"medium\\\"}\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"questionItems\": [\n" +
                "    {\n" +
                "      \"sequence\": 1,\n" +
                "      \"content\": \"What does the man want to do?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. Book a plane ticket<br/>B. Book a train ticket<br/>C. Book a bus ticket\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"B\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"The man clearly says 'I'd like to book a train ticket to Beijing tomorrow', so the answer is B. Option A (plane) and C (bus) are not mentioned in the conversation.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"捕捉核心动作'book a train ticket'\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 2,\n" +
                "      \"content\": \"Which train does the man choose?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. The 8:00 one<br/>B. The 10:30 one<br/>C. The 14:15 one\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"B\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"When the woman asks about the departure time, the man replies 'The 10:30 one, please', which directly corresponds to option B.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"定位时间类关键信息\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"sequence\": 3,\n" +
                "      \"content\": \"How much is the first-class ticket?\",\n" +
                "      \"typeId\": 1,\n" +
                "      \"questionComponents\": [\n" +
                "        {\n" +
                "          \"componentType\": \"option\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A. 380 yuan<br/>B. 480 yuan<br/>C. 580 yuan\\\"}\",\n" +
                "          \"meta\": \"{\\\"score\\\":2, \\\"optionLabels\\\":\\\"A|B|C\\\"}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"answer\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"A\\\"}\",\n" +
                "          \"meta\": \"{\\\"correctness\\\":true}\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"componentType\": \"analysis\",\n" +
                "          \"content\": \"{\\\"text\\\":\\\"The woman tells the man '380 yuan' when he asks about the price of the first-class ticket, so the answer is A.\\\"}\",\n" +
                "          \"meta\": \"{\\\"keyPoint\\\":\\\"精准抓取数字类信息\\\"}\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 程序题
        put("程序题", "{\n" +
                "  \"title\": \"实现冒泡排序算法，对整数数组进行升序排序。\",\n" +
                "  \"typeId\": 14,\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 1,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"code\\\": \\\"public static void bubbleSort(int[] arr) {\\\\n    int n = arr.length;\\\\n    for (int i = 0; i < n - 1; i++) {\\\\n        for (int j = 0; j < n - i - 1; j++) {\\\\n            if (arr[j] > arr[j + 1]) {\\\\n                int temp = arr[j];\\\\n                arr[j] = arr[j + 1];\\\\n                arr[j + 1] = temp;\\\\n            }\\\\n        }\\\\n    }\\\\n}\\\", \\\"language\\\": \\\"java\\\"}\",\n" +
                "      \"meta\": \"{\\\"timeComplexity\\\": \\\"O(n²)\\\", \\\"spaceComplexity\\\": \\\"O(1)\\\"}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysis\\\": \\\"冒泡排序通过重复遍历数组，比较相邻元素并交换位置来实现排序。\\\"}\",\n" +
                "      \"meta\": \"{\\\"keyPoints\\\": [\\\"嵌套循环\\\", \\\"相邻元素比较\\\", \\\"原地排序\\\"]}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 口语题
        put("口语题", "{\n" +
                "  \"title\": \"请用英语介绍一下你的家乡，包括它的地理位置、特色美食和一处著名的旅游景点。\",\n" +
                "  \"typeId\": 15,\n" +
                "  \"difficulty\": 2,\n" +
                "  \"subjectId\": 1,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"answer\",\n" +
                "      \"content\": \"{\\\"keywords\\\": [\\\"hometown\\\", \\\"location\\\", \\\"specialty food\\\", \\\"famous landmark\\\", \\\"culture\\\"], \\\"sampleAnswer\\\": \\\"My hometown is Beijing, the capital of China, located in the northern part of the country. It's famous for its delicious Peking Duck, which is a must-try for visitors. One of the most renowned landmarks is the Forbidden City, a magnificent palace complex that showcases ancient Chinese architecture and history.\\\"}\",\n" +
                "      \"meta\": \"{\\\"expectedDuration\\\": \\\"90-120 seconds\\\", \\\"scoringCriteria\\\": [\\\"Fluency\\\", \\\"Pronunciation\\\", \\\"Grammar accuracy\\\", \\\"Content completeness\\\"]}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysis\\\": \\\"这道题考察学生的英语口语表达能力和组织信息的能力。\\\"}\",\n" +
                "      \"meta\": \"{\\\"tips\\\": [\\\"Use simple and clear language\\\", \\\"Structure your answer\\\"]}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        // 投票题
        put("投票题", "{\n" +
                "  \"title\": \"你认为在Java项目中，哪个版本引入的特性最具革命性？\",\n" +
                "  \"typeId\": 17,\n" +
                "  \"difficulty\": 1,\n" +
                "  \"subjectId\": 1,\n" +
                "  \"creatorId\": 2,\n" +
                "  \"questionComponents\": [\n" +
                "    {\n" +
                "      \"componentType\": \"option\",\n" +
                "      \"content\": \"{\\\"options\\\": [{\\\"key\\\": \\\"A\\\", \\\"value\\\": \\\"Java 5 (泛型、注解、自动装箱)\\\"}, {\\\"key\\\": \\\"B\\\", \\\"value\\\": \\\"Java 8 (Lambda表达式、Stream API)\\\"}, {\\\"key\\\": \\\"C\\\", \\\"value\\\": \\\"Java 11 (新的HTTP客户端、局部变量类型推断)\\\"}, {\\\"key\\\": \\\"D\\\", \\\"value\\\": \\\"Java 17 (密封类、模式匹配预览)\\\"}], \\\"allowMultiple\\\": false}\",\n" +
                "      \"meta\": \"{\\\"minSelections\\\": 1, \\\"maxSelections\\\": 1}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"componentType\": \"analysis\",\n" +
                "      \"content\": \"{\\\"analysis\\\": \\\"这是一个开放性的投票问题，旨在了解大家对Java发展历程的看法。\\\"}\",\n" +
                "      \"meta\": \"{\\\"discussionPoints\\\": [\\\"LTS版本的重要性\\\", \\\"向后兼容性\\\"]}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}");
    }};

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    // 添加题目
    public boolean addQuestion(Questions question){

        // 生成题目标签
        Gson gson = new Gson();
        String questionStr = gson.toJson(question);

        String questionTagsJson = generateTags(questionStr);
        List<QuestionTags> questionTags = gson.fromJson(questionTagsJson, new TypeToken<List<QuestionTags>>(){}.getType());

        // 手动建立双向引用
        bindQuestionRelations(question);
        question.setCreatorId(UserContext.getUserId());     // 设置创建者ID

        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        QuestionTagsDAO questionTagsDAO = new QuestionTagsDAO();
        dao.setSession(session);
        questionTagsDAO.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();

            dao.add(question);  // 插入题目
            // 插入标签
            for(QuestionTags questionTag : questionTags){
                questionTag.setQuestionId(question.getId());
                questionTagsDAO.add(questionTag);
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

    // 批量导入（列表）
    public boolean importQuestion(List<Questions> questions){

        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        QuestionTagsDAO questionTagsDAO = new QuestionTagsDAO();
        dao.setSession(session);
        questionTagsDAO.setSession(session);
        Transaction tran = null;

        try {
            tran = session.beginTransaction();

            for(Questions question : questions){
                // 生成题目标签
                Gson gson = new Gson();
                String questionStr = gson.toJson(question);

                String questionTagsJson = generateTags(questionStr);
                System.out.println(questionTagsJson);
                List<QuestionTags> questionTags = gson.fromJson(questionTagsJson, new TypeToken<List<QuestionTags>>(){}.getType());

                // 手动建立双向引用
                bindQuestionRelations(question);
                question.setCreatorId(UserContext.getUserId());     // 设置创建者ID

                dao.add(question);  // 插入题目

                // 插入标签
                for(QuestionTags questionTag : questionTags){
                    questionTag.setQuestionId(question.getId());
                    questionTagsDAO.add(questionTag);
                }
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

    // 批量导入题目（Docx文档）
    public boolean fileImportQuestionsAsync(String fileContent, String taskId){

        updateProgress(taskId, 0, "开始解析文档..."); // 记录进度

        Session session = this.getSession();
        Transaction tran = null;

        try{
            tran = session.beginTransaction();

            XWPFDocument document = convertBase64ToDocument(fileContent);
//            System.out.println(document);

            // 题干与图片
            QuestionList questionList = extractQuestions(document);
            List<String> qs = questionList.getQuestions();
            List<List<String>> qis = questionList.getQuestionImages();

            int total = qs.size();
            System.out.println("共有（识别到）：" + total + "道题目");

            updateProgress(taskId, 10, "共发现 " + total + " 道题目");

            for (int i = 0; i < total; i++) {
                String q = qs.get(i);
                List<String> qi = qis.get(i);

                int firstSpaceIndex = q.indexOf('\n');
                String qMark = q.substring(0, firstSpaceIndex);     // 题目标识
                String qContent = q.substring(firstSpaceIndex + 1);   // 题目内容
                String qSubject = qMark.split("-")[0];     // 题目学科
                String qType = qMark.split("-")[1];         // 题目类型
                qType = qType.equals("完形填空") || qType.equals("阅读理解") ? qType : qType + "题";
                System.out.println(qSubject +" " + qType);

//                Thread.sleep(10000);

//                String askQuestion = "题目+答案+解析：\n“" + qContent + "”\n\n" + "JSON模板：\n" + QUESTION_JSON.get(qType) + "\n\n把题目按照JSON模板的格式，生成对应的JSON格式数据，有如下需要注意的点：\n1.typeId和creatorId可以直接照搬，其他的内容不要照搬\n2.给的模板中没有的字段不要自己添加上去，严格跟着格式的字段来（像option、answer、analysis、blank等，模板JSON中如果只有option和answer，就不要添加analysis和blank等不存在的字段）\n3.不要有重复的componentType，比如不要有两个answer或两个blank，都可以整合到一个questionComponent中）\n4.title可以包含全一点，感觉之前一些文字都没包含进去，比如“请从经济、文化、社会三个角度，论述科技创新对国家发展的战略意义。（字数不少于300字）”就会包含不进前面的那三个角度和后面的括号提示；还有“请编制相应的会计分录。”这样的题目描述，当然前提是不能把选项也包含进去\n5.解析不用太多，稍微介绍以下就好了\n7.如果是程序题，就将答案，整个程序编码为Base64放在questionComponent的answer里面，防止JSON格式错误\n6.直接给我完整的JSON数据，不要任何多余的东西，也不要```json这样的代码框，就直接一段JSON格式的字符串";
//                System.out.println(askQuestion);
//                System.out.println();
//                String res = ChatGLM.inquire(askQuestion);
//                res = removeJsonCodeBlockMarkers(res);
//                System.out.println(res);
//
//                Questions question = gson.fromJson(res, Questions.class);

                int progress = 10 + (int) (80.0 * (i + 1) / total);
                updateProgress(taskId, progress, "正在处理第 " + (i + 1) + "/" + total + " 题");

                // 创建模板、生成数据、插入数据库

                if(!generateQuestionWithRetry(qContent, qType, qSubject, qi)) {
                    throw new Exception();
                }
//                Thread.sleep(2000);
            }

            tran.commit();

            updateProgress(taskId, 100, "导入成功！", "completed");

            return true;
        }catch (Exception e){
            if (tran != null) {
                tran.rollback();
            }
            updateProgress(taskId, -1, "失败: " + e.getMessage(), "failed");
            e.printStackTrace();
            return false;
        }
    }

    // 查询题目
    public List<Questions> queryQuestion(QuestionQueryDTO filterDTO){
        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        List<Questions> questions = dao.query(filterDTO);

        HibernateUtil.closeSession();
        return questions;
    }

    public Questions queryQuestion(Long questionId){
        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        Questions questions = dao.query(questionId);

        HibernateUtil.closeSession();
        return questions;
    }

    // 删除题目，支持批量删除
    public boolean deleteQuestion(List<Long> ids){
        Session session=this.getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for(Long id:ids){
                Questions questions = dao.query(id);
                if(Objects.equals(questions.getCreatorId(), UserContext.getUserId())){  // 只允许删除自己创建的题目
                    dao.delete(questions);
                }
            }
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("delete customer failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 修改题目
    public boolean updateQuestion(Questions question){
        question.setUpdatedAt(new Date());
        question.setCreatorId(UserContext.getUserId());

        bindQuestionRelations(question);

        Session session=this.getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
//            if(Objects.equals(question.getCreatorId(), UserContext.getUserId())){
//                dao.update(question);
//            }
            dao.update(question);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("update customer failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 手动建立题目（Questions、QuestionItems、QuestionComponents）之间的双向连接
    public static void bindQuestionRelations(Questions question){
        if (question.getQuestionComponents() != null) {
            for (QuestionComponents comp : question.getQuestionComponents()) {
                comp.setQuestion(question); // ← 建立反向引用
            }
        }
        if (question.getQuestionItems() != null) {
            for (QuestionItems item : question.getQuestionItems()) {
                item.setQuestion(question);

                if(item.getQuestionComponents() != null){
                    for (QuestionComponents comp : item.getQuestionComponents()) {
                        comp.setItem(item);
                    }
                }
            }
        }
    }

    // 生成题目标签
    private String generateTags(String questionJson){
        String prompt = questionJson + "我要往数据库中插入上面这道题目，帮忙给这个题目打几个标签，有如下注意的点：\n" +
                "1. 不需要科目、题型等标签，比较多余，最好更多是知识点、重点、关联点等\n" +
                "2. 按以下格式生成一个JSON格式的数组（即使只有一个，也要放在数组里）\n" +
                "3. 给出一个JSON数据即可，不需要其他任何内容，也不要有```json代码框\n" +
                "\n" +
                "[\n" +
                "  {\n" +
                "    \"questionId\": 4,\n" +
                "    \"tagName\": \"多态\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": 4,\n" +
                "    \"tagName\": \"继承\"\n" +
                "  }\n" +
                "]";

        return ChatGLM_N.inquire(prompt, false);
    }

    // 更新任务进度到Redis数据库

    private void updateProgress(String taskId, int progress, String message) {
        updateProgress(taskId, progress, message, "processing");
    }

    private void updateProgress(String taskId, int progress, String message, String status) {
        String key = "task:" + taskId;
        Jedis jedis = null;
        try {
            jedis = JedisConnectionFactory.getJedis();
            jedis.hset(key, "progress", String.valueOf(progress));
            jedis.hset(key, "message", message);
            jedis.hset(key, "status", status);
            jedis.expire(key, 3600 * 24); // 24小时过期
        } catch (Exception e) {
            // 建议记录日志（可用你项目的 Log 工具）
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close(); // 归还连接到池
            }
        }
    }

    // 将Base64文件解码为XWPFDocument对象
    private XWPFDocument convertBase64ToDocument(String base64Content) throws IOException {
        // 解码 Base64
        byte[] fileBytes = Base64.getDecoder().decode(base64Content);

        // 创建输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

        // 转换为 XWPFDocument
        return new XWPFDocument(inputStream);
    }


    // 从XWPFDocument对象中提取题目（文字图片）
    public QuestionList extractQuestions(XWPFDocument document) {
        List<String> questions = new ArrayList<>();
        List<List<String>> questionImages = new ArrayList<>();

        StringBuilder currentQuestion = null;
        List<String> currentQuestionImage = new ArrayList<>();

        // 匹配题目标识的正则表达式（不限制题型）
        Pattern questionPattern = Pattern.compile("^[^-]+-[^-]+-\\d+$");

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText().trim();
            List<String> imagesPath = extractImagesFromParagraph(paragraph);
            currentQuestionImage.addAll(imagesPath);

            if (text.isEmpty()) continue;

            // 检查是否是题目标识（加粗且匹配格式）
            boolean isBold = paragraph.getRuns().stream().anyMatch(XWPFRun::isBold);

            if (isBold && questionPattern.matcher(text).find()) {
                // 保存上一题（如果有）
                if (currentQuestion != null) {
                    questions.add(currentQuestion.toString().trim());
                    questionImages.add(currentQuestionImage);
                    currentQuestionImage = new ArrayList<>();
                }
                // 开始新题目
                currentQuestion = new StringBuilder(text);
            } else if (currentQuestion != null) {
                // 继续添加题目内容
                currentQuestion.append("\n").append(text);
            }
        }

        // 保存最后一题
        if (currentQuestion != null) {
            questions.add(currentQuestion.toString().trim());
            questionImages.add(currentQuestionImage);
        }

        return new QuestionList(questions, questionImages);
    }

    // 从段落中提取图片（保存图片到本地，并返回链接）
    private List<String> extractImagesFromParagraph(XWPFParagraph paragraph) {

        List<String> imagePaths = new ArrayList<>();

        String imageDir = "C:\\Users\\31986\\Desktop\\images";
        File dir = new File(imageDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (XWPFRun run : paragraph.getRuns()) {
            List<XWPFPicture> pictures = run.getEmbeddedPictures();
            for (XWPFPicture picture : pictures) {
                try {
                    // 获取图片数据
                    XWPFPictureData pictureData = picture.getPictureData();
                    byte[] imageData = pictureData.getData();

                    // 生成图片文件名（可以用时间戳或UUID）
                    String fileName = UUID.randomUUID().toString() + "." + pictureData.suggestFileExtension();
                    String imagePath = imageDir + File.separator + fileName;

                    // 保存图片到文件
                    saveImageToFile(imageData, imagePath);
                    imagePaths.add(imagePath);
                } catch (Exception e) {
                    System.err.println("提取图片时出错: " + e.getMessage());
                }
            }
        }

        return imagePaths;
    }

    // 保存图片到文件
    private void saveImageToFile(byte[] imageData, String imagePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageData);
            fos.flush();
        }
    }

    private static String removeJsonCodeBlockMarkers(String input) {
        Pattern JSON_CODE_BLOCK_PATTERN = Pattern.compile("^```json\\s*(.*?)\\s*```$", Pattern.DOTALL);
        if (input == null || input.isBlank()) {
            return input;
        }
        Matcher matcher = JSON_CODE_BLOCK_PATTERN.matcher(input);
        // 匹配成功则提取中间内容，否则返回原字符串
        return matcher.find() ? matcher.group(1) : input;
    }


    // 生成JSON格式的请求，如果失败了进行重试
    private boolean generateQuestionWithRetry(String qContent, String qType, String qSubject, List<String> qi) {
        int maxRetries = 3;
        int retryCount = 0;
        Session innerSession = null;
        innerSession = this.getSession();
        QuestionDAO qdao = new QuestionDAO();
        SubjectDAO sdao = new SubjectDAO();
        QuestionTagsDAO qtdao = new QuestionTagsDAO();
        qdao.setSession(innerSession);
        sdao.setSession(innerSession);
        qtdao.setSession(innerSession);

        while(retryCount < maxRetries) {
            innerSession.clear();
            try {

                String askQuestion = buildPrompt(qContent, qType, retryCount);
                System.out.println(askQuestion);
                String res = ChatGLM_N.inquire(askQuestion, false);
                res = removeJsonCodeBlockMarkers(res);

                // 预处理：尝试修复常见的JSON格式问题
                res = preprocessJson(res);
//                System.out.println(res);

                Questions question = gson.fromJson(res, Questions.class);
//                System.out.println(question);

                // 生成题目标签
                String questionTagsJson = generateTags(res);
                List<QuestionTags> questionTags = gson.fromJson(questionTagsJson, new TypeToken<List<QuestionTags>>(){}.getType());

                // 验证必要字段
                if(validateQuestion(question)) {
                    // 赋值学科ID
                    Subject subject = null;
                    try{
                        SubjectQueryDTO subjectQueryDTO = new SubjectQueryDTO();
                        subjectQueryDTO.setSubjectName(qSubject);
                        subject = sdao.query(subjectQueryDTO).get(0);
                    }catch (Exception e){
                        System.err.println(e.getMessage());
                    }

                    if(subject==null){
                        question.setSubjectId(null);
                    }else{
                        question.setSubjectId(subject.getSubjectId());
                    }

                    if(!qi.isEmpty()) {
                        // 添加题干图片
                        QuestionComponents component = new QuestionComponents();
                        component.setComponentType("image");
                        // 构建content JSON字符串
                        String content = "{\"images\":"+ gson.toJson(qi)  +"}";
                        component.setContent(content);

                        question.getQuestionComponents().add(component);
                    }

                    // 手动建立双向连接
                    bindQuestionRelations(question);
                    question.setCreatorId(UserContext.getUserId());
                    qdao.add(question);
                    for(QuestionTags questionTag : questionTags) {
                        questionTag.setQuestionId(question.getId());
                        qtdao.add(questionTag);
                    }
                    return true;
                } else {
                    retryCount++;
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    System.out.println("题目验证失败，重试第 " + retryCount + " 次");
                }

            } catch (Exception e) {
                retryCount++;
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
                System.out.println("JSON解析失败，重试第 " + retryCount + " 次：" + e.getMessage());
            }
        }

        System.out.println("达到最大重试次数，生成失败");
        return false;
    }

    private String buildPrompt(String qContent, String qType, int retryCount) {
        String basePrompt = "题目+答案+解析：\n“" + qContent + "”\n\n" + "JSON模板：\n" + QUESTION_JSON.get(qType) + "\n\n把题目按照JSON模板的格式，生成对应的JSON格式数据，有如下需要注意的点：\n1.typeId和creatorId可以直接照搬JSON模板的，其他的内容不要照搬\n2.给的JSON模板中没有的元素不要自己添加上去，严格跟着模板的字段来（questionComponents中content字符串要是数据库能解析的JSON格式，花括号开始，花括号结束）\n3.不要有重复的componentType，比如不要有两个answer或两个blank，都可以整合到一个questionComponent中）\n4.title可以包含全一点，感觉之前一些文字都没包含进去，比如“请从经济、文化、社会三个角度，论述科技创新对国家发展的战略意义。（字数不少于300字）”就会包含不进前面的那三个角度和后面的括号提示；还有“请编制相应的会计分录。”这样的题目描述，当然前提是不能把选项也包含进去\n5.解析不用太多，稍微介绍以下就好了\n6.直接给我完整的JSON数据，不要任何多余的东西，也不要```json这样的代码框，就直接一段JSON格式的字符串";

        if (retryCount > 0) {
            basePrompt += "\n\n注意：之前生成的JSON格式有误，请确保：\n" +
                    "- 所有字符串都正确转义\n" +
                    "- 没有多余的逗号\n" +
                    "- 中括号与花括号括号都正确匹配\n" +
                    "- 严格按照模板结构\n" +
                    "- QuestionComponent的Content字符串要是严格的JSON格式";
        }

        return basePrompt;
    }

    private String preprocessJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return jsonStr;
        }
        return jsonStr
                // 移除对象/数组末尾的多余逗号（兼容换行，非贪婪匹配）
                .replaceAll(",\\s*([}\\]])", "$1")
                // 移除JSON中的注释（// 单行注释）
                .replaceAll("//.*\\n", "")
                // 移除JSON中的注释（/* 多行注释 */）
                .replaceAll("/\\*[\\s\\S]*?\\*/", "")
                // 单引号替换为双引号（简单场景，避免值里的单引号被误换）
                .replaceAll("(['\"])?([a-zA-Z0-9_]+)\\1\\s*:", "\"$2\":")
                // 修复未转义的双引号（值内的裸引号，简单兜底）
                .replaceAll("(?<!\\\\)\"([^:\"]*?)(?<!\\\\)\"\\s*:", "\"$1\":");
    }


    private boolean validateQuestion(Questions question) {
        if(question == null) return false;
        if(question.getTitle() == null || question.getTitle().trim().isEmpty()) return false;
        if((question.getQuestionComponents() == null || question.getQuestionComponents().isEmpty()) && (question.getQuestionItems()==null || question.getQuestionItems().isEmpty())) return false;

        // 可以根据需要添加更多验证规则
        return true;
    }

    @Data
    public static class QuestionList{
        private List<String> questions;
        private List<List<String>> questionImages;

        public QuestionList() {}

        public QuestionList(List<String> questions, List<List<String>> questionImages) {
            this.questions = questions;
            this.questionImages = questionImages;
        }
    }
}
