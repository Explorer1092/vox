package com.voxlearning.utopia.service.newhomework.api.mapper.readrecite;

import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/6/1 15:33
 */
@Setter
@Getter
public class ReadReciteBO implements Serializable {
    private static final long serialVersionUID = 1248428758973298995L;
    private String questionBoxId;
    private String questionBoxName;
    private QuestionBoxType questionBoxType;
    private String lessonName;
    private List<String> questionIds;
    private Integer questionNum;
    private List<Map<String, Object>> questions;

}
