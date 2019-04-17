package com.voxlearning.utopia.service.reward.api.mapper;

import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class CacheCollectMapper implements java.io.Serializable {
    private static final long serialVersionUID = 979850759327966662L;

    private Long userId;
    private String collectId;
    private Long styleId;
    private Long activityId;
    private Long money;
    private List<String> enableCode; // 点亮的物件 code
    private Integer type; // 0 老师 1 学生
    private Boolean done; // 是否建设完成
    private Date updateTime; // 最后更新时间


    // 不存,只展示
    @DocumentFieldIgnore
    private String userName;
    @DocumentFieldIgnore
    private String imgUrl;
    @DocumentFieldIgnore
    private Boolean liked;
}
