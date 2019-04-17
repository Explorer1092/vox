package com.voxlearning.utopia.agent.bean;


import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentPerformanceGoal;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Jia HuanYin
 * @since 2016/2/19
 */
@Getter
@Setter
@UtopiaCacheRevision("20170830")
public class PerformanceData implements Serializable {
    private static final long serialVersionUID = -2694847851658314605L;

    public static final DecimalFormat df = new DecimalFormat("######0");

    public static final String ID_TYPE_SCHOOL = "SCHOOL";
    public static final String ID_TYPE_GROUP = "GROUP";
    public static final String ID_TYPE_GROUP_OTHER = "OTHER_SCHOOL";
    public static final String ID_TYPE_USER = "USER";
    public static final String ID_TYPE_GROUP_REGION = "GROUP_REGION";
    public static final String ID_TYPE_CITY = "CITY";

    @Deprecated public static final int VIEW_TYPE_JUNIOR_ENG = 1;  // 小学英语
    @Deprecated public static final int VIEW_TYPE_JUNIOR_MATH = 2;  // 小学数学
    @Deprecated public static final int VIEW_TYPE_MIDDLE_ENG = 3;  // 中学英语
    @Deprecated public static final int VIEW_TYPE_MIDDLE_MATH = 4;  // 中学数学
    @Deprecated public static final int VIEW_TYPE_HIGH_MATH = 5;  // 高中数学
    @Deprecated public static final int VIEW_TYPE_JUNIOR_CHN = 6;  // 小学语文
    @Deprecated public static final int VIEW_TYPE_JUNIOR_SGL_SUBJ = 7;  // 小学

    /// code 定义规则： 共
    //  code定义为5位:
    // 第一位表示中小学：  1： 小学   2：初高中线上   3：初高中扫描
    // 第二位表示看完成还是看环比：  1：看完成    2：看环比
    // 第三位表示渗透情况： 1：全部  2：低渗   3：中渗  3：高渗   4：超高渗
    // 第四五为表示指标： 01：月活  02：新增  03：长回 04：短回  05：注册认证  06:1套到3套
    public static final int VIEW_TYPE_OVERVIEW_JUNIOR = 10000;                           // 小学概览
    public static final int VIEW_TYPE_OVERVIEW_MIDDLE = 20000;                           // 初高中概览线上（17作业模式）
    public static final int VIEW_TYPE_OVERVIEW_MIDDLE_KLX = 30000;                       // 初高中概览扫描（快乐学模式）

    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_MAUC = 11101;           // 小学单科月活（全部）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_INC_MAUC = 11102;       // 小学单科新增月活（全部）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_LTBF_MAUC = 11103;      // 小学单科长回月活（全部）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_STBF_MAUC = 11104;      // 小学单科短回月活（全部）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_REG_AUTH = 11105;               // 小学注册认证（全部）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_PROCESS = 11106;                // 小学过程指标（1套 - 3套）

    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_MAUC = 11201;           // 小学单科月活（低渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_INC_MAUC = 11202;       // 小学单科新增月活（低渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_LTBF_MAUC = 11203;      // 小学单科长回月活（低渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_STBF_MAUC = 11204;      // 小学单科短回月活（低渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_REG_AUTH = 11205;               // 小学注册认证（低渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_LP_PROCESS = 11206;                // 小学过程指标（1套 - 3套）（低渗）

    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_MAUC = 11301;           // 小学单科月活（中渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_INC_MAUC = 11302;       // 小学单科新增月活（中渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_LTBF_MAUC = 11303;      // 小学单科长回月活（中渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_STBF_MAUC = 11304;      // 小学单科短回月活（中渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_REG_AUTH = 11305;               // 小学注册认证（中渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_MP_PROCESS = 11306;                // 小学过程指标（1套 - 3套）（中渗）

    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_MAUC = 11401;           // 小学单科月活（高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_INC_MAUC = 11402;       // 小学单科新增月活（高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_LTBF_MAUC = 11403;      // 小学单科长回月活（高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_STBF_MAUC = 11404;      // 小学单科短回月活（高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_REG_AUTH = 11405;               // 小学注册认证（高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_HP_PROCESS = 11406;                // 小学过程指标（1套 - 3套）（高渗）

    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_MAUC = 11501;           // 小学单科月活（超高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_INC_MAUC = 11502;       // 小学单科新增月活（超高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_LTBF_MAUC = 11503;      // 小学单科长回月活（超高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_STBF_MAUC = 11504;      // 小学单科短回月活（超高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_REG_AUTH = 11505;               // 小学注册认证（超高渗）
    public static final int VIEW_TYPE_PERFORMANCE_JUNIOR_SP_PROCESS = 11506;                // 小学过程指标（1套 - 3套）（超高渗）





    public static final int VIEW_TYPE_LM_JUNIOR_SGLSUBJ_MAUC = 12101;                    // 小学单科月活（月环比）（全部）
    public static final int VIEW_TYPE_LM_JUNIOR_SGLSUBJ_INC_MAUC = 12102;                // 小学单科新增月活（月环比）（全部）
    public static final int VIEW_TYPE_LM_JUNIOR_SGLSUBJ_LTBF_MAUC = 12103;               // 小学单科长回月活（月环比）（全部）
    public static final int VIEW_TYPE_LM_JUNIOR_SGLSUBJ_STBF_MAUC = 12104;               // 小学单科短回月活（月环比）（全部）

    public static final int VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_MAUC = 12201;                 // 小学单科月活（月环比）（低渗）
    public static final int VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_INC_MAUC = 12202;             // 小学单科新增月活（月环比）（低渗）
    public static final int VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_LTBF_MAUC = 12203;            // 小学单科长回月活（月环比）（低渗）
    public static final int VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_STBF_MAUC = 12204;            // 小学单科短回月活（月环比）（低渗）

    public static final int VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_MAUC = 12301;                 // 小学单科月活（月环比）（中渗）
    public static final int VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_INC_MAUC = 12302;             // 小学单科新增月活（月环比）（中渗）
    public static final int VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_LTBF_MAUC = 12303;            // 小学单科长回月活（月环比）（中渗）
    public static final int VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_STBF_MAUC = 12304;            // 小学单科短回月活（月环比）（中渗）

    public static final int VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_MAUC = 12401;                 // 小学单科月活（月环比）（高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_INC_MAUC = 12402;             // 小学单科新增月活（月环比）（高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_LTBF_MAUC = 12403;            // 小学单科长回月活（月环比）（高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_STBF_MAUC = 12404;            // 小学单科短回月活（月环比）（高渗）

    public static final int VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_MAUC = 12501;                 // 小学单科月活（月环比）（超高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_INC_MAUC = 12502;             // 小学单科新增月活（月环比）（超高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_LTBF_MAUC = 12503;            // 小学单科长回月活（月环比）（超高渗）
    public static final int VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_STBF_MAUC = 12504;            // 小学单科短回月活（月环比）（超高渗）


    // 初高中线上
    public static final int VIEW_TYPE_PERFORMANCE_MIDDLE_ENG_MAUC = 21101;                  // 初高中英语月活
    public static final int VIEW_TYPE_PERFORMANCE_MIDDLE_REG_AUTH = 21102;                  // 初高中注册认证
    public static final int VIEW_TYPE_PERFORMANCE_MIDDLE_PROCESS = 21103;                   // 初高中过程指标（1套 - 3套）



    // 初高中扫描
    public static final int VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_PROCESS = 31101;               // 初高中扫描过程指标（低标，高标）
    public static final int VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_ALL_SUBJ = 31102;              // 初高中全科扫描

    public static final int VIET_TYPE_SCHOOL_LIST_JUNIOR = 9;                               // 小学学校列表
    public static final int VIET_TYPE_SCHOOL_LIST_MIDDLE = 10;                              // 初高中学校列表

    private Integer day;               //最新的业绩日期
    private Long key;                  //数据记录的键
    private Integer regionCode;            //regionCode 按城市维度查看该部门在该城市下的业绩时使用，其他情况下暂不使用，这种情况下name的值为城市名称
    private String name;               //数据记录的名称
    private String type;               //数据记录的类型
    private String note;               //数据记录的备注


    ////// 小学部分
    private int juniorStuScale;                            // 学生规模
    private int juniorRegStuCount;                         // 注册学生数
    private int juniorAuthStuCount;                        // 认证学生数
    // 小学注册认证
    private int juniorMonthRegStuCount;                    //本月注册学生数
    private int juniorMonthAuthStuCount;                   //本月认证学生数
    private int juniorRegStuCountDf;                       // 注册学生数日浮（昨日新增注册学生数）
    private int juniorAuthStuCountDf;                      // 认证学生数日浮（昨日新增认证学生数）

    // 小学单科月活
    private int juniorSglSubjMauc;                         // 小学单科月活
    private int juniorSglSubjMaucDf;                       // 小学单科月活日浮
    private int juniorSglSubjMaucBudget;                   // 小学单科月活预算
    // 小学单科新增月活
    private int juniorSglSubjIncMauc;                      // 小学单科新增月活
    private int juniorSglSubjIncMaucDf;                    // 小学单科新增月活日浮
    private int juniorSglSubjIncMaucBudget;                // 小学单科新增月活预算
    // 小学单科回流（长回）
    private int juniorSglSubjLtBfMauc;                     // 小学单科回流月活（长回）
    private int juniorSglSubjLtBfMaucDf;                   // 小学单科回流月活日浮（长回）
    private int juniorSglSubjLtBfMaucBudget;               // 小学单科回流月活预算（长回）
    // 小学单科回流（短回）
    private int juniorSglSubjStBfMauc;                     // 小学单科回流月活（短回）
    private int juniorSglSubjStBfMaucDf;                   // 小学单科回流月活日浮（短回）
    private int juniorSglSubjStBfMaucBudget;               // 小学单科回流月活预算（短回）

