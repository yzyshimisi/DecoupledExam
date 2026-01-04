package cn.edu.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperQueryDTO implements Serializable {

    // ====== 查询条件 ======

    /**
     * 试卷名称（模糊匹配）
     */
    private String paperName;

    /**
     * 课程ID（精确匹配）
     */
    private Long courseId;

    /**
     * 组卷类型：1-自动组卷，2-手动组卷（或其他业务定义）
     */
    private String composeType;

    /**
     * 是否密封：0-未密封，1-已密封
     */
    private String isSealed;

    /**
     * 创建人ID
     */
    private Long creatorId;

    // ====== 分页参数 ======

    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    // ===== 工具方法（可选） =====
}
