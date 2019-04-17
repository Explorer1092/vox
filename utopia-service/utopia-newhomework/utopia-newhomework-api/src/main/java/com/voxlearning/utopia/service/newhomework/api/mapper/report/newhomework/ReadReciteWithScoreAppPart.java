package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReadReciteWithScoreAppPart implements Serializable {
    private static final long serialVersionUID = 7712430328316475719L;
    private QuestionBoxType questionBoxType;
    private String lessonName = "";
    private String questionBoxTypeName;
    private int standardUserCount;
    private int userCount;
    private List<ReadReciteWithScoreAppPartUser> users = new LinkedList<>();
    @Getter
    @Setter
    public static class ReadReciteWithScoreAppPartUser implements Serializable {

        private static final long serialVersionUID = -1425537482172973296L;
        private Long userId;
        private String userName = "";
        private boolean standard;
        private List<String> voices = new LinkedList<>();
        private String duration;
    }
}
