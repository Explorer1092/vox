package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/15.
 * 教研员语言知识页面中单行mapper
 */
@Data
public class ResearchStaffKnowledgeUnitMapper implements Serializable {

    private static final long serialVersionUID = 1072675077172668032L;

    private String name;

    private ResearchStaffKnowledgeDetailMapper wordDetail;
    private ResearchStaffKnowledgeDetailMapper topicDetail;
    private ResearchStaffKnowledgeDetailMapper gramDetail;

    private long studentCount;
}
