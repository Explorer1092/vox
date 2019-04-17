package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class NewReadReciteAppPart implements Serializable {
    private static final long serialVersionUID = 5949657810989330918L;
    private List<ParagraphDetailed> paragraphDetaileds = new LinkedList<>();
    private List<NewReadReciteUser> users = new LinkedList<>();
    private QuestionBoxType questionBoxType;
    private String paragraphName;

    @Getter
    @Setter
    public static class NewReadReciteUser implements Serializable {

        private static final long serialVersionUID = -1425537482172973296L;
        private Long userId;
        private String userName = "";
        private String duration;
        private List<String> showPics = new LinkedList<>();
        private boolean review;
        private String correction;
        private String correct_des;
    }

    /**
     * 每一个段落的信息
     */
    @Getter
    @Setter
    public static class ParagraphDetailed implements Serializable {
        private static final long serialVersionUID = -4158127465980577957L;
        private String paragraphOrder;//段落次序:第一段
        private String questionId;
        private boolean paragraphDifficultyType;//段落困难描述： 重点段落
    }

}
