package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.persist.entity.SchoolDayIncreaseData;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 每日维度的地区绩效信息表
 * <p>
 * Created by alex on 2016/5/19.
 */
@Data
@UtopiaCacheRevision("20170517")
public class DailyPerformanceInfo implements Serializable {

    private Integer regionCode;        // type = region  required
    private Integer date;              // 日期

    private Long addStuRegNum = 0L;         //新增学生注册数
    private Long addStuAuthNum = 0L;        //新增学生认证数
    private Long addTeaRegNum = 0L;         //新增老师注册数
    private Long addTeaAuthNum = 0L;        //新增老师认证数

    private Long monthStuRegNum = 0L;           //月新增学生注册数
    private Long monthStuAuthNum = 0L;          //月新增学生认证数


    @Deprecated private Integer middleMathMauc = 0;              // 初中数学月活 20+10模式
    @Deprecated private Integer middleMathMaucDf = 0;            // 初中数学月活日浮 20+10模式
    @Deprecated private Long middleMathMaucBudget = 0L;             // 初中数学月活预算 20+10模式

    @Deprecated private Integer middleMath2Mauc = 0;              // 初中数学月活 2+2模式
    @Deprecated private Integer middleMath2MaucDf = 0;            // 初中数学月活日浮 2+2模式

    @Deprecated private Integer highMathMauc = 0;                // 高中数学月活 20+10模式
    @Deprecated private Integer highMathMaucDf = 0;              // 高中数学月活日浮 20+10模式
    @Deprecated private Long highMathMaucBudget = 0L;               // 高中数学月活预算 20+10模式

    @Deprecated private Integer highMath2Mauc = 0;                // 高中数学月活 2+2模式
    @Deprecated private Integer highMath2MaucDf = 0;              // 高中数学月活日浮 2+2模式

    // 20170511    只有小学单科有预算，  传统作业中的 小学英语， 小学数学， 小学语文， 中学英语 都没有预算
    private int juniorEngMauc;        // 小学英语月活
    private int juniorEngMaucDf;    // 小学英语月活日浮
    private int juniorEngMaucBudget;          // 小学英语月活预算
    private int juniorEngAddMauc;       // 小学英语新增月活
    private int juniorEngAddMaucDf;     // 小学英语新增月活日浮
    private int juniorEngAddMaucBudget;  // 小学英语新增月活预算
    private int juniorEngBfMauc;        // 小学英语回流月活
    private int juniorEngBfMaucDf;        // 小学英语回流月活日浮
    private int juniorEngBfMaucBudget;              // 小学英语回流月活预算

    // 小学数学
    private int juniorMathMauc;        // 小学数学月活
    private int juniorMathMaucDf;    // 小学数学月活日浮
    private int juniorMathMaucBudget;          // 小学数学月活预算
    private int juniorMathAddMauc;  // 小学数学新增月活
    private int juniorMathAddMaucDf; // 小学数学新增月活日浮
    private int juniorMathAddMaucBudget;  // 小学数学新增月活预算
    private int juniorMathBfMauc;   // 小学数学回流月活
    private int juniorMathBfMaucDf; // 小学数学回流月活日浮
    private int juniorMathBfMaucBudget; // 小学数学回流月活预算

    // 小学语文
    private int juniorChnMauc; // 小学语文月活
    private int juniorChnMaucDf; // 小学语文月活日浮
    private int juniorChnMaucBudget; // 小学语文月活预算
    private int juniorChnAddMauc;  // 小学数学新增月活
    private int juniorChnAddMaucDf; // 小学数学新增月活日浮
    private int juniorChnAddMaucBudget;  // 小学语文新增月活预算
    private int juniorChnBfMauc;   // 小学数学回流月活
    private int juniorChnBfMaucDf; // 小学数学回流月活日浮
    private int juniorChnBfMaucBudget; // 小学数学回流月活预算