    // 一套到3套（所有学生）
    private int juniorFinSglSubjHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）
    private int juniorFinSglSubjHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）
    private int juniorFinSglSubjHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）


    // 高低渗（本月）
    // 本月低渗
    private int juniorLpSglSubjMauc;                       // 单科月活（本月低渗）
    private int juniorLpSglSubjMaucDf;                       // 单科月活（本月低渗日浮）
    private int juniorLpSglSubjIncMauc;                    // 新增月活（本月低渗）
    private int juniorLpSglSubjIncMaucDf;                    // 新增月活（本月低渗日浮）
    private int juniorLpSglSubjLtBfMauc;                   // 长回月活（本月低渗）
    private int juniorLpSglSubjLtBfMaucDf;                   // 长回月活（本月低渗日浮）
    private int juniorLpSglSubjStBfMauc;                   // 短回月活（本月低渗）
    private int juniorLpSglSubjStBfMaucDf;                   // 短回月活（本月低渗日浮）

    private int juniorLpMonthRegStuCount;                    //本月注册学生数（本月低渗）
    private int juniorLpMonthAuthStuCount;                   //本月认证学生数（本月低渗）
    private int juniorLpRegStuCountDf;                       // 注册学生数日浮（本月低渗）
    private int juniorLpAuthStuCountDf;                      // 认证学生数日浮（本月低渗）

    private int juniorLpFinSglSubjHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）（低渗）
    private int juniorLpFinSglSubjHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）（低渗）
    private int juniorLpFinSglSubjHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）（低渗）

    // 本月中渗
    private int juniorMpSglSubjMauc;                       // 单科月活（本月中渗）
    private int juniorMpSglSubjMaucDf;                       // 单科月活（本月中渗日浮）
    private int juniorMpSglSubjIncMauc;                    // 新增月活（本月中渗）
    private int juniorMpSglSubjIncMaucDf;                    // 新增月活（本月中渗日浮）
    private int juniorMpSglSubjLtBfMauc;                   // 长回月活（本月中渗）
    private int juniorMpSglSubjLtBfMaucDf;                   // 长回月活（本月中渗日浮）
    private int juniorMpSglSubjStBfMauc;                   // 短回月活（本月中渗）
    private int juniorMpSglSubjStBfMaucDf;                   // 短回月活（本月中渗日浮）

    private int juniorMpMonthRegStuCount;                    //本月注册学生数（本月中渗）
    private int juniorMpMonthAuthStuCount;                   //本月认证学生数（本月中渗）
    private int juniorMpRegStuCountDf;                       // 注册学生数日浮（本月中渗）
    private int juniorMpAuthStuCountDf;                      // 认证学生数日浮（本月中渗）

    private int juniorMpFinSglSubjHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）（中渗）
    private int juniorMpFinSglSubjHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）（中渗）
    private int juniorMpFinSglSubjHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）（中渗）

    // 本月高渗
    private int juniorHpSglSubjMauc;                       // 单科月活（本月高渗）
    private int juniorHpSglSubjMaucDf;                       // 单科月活（本月高渗日浮）
    private int juniorHpSglSubjIncMauc;                    // 新增月活（本月高渗）
    private int juniorHpSglSubjIncMaucDf;                    // 新增月活（本月高渗日浮）
    private int juniorHpSglSubjLtBfMauc;                   // 长回月活（本月高渗）
    private int juniorHpSglSubjLtBfMaucDf;                   // 长回月活（本月高渗日浮）
    private int juniorHpSglSubjStBfMauc;                   // 短回月活（本月高渗）
    private int juniorHpSglSubjStBfMaucDf;                   // 短回月活（本月高渗日浮）

    private int juniorHpMonthRegStuCount;                    //本月注册学生数（本月高渗）
    private int juniorHpMonthAuthStuCount;                   //本月认证学生数（本月高渗）
    private int juniorHpRegStuCountDf;                       // 注册学生数日浮（本月高渗）
    private int juniorHpAuthStuCountDf;                      // 认证学生数日浮（本月高渗）

    private int juniorHpFinSglSubjHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）（高渗）
    private int juniorHpFinSglSubjHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）（高渗）
    private int juniorHpFinSglSubjHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）（高渗）

    // 本月超高渗
    private int juniorSpSglSubjMauc;                       // 单科月活（本月超高渗）
    private int juniorSpSglSubjMaucDf;                       // 单科月活（本月超高渗日浮）
    private int juniorSpSglSubjIncMauc;                    // 新增月活（本月超高渗）
    private int juniorSpSglSubjIncMaucDf;                    // 新增月活（本月超高渗日浮）
    private int juniorSpSglSubjLtBfMauc;                   // 长回月活（本月超高渗）
    private int juniorSpSglSubjLtBfMaucDf;                   // 长回月活（本月超高渗日浮）
    private int juniorSpSglSubjStBfMauc;                   // 短回月活（本月超高渗）
    private int juniorSpSglSubjStBfMaucDf;                   // 短回月活（本月超高渗日浮）

    private int juniorSpMonthRegStuCount;                    //本月注册学生数（本月超高渗）
    private int juniorSpMonthAuthStuCount;                   //本月认证学生数（本月超高渗）
    private int juniorSpRegStuCountDf;                       // 注册学生数日浮（本月超高渗）
    private int juniorSpAuthStuCountDf;                      // 认证学生数日浮（本月超高渗）

    private int juniorSpFinSglSubjHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）（超高渗）
    private int juniorSpFinSglSubjHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）（超高渗）
    private int juniorSpFinSglSubjHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）（超高渗）

    ///// 高低渗（上月同天）
    // 上月同天全部
    private int juniorLmSglSubjMauc;                       // 单科月活（上月同天）
    private int juniorLmSglSubjIncMauc;                      // 上月同天新增月活
    private int juniorLmSglSubjLtBfMauc;                      // 上月同天长回月活
    private int juniorLmSglSubjStBfMauc;                     // 上月同天短回月活

    // 上月同天低渗
    private int juniorLmLpSglSubjMauc;                     // 单科月活（上月同天低渗）
    private int juniorLmLpSglSubjIncMauc;                  // 新增月活（上月同天低渗）
    private int juniorLmLpSglSubjLtBfMauc;                 // 长回月活（上月同天低渗）
    private int juniorLmLpSglSubjStBfMauc;                 // 短回月活（上月同天低渗）

    // 上月同天中渗
    private int juniorLmMpSglSubjMauc;                     // 单科月活（上月同天中渗）
    private int juniorLmMpSglSubjIncMauc;                  // 新增月活（上月同天中渗）
    private int juniorLmMpSglSubjLtBfMauc;                 // 长回月活（上月同天中渗）
    private int juniorLmMpSglSubjStBfMauc;                 // 短回月活（上月同天中渗）

    // 上月同天高渗
    private int juniorLmHpSglSubjMauc;                     // 单科月活（上月同天高渗）
    private int juniorLmHpSglSubjIncMauc;                  // 新增月活（上月同天高渗）
    private int juniorLmHpSglSubjLtBfMauc;                 // 长回月活（上月同天高渗）
    private int juniorLmHpSglSubjStBfMauc;                 // 短回月活（上月同天高渗）

    // 上月同天超高渗
    private int juniorLmSpSglSubjMauc;                     // 单科月活（上月同天超高渗）
    private int juniorLmSpSglSubjIncMauc;                  // 新增月活（上月同天超高渗）
    private int juniorLmSpSglSubjLtBfMauc;                 // 长回月活（上月同天超高渗）
    private int juniorLmSpSglSubjStBfMauc;                 // 短回月活（上月同天超高渗）


    ////// 初高中
    private int middleStuScale;                            // 学生规模
    private int stuKlxTnCount;
    private int middleRegStuCount;                         // 注册学生数
    private int middleAuthStuCount;                        // 认证学生数

    //////  初高中线上

    // 初高中注册认证
    private int middleMonthRegStuCount;                    // 本月注册学生数
    private int middleMonthAuthStuCount;                   // 本月认证学生数
    private int middleRegStuCountDf;                       // 注册学生数日浮（昨日新增注册学生数）
    private int middleAuthStuCountDf;                      // 认证学生数日浮（昨日新增认证学生数）

    // 一套到3套（所有学生）
    private int middleFinEngHwEq1StuCount;                   // 完成1套作业的学生数（所有学生）
    private int middleFinEngHwEq2StuCount;                   // 完成2套作业的学生数（所有学生）
    private int middleFinEngHwGte3StuCount;                  // 完成3套及作业及以上的学生数（所有学生）

    // 初高中月活数据
    private int middleEngMauc;                         // 初高中英语月活
    private int middleEngIncMauc;                      // 初高中英语新增月活
    private int middleEngBfMauc;                       // 初高中英语回流月活
    private int middleEngMaucDf;                       // 初高中英语月活日浮



    ////// 初高中扫描
    // 本月扫描情况
    private int finLowAllSubjAnshEq1StuCount;              // 低标=1，全部扫描
    private int finLowAllSubjAnshGte2StuCount;             // 低标≥2，全部扫描
    private int finHighAllSubjAnshEq1StuCount;             // 高标=1，全部扫描
    private int finHighAllSubjAnshGte2StuCount;             // 高标≥2，全部扫描

    // 全科
    private int allSubjAnshGte2StuCount;                   // 全科扫描
    private int allSubjAnshGte2IncStuCount;                // 全科扫描（新增）
    private int allSubjAnshGte2BfStuCount;                 // 全科扫描（回流）
    private int allSubjAnshGte2StuCountDf;                 // 全科扫描（日浮）

