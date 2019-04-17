package com.voxlearning.utopia.agent.bean.indicator;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * OnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/2
 */
@Getter
@Setter
public class OnlineIndicator implements Serializable {

    private static final long serialVersionUID = 6967521598551978675L;

    private Integer stuScale;                    // 学生规模
    private Integer regStuCount;                 // 学生注册数
    private Integer auStuCount;                  // 认证学生数

    private Integer regSglSubjTeaCount;          // 老师注册数
    private Integer regEngTeaCount;              // 英语老师注册数
    private Integer regMathTeaCount;             // 数学老师注册数
    private Integer regChnTeaCount;              // 语文老师注册数

    private Integer auSglSubjTeaHwCount;         // 认证老师使用数
    private Integer auEngTeaHwCount;             // 认证英语老师使用数
    private Integer auMathTeaHwCount;            // 认证数学老师使用数
    private Integer auChnTeaHwCount;             // 认证语文老师使用数



    // 老师新增使用数
    private Integer hwSglSubjTeaCount;
    private Integer hwEngTeaCount;
    private Integer hwMathTeaCount;
    private Integer hwChnTeaCount;

    // 老师新增认证数
    private Integer auSglSubjTeaCount;
    private Integer auEngTeaCount;
    private Integer auMathTeaCount;
    private Integer auChnTeaCount;

    // 认证老师布置假期作业数
    private Integer assignVacnHwAuSglSubjTeaCount;
    private Integer assignVacnHwAuEngTeaCount;
    private Integer assignVacnHwAuMathTeaCount;
    private Integer assignVacnHwAuChnTeaCount;

    // 学生登录数
    private Integer loginStuCount;

    // 学生完成1套及以上作业数
    private Integer finSglSubjHwGte1StuCount;
    private Integer finEngHwGte1StuCount;
    private Integer finMathHwGte1StuCount;
    private Integer finChnHwGte1StuCount;

    // 学生完成3套及以上作业数
    private Integer finSglSubjHwGte3StuCount;
    private Integer finEngHwGte3StuCount;
    private Integer finMathHwGte3StuCount;
    private Integer finChnHwGte3StuCount;

    // 认证学生完成1套及以上作业数
    private Integer finSglSubjHwGte1AuStuCount;
    private Integer finEngHwGte1AuStuCount;
    private Integer finMathHwGte1AuStuCount;
    private Integer finChnHwGte1AuStuCount;

    // 认证学生完成3套及以上作业数
    private Integer finSglSubjHwGte3AuStuCount;
    private Integer finEngHwGte3AuStuCount;
    private Integer finMathHwGte3AuStuCount;
    private Integer finChnHwGte3AuStuCount;

    // 认证学生完成3套及以上作业数基数
    private Integer baseFinSglSubjHwGte3AuStuCount;
    private Integer baseFinEngHwGte3AuStuCount;
    private Integer baseFinMathHwGte3AuStuCount;
    private Integer baseFinChnHwGte3AuStuCount;


    // 未新增结算学生完成1套及以上作业数
    private Integer finSglSubjHwGte1UnSettleStuCount;
    private Integer finEngHwGte1UnSettleStuCount;
    private Integer finMathHwGte1UnSettleStuCount;
    private Integer finChnHwGte1UnSettleStuCount;


    // 未新增结算学生完成1套作业数
    private Integer finSglSubjHwEq1UnSettleStuCount;
    private Integer finEngHwEq1UnSettleStuCount;
    private Integer finMathHwEq1UnSettleStuCount;
    private Integer finChnHwEq1UnSettleStuCount;

    // 未新增结算学生完成2套作业数
    private Integer finSglSubjHwEq2UnSettleStuCount;
    private Integer finEngHwEq2UnSettleStuCount;
    private Integer finMathHwEq2UnSettleStuCount;
    private Integer finChnHwEq2UnSettleStuCount;

