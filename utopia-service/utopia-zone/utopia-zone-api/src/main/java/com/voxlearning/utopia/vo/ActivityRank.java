package com.voxlearning.utopia.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 排行
 * @author chensn
 * @date 2018-11-10 16:55
 */
@Getter
@Setter
public class ActivityRank implements Serializable {

    private static final long serialVersionUID = -4808478366827610881L;
    private Long clazzId;
    private Long userId;
    private String clazzName;
    private String userName;
    private String pic;
    /**
     * 描述
     */
    private String discription;
    private String grade;
    private Integer num;
    private Integer index;
    private Boolean isVip;
    private Integer likeNum;
    private Boolean isLike;
}
