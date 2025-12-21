package cn.edu.zjut.backend.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "question_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 30)
    private String typeCode;

    @Column(name = "type_name", nullable = false, unique = true, length = 30)
    private String typeName;
}
