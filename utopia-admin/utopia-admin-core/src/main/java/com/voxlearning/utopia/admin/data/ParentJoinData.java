package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuerui.zhang
 * @since 2018/8/7 下午6:09
 */
@Data
public class ParentJoinData implements Serializable {

    private static final long serialVersionUID = 1985353175972564525L;

    private Long parentId;

    private String studyLessonId;

    private String createDate;

    private String wechatNumber;

    private String lessonName;
}