    // 小学单科
    private int juniorSglSubjMauc;  // 小学单科月活
    private int juniorSglSubjMaucDf;  // 小学单科月活日浮
    private int juniorSglSubjMaucBudget; // 小学单科月活预算
    private int juniorSglSubjAddMauc; // 小学单科新增月活
    private int juniorSglSubjAddMaucDf; // 小学单科新增月活日浮
    private int juniorSglSubjAddMaucBudget; // 小学单科新增月活预算
    private int juniorSglSubjBfMauc;  // 小学单科回流月活
    private int juniorSglSubjBfMaucDf; // 小学单科回流月活日浮
    private int juniorSglSubjBfMaucBudget; // 小学单科回流月活预算


    // 初中英语
    private int middleEngMauc;        // 初中英语月活
    private int middleEngMaucDf;    // 初中英语月活日浮
    private int middleEngMaucBudget;          // 初中英语月活预算
    private int middleEngAddMauc;    // 初中英语新增月活
    private int middleEngAddMaucDf;  // 初中英语新增月活日浮
    private int middleEngAddMaucBudget;    // 初中英语新增月活预算
    private int middleEngBfMauc;        // 初中英语回流月活
    private int middleEngBfMaucDf;        // 初中英语回流月活日浮
    private int middleEngBfMaucBudget;              // 初中英语回流月活预算

    // 初中数学
    private int middleEliteMathAddMauc;  // 初中数学新增月活（名校）
    private int middleEliteMathAddMaucDf; // 初中数学新增月活日浮（名校）
    private int middleEliteMathBfMauc;   // 初中数学回流月活（名校）
    private int middleEliteMathBfMaucDf; // 初中数学回流月活日浮（名校）

    private int middleKeyMathAddMauc;  // 初中数学新增月活（重点校）
    private int middleKeyMathAddMaucDf; // 初中数学新增月活日浮（重点校）
    private int middleKeyMathBfMauc;   // 初中数学回流月活（重点校）
    private int middleKeyMathBfMaucDf; // 初中数学回流月活日浮（重点校）

    // 初中数学扫描数大于等于1次的数据
    private int middleMathScanGte1StuCount;// 初中数学扫描大于等于1次的学生数
    private int middleMathScanGte1StuCountDf;// 初中数学扫描大于等于1次的学生数日浮
    private int middleMathScanGte1AddStuCount; // 初中数学扫描大于等于1次的新增学生数
    private int middleMathScanGte1AddStuCountDf;// 初中数学扫描大于等于1次的新增学生数日浮
    private int middleMathScanGte1BfStuCount;// 初中数学扫描大于等于1次的回流学生数
    private int middleMathScanGte1BfStuCountDf;// 初中数学扫描大于等于1次的回流学生数日浮

    // 初中数学扫描数大于等于2次的数据
    private int middleMathScanGte2StuCount;// 初中数学扫描大于等于2次的学生数
    private int middleMathScanGte2StuCountDf;// 初中数学扫描大于等于2次的学生数日浮
    private int middleMathScanGte2AddStuCount;// 初中数学扫描大于等于2次的新增学生数
    private int middleMathScanGte2AddStuCountDf;// 初中数学扫描大于等于2次的新增学生数日浮
    private int middleMathScanGte2BfStuCount;// 初中数学扫描大于等于2次的回流学生数
    private int middleMathScanGte2BfStuCountDf;// 初中数学扫描大于等于2次的回流学生数日浮

    // 高中数学
    private int highEliteMathAddMauc;  // 高中数学新增月活（名校）
    private int highEliteMathAddMaucDf; // 高中数学新增月活日浮（名校）
    private int highEliteMathBfMauc;   // 高中数学回流月活（名校）
    private int highEliteMathBfMaucDf; // 高中数学回流月活日浮（名校）

    private int highKeyMathAddMauc;  // 高中数学新增月活（重点校）
    private int highKeyMathAddMaucDf; // 高中数学新增月活日浮（重点校）
    private int highKeyMathBfMauc;   // 高中数学回流月活（重点校）
    private int highKeyMathBfMaucDf; // 高中数学回流月活日浮（重点校）

