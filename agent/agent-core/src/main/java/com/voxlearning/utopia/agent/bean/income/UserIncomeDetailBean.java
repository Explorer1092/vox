package com.voxlearning.utopia.agent.bean.income;

import com.voxlearning.utopia.agent.persist.entity.AgentUserKpiResult;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Alex on 15-3-12.
 */
@Data
public class UserIncomeDetailBean implements Serializable {

    private String source;       // 收入内容
    private Double cashIncome;   // 现金收入
    private Double pointIncome;  // 点数收入
    private String extInfo;      // 附加信息

}
