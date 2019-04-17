package com.voxlearning.utopia.service.newhomework.api.mapper.request;

import com.voxlearning.utopia.service.newhomework.api.mapper.request.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Auther: majianxin
 * @Date: 2018/5/2
 * @Description: 查看干预信息
 */
@Getter
@Setter
public class ViewHintReq extends BaseReq {

    private static final long serialVersionUID = 2095999930085548071L;

    private String verb;                    // 行为viewHint, pickHint
    private Integer hintId;                 // 提示ID--> ImmediateInterventionType
    private String homeworkId;              //作业ID
    private String questionId;              //题目ID
    private String objectiveConfigType;     //作业类型
    private String hintTag;                 //提示来源标签：string：举例，qc-label-note；qc-label-vote
    private Date timestamp;                 //时间戳(开始时间)
    private Long duration;                  //持续时间
    private Boolean result;                 //Answer：答案是否正确/ pickHint：是否选择“是”

}
