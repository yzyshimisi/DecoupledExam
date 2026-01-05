package cn.edu.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvigilationResDTO {
    private boolean isViolation = false;
    private Integer attentionScore = 0;
    private String message = "";
}
