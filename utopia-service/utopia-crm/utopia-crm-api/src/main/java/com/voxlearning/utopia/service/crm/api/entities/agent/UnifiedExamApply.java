package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.*;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统考申请
 * Created by zang.tao on 2017/4/17.
 */
@EqualsAndHashCode(callSuper = false)
@DocumentTable(table = "AGENT_UNIFIED_EXAM_APPLY")
@UtopiaCacheExpiration
@DocumentConnection(configName = "agent")
@UtopiaCacheRevision("20171106")
@Getter
@Setter
public class UnifiedExamApply extends AbstractBaseApply {
    private final static List<Integer> GRADE_LIST = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private final static String COMMA = ",";


    @UtopiaSqlColumn private String unifiedExamName;                //考试名称
    @UtopiaSqlColumn private String subject;                        //学科 101：小学语文 ；102：小学数学；103：小学英语；201：初中语文；202：初中数学；203：初中英语 ； 若是有新数据添加 请与内容库协商
    @UtopiaSqlColumn private Integer gradeLevel;                    //年级 1~9
    @UtopiaSqlColumn private String regionLeve;                        //地区级别   NewExamRegionLevel | city:市级;country:区级;school:校级
    @UtopiaSqlColumn private String provinceCode;                    //省
    @UtopiaSqlColumn private String cityCode;                        //市
    @UtopiaSqlColumn private String regionCode;                        //区
    @UtopiaSqlColumn private String provinceName;                    //省名称
    @UtopiaSqlColumn private String cityName;                        //市名称
    @UtopiaSqlColumn private String regionName;                        //区名称
    @UtopiaSqlColumn private String unifiedExamSchool;              //统考学校列表 id + ","
    @UtopiaSqlColumn private UnifiedExamTestPaperSourceType testPaperSourceType;    //试卷来源
    @UtopiaSqlColumn private String testPaperAddress;                                //试卷地址
    @UtopiaSqlColumn private String testPaperId;                                    //试卷名称Id
    @UtopiaSqlColumn private String testPaperType;                                  //统考试卷类型
    @UtopiaSqlColumn private String sendEmail;                                      //需要发送邮箱地址 邮箱格式为 xxxx.yy@17zuoy.com 若是需要通知多个联系人则需 用 “;” 隔开
    @UtopiaSqlColumn private Date unifiedExamBeginTime;                            // 考试开始时间
    @UtopiaSqlColumn private Date unifiedExamEndTime;                              //考试结束时间
    @UtopiaSqlColumn private Date correctingTestPaper;                              //批改试卷时间
    @UtopiaSqlColumn private Date achievementReleaseTime;                           // 成绩发布时间
    @UtopiaSqlColumn private Integer minSubmittedTestPaper;                         // 最短上交试卷时间 分钟
    @UtopiaSqlColumn private Integer oralLanguageFrequency;                         // 口语可答题次数
    @UtopiaSqlColumn private Integer maxSubmittedTestPaper;                         //最长上交试卷时间
    @UtopiaSqlColumn private UnifiedExamApplyStatus unifiedExamStatus;              // 统考记录的状态
    @UtopiaSqlColumn private TestPaperEntryStatus entryStatus;                        //录入状态				待录入 录入完成 录入失败
    @UtopiaSqlColumn private String entryTestPaperAddress;                            //录入试卷地址        //用于记录 内容库上传试卷 防止意外发生
    @UtopiaSqlColumn private String entryTestPapeName;                                //录入试卷名称
    @UtopiaSqlColumn private Double score;                                            //试卷总分值
    @UtopiaSqlColumn private String bookCatalogId;                                    //教材ID
    @UtopiaSqlColumn private String bookCatalogName;                                  //教材名称

    // 新增字段
    @UtopiaSqlColumn private Integer distribution;                                    //学生获取试卷的方式
    @UtopiaSqlColumn private Integer testScene;                                       //考试场景
    @UtopiaSqlColumn private Integer gradeType;                                       //分级制
    @UtopiaSqlColumn private String ranks;                                            //等级制内容是 ExamRank List 对象的 Json

