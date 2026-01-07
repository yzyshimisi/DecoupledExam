package cn.edu.zjut.backend.dto;

import cn.edu.zjut.backend.po.ExamWrongBook;
import cn.edu.zjut.backend.po.Questions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamWrongBookDTO {
    private ExamWrongBook examWrongBook;
    private Questions questions;
}