    // 数学
    private int mathAnshGte2StuCount;                      // 数学扫描数
    private int mathAnshGte2StuCountBudget;                // 数学扫描数（预算）
    private int mathAnshGte2IncStuCount;                   // 数学扫描数（新增）
    private int mathAnshGte2IncStuCountBudget;             // 数学扫描数（新增预算）
    private int mathAnshGte2BfStuCount;                    // 数学扫描数（回流）
    private int mathAnshGte2BfStuCountBudget;              // 数学扫描数（回流预算）
    private int mathAnshGte2StuCountDf;                    // 数学扫描数（日浮）

    // 副科
    private int deputyAnshGet2StuCount;                    // 副科扫描数
    private int deputyAnshGet2IncStuCount;                 // 副科扫描数（新增）
    private int deputyAnshGet2BfStuCount;                  // 副科扫描数（回流）
    private int deputyAnshGet2StuCountDf;                  // 副科扫描数（日浮）

    // 其他科目
    private int otherAnshGte2StuCount;                     // 其他科目扫描数
    private int otherAnshGte2IncStuCount;                  // 其他科目扫描数（新增）
    private int otherAnshGte2BfStuCount;                   // 其他科目扫描数（回流）
    private int otherAnshGte2StuCountDf;                   // 其他科目扫描数（日浮）

    // 中学英语月活
    @Deprecated private int engHwGte3AuStuCount;                       // 英语月活
    @Deprecated private int engHwGte3AuStuCountDf;                     // 英语月活（日浮）


    private int thSemUnauthStuCount;                        // 本学期注册未认证学生数

    public static String ck_id_type_day(Long key, String type, Integer day) {
        return CacheKeyGenerator.generateCacheKey(PerformanceData.class,
                new String[]{"k", "t", "d"},
                new Object[]{key, type, day});
    }

    public static String ck_id_region_type_day(Long key, Integer regionCode, String type, Integer day){
        return CacheKeyGenerator.generateCacheKey(PerformanceData.class,
                new String[]{"k", "r", "t", "d"},
                new Object[]{key, regionCode, type, day});
    }

    public PerformanceData() {
    }

    public PerformanceData(Integer day) {
        this.day = day;
    }

    // 只加业绩数据，预算数据单独计算
    public PerformanceData appendData(PerformanceData p) {
        if(p == null){
            return this;
        }

        this.juniorStuScale += p.getJuniorStuScale();
        this.juniorRegStuCount += p.getJuniorRegStuCount();
        this.juniorAuthStuCount += p.getJuniorAuthStuCount();

        this.juniorMonthRegStuCount += p.getJuniorMonthRegStuCount();
        this.juniorMonthAuthStuCount += p.getJuniorMonthAuthStuCount();
        this.juniorRegStuCountDf += p.getJuniorRegStuCountDf();
        this.juniorAuthStuCountDf += p.getJuniorAuthStuCountDf();

        this.juniorSglSubjMauc += p.getJuniorSglSubjMauc();
        this.juniorSglSubjMaucDf += p.getJuniorSglSubjMaucDf();
        this.juniorSglSubjIncMauc += p.getJuniorSglSubjIncMauc();
        this.juniorSglSubjIncMaucDf += p.getJuniorSglSubjIncMaucDf();
        this.juniorSglSubjLtBfMauc += p.getJuniorSglSubjLtBfMauc();
        this.juniorSglSubjLtBfMaucDf += p.getJuniorSglSubjLtBfMaucDf();
        this.juniorSglSubjStBfMauc += p.getJuniorSglSubjStBfMauc();
        this.juniorSglSubjStBfMaucDf += p.getJuniorSglSubjStBfMaucDf();

        // 一套到3套（所有学生）
        this.juniorFinSglSubjHwEq1StuCount += p.getJuniorFinSglSubjHwEq1StuCount();
        this.juniorFinSglSubjHwEq2StuCount += p.getJuniorFinSglSubjHwEq2StuCount();
        this.juniorFinSglSubjHwGte3StuCount += p.getJuniorFinSglSubjHwGte3StuCount();


        // 高低渗（本月）
        // 本月低渗
        this.juniorLpSglSubjMauc += p.getJuniorLpSglSubjMauc();
        this.juniorLpSglSubjMaucDf += p.getJuniorLpSglSubjMaucDf();
        this.juniorLpSglSubjIncMauc += p.getJuniorLpSglSubjIncMauc();
        this.juniorLpSglSubjIncMaucDf += p.getJuniorLpSglSubjIncMaucDf();
        this.juniorLpSglSubjLtBfMauc += p.getJuniorLpSglSubjLtBfMauc();
        this.juniorLpSglSubjLtBfMaucDf += p.getJuniorLpSglSubjLtBfMaucDf();
        this.juniorLpSglSubjStBfMauc += p.getJuniorLpSglSubjStBfMauc();
        this.juniorLpSglSubjStBfMaucDf += p.getJuniorLpSglSubjStBfMaucDf();

        this.juniorLpMonthRegStuCount += p.getJuniorLpMonthRegStuCount();
        this.juniorLpMonthAuthStuCount += p.getJuniorLpMonthAuthStuCount();
        this.juniorLpRegStuCountDf += p.getJuniorLpRegStuCountDf();
        this.juniorLpAuthStuCountDf += p.getJuniorLpAuthStuCountDf();

        this.juniorLpFinSglSubjHwEq1StuCount += p.getJuniorLpFinSglSubjHwEq1StuCount();
        this.juniorLpFinSglSubjHwEq2StuCount += p.getJuniorLpFinSglSubjHwEq2StuCount();
        this.juniorLpFinSglSubjHwGte3StuCount += p.getJuniorLpFinSglSubjHwGte3StuCount();

        // 本月中渗
        this.juniorMpSglSubjMauc += p.getJuniorMpSglSubjMauc();
        this.juniorMpSglSubjMaucDf += p.getJuniorMpSglSubjMaucDf();
        this.juniorMpSglSubjIncMauc += p.getJuniorMpSglSubjIncMauc();
        this.juniorMpSglSubjIncMaucDf += p.getJuniorMpSglSubjIncMaucDf();
        this.juniorMpSglSubjLtBfMauc += p.getJuniorMpSglSubjLtBfMauc();
        this.juniorMpSglSubjLtBfMaucDf += p.getJuniorMpSglSubjLtBfMaucDf();
        this.juniorMpSglSubjStBfMauc += p.getJuniorMpSglSubjStBfMauc();
        this.juniorMpSglSubjStBfMaucDf += p.getJuniorMpSglSubjStBfMaucDf();

        this.juniorMpMonthRegStuCount += p.getJuniorMpMonthRegStuCount();
        this.juniorMpMonthAuthStuCount += p.getJuniorMpMonthAuthStuCount();
        this.juniorMpRegStuCountDf += p.getJuniorMpRegStuCountDf();
        this.juniorMpAuthStuCountDf += p.getJuniorMpAuthStuCountDf();

        this.juniorMpFinSglSubjHwEq1StuCount += p.getJuniorMpFinSglSubjHwEq1StuCount();
        this.juniorMpFinSglSubjHwEq2StuCount += p.getJuniorMpFinSglSubjHwEq2StuCount();
        this.juniorMpFinSglSubjHwGte3StuCount += p.getJuniorMpFinSglSubjHwGte3StuCount();

        // 本月高渗
        this.juniorHpSglSubjMauc += p.getJuniorHpSglSubjMauc();
        this.juniorHpSglSubjMaucDf += p.getJuniorHpSglSubjMaucDf();
        this.juniorHpSglSubjIncMauc += p.getJuniorHpSglSubjIncMauc();
        this.juniorHpSglSubjIncMaucDf += p.getJuniorHpSglSubjIncMaucDf();
        this.juniorHpSglSubjLtBfMauc += p.getJuniorHpSglSubjLtBfMauc();
        this.juniorHpSglSubjLtBfMaucDf += p.getJuniorHpSglSubjLtBfMaucDf();
        this.juniorHpSglSubjStBfMauc += p.getJuniorHpSglSubjStBfMauc();
        this.juniorHpSglSubjStBfMaucDf += p.getJuniorHpSglSubjStBfMaucDf();

        this.juniorHpMonthRegStuCount += p.getJuniorHpMonthRegStuCount();
        this.juniorHpMonthAuthStuCount += p.getJuniorHpMonthAuthStuCount();
        this.juniorHpRegStuCountDf += p.getJuniorHpRegStuCountDf();
        this.juniorHpAuthStuCountDf += p.getJuniorHpAuthStuCountDf();

        this.juniorHpFinSglSubjHwEq1StuCount += p.getJuniorHpFinSglSubjHwEq1StuCount();
        this.juniorHpFinSglSubjHwEq2StuCount += p.getJuniorHpFinSglSubjHwEq2StuCount();
        this.juniorHpFinSglSubjHwGte3StuCount += p.getJuniorHpFinSglSubjHwGte3StuCount();

        // 本月超高渗
        this.juniorSpSglSubjMauc += p.getJuniorSpSglSubjMauc();
        this.juniorSpSglSubjMaucDf += p.getJuniorSpSglSubjMaucDf();
        this.juniorSpSglSubjIncMauc += p.getJuniorSpSglSubjIncMauc();
        this.juniorSpSglSubjIncMaucDf += p.getJuniorSpSglSubjIncMaucDf();
        this.juniorSpSglSubjLtBfMauc += p.getJuniorSpSglSubjLtBfMauc();
        this.juniorSpSglSubjLtBfMaucDf += p.getJuniorSpSglSubjLtBfMaucDf();
        this.juniorSpSglSubjStBfMauc += p.getJuniorSpSglSubjStBfMauc();
        this.juniorSpSglSubjStBfMaucDf += p.getJuniorSpSglSubjStBfMaucDf();

        this.juniorSpMonthRegStuCount += p.getJuniorSpMonthRegStuCount();
        this.juniorSpMonthAuthStuCount += p.getJuniorSpMonthAuthStuCount();
        this.juniorSpRegStuCountDf += p.getJuniorSpRegStuCountDf();
        this.juniorSpAuthStuCountDf += p.getJuniorSpAuthStuCountDf();

        this.juniorSpFinSglSubjHwEq1StuCount += p.getJuniorSpFinSglSubjHwEq1StuCount();
        this.juniorSpFinSglSubjHwEq2StuCount += p.getJuniorSpFinSglSubjHwEq2StuCount();
        this.juniorSpFinSglSubjHwGte3StuCount += p.getJuniorSpFinSglSubjHwGte3StuCount();

        ///// 高低渗（上月同天）
        // 上月同天全部
        this.juniorLmSglSubjMauc += p.getJuniorLmSglSubjMauc();
        this.juniorLmSglSubjIncMauc += p.getJuniorLmSglSubjIncMauc();
        this.juniorLmSglSubjLtBfMauc += p.getJuniorLmSglSubjLtBfMauc();
        this.juniorLmSglSubjStBfMauc += p.getJuniorLmSglSubjStBfMauc();

        // 上月同天低渗
        this.juniorLmLpSglSubjMauc += p.getJuniorLmLpSglSubjMauc();
        this.juniorLmLpSglSubjIncMauc += p.getJuniorLmLpSglSubjIncMauc();
        this.juniorLmLpSglSubjLtBfMauc += p.getJuniorLmLpSglSubjLtBfMauc();
        this.juniorLmLpSglSubjStBfMauc += p.getJuniorLmLpSglSubjStBfMauc();

        // 上月同天中渗
        this.juniorLmMpSglSubjMauc += p.getJuniorLmMpSglSubjMauc();
        this.juniorLmMpSglSubjIncMauc += p.getJuniorLmMpSglSubjIncMauc();
        this.juniorLmMpSglSubjLtBfMauc += p.getJuniorLmMpSglSubjLtBfMauc();
        this.juniorLmMpSglSubjStBfMauc += p.getJuniorLmMpSglSubjStBfMauc();

        // 上月同天高渗
        this.juniorLmHpSglSubjMauc += p.getJuniorLmHpSglSubjMauc();
        this.juniorLmHpSglSubjIncMauc += p.getJuniorLmHpSglSubjIncMauc();
        this.juniorLmHpSglSubjLtBfMauc += p.getJuniorLmHpSglSubjLtBfMauc();
        this.juniorLmHpSglSubjStBfMauc += p.getJuniorLmHpSglSubjStBfMauc();

        // 上月同天超高渗
        this.juniorLmSpSglSubjMauc += p.getJuniorLmSpSglSubjMauc();
        this.juniorLmSpSglSubjIncMauc += p.getJuniorLmSpSglSubjIncMauc();
        this.juniorLmSpSglSubjLtBfMauc += p.getJuniorLmSpSglSubjLtBfMauc();
        this.juniorLmSpSglSubjStBfMauc += p.getJuniorLmSpSglSubjStBfMauc();


        ////// 初高中
        this.middleStuScale += p.getMiddleStuScale();
        this.stuKlxTnCount += p.getStuKlxTnCount();
        this.middleRegStuCount += p.getMiddleRegStuCount();
        this.middleAuthStuCount += p.getMiddleAuthStuCount();

        //////  初高中线上
        // 初高中注册认证
        this.middleMonthRegStuCount += p.getMiddleMonthRegStuCount();
        this.middleMonthAuthStuCount += p.getMiddleMonthAuthStuCount();
        this.middleRegStuCountDf += p.getMiddleRegStuCountDf();
        this.middleAuthStuCountDf += p.getMiddleAuthStuCountDf();

        // 一套到3套（所有学生）
        this.middleFinEngHwEq1StuCount += p.getMiddleFinEngHwEq1StuCount();
        this.middleFinEngHwEq2StuCount += p.getMiddleFinEngHwEq2StuCount();
        this.middleFinEngHwGte3StuCount += p.getMiddleFinEngHwGte3StuCount();

        // 初高中月活数据
        this.middleEngMauc += p.getMiddleEngMauc();
        this.middleEngIncMauc += p.getMiddleEngIncMauc();
        this.middleEngBfMauc += p.getMiddleEngBfMauc();
        this.middleEngMaucDf += p.getMiddleEngMaucDf();

        ////// 初高中扫描
        // 本月扫描情况
        this.finLowAllSubjAnshEq1StuCount += p.getFinLowAllSubjAnshEq1StuCount();
        this.finLowAllSubjAnshGte2StuCount += p.getFinLowAllSubjAnshGte2StuCount();
        this.finHighAllSubjAnshEq1StuCount += p.getFinHighAllSubjAnshEq1StuCount();
        this.finHighAllSubjAnshGte2StuCount += p.getFinHighAllSubjAnshGte2StuCount();

        this.allSubjAnshGte2StuCount += p.getAllSubjAnshGte2StuCount();
        this.allSubjAnshGte2IncStuCount += p.getAllSubjAnshGte2IncStuCount();
        this.allSubjAnshGte2BfStuCount += p.getAllSubjAnshGte2BfStuCount();
        this.allSubjAnshGte2StuCountDf += p.getAllSubjAnshGte2StuCountDf();

        this.mathAnshGte2StuCount += p.getMathAnshGte2StuCount();
        this.mathAnshGte2IncStuCount += p.getMathAnshGte2IncStuCount();
        this.mathAnshGte2BfStuCount += p.getMathAnshGte2BfStuCount();
        this.mathAnshGte2StuCountDf += p.getMathAnshGte2StuCountDf();

        this.deputyAnshGet2StuCount += p.getDeputyAnshGet2StuCount();
        this.deputyAnshGet2IncStuCount += p.getDeputyAnshGet2IncStuCount();
        this.deputyAnshGet2BfStuCount += p.getDeputyAnshGet2BfStuCount();
        this.deputyAnshGet2StuCountDf += p.getDeputyAnshGet2StuCountDf();

        this.otherAnshGte2StuCount += p.getOtherAnshGte2StuCount();
        this.otherAnshGte2IncStuCount += p.getOtherAnshGte2IncStuCount();
        this.otherAnshGte2BfStuCount += p.getOtherAnshGte2BfStuCount();
        this.otherAnshGte2StuCountDf += p.getOtherAnshGte2StuCountDf();

        this.engHwGte3AuStuCount += p.getEngHwGte3AuStuCount();
        this.engHwGte3AuStuCountDf += p.getEngHwGte3AuStuCountDf();

        this.thSemUnauthStuCount += p.getThSemUnauthStuCount();
        return this;
    }


