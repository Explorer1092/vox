package com.voxlearning.utopia.service.psr.entity.midtermreview;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/10.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class QuestionInfo implements Serializable{
    private static final long serialVersionUID = -5334774811705651500L;

    @DocumentField("all_num")
    private Integer allNum;
    @DocumentField("right_rate")
    private Double rightRate;
    @DocumentField("right_num")
    private Integer rightNum;
    @DocumentField("doc_id")
    private String docId;
}
