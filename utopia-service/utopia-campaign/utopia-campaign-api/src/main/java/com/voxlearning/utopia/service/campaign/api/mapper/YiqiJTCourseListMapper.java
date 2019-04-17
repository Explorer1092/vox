package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 一起新讲堂课程
 */
@Getter
@Setter
@ToString
public class YiqiJTCourseListMapper implements Serializable {
    private Long id;
    private String title;
    private String titlePictureUrl;
    private String lecturerUserName;
    private String lecturerIntroduction;
    private String attendNum;
}