    public void appendPerformanceSumData(PerformanceSumData sumData, int mode, boolean authFlg){
        if(sumData == null || (mode != 1 && mode != 2)){
            return;
        }
        this.thSemUnauthStuCount += sumData.getTSemUnauthStuCount();
        if(mode == 1){ // 小学
            this.juniorStuScale += sumData.getStuScale();
            this.juniorRegStuCount += sumData.getRegStuCount();
            this.juniorAuthStuCount += sumData.getAuthStuCount();

            // 设置完成数和日浮
            // 未鉴定学校的完成数和日浮数据不做计算，但预算正常参与计算
            if(authFlg) {
                this.juniorMonthRegStuCount += sumData.getTmIncRegStuCount();
                this.juniorMonthAuthStuCount += sumData.getTmIncAuthStuCount();
                this.juniorRegStuCountDf += sumData.getRegStuCountDf();
                this.juniorAuthStuCountDf += sumData.getAuthStuCountDf();

                this.juniorSglSubjMauc += sumData.getFinSglSubjHwGte3AuStuCount();
                this.juniorSglSubjMaucDf += sumData.getFinSglSubjHwGte3AuStuCountDf();

                this.juniorSglSubjIncMauc += sumData.getFinSglSubjHwGte3IncAuStuCount();
                this.juniorSglSubjIncMaucDf += sumData.getFinSglSubjHwGte3IncAuStuCountDf();

                this.juniorSglSubjLtBfMauc += sumData.getFinSglSubjHwGte3LtBfAuStuCount();
                this.juniorSglSubjLtBfMaucDf += sumData.getFinSglSubjHwGte3LtBfAuStuCountDf();

                this.juniorSglSubjStBfMauc += sumData.getFinSglSubjHwGte3StBfAuStuCount();
                this.juniorSglSubjStBfMaucDf += sumData.getFinSglSubjHwGte3StBfAuStuCountDf();

            }
        }else { // 初高中

            // 设置完成数和日浮
            // 未鉴定学校的完成数和日浮数据不做计算，但预算正常参与计算
            if(authFlg) {
                this.allSubjAnshGte2StuCount += sumData.getFinAllSubjAnshGte2StuCount();
                this.allSubjAnshGte2IncStuCount += sumData.getFinAllSubjAnshGte2IncStuCount();
                this.allSubjAnshGte2BfStuCount += sumData.getFinAllSubjAnshGte2BfStuCount();
                this.allSubjAnshGte2StuCountDf += sumData.getFinAllSubjAnshGte2StuCountDf();

                this.mathAnshGte2StuCount += sumData.getFinMathAnshGte2StuCount();
                this.mathAnshGte2IncStuCount += sumData.getFinMathAnshGte2IncStuCount();
                this.mathAnshGte2BfStuCount += sumData.getFinMathAnshGte2BfStuCount();
                this.mathAnshGte2StuCountDf += sumData.getFinMathAnshGte2StuCountDf();

                this.deputyAnshGet2StuCount += sumData.getFinDeputyAnshGte2StuCount();
                this.deputyAnshGet2IncStuCount += sumData.getFinDeputyAnshGte2IncStuCount();
                this.deputyAnshGet2BfStuCount += sumData.getFinDeputyAnshGte2BfStuCount();
                this.deputyAnshGet2StuCountDf += sumData.getFinDeputyAnshGte2StuCountDf();

                this.otherAnshGte2StuCount += sumData.getFinOtherAnshGte2StuCount();
                this.otherAnshGte2IncStuCount += sumData.getFinOtherAnshGte2IncStuCount();
                this.otherAnshGte2BfStuCount += sumData.getFinOtherAnshGte2BfStuCount();
                this.otherAnshGte2StuCountDf += sumData.getFinOtherAnshGte2StuCountDf();

                this.engHwGte3AuStuCount += sumData.getFinEngHwGte3AuStuCount();
                this.engHwGte3AuStuCountDf += sumData.getFinEngHwGte3AuStuCountDf();
            }

//            this.mathAnshGte2StuCountBudget += sumData.getFinMathAnshGte2StuCountBudget();
//            this.mathAnshGte2IncStuCountBudget += sumData.getFinMathAnshGte2IncStuCountBudget();
//            this.mathAnshGte2BfStuCountBudget += sumData.getFinMathAnshGte2BfStuCountBudget();
        }

    }

