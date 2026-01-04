package cn.edu.zjut.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectQueryDTO {
    private Integer subjectId;
    private String subjectName;
    private String subjectCode;
    private List<Byte> gradeLevels;
    private Byte status;

    private Integer pageNum;
    private Integer pageSize;
}
