package com.voxlearning.utopia.agent.bean.productfeedback;

import com.voxlearning.utopia.service.crm.api.bean.ApplyProcessResult;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 单条产品反馈信息
 * Created by yaguang.wang on 2017/2/21.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductFeedbackInfo implements Serializable {
    private static final long serialVersionUID = 7048897361881048074L;
    private Long id;
    private String teacherName;
    private Long teacherId;
    private String fbContent;       // 反馈内容
    private Date createTime;        // 反馈的创建时间
    private AgentProductFeedbackType feedbackType;
//    private String marketName;      // 销运姓名
//    private String marketOpinion;   // 销运意见
//    private String pmName;          // 产品的姓名
//    private String pmOpinion;       // 产品意见
    private Boolean onlineFlag;     // 已上线
    private AgentProductFeedbackStatus feedbackStatus;      // 反馈状态
    private String statusDesc;       // 状态详情
    private String onlineDate;         // 预计上线时间

    private List<ApplyProcessResult> processResultList; // 申请的处理结果
}
