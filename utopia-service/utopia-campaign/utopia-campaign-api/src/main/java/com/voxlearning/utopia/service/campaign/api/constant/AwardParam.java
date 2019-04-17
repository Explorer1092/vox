package com.voxlearning.utopia.service.campaign.api.constant;

import lombok.Data;

import java.io.Serializable;

/**
 * 奖项参数
 *
 * @Author: peng.zhang
 * @Date: 2018/10/23
 */
@Data
public class AwardParam implements Serializable {
    private String id;
    private String fileUrl;
    private String fileName;
    private String awardLevelName;
    private Integer awardLevelId;
    private String awardIntroduction;
}
