package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ReadReciteTypePart implements Serializable {
    private static final long serialVersionUID = 5128290928171380802L;
    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl;
    private String url;
    private int tapType = 5;
    private boolean hasFinishUser;
    private int unCorrect;
    private boolean showCorrectUrl;
    private String correctUrl;
    private String subContent;
    private List<ReadReciteType> tabs = new LinkedList<>();

    @Getter
    @Setter
    public static class ReadReciteType implements Serializable {
        private static final long serialVersionUID = -8295181474756442825L;
        private String tabName;
        private QuestionBoxType questionBoxType;
        private List<TabObject> tabs = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class TabObject implements Serializable {
        private static final long serialVersionUID = 7841762631650719804L;
        private String tabName = "";
        private QuestionBoxType questionBoxType;
        private String tabValue;
        private boolean finishCorrect = true;
        private String url;
        private String lessonId;
        private boolean showUrl = true;
        private String questionBoxId;
        private int standardNum;//达标数量
    }
}
