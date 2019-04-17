package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

@Data
public class MoralMedalTeacherClazz implements java.io.Serializable {

    private Long clazzId;
    private String clazzName;

    private Long groupId;
    private String groupSubject;
    private String groupSubjectName;

    private Long studentSize;
    private String createTime;

}
