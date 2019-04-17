package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/7/23
 * \* Time: 下午4:27
 * \* Description:生字认读报告
 * \
 */
@Getter
@Setter
public class WordRecognitionAndReadingTypePart implements Serializable {
    private static final long serialVersionUID = 3276199066568883956L;
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
    private List<WordRecognitionAndReadingTypePart.WordRecognitionType> tabs = new LinkedList<>();

    @Getter
    @Setter
    public static class WordRecognitionType implements Serializable {
        private static final long serialVersionUID = 4460974318473943995L;

        private String tabName;
        private List<TabObject> tabs = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class TabObject implements Serializable {
        private static final long serialVersionUID = 99966005799327326L;
        private String tabName = "";
        private String tabValue;
        private boolean finishCorrect = true;
        private String url;
        private String lessonId;
        private boolean showUrl = true;
        private String questionBoxId;
        private int standardNum;//达标数量
    }

}