    public void appendPerformanceGoalData(AgentPerformanceGoal performanceGoal){
        if(performanceGoal == null){
            return;
        }

        // 设置预算（小学）
        this.juniorSglSubjMaucBudget += SafeConverter.toInt(performanceGoal.getSglSubjIncGoal()) + SafeConverter.toInt(performanceGoal.getSglSubjLtBfGoal()) + SafeConverter.toInt(performanceGoal.getSglSubjStBfGoal());
        this.juniorSglSubjIncMaucBudget += SafeConverter.toInt(performanceGoal.getSglSubjIncGoal());
        this.juniorSglSubjLtBfMaucBudget += SafeConverter.toInt(performanceGoal.getSglSubjLtBfGoal());
        this.juniorSglSubjStBfMaucBudget += SafeConverter.toInt(performanceGoal.getSglSubjStBfGoal());
    }

    public void appendSchoolPerformanceDetailData(SchoolPerformanceDetailData p, boolean isCalPerformance){
        if(p == null){
            return;
        }
        if(p.getSchoolLevel() == SchoolLevel.JUNIOR){
            this.juniorStuScale += p.getStuScale();
            this.juniorRegStuCount += p.getRegStuCount();
            this.juniorAuthStuCount += p.getAuthStuCount();

            this.juniorMonthRegStuCount += p.getTmIncRegStuCount();
            this.juniorMonthAuthStuCount += p.getTmIncAuthStuCount();
            this.juniorRegStuCountDf += p.getRegStuCountDf();
            this.juniorAuthStuCountDf += p.getAuthStuCountDf();

            if(isCalPerformance) {
                this.juniorSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.juniorSglSubjMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
                this.juniorSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                this.juniorSglSubjIncMaucDf += p.getFinSglSubjHwGte3IncAuStuCountDf();
                this.juniorSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                this.juniorSglSubjLtBfMaucDf += p.getFinSglSubjHwGte3LtBfAuStuCountDf();
                this.juniorSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
                this.juniorSglSubjStBfMaucDf += p.getFinSglSubjHwGte3StBfAuStuCountDf();
            }

            // 一套到3套（所有学生）
            this.juniorFinSglSubjHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
            this.juniorFinSglSubjHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
            this.juniorFinSglSubjHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();


            if(p.getPermeability() == AgentSchoolPermeabilityType.LOW){
                if(isCalPerformance) {
                    this.juniorLpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                    this.juniorLpSglSubjMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
                    this.juniorLpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                    this.juniorLpSglSubjIncMaucDf += p.getFinSglSubjHwGte3IncAuStuCountDf();
                    this.juniorLpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                    this.juniorLpSglSubjLtBfMaucDf += p.getFinSglSubjHwGte3LtBfAuStuCountDf();
                    this.juniorLpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
                    this.juniorLpSglSubjStBfMaucDf += p.getFinSglSubjHwGte3StBfAuStuCountDf();
                }

                this.juniorLpMonthRegStuCount += p.getTmIncRegStuCount();
                this.juniorLpMonthAuthStuCount += p.getTmIncAuthStuCount();
                this.juniorLpRegStuCountDf += p.getRegStuCountDf();
                this.juniorLpAuthStuCountDf += p.getAuthStuCountDf();

                this.juniorLpFinSglSubjHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
                this.juniorLpFinSglSubjHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
                this.juniorLpFinSglSubjHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.MIDDLE){
                if(isCalPerformance) {
                    this.juniorMpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                    this.juniorMpSglSubjMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
                    this.juniorMpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                    this.juniorMpSglSubjIncMaucDf += p.getFinSglSubjHwGte3IncAuStuCountDf();
                    this.juniorMpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                    this.juniorMpSglSubjLtBfMaucDf += p.getFinSglSubjHwGte3LtBfAuStuCountDf();
                    this.juniorMpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
                    this.juniorMpSglSubjStBfMaucDf += p.getFinSglSubjHwGte3StBfAuStuCountDf();
                }

                this.juniorMpMonthRegStuCount += p.getTmIncRegStuCount();
                this.juniorMpMonthAuthStuCount += p.getTmIncAuthStuCount();
                this.juniorMpRegStuCountDf += p.getRegStuCountDf();
                this.juniorMpAuthStuCountDf += p.getAuthStuCountDf();

                this.juniorMpFinSglSubjHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
                this.juniorMpFinSglSubjHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
                this.juniorMpFinSglSubjHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.HIGH){
                if(isCalPerformance) {
                    this.juniorHpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                    this.juniorHpSglSubjMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
                    this.juniorHpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                    this.juniorHpSglSubjIncMaucDf += p.getFinSglSubjHwGte3IncAuStuCountDf();
                    this.juniorHpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                    this.juniorHpSglSubjLtBfMaucDf += p.getFinSglSubjHwGte3LtBfAuStuCountDf();
                    this.juniorHpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
                    this.juniorHpSglSubjStBfMaucDf += p.getFinSglSubjHwGte3StBfAuStuCountDf();
                }

                this.juniorHpMonthRegStuCount += p.getTmIncRegStuCount();
                this.juniorHpMonthAuthStuCount += p.getTmIncAuthStuCount();
                this.juniorHpRegStuCountDf += p.getRegStuCountDf();
                this.juniorHpAuthStuCountDf += p.getAuthStuCountDf();

                this.juniorHpFinSglSubjHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
                this.juniorHpFinSglSubjHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
                this.juniorHpFinSglSubjHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.SUPER_HIGH){
                if(isCalPerformance) {
                    this.juniorSpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                    this.juniorSpSglSubjMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
                    this.juniorSpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                    this.juniorSpSglSubjIncMaucDf += p.getFinSglSubjHwGte3IncAuStuCountDf();
                    this.juniorSpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                    this.juniorSpSglSubjLtBfMaucDf += p.getFinSglSubjHwGte3LtBfAuStuCountDf();
                    this.juniorSpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
                    this.juniorSpSglSubjStBfMaucDf += p.getFinSglSubjHwGte3StBfAuStuCountDf();
                }

                this.juniorSpMonthRegStuCount += p.getTmIncRegStuCount();
                this.juniorSpMonthAuthStuCount += p.getTmIncAuthStuCount();
                this.juniorSpRegStuCountDf += p.getRegStuCountDf();
                this.juniorSpAuthStuCountDf += p.getAuthStuCountDf();

                this.juniorSpFinSglSubjHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
                this.juniorSpFinSglSubjHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
                this.juniorSpFinSglSubjHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();
            }
        }else if(p.getSchoolLevel() == SchoolLevel.MIDDLE || p.getSchoolLevel() == SchoolLevel.HIGH){
            this.middleStuScale += p.getStuScale();
            this.stuKlxTnCount += p.getStuKlxTnCount();
            this.middleRegStuCount +=  p.getRegStuCount();
            this.middleAuthStuCount += p.getAuthStuCount();

            //////  初高中线上
            // 初高中注册认证
            this.middleMonthRegStuCount += p.getTmIncRegStuCount();
            this.middleMonthAuthStuCount += p.getTmIncAuthStuCount();
            this.middleRegStuCountDf += p.getRegStuCountDf();
            this.middleAuthStuCountDf += p.getAuthStuCountDf();

            // 一套到3套（所有学生）
            this.middleFinEngHwEq1StuCount += p.getFinSglSubjHwEq1UnAuStuCount() + p.getFinSglSubjHwEq1IncAuStuCount() + p.getFinSglSubjHwEq1StBfAuStuCount() + p.getFinSglSubjHwEq1LtBfAuStuCount();
            this.middleFinEngHwEq2StuCount += p.getFinSglSubjHwEq2UnAuStuCount() + p.getFinSglSubjHwEq2IncAuStuCount() + p.getFinSglSubjHwEq2StBfAuStuCount() + p.getFinSglSubjHwEq2LtBfAuStuCount();
            this.middleFinEngHwGte3StuCount += p.getFinSglSubjHwGte3UnAuStuCount() + p.getFinSglSubjHwGte3AuStuCount();

            // 初高中月活数据
            if(isCalPerformance) {
                this.middleEngMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.middleEngIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                this.middleEngBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount() + p.getFinSglSubjHwGte3StBfAuStuCount();
                this.middleEngMaucDf += p.getFinSglSubjHwGte3AuStuCountDf();
            }

            ////// 初高中扫描
            // 低标，高标
            this.finLowAllSubjAnshEq1StuCount += p.getFinLowAllSubjAnshEq1StuCount();
            this.finLowAllSubjAnshGte2StuCount += p.getFinLowAllSubjAnshGte2StuCount();
            this.finHighAllSubjAnshEq1StuCount += p.getFinHighAllSubjAnshEq1StuCount();
            this.finHighAllSubjAnshGte2StuCount += p.getFinAllSubjAnshGte2StuCount();

            // 本月扫描情况
            if(isCalPerformance) {
                this.allSubjAnshGte2StuCount += p.getFinAllSubjAnshGte2StuCount();
                this.allSubjAnshGte2IncStuCount += p.getFinAllSubjAnshGte2IncStuCount();
                this.allSubjAnshGte2BfStuCount += p.getFinAllSubjAnshGte2BfStuCount();
                this.allSubjAnshGte2StuCountDf += p.getFinAllSubjAnshGte2StuCountDf();

                this.mathAnshGte2StuCount += p.getFinMathAnshGte2StuCount();
                this.mathAnshGte2IncStuCount += p.getFinMathAnshGte2IncStuCount();
                this.mathAnshGte2BfStuCount += p.getFinMathAnshGte2BfStuCount();
                this.mathAnshGte2StuCountDf += p.getFinMathAnshGte2StuCountDf();

                this.deputyAnshGet2StuCount += p.getFinDeputyAnshGte2StuCount();
                this.deputyAnshGet2IncStuCount += p.getFinDeputyAnshGte2IncStuCount();
                this.deputyAnshGet2BfStuCount += p.getFinDeputyAnshGte2BfStuCount();
                this.deputyAnshGet2StuCountDf += p.getFinDeputyAnshGte2StuCountDf();

                this.otherAnshGte2StuCount += p.getFinOtherAnshGte2StuCount();
                this.otherAnshGte2IncStuCount += p.getFinOtherAnshGte2IncStuCount();
                this.otherAnshGte2BfStuCount += p.getFinOtherAnshGte2BfStuCount();
                this.otherAnshGte2StuCountDf += p.getFinOtherAnshGte2StuCountDf();
            }
        }
    }

