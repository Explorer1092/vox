package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeachingDianosisPreQuestionExtraRequest implements Serializable {

    private static final long serialVersionUID = 8388236104563275811L;
    private Long createTime;           //创建时间
    private String experimentId;       //实验id -- 前测题会有
    private String experimentGroupId;  //实验组id-- 前测题会有
}
