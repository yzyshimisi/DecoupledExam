package cn.edu.zjut.backend.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "question_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
}