    // 设置上月同天数据
    public void appendSchoolLmPerformanceDetailData(SchoolPerformanceDetailData p){
        if(p == null){
            return;
        }
        if(p.getSchoolLevel() == SchoolLevel.JUNIOR){
            // 上月同天低渗全部
            this.juniorLmSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
            this.juniorLmSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
            this.juniorLmSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
            this.juniorLmSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();

            if(p.getPermeability() == AgentSchoolPermeabilityType.LOW){
                this.juniorLmLpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.juniorLmLpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                this.juniorLmLpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();;
                this.juniorLmLpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.MIDDLE){
                this.juniorLmMpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.juniorLmMpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();;
                this.juniorLmMpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                this.juniorLmMpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.HIGH){
                this.juniorLmHpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.juniorLmHpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                this.juniorLmHpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                this.juniorLmHpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
            }else if(p.getPermeability() == AgentSchoolPermeabilityType.SUPER_HIGH){
                this.juniorLmSpSglSubjMauc += p.getFinSglSubjHwGte3AuStuCount();
                this.juniorLmSpSglSubjIncMauc += p.getFinSglSubjHwGte3IncAuStuCount();
                this.juniorLmSpSglSubjLtBfMauc += p.getFinSglSubjHwGte3LtBfAuStuCount();
                this.juniorLmSpSglSubjStBfMauc += p.getFinSglSubjHwGte3StBfAuStuCount();
            }
        }
    }

    public double calCompleteRate(double d1, double d2){
        return calCompleteRate(d1, d2, 2);
    }

    public double calCompleteRate(double d1, double d2, int newScale){
        return calCompleteRate(d1, d2, newScale, BigDecimal.ROUND_FLOOR);
    }

    public double calCompleteRate(double d1, double d2, int newScale, int roundingMode){
        if(d2 == 0){
            return 0;
        }
        return MathUtils.doubleDivide(d1, d2, newScale, roundingMode);
    }

