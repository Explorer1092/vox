package com.voxlearning.utopia.agent.bean.productfeedback;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 产品反馈
 * Created by yaguang.wang on 2017/2/21.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductFeedbackListInfo implements Serializable {
    private static final long serialVersionUID = 1894176902053007965L;

    private Integer totalFeedbackCount;         // 累计反馈的条数
    private Long tmFeedbackCount;            // 本月反馈的条数
    private Boolean isManager;                  // 是否是管理者
    private List<ProductFeedbackInfo> productFeedbackInfos;     // 此产品反馈页内的数据
}
