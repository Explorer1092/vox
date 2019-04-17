package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生课外阅读打卡动态缓存
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"readingId", "missionId", "studentId"})
@RequiredArgsConstructor
public class OutsideReadingDynamicCacheMapper implements Serializable {

    private static final long serialVersionUID = -2124003226335392073L;

    private String readingId;        //阅读任务ID

    private Long studentId;
    private String studentName;
    private String studentImage;

    private String bookId;           //图书ID
    private String bookName;         //图书名
    private String coverPic;         //封面
    private String author;           //作者

    private String missionId;        //关卡ID
    private String missionName;      //关卡名

    private double addReadingCount; //增加的阅读字数
    private Date finishAt;           //打卡时间
}