    /**
     * @return 获取当前考试试卷获取方式的枚举
     */
    public UnifiedExamTestWayType fetchDistribution() {
        return UnifiedExamTestWayType.of(distribution);
    }

    /**
     * @return 获取考试场景的枚举
     */
    public UnifiedExamTestPaperScene fetchTestScene() {
        return UnifiedExamTestPaperScene.of(testScene);
    }

    public UnifiedExamTestGradeType fetchTestGradeType() {
        return UnifiedExamTestGradeType.of(gradeType);
    }


    /**
     * 这个方法是用在前端 展示简介
     * 通用方式  若是有改动的地方则需要同步修正此处信息
     *
     * @return
     */
    public String generateSummary() {
        StringBuilder sb = new StringBuilder("学科：");
        switch (subject) {
            case "101":
                sb.append("小学语文；");
                break;
            case "102":
                sb.append("小学数学；");
                break;
            case "103":
                sb.append("小学英语；");
                break;
            case "201":
                sb.append("初中语文；");
                break;
            case "202":
                sb.append("初中数学；");
                break;
            case "203":
                sb.append("初中英语；");
                break;
            default:
                sb.append("暂无学科；");
        }
        sb.append("级别：");
        switch (regionLeve) {
            case "city":
                sb.append("市级；");
                break;
            case "country":
                sb.append("区级；");
                break;
            case "school":
                sb.append("校级；");
                break;
            default:
                sb.append("暂无级别；");
        }
        sb.append("考试名称：").append(unifiedExamName).append("；");
        sb.append("是否重复使用已录入试卷:").append(testPaperSourceType.getType() == 0 ? "否 " : "是 ");
        return sb.toString();
    }

    //生成缓存使用的key 值
    public static String ck_wid(Long workflowId) {
        return CacheKeyGenerator.generateCacheKey(UnifiedExamApply.class, "wid", workflowId);
    }

    public static String ck_platform_uid(SystemPlatformType userPlatform, String userAccount) {
        return CacheKeyGenerator.generateCacheKey(UnifiedExamApply.class,
                new String[]{"platform", "uid"},
                new Object[]{userPlatform, userAccount});
    }

    /**
     * @return 当前对象所选择的年级
     */
    public List<Integer> fetchGradeLevel() {
        return fetchGradeLevel(gradeLevel);
    }


    /**
     * 获取已取得的年纪的存储值   例子： 0  1  0   1  0  1
     * 六年级  五年级 四年级 三年级 二年级 一年级
     */

    public static Integer generateGradeLevel(Collection<Integer> gradeLevel) {
        if (CollectionUtils.isEmpty(gradeLevel)) {
            return 0;
        }
        Integer result = 0;
        for (Integer level : gradeLevel) {
            result += 1 << (level - 1);
        }
        return result;
    }

    /**
     * 取得选择的班级
     *
     * @param gradeLevel 获取已取得的年纪的存储值
     * @return
     */
    public static List<Integer> fetchGradeLevel(Integer gradeLevel) {
        return GRADE_LIST.stream().filter(p -> {
            int v= (1 << (p - 1));
            return (gradeLevel & v) == v;
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Integer> aaa = fetchGradeLevel(481);
        System.out.println();
    }

    /**
     * @return 获取当前对象所选择的考试类型
     */
    public List<UnifiedExamTestPaperType> fetchTestPaperType() {
        List<UnifiedExamTestPaperType> result = new ArrayList<>();
        String[] types = testPaperType.split(COMMA);
        for (String type : types) {
            UnifiedExamTestPaperType paperType = UnifiedExamTestPaperType.of(type);
            if (paperType != null) {
                result.add(paperType);
            }
        }
        return result;
    }

    /**
     * @param testPaperTypes 前端传回来的所选的考试类型
     * @return 存在数据库中内容
     */
    public static String generateTestPaperType(Collection<UnifiedExamTestPaperType> testPaperTypes) {
        return StringUtils.join(testPaperTypes, COMMA);
    }


    public List<String> fetchPaperId() {
        return Arrays.asList(testPaperId.split(COMMA));
    }

    public List<String> fetchPaperAddress(){
        return Arrays.asList(testPaperAddress.split(COMMA));
    }
}