    public PerformanceViewData generateViewData(Integer viewType) {
        PerformanceViewData data = new PerformanceViewData();
        data.setId(this.key);
        data.setIdType(this.type);
        data.setName(this.name);
        data.setViewType(viewType);
        data.setThSemUnauthStuCount(this.thSemUnauthStuCount);

        if (viewType == VIEW_TYPE_OVERVIEW_JUNIOR) {  // 小学概览
            data.setViewName("概览");
            // 区域概况
            data.setStuScale(this.juniorStuScale);
            data.setRegStuCount(this.juniorRegStuCount);
            data.setAuthStuCount(this.juniorAuthStuCount);

            // 注册认证
            data.setMonthRegStuCount(this.juniorMonthRegStuCount);
            data.setMonthAuthStuCount(this.juniorMonthAuthStuCount);
            data.setRegStuCountDf(this.juniorRegStuCountDf);
            data.setAuthStuCountDf(this.juniorAuthStuCountDf);

            // 1套到3套
            data.setFinHwEq1StuCount(this.juniorFinSglSubjHwEq1StuCount);
            data.setFinHwEq2StuCount(this.juniorFinSglSubjHwEq2StuCount);
            data.setFinHwGte3StuCount(this.juniorFinSglSubjHwGte3StuCount);

            // 月环比
            // 所有学校
            PerformanceViewDataLmRateItem allRate = new PerformanceViewDataLmRateItem();
            allRate.setName("所有学校");
            allRate.setMaucLmRate(calCompleteRate(this.juniorSglSubjMauc, this.juniorLmSglSubjMauc));
            allRate.setIncMaucLmRate(calCompleteRate(this.juniorSglSubjIncMauc, this.juniorLmSglSubjIncMauc));
            allRate.setLtMaucLmRate(calCompleteRate(this.juniorSglSubjLtBfMauc, this.juniorLmSglSubjLtBfMauc));
            allRate.setStMaucLmRate(calCompleteRate(this.juniorSglSubjStBfMauc, this.juniorLmSglSubjStBfMauc));
            data.getLmRateDataList().add(allRate);

            PerformanceViewDataLmRateItem lpRate = new PerformanceViewDataLmRateItem();
            lpRate.setName("低渗");
            lpRate.setMaucLmRate(calCompleteRate(this.juniorLpSglSubjMauc, this.juniorLmLpSglSubjMauc));
            lpRate.setIncMaucLmRate(calCompleteRate(this.juniorLpSglSubjIncMauc, this.juniorLmLpSglSubjIncMauc));
            lpRate.setLtMaucLmRate(calCompleteRate(this.juniorLpSglSubjLtBfMauc, this.juniorLmLpSglSubjLtBfMauc));
            lpRate.setStMaucLmRate(calCompleteRate(this.juniorLpSglSubjStBfMauc, this.juniorLmLpSglSubjStBfMauc));
            data.getLmRateDataList().add(lpRate);

            PerformanceViewDataLmRateItem mpRate = new PerformanceViewDataLmRateItem();
            mpRate.setName("中渗");
            mpRate.setMaucLmRate(calCompleteRate(this.juniorMpSglSubjMauc, this.juniorLmMpSglSubjMauc));
            mpRate.setIncMaucLmRate(calCompleteRate(this.juniorMpSglSubjIncMauc, this.juniorLmMpSglSubjIncMauc));
            mpRate.setLtMaucLmRate(calCompleteRate(this.juniorMpSglSubjLtBfMauc, this.juniorLmMpSglSubjLtBfMauc));
            mpRate.setStMaucLmRate(calCompleteRate(this.juniorMpSglSubjStBfMauc, this.juniorLmMpSglSubjStBfMauc));
            data.getLmRateDataList().add(mpRate);

            PerformanceViewDataLmRateItem hpRate = new PerformanceViewDataLmRateItem();
            hpRate.setName("高渗");
            hpRate.setMaucLmRate(calCompleteRate(this.juniorHpSglSubjMauc, this.juniorLmHpSglSubjMauc));
            hpRate.setIncMaucLmRate(calCompleteRate(this.juniorHpSglSubjIncMauc, this.juniorLmHpSglSubjIncMauc));
            hpRate.setLtMaucLmRate(calCompleteRate(this.juniorHpSglSubjLtBfMauc, this.juniorLmHpSglSubjLtBfMauc));
            hpRate.setStMaucLmRate(calCompleteRate(this.juniorHpSglSubjStBfMauc, this.juniorLmHpSglSubjStBfMauc));
            data.getLmRateDataList().add(hpRate);

            PerformanceViewDataLmRateItem spRate = new PerformanceViewDataLmRateItem();
            spRate.setName("超高渗");
            spRate.setMaucLmRate(calCompleteRate(this.juniorSpSglSubjMauc, this.juniorLmSpSglSubjMauc));
            spRate.setIncMaucLmRate(calCompleteRate(this.juniorSpSglSubjIncMauc, this.juniorLmSpSglSubjIncMauc));
            spRate.setLtMaucLmRate(calCompleteRate(this.juniorSpSglSubjLtBfMauc, this.juniorLmSpSglSubjLtBfMauc));
            spRate.setStMaucLmRate(calCompleteRate(this.juniorSpSglSubjStBfMauc, this.juniorLmSpSglSubjStBfMauc));
            data.getLmRateDataList().add(spRate);

            // 低渗学校

            // 目标达成情况
            // 小学单科指标
            PerformanceViewDataItem maucItem = new PerformanceViewDataItem();
            maucItem.setName("月活");
            maucItem.setMauc(this.juniorSglSubjMauc);
            maucItem.setMaucDf(this.juniorSglSubjMaucDf);
            maucItem.setMaucBudget(this.juniorSglSubjMaucBudget);
            maucItem.setMaucCompleteRate(calCompleteRate(this.juniorSglSubjMauc, this.juniorSglSubjMaucBudget));
            data.getDataItemList().add(maucItem);

            // 小学单科新增指标
            PerformanceViewDataItem incMaucItem = new PerformanceViewDataItem();
            incMaucItem.setName("新增");
            incMaucItem.setMauc(this.juniorSglSubjIncMauc);
            incMaucItem.setMaucDf(this.juniorSglSubjIncMaucDf);
            incMaucItem.setMaucBudget(this.juniorSglSubjIncMaucBudget);
            incMaucItem.setMaucCompleteRate(calCompleteRate(this.juniorSglSubjIncMauc, this.juniorSglSubjIncMaucBudget));
            data.getDataItemList().add(incMaucItem);

            // 小学单科长回指标
            PerformanceViewDataItem ltBfMaucItem = new PerformanceViewDataItem();
            ltBfMaucItem.setName("长回");
            ltBfMaucItem.setMauc(this.juniorSglSubjLtBfMauc);
            ltBfMaucItem.setMaucDf(this.juniorSglSubjLtBfMaucDf);
            ltBfMaucItem.setMaucBudget(this.juniorSglSubjLtBfMaucBudget);
            ltBfMaucItem.setMaucCompleteRate(calCompleteRate(this.juniorSglSubjLtBfMauc, this.juniorSglSubjLtBfMaucBudget));
            data.getDataItemList().add(ltBfMaucItem);

            // 小学单科短回指标
            PerformanceViewDataItem stBfMaucItem = new PerformanceViewDataItem();
            stBfMaucItem.setName("短回");
            stBfMaucItem.setMauc(this.juniorSglSubjStBfMauc);
            stBfMaucItem.setMaucDf(this.juniorSglSubjStBfMaucDf);
            stBfMaucItem.setMaucBudget(this.juniorSglSubjStBfMaucBudget);
            stBfMaucItem.setMaucCompleteRate(calCompleteRate(this.juniorSglSubjStBfMauc, this.juniorSglSubjStBfMaucBudget));
            data.getDataItemList().add(stBfMaucItem);
        } else if (viewType == VIEW_TYPE_OVERVIEW_MIDDLE) { // 初高中线上概览
            data.setViewName("概览");
            // 区域概况
            data.setStuScale(this.middleStuScale);
            data.setRegStuCount(this.middleRegStuCount);
            data.setAuthStuCount(this.middleAuthStuCount);

            // 注册认证
            data.setMonthRegStuCount(this.middleMonthRegStuCount);
            data.setMonthAuthStuCount(this.middleMonthAuthStuCount);
            data.setRegStuCountDf(this.middleRegStuCountDf);
            data.setAuthStuCountDf(this.middleAuthStuCountDf);

            // 1套到3套
            data.setFinHwEq1StuCount(this.middleFinEngHwEq1StuCount);
            data.setFinHwEq2StuCount(this.middleFinEngHwEq2StuCount);
            data.setFinHwGte3StuCount(this.middleFinEngHwGte3StuCount);

            // 初高中英语月活
            PerformanceViewDataItem engMaucItem = new PerformanceViewDataItem();
            engMaucItem.setMauc(this.middleEngMauc);
            engMaucItem.setMaucDf(this.middleEngMaucDf);
            engMaucItem.setIncMauc(this.middleEngIncMauc);
            engMaucItem.setBfMauc(this.middleEngBfMauc);
            data.getDataItemList().add(engMaucItem);

        } else if (viewType == VIEW_TYPE_OVERVIEW_MIDDLE_KLX) { // 初高中扫描概览
            data.setViewName("概览");

            data.setStuScale(this.middleStuScale);
            data.setStuKlxTnCount(this.stuKlxTnCount);

            // 低高标数据
            data.setLowAnshEq1StuCount(this.finLowAllSubjAnshEq1StuCount);
            data.setLowAnshGte2StuCount(this.finLowAllSubjAnshGte2StuCount);
            data.setHighAnshEq1StuCount(this.finHighAllSubjAnshEq1StuCount);
            data.setHighAnshGte2StuCount(this.finHighAllSubjAnshGte2StuCount);


            // 扫描数据
            // 数学
            PerformanceViewDataItem mathItem = new PerformanceViewDataItem();
            mathItem.setName("数学");
            mathItem.setAnshGte2StuCount(this.mathAnshGte2StuCount);
            mathItem.setAnshGte2IncStuCount(this.mathAnshGte2IncStuCount);
            mathItem.setAnshGte2BfStuCount(this.mathAnshGte2BfStuCount);
            mathItem.setAnshGte2StuCountDf(this.mathAnshGte2StuCountDf);
            data.getDataItemList().add(mathItem);

            // 副科
            PerformanceViewDataItem fuItem = new PerformanceViewDataItem();
            fuItem.setName("副科");
            fuItem.setAnshGte2StuCount(this.deputyAnshGet2StuCount);
            fuItem.setAnshGte2IncStuCount(this.deputyAnshGet2IncStuCount);
            fuItem.setAnshGte2BfStuCount(this.deputyAnshGet2BfStuCount);
            fuItem.setAnshGte2StuCountDf(this.deputyAnshGet2StuCountDf);
            data.getDataItemList().add(fuItem);

            // 其他
            PerformanceViewDataItem otherItem = new PerformanceViewDataItem();
            otherItem.setName("其他");
            otherItem.setAnshGte2StuCount(this.otherAnshGte2StuCount);
            otherItem.setAnshGte2IncStuCount(this.otherAnshGte2IncStuCount);
            otherItem.setAnshGte2BfStuCount(this.otherAnshGte2BfStuCount);
            otherItem.setAnshGte2StuCountDf(this.otherAnshGte2StuCountDf);
            data.getDataItemList().add(otherItem);

            // 全科
            PerformanceViewDataItem allItem = new PerformanceViewDataItem();
            allItem.setName("全科");
            allItem.setAnshGte2StuCount(this.allSubjAnshGte2StuCount);
            allItem.setAnshGte2IncStuCount(this.allSubjAnshGte2IncStuCount);
            allItem.setAnshGte2BfStuCount(this.allSubjAnshGte2BfStuCount);
            allItem.setAnshGte2StuCountDf(this.allSubjAnshGte2StuCountDf);
            data.getDataItemList().add(allItem);

        }

        // 完成数在各种渗透情形下的 月活，新增，长回，短回，注册认证，1套到3套数据
        else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_MAUC) {  // 全部
            data.setMaucData("月活", this.juniorSglSubjMauc, this.juniorSglSubjMaucDf, this.juniorSglSubjMaucBudget, calCompleteRate(this.juniorSglSubjMauc, this.juniorSglSubjMaucBudget));
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_INC_MAUC) {  // 全部
            data.setMaucData("新增", this.juniorSglSubjIncMauc, this.juniorSglSubjIncMaucDf, this.juniorSglSubjIncMaucBudget, calCompleteRate(this.juniorSglSubjIncMauc, this.juniorSglSubjIncMaucBudget));
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_LTBF_MAUC) {  // 全部
            data.setMaucData("长回", this.juniorSglSubjLtBfMauc, this.juniorSglSubjLtBfMaucDf, this.juniorSglSubjLtBfMaucBudget, calCompleteRate(this.juniorSglSubjLtBfMauc, this.juniorSglSubjLtBfMaucBudget));
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_STBF_MAUC) {  // 全部
            data.setMaucData("短回", this.juniorSglSubjStBfMauc, this.juniorSglSubjStBfMaucDf, this.juniorSglSubjStBfMaucBudget, calCompleteRate(this.juniorSglSubjStBfMauc, this.juniorSglSubjStBfMaucBudget));
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_REG_AUTH) { // 全部
            data.setRegAndAuthData("注册认证", this.juniorMonthRegStuCount, this.juniorMonthAuthStuCount, this.juniorRegStuCountDf, this.juniorAuthStuCountDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_PROCESS) { // 全部
            data.setFinHwData("1套到3套", this.juniorFinSglSubjHwEq1StuCount, this.juniorFinSglSubjHwEq2StuCount, this.juniorFinSglSubjHwGte3StuCount);
        }

        else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_MAUC) {  // 低渗
            data.setMaucData("月活", this.juniorLpSglSubjMauc, this.juniorLpSglSubjMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_INC_MAUC) {  // 低渗
            data.setMaucData("新增", this.juniorLpSglSubjIncMauc, this.juniorLpSglSubjIncMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_LTBF_MAUC) {  // 低渗
            data.setMaucData("长回", this.juniorLpSglSubjLtBfMauc, this.juniorLpSglSubjLtBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_STBF_MAUC) {  // 低渗
            data.setMaucData("短回", this.juniorLpSglSubjStBfMauc, this.juniorLpSglSubjStBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_REG_AUTH) { // 低渗
            data.setRegAndAuthData("注册认证", this.juniorLpMonthRegStuCount, this.juniorLpMonthAuthStuCount, this.juniorLpRegStuCountDf, this.juniorLpAuthStuCountDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_LP_PROCESS) { // 低渗
            data.setFinHwData("1套到3套", this.juniorLpFinSglSubjHwEq1StuCount, this.juniorLpFinSglSubjHwEq2StuCount, this.juniorLpFinSglSubjHwGte3StuCount);
        }

        else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_MAUC) {  // 中渗
            data.setMaucData("月活", this.juniorMpSglSubjMauc, this.juniorMpSglSubjMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_INC_MAUC) {  // 中渗
            data.setMaucData("新增", this.juniorMpSglSubjIncMauc, this.juniorMpSglSubjIncMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_LTBF_MAUC) {  // 中渗
            data.setMaucData("长回", this.juniorMpSglSubjLtBfMauc, this.juniorMpSglSubjLtBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_STBF_MAUC) {  // 中渗
            data.setMaucData("短回", this.juniorMpSglSubjStBfMauc, this.juniorMpSglSubjStBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_REG_AUTH) { // 中渗
            data.setRegAndAuthData("注册认证", this.juniorMpMonthRegStuCount, this.juniorMpMonthAuthStuCount, this.juniorMpRegStuCountDf, this.juniorMpAuthStuCountDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_MP_PROCESS) { // 中渗
            data.setFinHwData("1套到3套", this.juniorMpFinSglSubjHwEq1StuCount, this.juniorMpFinSglSubjHwEq2StuCount, this.juniorMpFinSglSubjHwGte3StuCount);
        }

        else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_MAUC) {  // 高渗
            data.setMaucData("月活", this.juniorHpSglSubjMauc, this.juniorHpSglSubjMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_INC_MAUC) {  // 高渗
            data.setMaucData("新增", this.juniorHpSglSubjIncMauc, this.juniorHpSglSubjIncMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_LTBF_MAUC) {  // 高渗
            data.setMaucData("长回", this.juniorHpSglSubjLtBfMauc, this.juniorHpSglSubjLtBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_STBF_MAUC) {  // 高渗
            data.setMaucData("短回", this.juniorHpSglSubjStBfMauc, this.juniorHpSglSubjStBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_REG_AUTH) { // 高渗
            data.setRegAndAuthData("注册认证", this.juniorHpMonthRegStuCount, this.juniorHpMonthAuthStuCount, this.juniorHpRegStuCountDf, this.juniorHpAuthStuCountDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_HP_PROCESS) { // 高渗
            data.setFinHwData("1套到3套", this.juniorHpFinSglSubjHwEq1StuCount, this.juniorHpFinSglSubjHwEq2StuCount, this.juniorHpFinSglSubjHwGte3StuCount);
        }

        else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_MAUC) {  // 超高渗
            data.setMaucData("月活", this.juniorSpSglSubjMauc, this.juniorSpSglSubjMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_INC_MAUC) {  // 超高渗
            data.setMaucData("新增", this.juniorSpSglSubjIncMauc, this.juniorSpSglSubjIncMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_LTBF_MAUC) {  // 超高渗
            data.setMaucData("长回", this.juniorSpSglSubjLtBfMauc, this.juniorSpSglSubjLtBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_STBF_MAUC) {  // 超高渗
            data.setMaucData("短回", this.juniorSpSglSubjStBfMauc, this.juniorSpSglSubjStBfMaucDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_REG_AUTH) { // 超高渗
            data.setRegAndAuthData("注册认证", this.juniorSpMonthRegStuCount, this.juniorSpMonthAuthStuCount, this.juniorSpRegStuCountDf, this.juniorSpAuthStuCountDf);
        } else if (viewType == VIEW_TYPE_PERFORMANCE_JUNIOR_SP_PROCESS) { // 超高渗
            data.setFinHwData("1套到3套", this.juniorSpFinSglSubjHwEq1StuCount, this.juniorSpFinSglSubjHwEq2StuCount, this.juniorSpFinSglSubjHwGte3StuCount);
        }

        // 月环比在各种渗透情形下的 月活，新增，长回，短回数据
        else if(viewType == VIEW_TYPE_LM_JUNIOR_SGLSUBJ_MAUC){   // 月环比（全部）
            data.setLmRateData("月活", this.juniorSglSubjMauc, this.juniorLmSglSubjMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SGLSUBJ_INC_MAUC){   // 月环比（全部）
            data.setLmRateData("新增", this.juniorSglSubjIncMauc, this.juniorLmSglSubjIncMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SGLSUBJ_LTBF_MAUC){   // 月环比（全部）
            data.setLmRateData("长回", this.juniorSglSubjLtBfMauc, this.juniorLmSglSubjLtBfMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SGLSUBJ_STBF_MAUC){   // 月环比（全部）
            data.setLmRateData("短回", this.juniorSglSubjStBfMauc, this.juniorLmSglSubjStBfMauc);
        }

        else if(viewType == VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_MAUC){   // 月环比（低渗）
            data.setLmRateData("月活", this.juniorLpSglSubjMauc, this.juniorLmLpSglSubjMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_INC_MAUC){   // 月环比（低渗）
            data.setLmRateData("新增", this.juniorLpSglSubjIncMauc, this.juniorLmLpSglSubjIncMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_LTBF_MAUC){   // 月环比（低渗）
            data.setLmRateData("长回", this.juniorLpSglSubjLtBfMauc, this.juniorLmLpSglSubjLtBfMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_STBF_MAUC){   // 月环比（低渗）
            data.setLmRateData("短回", this.juniorLpSglSubjStBfMauc, this.juniorLmLpSglSubjStBfMauc);
        }

        else if(viewType == VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_MAUC){   // 月环比（中渗）
            data.setLmRateData("月活", this.juniorMpSglSubjMauc, this.juniorLmMpSglSubjMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_INC_MAUC){   // 月环比（中渗）
            data.setLmRateData("新增", this.juniorMpSglSubjIncMauc, this.juniorLmMpSglSubjIncMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_LTBF_MAUC){   // 月环比（中渗）
            data.setLmRateData("长回", this.juniorMpSglSubjLtBfMauc, this.juniorLmMpSglSubjLtBfMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_STBF_MAUC){   // 月环比（中渗）
            data.setLmRateData("短回", this.juniorMpSglSubjStBfMauc, this.juniorLmMpSglSubjStBfMauc);
        }

        else if(viewType == VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_MAUC){   // 月环比（高渗）
            data.setLmRateData("月活", this.juniorHpSglSubjMauc, this.juniorLmHpSglSubjMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_INC_MAUC){   // 月环比（高渗）
            data.setLmRateData("新增", this.juniorHpSglSubjIncMauc, this.juniorLmHpSglSubjIncMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_LTBF_MAUC){   // 月环比（高渗）
            data.setLmRateData("长回", this.juniorHpSglSubjLtBfMauc, this.juniorLmHpSglSubjLtBfMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_STBF_MAUC){   // 月环比（高渗）
            data.setLmRateData("短回", this.juniorHpSglSubjStBfMauc, this.juniorLmHpSglSubjStBfMauc);
        }

        else if(viewType == VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_MAUC){   // 月环比（超高渗）
            data.setLmRateData("月活", this.juniorSpSglSubjMauc, this.juniorLmSpSglSubjMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_INC_MAUC){   // 月环比（超高渗）
            data.setLmRateData("新增", this.juniorSpSglSubjIncMauc, this.juniorLmSpSglSubjIncMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_LTBF_MAUC){   // 月环比（超高渗）
            data.setLmRateData("长回", this.juniorSpSglSubjLtBfMauc, this.juniorLmSpSglSubjLtBfMauc);
        }else if(viewType == VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_STBF_MAUC){   // 月环比（超高渗）
            data.setLmRateData("短回", this.juniorSpSglSubjStBfMauc, this.juniorLmSpSglSubjStBfMauc);
        }


        // 初高中线上英语月活， 注册认证， 1套到3套数据
        else if(viewType == VIEW_TYPE_PERFORMANCE_MIDDLE_ENG_MAUC){
            data.setMaucData("英语月活", this.middleEngMauc, this.middleEngIncMauc, this.middleEngBfMauc, this.middleEngMaucDf);
        }else if(viewType == VIEW_TYPE_PERFORMANCE_MIDDLE_REG_AUTH){
            data.setRegAndAuthData("注册认证", this.middleMonthRegStuCount, this.middleMonthAuthStuCount, this.middleRegStuCountDf, this.middleAuthStuCountDf);
        }else if(viewType == VIEW_TYPE_PERFORMANCE_MIDDLE_PROCESS){
            data.setFinHwData("1套到3套", this.middleFinEngHwEq1StuCount, this.middleFinEngHwEq2StuCount, this.middleFinEngHwGte3StuCount);
        }

        // 初高中扫描低高标，扫描数据
        else if(viewType == VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_PROCESS){
            data.setLowAndHighData("本月扫描情况", this.finLowAllSubjAnshEq1StuCount, this.finLowAllSubjAnshGte2StuCount, this.finHighAllSubjAnshEq1StuCount, this.finHighAllSubjAnshGte2StuCount);
        }else if(viewType == VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_ALL_SUBJ){
            data.setAnshData("高标≥2明细", this.allSubjAnshGte2StuCount, this.allSubjAnshGte2IncStuCount, this.allSubjAnshGte2BfStuCount, this.allSubjAnshGte2StuCountDf);
        }

        else if(viewType == VIET_TYPE_SCHOOL_LIST_JUNIOR){         // 小学学校列表
            data.setViewName("小学学校列表");
            data.setStuScale(this.juniorStuScale);
            data.setRegStuCount(this.juniorRegStuCount);
            data.setAuthStuCount(this.juniorAuthStuCount);
            data.setMauc(this.juniorSglSubjMauc);
            data.setMaucDf(this.juniorSglSubjMaucDf);
            data.setRegStuCountDf(this.juniorRegStuCountDf);
        } else if(viewType == VIET_TYPE_SCHOOL_LIST_MIDDLE){        // 初高中学校列表
            data.setViewName("初高中学校列表");
            data.setStuScale(this.middleStuScale);
            data.setStuKlxTnCount(this.stuKlxTnCount);
            data.setAnshGte2StuCount(this.allSubjAnshGte2StuCount);
            data.setAnshGte2StuCountDf(this.allSubjAnshGte2StuCountDf);
            data.setEngHwGte3AuStuCount(this.middleEngMauc);
            data.setEngHwGte3AuStuCountDf(this.middleEngMaucDf);
        }

        return data;
    }

}
