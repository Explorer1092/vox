package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xinxin
 * @since 17/8/2016
 */
@Getter
@Setter
public class StudentRemindMessageMapper implements Serializable {
    private static final long serialVersionUID = -138492799961408565L;

    private String id;
    private String userName;   // 用户姓名
    private String userImg;    // 用户头像
    private String headWear;   // 用户头饰
    private String type;       // 类型
    private Long dateTime;     // 消息生成时间
    private String content;    // 消息原生内容
    private String icon;       // 小图标

    // 成就榜点赞、成就榜头条会需要以下字段
    private String achievementType;
    private String achievementTitle;
    private Integer achievementLevel;

}
