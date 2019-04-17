package com.voxlearning.utopia.agent.bean.performance.school;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 学校online指标数据
 *
 * @author deliang.che
 * @since  2018/8/6
 **/
@Setter
@Getter
public class SchoolOnlineIndicatorData {

    private Integer stuScale;       //学生规模
    private Integer regStuCount;    //累计注册学生数
    private Integer auStuCount;     //累计认证学生数
    private Integer currentMonthIncRegStuCount;//本月增加注册学生数
    private Integer currentMonthIncAuStuCount;//本月增加认证学生数

    private AgentSchoolPermeabilityType engPermeabilityType;    //英语渗透类型
    private AgentSchoolPermeabilityType mathPermeabilityType;   //数学渗透类型
    private AgentSchoolPermeabilityType chnPermeabilityType;    //语文渗透类型
    private AgentSchoolPermeabilityType sglSubjPermeabilityType;    //单科渗透类型

    private Map<String,SubjectIndicator> subjectIndicatorMap;//学科与学科指标对应关系
    @Setter
    @Getter
    public class SubjectIndicator {
        private String subjectName;                 //学科名称
        private Integer finHwGte3AuStuCount;        //本月月活
        private Integer lastSixMonthsMaxMauc;       //近六月月活峰值
        private Double rtRate1;                      // 1套留存率
        private Double rtRate2;                      // 3套留存率
        private Integer finHwGte3UnSettleStuCount;  //未新增结算学生完成3套及以上学生数（新增≥3）
        private Integer finHwEq1UnSettleStuCount;//未新增结算学生完成1套学生数（新增=1）
        private Integer finHwEq2UnSettleStuCount;//未新增结算学生完成2套学生数（新增=2）
        private Integer finHwGte3SettleStuCount;    //已新增结算学生完成3套及以上些学生数（回流≥3）
        private Integer finHwEq1SettleStuCount;  //已新增结算学生完成1套学生数（回流=1）
        private Integer finHwEq2SettleStuCount;  //已新增结算学生完成2套学生数（回流=2）
    }

}
