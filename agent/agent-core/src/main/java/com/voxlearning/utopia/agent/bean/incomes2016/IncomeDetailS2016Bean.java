package com.voxlearning.utopia.agent.bean.incomes2016;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 2016春季用户收入详情
 * Created by alex on 2016/3/29.
 */
@Data
public class IncomeDetailS2016Bean implements Serializable {

    private String source;       // 收入内容
    private Long income;         // 现金收入
    private String extInfo;      // 附加信息
    private Date startTime;      // 开始时间
    private Date endTime;        // 结束时间

}
