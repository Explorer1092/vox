package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ReadReciteAppInfo implements Serializable {
    private static final long serialVersionUID = 6825202032420729353L;
    private String lessonName = "";
    private QuestionBoxType questionBoxType;
    private String questionBoxTypeName;
    private String lessonId;
    private String questionBoxId;
    private int standardNum;//达标数量
}