    // 未新增结算学生完成3套及以上作业数
    private Integer finSglSubjHwGte3UnSettleStuCount;
    private Integer finEngHwGte3UnSettleStuCount;
    private Integer finMathHwGte3UnSettleStuCount;
    private Integer finChnHwGte3UnSettleStuCount;

    // 学生新增结算数
    private Integer incSettlementSglSubjStuCount;
    private Integer incSettlementEngStuCount;
    private Integer incSettlementMathStuCount;
    private Integer incSettlementChnStuCount;

    // 已新增结算学生完成1套及以上作业数
    private Integer finSglSubjHwGte1SettleStuCount;
    private Integer finEngHwGte1SettleStuCount;
    private Integer finMathHwGte1SettleStuCount;
    private Integer finChnHwGte1SettleStuCount;

    // 已新增结算学生完成1套作业数
    private Integer finSglSubjHwEq1SettleStuCount;
    private Integer finEngHwEq1SettleStuCount;
    private Integer finMathHwEq1SettleStuCount;
    private Integer finChnHwEq1SettleStuCount;

    // 已新增结算学生完成2套作业数
    private Integer finSglSubjHwEq2SettleStuCount;
    private Integer finEngHwEq2SettleStuCount;
    private Integer finMathHwEq2SettleStuCount;
    private Integer finChnHwEq2SettleStuCount;

    // 已新增结算学生完成3套及以上作业数
    private Integer finSglSubjHwGte3SettleStuCount;
    private Integer finEngHwGte3SettleStuCount;
    private Integer finMathHwGte3SettleStuCount;
    private Integer finChnHwGte3SettleStuCount;

    // 回流结算学生
    private Integer returnSettleNumSglSubj;
    private Integer returnSettleNumEng;
    private Integer returnSettleNumMath;
    private Integer returnSettleNumChn;
    private Integer returnSettleNum;   //(老师和班组)





    // 渗透率
    private Double penetrateRateSglSubj;
    private Double penetrateRateEng;
    private Double penetrateRateMath;
    private Double penetrateRateChn;

    // 最大渗透率
    private Double maxPenetrateRateSglSubj;
    private Double maxPenetrateRateEng;
    private Double maxPenetrateRateMath;
    private Double maxPenetrateRateChn;

    // 最大布置作业套数
    private Integer maxHwSuitCount;
    private Integer maxEngHwSc;
    private Integer maxMathHwSc;
    private Integer maxChnHwSc;

    // 最小布置作业套数
    private Integer minHwSuitCount;

    // 是否布置期末复习
    private Boolean termReviewFlag;
    private Boolean termReviewEngFlag;
    private Boolean termReviewMathFlag;
    private Boolean termReviewChnFlag;

    // 是否布置寒/暑假作业
    private Boolean vacnHwFlag;
    private Boolean vacnEngHwFlag;
    private Boolean vacnMathHwFlag;
    private Boolean vacnChnHwFlag;

    //注册日期
    private Date registerTime;

    // 认证日期
    private Date authTime;

    // 认证状态
    private Integer authStaus;

    // 带班数量
    private Integer groupCount;

    // 上次布置作业日期
    private Date latestHwTime;

    // 布置所有作业套数
    private Integer tmHwSc;

    // 布置指定作业套数
    private Integer tmTgtHwSc;

    // 布置期末复习的班组数
    private Integer termReviewGroupCount;

    // 布置寒/暑假作业的班组数
    private Integer vacnHwGroupCount;

    //学生注册数（升学）
    private Integer promoteRegStuCount;

    // 新老师名下未新增结算学生完成3套及以上作业数
    private Integer newEngTeaHwGte3UnSettleStuCount;
    private Integer newMathTeaHwGte3UnSettleStuCount;
    private Integer newChnTeaHwGte3UnSettleStuCount;
    // 老老师名下未新增结算学生完成3套及以上作业数
    private Integer oldEngTeaHwGte3UnSettleStuCount;
    private Integer oldMathTeaHwGte3UnSettleStuCount;
    private Integer oldChnTeaHwGte3UnSettleStuCount;

}