    // 高中数学扫描数大于等于1次的数据
    private int highMathScanGte1StuCount;// 高中数学扫描大于等于1次的学生数
    private int highMathScanGte1StuCountDf;// 高中数学扫描大于等于1次的学生数日浮
    private int highMathScanGte1AddStuCount; // 高中数学扫描大于等于1次的新增学生数
    private int highMathScanGte1AddStuCountDf;// 高中数学扫描大于等于1次的新增学生数日浮
    private int highMathScanGte1BfStuCount;// 高中数学扫描大于等于1次的回流学生数
    private int highMathScanGte1BfStuCountDf;// 高中数学扫描大于等于1次的回流学生数日浮

    // 高中数学扫描数大于等于2次的数据
    private int highMathScanGte2StuCount;// 高中数学扫描大于等于2次的学生数
    private int highMathScanGte2StuCountDf;// 高中数学扫描大于等于2次的学生数日浮
    private int highMathScanGte2AddStuCount;// 高中数学扫描大于等于2次的新增学生数
    private int highMathScanGte2AddStuCountDf;// 高中数学扫描大于等于2次的新增学生数日浮
    private int highMathScanGte2BfStuCount;// 高中数学扫描大于等于2次的回流学生数
    private int highMathScanGte2BfStuCountDf;// 高中数学扫描大于等于2次的回流学生数日浮

    private DailyPerformanceInfo() {
    }

    private DailyPerformanceInfo(Integer regionCode, Integer date) {
        this.regionCode = regionCode;
        this.date = date;
    }

    public static DailyPerformanceInfo newInstance(Integer regionCode, Integer date) {
        if (regionCode == null || date == null) {
            throw new IllegalArgumentException("region code: " + regionCode + ",date: " + date);
        }
        return new DailyPerformanceInfo(regionCode, date);
    }

    public static String ck_region_day(Integer regionCode, Integer date) {
        return CacheKeyGenerator.generateCacheKey(DailyPerformanceInfo.class,
                new String[]{"r", "d"},
                new Object[]{regionCode, date});
    }

