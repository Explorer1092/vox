package com.voxlearning.utopia.service.newhomework.api.entity.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class NewHomeworkBookInfo implements Serializable {

    private static final long serialVersionUID = -7342061915001280030L;

    // 这里的id用String，在2016-03以后的时候，内容库重构教材时，所有id统一会变成String预留
    private String bookId;
    private String bookName;
    private String unitId;
    private String unitName;
    private String unitGroupId;
    private String unitGroupName;
    private String lessonId;
    private String lessonName;
    private String sectionId;
    private String sectionName;
    private String objectiveId;
    private String objectiveName;

    private List<String> questions;
    private List<String> papers;
    private List<String> pictureBooks;
    private List<String> videos;
    private List<String> questionBoxIds;
    private List<String> dubbingIds;
    private List<String> appIds;
    private List<String> stoneIds;
}
