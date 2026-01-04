package cn.edu.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddExamAnswerDTO {
    private Map<Long, Object> answers;
    private Long examId;
}
