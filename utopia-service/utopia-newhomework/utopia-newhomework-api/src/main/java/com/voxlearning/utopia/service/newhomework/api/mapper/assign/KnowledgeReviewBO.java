package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangbin
 * @since 2017/1/12 10:40
 */
@Setter
@Getter
public class KnowledgeReviewBO implements Serializable {
    private static final long serialVersionUID = -9195400262847155537L;
    private String sectionId;   //课时id
    private String sectionName; //课时名称
    private List<KnowledgePointQuestionBO> knowledgePointQuestionBOList;
}
