package cn.edu.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionQueryDTO {
    // 仅显示我的题目
    private boolean mineOnly;
    // 题干关键词
    private String stemKeyword;
    // 选择的题型 ID 数组
    private List<Integer> selectedTypes;
    // 选择的学科 ID 数组
    private List<Integer> selectedSubjects;
    // 最大难度（≤）
    private Integer maxDifficulty;
    // 选择的审核状态数组（0: 待审核, 1: 已通过, 2: 已拒绝）
    private List<Byte> selectedStatuses;
    // 出题人查询（ID 或姓名）
    private Long authorQuery;

    private Integer pageNum;
    private Integer pageSize;
}
