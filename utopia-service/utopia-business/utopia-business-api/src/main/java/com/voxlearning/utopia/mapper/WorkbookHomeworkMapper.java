package com.voxlearning.utopia.mapper;

import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkState;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by tanguohong on 2015/8/31.
 */
@Data
public class WorkbookHomeworkMapper implements Serializable{

    private static final long serialVersionUID = 2855266365785412317L;

    private Long clazzId;
    private String clazzName;
    private String clazzLevel;
    private int studentCount;
    private boolean pastdue;

    private Long workbookHomeworkId;
    private String workbookHomeworkName;
    private HomeworkState state;
    private String startDate;
    private String endDate;
    private long normalFinishTime;
    private int practiceCount;

    private int finishCount;
    private int unfinishCount;
    private int finishPercent;

    private boolean canAssign;
    private String message;

    private Long groupId;
    private String groupName;



    public static WorkbookHomeworkMapper bulidNeonatalMapper() {
        WorkbookHomeworkMapper mapper = new WorkbookHomeworkMapper();
        mapper.setState(HomeworkState.ASSIGN_HOMEWORK);
        mapper.setFinishCount(0);
        mapper.setUnfinishCount(0);
        mapper.setCanAssign(true);
        mapper.setPastdue(false);
        return mapper;
    }
}
