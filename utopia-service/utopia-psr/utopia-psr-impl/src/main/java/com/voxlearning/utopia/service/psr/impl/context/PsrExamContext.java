/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.context;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.random.RandomProvider;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class PsrExamContext {
    @Getter
    @Setter
    private PsrConfig psrConfig;

    /************************************************************************************/
    // 用户参数列表
    private String product;
    private String uType;
    private Long userId;
    private String bookId;
    private String unitId;
    private float minP;
    private float maxP;
    private int eCount;
    private int regionCode;
    private String eType;    // AppEn 知识点题型
    private int matchCount;  // AppEnMatch 知识点配错个数
    private List<String> requiredEids; // 类题请求数据列表,取出每个eid的类题
    private int grade; // 年级
    private Subject subject;
    private boolean isModuleId;


    /************************************************************************************/
    // 依赖数据项
    //private PsrBookPersistence psrBookPersistence;               // 教材结构
    //private List<UnitKnowledgePointRef> unitKnowledgePointRefs;  // 课本信息数据

    private UserExamContent userExamContentId;          // 应试 用户的learning profile,新知识点题型,知识点已经用Id表示
    private UserExamUcContent userExamUcContentId;      // 应试 用户Uc数据,新知识点体系id表示

    private UserExamQuestionResultInfo userExamQuestionResultInfo;  // 用户当天的做题数据,处理后的数据
    private PsrUserHistoryEid psrUserHistoryEid;                    // 用户的历史做题数据

    private UserExamEnWrongContent userExamEnWrongContent;       // 错题推荐接口使用数据,fixme 保留(错题逻辑暂停了)

    ///////////////////////////////////////////////
    private PsrBookPersistenceNew psrBookPersistenceNew;  // 教材结构

    /************************************************************************************/
    // 本次推荐的状态信息
    private String errorMsg;            // 本次的推题的错误信息
    private String psrExamType;         // 记录psr的推荐逻辑,确定是那个任务推出来的题
    private List<String> recommendEids; // 本次的推题记录,排重使用(不推重复的题)
    private EkEidListContent ekEidListContent;

    private EkFilter ekFilter;          // 知识点过滤结构
    private EidFilter eidFilter;        // 题id过滤结构
    // // FIXME: 2017/6/28   // TODO: 2017/6/28
    //private List<KeyValuePair<String, EidItem>> retainList; // 保存知识点还剩多少题可用,在默认补充题目之前优先推荐
    private List<KeyValuePair<String, EidItem>> recommendedList; // 保存之前单元推荐过的题目,在默认补充题目之前优先推荐

    /************************************************************************************/
    // 系统参数列表
    private float defaultMinP;       // 默认的minP,预估通过率的下限范围,预估通过率越低说明题的难度越高
    private float defaultMaxP;       // 默认的maxP,预估通过率的上限范围
    private double highIrtTheta;     // 用户的能力值范围,超过该能力值的认为是 高能力的学生,因此推题策略有所不同
    private double lowIrtTheta;      // 用户的能力值范围,低于该能力值的认为是 低能力的学生,在high和low之间的认为是中能力的同学
    private double eidCountRatePerEk;   // 应试中,用来分配每个Ek对应的eid的个数,eid个数越多则ek的个数就会变少,因为总的eid个数是固定的
    private double downSetRangeHigh;    // 用户对某个知识点掌握程度>该值 则认为已经掌握该知识点,则本次不推荐
    private double downSetRangeLow;     // 用户对某个知识点掌握程度<该值 则认为该知识点太难,则本次不推荐
    private int ekCountByUserDone;      // 用户对某个知识点做的次数,若<3(默认)次则认为改用户没有掌握该知识点
    private int minNotAboveLevelEids;   // 不超纲题目数量 小于 该值 则记录日志
    private int baseNumberForWeight;    // 计算归一化的时候所使用的百分数
    private int maxEidCount;            // 推荐系统允许推荐的最大题目数量
    private int useDefaultMinPMaxP;     // 是否使用默认的MinP和MaxP,这两个值在配置文件中可随时修改
    private boolean isFilterByBook;     // 是否按照Book过滤
    private boolean isFilterByUnit;     // 是否按照Unit过滤
    private boolean isExamEnUseAdapt;   // 是否启用 text(ek) 适配, 启用则在题不够的情况下 扩充 历史上做过的题
    //private boolean isPointId;     // 标示 新旧知识点体系, true 新知识点 并且使用 pointid, false 旧知识点, fixme 新版已启用使用Id形式
    private boolean isAdaptive;      // 是否为自适应算法
    private boolean isWriteLog;      // 默认打印日志,否则不打印日志,内部调用的时候使用
    private boolean isFilterFromOnlineQuestion; // 是否实时查询xx_online_question判断eid是否有效,离线数据不稳定的时候使用

    private String examinationGroupId;  // mysql 数据库中 测验id, fixme 保留(测验逻辑暂停了)
    private boolean isExamTestFiveEid;  // 是否进行测验题及练习题Test, fixme 保留(该逻辑暂停了)
    private boolean isExamination;      // 是否进行考试测验, fixme 保留(该逻辑暂停了)
    private boolean isUserGroup;        // 是否开启用户分组功能, fixme 保留(分组逻辑暂停了)
    private boolean isExamEnAfentiQuiz; // 是否启用afenti教研员版, fixme 保留(该逻辑暂停了)

    private Integer eidPsrDays;         // 推题的间隔时间,默认3天
    private double minPDown;            // 当用户有learning_profile 或者 有 Uc值时向下扩充知识点设定的范围(不能推minP很低的难题)
    private double gPredictWeight;      // 设置预估通过率算法的权重值

    private Integer debugLogLevel;      // 调试日志选项,从psr_config 中获取 0:关闭
    private Integer aboveLevelErrorCount;   // 超纲出问题的知识点个数,当>=3个知识点超纲有问题(知识点下挂载的Eid个数==超纲列表个数,换言之所有的Eid都被超纲过滤了),则自动设置isAdaptive启用自适应算法

    private Random random;                  // 每次请求分配一个Random

    private long thresholdValue;           // 保留threshold内推荐次数，默认1天

    private Integer adaptivePaperLevel;    // 0: 不适用paper补题, 1:使用paper补题, 2: 部分教材使用paper补题,需查看那些教材可以使用paper补题

    private Integer adaptiveDefaultPreUnitLevel;    // 0: 不补充推荐过的题目, 1:补充之前单元推荐过的题目

    public PsrExamContext(String product, String uType,
                          Long userId, int regionCode, String bookId, String unitId, int eCount,
                          float minP, float maxP) {
        init();
        setPara(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, 3);
    }

    public PsrExamContext(String product, String uType,
                          Long userId, int regionCode, String bookId, String unitId, int eCount,
                          float minP, float maxP, int grade) {
        init();
        setPara(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
    }

    public PsrExamContext(PsrConfig psrConfig, String product, String uType,
                          Long userId, int regionCode, String bookId, String unitId, int eCount,
                          float minP, float maxP) {
        init();
        setPara(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, 3);
        setPsrConfig(psrConfig);
        setConfigToContext();
    }

    public PsrExamContext(PsrConfig psrConfig, String product, String uType,
                          Long userId, int regionCode, String bookId, String unitId, int eCount,
                          float minP, float maxP, int grade) {
        init();
        setPara(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
        setPsrConfig(psrConfig);
        setConfigToContext();
    }

    public void init() {
        product = "17zuoye";
        uType = "student";
        userId = 0L;
        bookId = "0";
        unitId = "-1";
        minP = 0.7f;
        maxP = 0.85f;
        eCount = 5;
        regionCode = 0;
        grade = 3;
        subject = Subject.ENGLISH;
        isModuleId = false;

        eType = "";
        matchCount = 4;

        //psrBookPersistence = null;
        userExamContentId = null;
        userExamQuestionResultInfo = null;
        //unitKnowledgePointRefs = null;
        userExamUcContentId = null;
        psrUserHistoryEid = null;
        userExamEnWrongContent = null;

        psrBookPersistenceNew = null;

        eidFilter = null;
        ekFilter = null;
        recommendEids = new ArrayList<>();
        ekEidListContent = new EkEidListContent();
        recommendedList = new ArrayList<>();

        errorMsg = "success";
        examinationGroupId = "";
        psrExamType = "psrexam";

        defaultMinP = 0.7f;
        defaultMaxP = 0.85f;
        highIrtTheta = 1.5D;
        lowIrtTheta = 0.0D;
        eidCountRatePerEk = 0.4D;
        downSetRangeHigh = 0.85D;
        downSetRangeLow = 0.35D;
        ekCountByUserDone = 3;
        minNotAboveLevelEids = 5;
        baseNumberForWeight = 100;
        maxEidCount = 50;
        useDefaultMinPMaxP = 0;
        isFilterByBook = true;
        isFilterByUnit = true;
        isExamEnUseAdapt = true;
        //isPointId = true;
        isAdaptive = false;
        isWriteLog = true;
        isFilterFromOnlineQuestion = true;

        isExamination = false;
        isExamTestFiveEid = false;
        isUserGroup = false;
        isExamEnAfentiQuiz = true;

        eidPsrDays = 3;

        minPDown = 0.05D;
        gPredictWeight = 0.7D;

        debugLogLevel = 0;
        aboveLevelErrorCount = 0;

        random = RandomProvider.getInstance().getRandom();

        thresholdValue = 86400L; // 一天

        adaptivePaperLevel = 1; // 默认使用paper补题
        adaptiveDefaultPreUnitLevel = 1; // 默认使用推荐过的题目补充
    }

    private void setPara(String product, String uType,
                         Long userId, int regionCode, String bookId, String unitId, int eCount,
                         float minP, float maxP, int grade) {

        this.product = product;
        this.uType = uType;
        this.userId = userId;
        this.regionCode = regionCode;
        this.bookId = bookId;
        this.unitId = unitId;
        this.eCount = eCount;
        this.minP = minP;
        this.maxP = maxP;
        if (grade >= 1 && grade <= 6)
            this.grade = grade;
    }

    // 根据配置文件更新参数
    private void setConfigToContext() {
        if (psrConfig == null)
            return;

        if (psrConfig.containsKey("lowirttheta"))
            this.setLowIrtTheta(psrConfig.getDoubleValue("lowirttheta"));
        if (psrConfig.containsKey("highirttheta"))
            this.setHighIrtTheta(psrConfig.getDoubleValue("highirttheta"));
        if (psrConfig.containsKey("eidcountrateperek"))
            this.setEidCountRatePerEk(psrConfig.getDoubleValue("eidcountrateperek"));
        if (psrConfig.containsKey("downsetrange"))
            this.setDownSetRangeHigh(psrConfig.getDoubleValue("downsetrange"));
        if (psrConfig.containsKey("downsetrangelow"))
            this.setDownSetRangeLow(psrConfig.getDoubleValue("downsetrangelow"));
        if (psrConfig.containsKey("ekcountbyuserdone"))
            this.setEkCountByUserDone(psrConfig.getIntegerValue("ekcountbyuserdone"));
        if (psrConfig.containsKey("basenumberforweight"))
            this.setBaseNumberForWeight(psrConfig.getIntegerValue("basenumberforweight"));
        if (psrConfig.containsKey("minnotaboveleveleids"))
            this.setMinNotAboveLevelEids(psrConfig.getIntegerValue("minnotaboveleveleids"));
        if (psrConfig.containsKey("maxeidcount"))
            this.setMaxEidCount(psrConfig.getIntegerValue("maxeidcount"));
        if (psrConfig.containsKey("usedefaultminpmaxp"))
            this.setUseDefaultMinPMaxP(psrConfig.getIntegerValue("usedefaultminpmaxp"));
        if (psrConfig.containsKey("minp"))
            this.setDefaultMinP(psrConfig.getDoubleValue("minp").floatValue());
        if (psrConfig.containsKey("maxp"))
            this.setDefaultMaxP(psrConfig.getDoubleValue("maxp").floatValue());
        if (psrConfig.containsKey("eidpsrdays"))
            this.setEidPsrDays(psrConfig.getIntegerValue("eidpsrdays") >= 10 ? psrConfig.getIntegerValue("eidpsrdays") : 10);
        if (psrConfig.containsKey("minpdown"))
            this.setMinPDown(psrConfig.getDoubleValue("minpdown"));
        if (psrConfig.containsKey("gpredictweight"))
            this.setGPredictWeight(psrConfig.getDoubleValue("gpredictweight"));
        if (psrConfig.containsKey("isexamination")) {
            if (0 != psrConfig.getIntegerValue("isexamination"))
                isExamination = true;
        }
        if (psrConfig.containsKey("isexamtestfiveeid")) {
            if (0 != psrConfig.getIntegerValue("isexamtestfiveeid"))
                isExamTestFiveEid = true;
        }
        if (psrConfig.containsKey("isusergroup")) {
            if (0 != psrConfig.getIntegerValue("isusergroup"))
                isUserGroup = true;
        }
        if (psrConfig.containsKey("isexamenuseadapt")) {
            if (0 != psrConfig.getIntegerValue("isexamenuseadapt"))
                isExamEnUseAdapt = true;
        }
        if (psrConfig.containsKey("isexamenafentiquiz")) {
            if (0 == psrConfig.getIntegerValue("isexamenafentiquiz"))
                isExamEnAfentiQuiz = false;
        }
        if (psrConfig.containsKey("isfilterfromquestion")) {
            if (0 == psrConfig.getIntegerValue("isfilterfromquestion"))
                isFilterFromOnlineQuestion = false;
        }

        if (psrConfig.containsKey("examinationgroup"))
            examinationGroupId = psrConfig.getStringValue("examinationgroup");
        if (psrConfig.containsKey("debugloglevel"))
            debugLogLevel = psrConfig.getIntegerValue("debugloglevel");

        eCount = eCount > maxEidCount ? maxEidCount : eCount;

        setMinMaxP(minP, maxP);

        if (psrConfig.containsKey("thresholdvalue"))
            this.setThresholdValue(psrConfig.getLongValue("thresholdvalue"));

        if (psrConfig.containsKey("adaptivepaperlevel"))
            this.setAdaptivePaperLevel(psrConfig.getIntegerValue("adaptivepaperlevel"));

        if (psrConfig.containsKey("adaptivedefaultpreunitlevel"))
            this.setAdaptiveDefaultPreUnitLevel(psrConfig.getIntegerValue("adaptivedefaultpreunitlevel"));
    }

    /*
     * 是否使用默认的难度区间
     */
    public void setMinMaxP(float minP, float maxP) {
        if (minP > 0 && maxP > 0 && minP < maxP && maxP <= 1) {
            this.setMinP(minP);
            this.setMaxP(maxP);
        } else {
            this.setMinP(this.getDefaultMinP());
            this.setMaxP(this.getDefaultMaxP());
        }
        if (this.getUseDefaultMinPMaxP() != 0 && this.getDefaultMinP() > 0
                && this.getDefaultMaxP() > 0 && this.getDefaultMinP() < this.getDefaultMaxP()
                && this.getDefaultMaxP() <= 1) {
            this.setMinP(this.getDefaultMinP());
            this.setMaxP(this.getDefaultMaxP());
        }
    }
}
