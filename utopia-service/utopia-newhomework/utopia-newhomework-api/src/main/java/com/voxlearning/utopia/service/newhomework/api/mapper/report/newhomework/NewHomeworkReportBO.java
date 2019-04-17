package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class NewHomeworkReportBO implements Serializable {
    private static final long serialVersionUID = -981391493334197480L;
    private boolean assignSimilarHomework;
    private boolean isOldHomework;
    private boolean showCorrect;
    private int totalNeedCorrectedNum;
    private int finishedCorrectedCount;
    private List<ObjectiveConfigType> objectiveConfigTypes;
    private boolean includeSubjective;
    private Subject subject;
    private boolean corrected;
    private int subjectId;
    private int userCount;
    private int joinCount;
    private int finishCount;
    private int unfinishCount;
    private int unDoCount;
    private int avgScore;
    private NewHomeworkType homeworkType;
    private int avgFinishTime;
    //typeReportList
    //studentReportList
    private Long clazzId;
    private String clazzName;
    private String groupId;
    private String createAt;
    private String currentDateTime;
    private List<Map<String, Object>> subjectTypeList;


}
