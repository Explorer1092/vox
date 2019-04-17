package com.voxlearning.utopia.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by libin on 14-3-26.
 */
public class MobileHomeworkMapper implements Serializable {
    private static final long serialVersionUID = -4236983168662486528L;

    @Getter @Setter private String id;
    @Getter @Setter private String startTime;
    @Getter @Setter private String endTime;
    @Getter @Setter private String type;
    @Getter @Setter private String createDateTime;
    @Getter @Setter private String updateDateTime;
    @Getter @Setter private String homeworkName;
    @Getter @Setter private Integer finishNum;
    @Getter @Setter private Integer totalCount;
    @Getter @Setter private List<MobilePracticeMapper> taskList;


}
