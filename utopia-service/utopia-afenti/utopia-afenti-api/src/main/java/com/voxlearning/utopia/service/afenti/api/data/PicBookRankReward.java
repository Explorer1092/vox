package com.voxlearning.utopia.service.afenti.api.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2018/4/3
 */
@Getter
@Setter
public class PicBookRankReward implements Serializable {

    private static final long serialVersionUID = -1459241424362901872L;

    private Boolean show; // 是否显示弹窗
    private Boolean inRank;
    private Integer totalRewardScore;
    private String readRank;
    private String readRankSchool;
    private String wordRank;
    private String wordRankSchool;

    private String callName;
    private String content;
}
