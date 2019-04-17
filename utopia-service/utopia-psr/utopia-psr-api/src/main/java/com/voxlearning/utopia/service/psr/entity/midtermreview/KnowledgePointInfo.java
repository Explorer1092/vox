package com.voxlearning.utopia.service.psr.entity.midtermreview;

import com.voxlearning.alps.annotation.dao.DocumentField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/10/10.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class KnowledgePointInfo implements Serializable{
    private static final long serialVersionUID = 4591268903536053008L;

    @DocumentField("kp_id")
    private String knowledgePointId;
    @DocumentField("kp_type")
    private String knowledgePointType;
    @DocumentField("right_rate_infos")
    private List<QuestionInfo> rightRateInfos;
}