    public void appendPerformanceData(SchoolDayIncreaseData item, AgentDictSchool dictData, Integer day) {
        // 新增数据累加处理
        if(item != null) {
            this.addStuRegNum += SafeConverter.toInt(item.getAddStuRegNum());
            this.addStuAuthNum += SafeConverter.toInt(item.getAddStuAuthNum());
            this.addTeaRegNum += SafeConverter.toInt(item.getAddTeaRegNum());
            this.addTeaAuthNum += SafeConverter.toInt(item.getAddTeaAuthNum());

            this.monthStuRegNum += SafeConverter.toInt(item.getMonthStuRegNum());
            this.monthStuAuthNum += SafeConverter.toInt(item.getMonthStuAuthNum());
        }

        if (Objects.equals(dictData.getSchoolLevel(), SchoolLevel.JUNIOR.getLevel())) {
            if(dictData.isCalPerformanceTrue() && item != null){

                // 小学英语
                this.juniorEngMauc += SafeConverter.toInt(item.getEngMauc());
                this.juniorEngMaucDf += SafeConverter.toInt(item.getEngMaucDf());
                this.juniorEngAddMauc += SafeConverter.toInt(item.getEngAddMauc());
                this.juniorEngAddMaucDf += SafeConverter.toInt(item.getEngAddMaucDf());
                this.juniorEngBfMauc += SafeConverter.toInt(item.getEngBfMauc());
                this.juniorEngBfMaucDf += SafeConverter.toInt(item.getEngBfMaucDf());

                // 小学数学
                this.juniorMathMauc += SafeConverter.toInt(item.getMathMauc());
                this.juniorMathMaucDf += SafeConverter.toInt(item.getMathMaucDf());
                this.juniorMathAddMauc += SafeConverter.toInt(item.getMathAddMauc());
                this.juniorMathAddMaucDf += SafeConverter.toInt(item.getMathAddMaucDf());
                this.juniorMathBfMauc += SafeConverter.toInt(item.getMathBfMauc());
                this.juniorMathBfMaucDf += SafeConverter.toInt(item.getMathBfMaucDf());

                this.juniorChnMauc += SafeConverter.toInt(item.getChnMauc());
                this.juniorChnMaucDf += SafeConverter.toInt(item.getChnMaucDf());
                this.juniorChnAddMauc += SafeConverter.toInt(item.getChnAddMauc());
                this.juniorChnAddMaucDf += SafeConverter.toInt(item.getChnAddMaucDf());
                this.juniorChnBfMauc += SafeConverter.toInt(item.getChnBfMauc());
                this.juniorChnBfMaucDf += SafeConverter.toInt(item.getChnBfMaucDf());

                // 小学单科
                this.juniorSglSubjMauc += SafeConverter.toInt(item.getSglSubjMauc());
                this.juniorSglSubjMaucDf += SafeConverter.toInt(item.getSglSubjMaucDf());
                this.juniorSglSubjAddMauc += SafeConverter.toInt(item.getSglSubjAddMauc());
                this.juniorSglSubjAddMaucDf += SafeConverter.toInt(item.getSglSubjAddMaucDf());
                this.juniorSglSubjBfMauc += SafeConverter.toInt(item.getSglSubjBfMauc());
                this.juniorSglSubjBfMaucDf += SafeConverter.toInt(item.getSglSubjBfMaucDf());

            }

            // 小学英语预算
            this.juniorEngMaucBudget += 0;
            this.juniorEngAddMaucBudget += 0;
            this.juniorEngBfMaucBudget += 0;

            // 小学数学预算
            this.juniorMathMaucBudget += 0;
            this.juniorMathAddMaucBudget += 0;
            this.juniorMathBfMaucBudget += 0;

            // 小学语文预算
            this.juniorChnMaucBudget += 0;
            this.juniorChnAddMaucBudget += 0;
            this.juniorChnBfMaucBudget += 0;

            // 小学单科预算
            this.juniorSglSubjMaucBudget += 0;
            this.juniorSglSubjAddMaucBudget += 0;
            this.juniorSglSubjBfMaucBudget += 0;

//            if(dictData.getEngMode() == 1){ // 17作业模式
//                // 英语指标
//                if(dictData.isCalPerformanceTrue() && item != null){
//                    this.juniorEngMauc += SafeConverter.toInt(item.getEngMauc());
//                    this.juniorEngMaucDf += SafeConverter.toInt(item.getEngMaucDf());
//
//                    this.juniorEngAddMauc += SafeConverter.toInt(item.getEngAddMauc());
//                    this.juniorEngAddMaucDf += SafeConverter.toInt(item.getEngAddMaucDf());
//                    this.juniorEngBfMauc += SafeConverter.toInt(item.getEngBfMauc());
//                    this.juniorEngBfMaucDf += SafeConverter.toInt(item.getEngBfMaucDf());
//                }
//                // 英语预算
//                this.juniorEngMaucBudget += SafeConverter.toLong(dictData.getEngBudget(day));
//                this.juniorEngBfMaucBudget += SafeConverter.toLong(dictData.getEngBudget(day));
//            }
//            if(dictData.getMathMode() == 1){// 17作业模式
//                // 数学指标
//                if(dictData.isCalPerformanceTrue() && item != null) {
//                    this.juniorMathMauc += SafeConverter.toInt(item.getMathMauc());
//                    this.juniorMathMaucDf += SafeConverter.toInt(item.getMathMaucDf());
//
//                    this.juniorMathAddMauc += SafeConverter.toInt(item.getMathAddMauc());
//                    this.juniorMathAddMaucDf += SafeConverter.toInt(item.getMathAddMaucDf());
//                    this.juniorMathBfMauc += SafeConverter.toInt(item.getMathBfMauc());
//                    this.juniorMathBfMaucDf += SafeConverter.toInt(item.getMathBfMaucDf());
//                }
//                // 数学预算
//                this.juniorMathMaucBudget += SafeConverter.toLong(dictData.getMathBudget(day));
//                this.juniorMathBfMaucBudget += SafeConverter.toLong(dictData.getMathBudget(day));
//            }
        } else if (Objects.equals(dictData.getSchoolLevel(), SchoolLevel.MIDDLE.getLevel())) {

            if(dictData.getEngMode() == 1) { // 17作业模式
                // 英语指标
                if(dictData.isCalPerformanceTrue() && item != null){
                    this.middleEngMauc += SafeConverter.toInt(item.getEngMauc());
                    this.middleEngMaucDf += SafeConverter.toInt(item.getEngMaucDf());

                    this.middleEngAddMauc += SafeConverter.toInt(item.getEngAddMauc());
                    this.middleEngAddMaucDf += SafeConverter.toInt(item.getEngAddMaucDf());
                    this.middleEngBfMauc += SafeConverter.toInt(item.getEngBfMauc());
                    this.middleEngBfMaucDf += SafeConverter.toInt(item.getEngBfMaucDf());
                }
                // 英语预算
                this.middleEngMaucBudget += SafeConverter.toLong(dictData.getEngBudget(day));
                this.middleEngBfMaucBudget += SafeConverter.toLong(dictData.getEngBudget(day));
            }

            if(dictData.getMathMode() == 2 || dictData.getMathMode() == 3){ // 快乐学模式
                // 数学指标
                if(dictData.isCalPerformanceTrue() && item != null) {
                    this.middleMathScanGte1StuCount += SafeConverter.toInt(item.getScanNumGte1StuCount());
                    this.middleMathScanGte1StuCountDf += SafeConverter.toInt(item.getScanNumGte1StuCountDf());
                    this.middleMathScanGte1AddStuCount += SafeConverter.toInt(item.getScanNumGte1AddStuCount());
                    this.middleMathScanGte1AddStuCountDf += SafeConverter.toInt(item.getScanNumGte1AddStuCountDf());
                    this.middleMathScanGte1BfStuCount += SafeConverter.toInt(item.getScanNumGte1BfStuCount());
                    this.middleMathScanGte1BfStuCountDf += SafeConverter.toInt(item.getScanNumGte1BfStuCountDf());

                    this.middleMathScanGte2StuCount += SafeConverter.toInt(item.getKlxScanMathTpCount());
                    this.middleMathScanGte2StuCountDf += SafeConverter.toInt(item.getKlxScanMathTpCountDf());
                    this.middleMathScanGte2AddStuCount += SafeConverter.toInt(item.getKlxMathAddMauc());
                    this.middleMathScanGte2AddStuCountDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
                    this.middleMathScanGte2BfStuCount += SafeConverter.toInt(item.getKlxMathBfMauc());
                    this.middleMathScanGte2BfStuCountDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());

//                    // 名校
//                    if(AgentSchoolPopularityType.A == AgentSchoolPopularityType.of(dictData.getSchoolPopularity())){
//                        this.middleEliteMathAddMauc += SafeConverter.toInt(item.getKlxMathAddMauc());
//                        this.middleEliteMathAddMaucDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
//                        this.middleEliteMathBfMauc += SafeConverter.toInt(item.getKlxMathBfMauc());
//                        this.middleEliteMathBfMaucDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());
//                    }else {
//                        this.middleKeyMathAddMauc += SafeConverter.toInt(item.getKlxMathAddMauc());
//                        this.middleKeyMathAddMaucDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
//                        this.middleKeyMathBfMauc += SafeConverter.toInt(item.getKlxMathBfMauc());
//                        this.middleKeyMathBfMaucDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());
//                    }
                }
            }

        } else if (Objects.equals(dictData.getSchoolLevel(), SchoolLevel.HIGH.getLevel())) {

            if(dictData.getMathMode() == 2 || dictData.getMathMode() == 3){ // 快乐学模式
                // 数学指标
                if(dictData.isCalPerformanceTrue() && item != null) {
                    this.highMathScanGte1StuCount += SafeConverter.toInt(item.getScanNumGte1StuCount());
                    this.highMathScanGte1StuCountDf += SafeConverter.toInt(item.getScanNumGte1StuCountDf());
                    this.highMathScanGte1AddStuCount += SafeConverter.toInt(item.getScanNumGte1AddStuCount());
                    this.highMathScanGte1AddStuCountDf += SafeConverter.toInt(item.getScanNumGte1AddStuCountDf());
                    this.highMathScanGte1BfStuCount += SafeConverter.toInt(item.getScanNumGte1BfStuCount());
                    this.highMathScanGte1BfStuCountDf += SafeConverter.toInt(item.getScanNumGte1BfStuCountDf());

                    this.highMathScanGte2StuCount += SafeConverter.toInt(item.getKlxScanMathTpCount());
                    this.highMathScanGte2StuCountDf += SafeConverter.toInt(item.getKlxScanMathTpCountDf());
                    this.highMathScanGte2AddStuCount += SafeConverter.toInt(item.getKlxMathAddMauc());
                    this.highMathScanGte2AddStuCountDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
                    this.highMathScanGte2BfStuCount += SafeConverter.toInt(item.getKlxMathBfMauc());
                    this.highMathScanGte2BfStuCountDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());
//                    // 名校
//                    if(AgentSchoolPopularityType.A == AgentSchoolPopularityType.of(dictData.getSchoolPopularity())){
//                        this.highEliteMathAddMauc += SafeConverter.toInt(item.getKlxMathAddMauc());
//                        this.highEliteMathAddMaucDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
//                        this.highEliteMathBfMauc += SafeConverter.toInt(item.getKlxMathBfMauc());
//                        this.highEliteMathBfMaucDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());
//                    }else {
//                        this.highKeyMathAddMauc += SafeConverter.toInt(item.getKlxMathAddMauc());
//                        this.highKeyMathAddMaucDf += SafeConverter.toInt(item.getKlxMathAddMaucDf());
//                        this.highKeyMathBfMauc += SafeConverter.toInt(item.getKlxMathBfMauc());
//                        this.highKeyMathBfMaucDf += SafeConverter.toInt(item.getKlxMathBfMaucDf());
//                    }
                }
            }
        }
    }

    public void appendPerformanceData(DailyPerformanceInfo item) {
        if(item == null){
            return;
        }
        // 新增数据累加处理
        this.addStuRegNum += SafeConverter.toInt(item.getAddStuRegNum());
        this.addStuAuthNum += SafeConverter.toInt(item.getAddStuAuthNum());
        this.addTeaRegNum += SafeConverter.toInt(item.getAddTeaRegNum());
        this.addTeaAuthNum += SafeConverter.toInt(item.getAddTeaAuthNum());

        this.monthStuRegNum += SafeConverter.toInt(item.getMonthStuRegNum());
        this.monthStuAuthNum += SafeConverter.toInt(item.getMonthStuAuthNum());


        // 初中数学指标 20+10模式
        this.middleMathMauc += SafeConverter.toInt(item.getMiddleMathMauc());
        this.middleMathMaucDf += SafeConverter.toInt(item.getMiddleMathMaucDf());
        this.middleMathMaucBudget += SafeConverter.toLong(item.getMiddleMathMaucBudget());

        // 初中数学指标 2+2模式
        this.middleMath2Mauc += SafeConverter.toInt(item.getMiddleMath2Mauc());
        this.middleMath2MaucDf += SafeConverter.toInt(item.getMiddleMath2MaucDf());

        // 高中数学指标 20+10模式
        this.highMathMauc += SafeConverter.toInt(item.getHighMathMauc());
        this.highMathMaucDf += SafeConverter.toInt(item.getHighMathMaucDf());
        this.highMathMaucBudget += SafeConverter.toLong(item.getHighMathMaucBudget());

        // 高中数学指标 2+2模式
        this.highMath2Mauc += SafeConverter.toInt(item.getHighMath2Mauc());
        this.highMath2MaucDf += SafeConverter.toInt(item.getHighMath2MaucDf());

        // 小学英语指标
        this.juniorEngMauc += item.getJuniorEngMauc();
        this.juniorEngMaucDf += item.getJuniorEngMaucDf();
        this.juniorEngMaucBudget += item.getJuniorEngMaucBudget();
        this.juniorEngAddMauc += item.getJuniorEngAddMauc();
        this.juniorEngAddMaucDf += item.getJuniorEngAddMaucDf();
        this.juniorEngAddMaucBudget += item.getJuniorEngAddMaucBudget();
        this.juniorEngBfMauc += item.getJuniorEngBfMauc();
        this.juniorEngBfMaucDf += item.getJuniorEngBfMaucDf();
        this.juniorEngBfMaucBudget += item.getJuniorEngBfMaucBudget();

        // 小学数学指标
        this.juniorMathMauc += item.getJuniorMathMauc();
        this.juniorMathMaucDf += item.getJuniorMathMaucDf();
        this.juniorMathMaucBudget += item.getJuniorMathMaucBudget();
        this.juniorMathAddMauc += item.getJuniorMathAddMauc();
        this.juniorMathAddMaucDf += item.getJuniorMathAddMaucDf();
        this.juniorMathAddMaucBudget += item.getJuniorMathAddMaucBudget();
        this.juniorMathBfMauc += item.getJuniorMathBfMauc();
        this.juniorMathBfMaucDf += item.getJuniorMathBfMaucDf();
        this.juniorMathBfMaucBudget += item.getJuniorMathBfMaucBudget();

        // 小学语文
        this.juniorChnMauc += item.getJuniorChnMauc();
        this.juniorChnMaucDf += item.getJuniorChnMaucDf();
        this.juniorChnMaucBudget += item.getJuniorChnMaucBudget();
        this.juniorChnAddMauc += item.getJuniorChnAddMauc();
        this.juniorChnAddMaucDf += item.getJuniorChnAddMaucDf();
        this.juniorChnAddMaucBudget += item.getJuniorChnAddMaucBudget();
        this.juniorChnBfMauc += item.getJuniorChnBfMauc();
        this.juniorChnBfMaucDf += item.getJuniorChnBfMaucDf();
        this.juniorChnBfMaucBudget += item.getJuniorChnBfMaucBudget();

        // 小学单科
        this.juniorSglSubjMauc += item.getJuniorSglSubjMauc();
        this.juniorSglSubjMaucDf += item.getJuniorSglSubjMaucDf();
        this.juniorSglSubjMaucBudget += item.getJuniorSglSubjMaucBudget();
        this.juniorSglSubjAddMauc += item.getJuniorSglSubjAddMauc();
        this.juniorSglSubjAddMaucDf += item.getJuniorSglSubjAddMaucDf();
        this.juniorSglSubjAddMaucBudget += item.getJuniorSglSubjAddMaucBudget();
        this.juniorSglSubjBfMauc += item.getJuniorSglSubjBfMauc();
        this.juniorSglSubjBfMaucDf += item.getJuniorSglSubjBfMaucDf();
        this.juniorSglSubjBfMaucBudget += item.getJuniorSglSubjBfMaucBudget();

        // 初中英语指标
        this.middleEngMauc += item.getMiddleEngMauc();
        this.middleEngMaucDf += item.getMiddleEngMaucDf();
        this.middleEngMaucBudget += item.getMiddleEngMaucBudget();
        this.middleEngAddMauc += item.getMiddleEngAddMauc();
        this.middleEngAddMaucDf += item.getMiddleEngAddMaucDf();
        this.middleEngAddMaucBudget += item.getMiddleEngAddMaucBudget();
        this.middleEngBfMauc += item.getMiddleEngBfMauc();
        this.middleEngBfMaucDf += item.getMiddleEngBfMaucDf();
        this.middleEngBfMaucBudget += item.getMiddleEngBfMaucBudget();

        // 初中数学
        this.middleEliteMathAddMauc += item.getMiddleEliteMathAddMauc();
        this.middleEliteMathAddMaucDf += item.getMiddleEliteMathAddMaucDf();
        this.middleEliteMathBfMauc += item.getMiddleEliteMathBfMauc();
        this.middleEliteMathBfMaucDf += item.getMiddleEliteMathBfMaucDf();

        this.middleKeyMathAddMauc += item.getMiddleKeyMathAddMauc();
        this.middleKeyMathAddMaucDf += item.getMiddleKeyMathAddMaucDf();
        this.middleKeyMathBfMauc += item.getMiddleKeyMathBfMauc();
        this.middleKeyMathBfMaucDf += item.getMiddleKeyMathBfMaucDf();

        // 初中数学扫描数大于等于1次的数据
        this.middleMathScanGte1StuCount += item.getMiddleMathScanGte1StuCount();
        this.middleMathScanGte1StuCountDf += item.getMiddleMathScanGte1StuCountDf();
        this.middleMathScanGte1AddStuCount += item.getMiddleMathScanGte1AddStuCount();
        this.middleMathScanGte1AddStuCountDf += item.getMiddleMathScanGte1AddStuCountDf();
        this.middleMathScanGte1BfStuCount += item.getMiddleMathScanGte1BfStuCount();
        this.middleMathScanGte1BfStuCountDf += item.getMiddleMathScanGte1BfStuCountDf();

        // 初中数学扫描数大于等于2次的数据
        this.middleMathScanGte2StuCount += item.getMiddleMathScanGte2StuCount();
        this.middleMathScanGte2StuCountDf += item.getMiddleMathScanGte2StuCountDf();
        this.middleMathScanGte2AddStuCount += item.getMiddleMathScanGte2AddStuCount();
        this.middleMathScanGte2AddStuCountDf += item.getMiddleMathScanGte2AddStuCountDf();
        this.middleMathScanGte2BfStuCount += item.getMiddleMathScanGte2BfStuCount();
        this.middleMathScanGte2BfStuCountDf += item.getMiddleMathScanGte2BfStuCountDf();

        // 高中数学
        this.highEliteMathAddMauc += item.getHighEliteMathAddMauc();
        this.highEliteMathAddMaucDf += item.getHighEliteMathAddMaucDf();
        this.highEliteMathBfMauc += item.getHighEliteMathBfMauc();
        this.highEliteMathBfMaucDf += item.getHighEliteMathBfMaucDf();

        this.highKeyMathAddMauc += item.getHighKeyMathAddMauc();
        this.highKeyMathAddMaucDf += item.getHighKeyMathAddMaucDf();
        this.highKeyMathBfMauc += item.getHighKeyMathBfMauc();
        this.highKeyMathBfMaucDf += item.getHighKeyMathBfMaucDf();

        // 高中数学扫描数大于等于1次的数据
        this.highMathScanGte1StuCount += item.getHighMathScanGte1StuCount();
        this.highMathScanGte1StuCountDf += item.getHighMathScanGte1StuCountDf();
        this.highMathScanGte1AddStuCount += item.getHighMathScanGte1AddStuCount();
        this.highMathScanGte1AddStuCountDf += item.getHighMathScanGte1AddStuCountDf();
        this.highMathScanGte1BfStuCount += item.getHighMathScanGte1BfStuCount();
        this.highMathScanGte1BfStuCountDf += item.getHighMathScanGte1BfStuCountDf();

        // 高中数学扫描数大于等于2次的数据
        this.highMathScanGte2StuCount += item.getHighMathScanGte2StuCount();
        this.highMathScanGte2StuCountDf += item.getHighMathScanGte2StuCountDf();
        this.highMathScanGte2AddStuCount += item.getHighMathScanGte2AddStuCount();
        this.highMathScanGte2AddStuCountDf += item.getHighMathScanGte2AddStuCountDf();
        this.highMathScanGte2BfStuCount += item.getHighMathScanGte2BfStuCount();
        this.highMathScanGte2BfStuCountDf += item.getHighMathScanGte2BfStuCountDf();
    }
}
