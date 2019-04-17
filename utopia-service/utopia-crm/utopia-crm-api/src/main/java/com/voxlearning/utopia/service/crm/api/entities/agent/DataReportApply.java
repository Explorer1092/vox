package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *
 *
 * @author song.wang
 * @date 2017/6/6
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@DocumentTable(table = "AGENT_DATA_REPORT_APPLY")
@UtopiaCacheExpiration(1800)
@UtopiaCacheRevision("20170607")
@DocumentConnection(configName = "agent")
public class DataReportApply extends AbstractBaseApply {
    private static final String FORMAT_TIME = "yyyyMM";
    private static final String FORMAT_SHOW_TIME = "yyyy年MM月";

    private static final long serialVersionUID = 3058512792651436667L;

    private Integer subject;                          // 学科 1: 小学英语  2：小学数学
    private Integer reportLevel;					    //报告级别   1:市级 2:区级 3:校级
    private Integer cityCode;						//市
    private String cityName;
    private Integer countyCode;
    private String countyName;
    private Long schoolId;
    private String schoolName;
    private Integer engStartGrade;      // 英语起始年级
    private Integer reportType;      // 报告类型 1：学期报告   2：月度报告
    private Integer reportTerm; // 学期  1:2016年9-12月  2：2017年1-6月 3:2017年7-12月 4:2018年1-6月
    private Integer reportMonth;    // 月份 201706
    private Long sampleSchoolId;  // 样本校ID
    private String sampleSchoolName; // 样本校名称
    private String comment;   // 申请原因
    private String firstDocument;       // 上传文档1  学习报告
    private String secondDocument;      // 上传文档2  样校文档

    public static String ck_wid(Long workflowId) {
        return CacheKeyGenerator.generateCacheKey(DataReportApply.class, "wid", workflowId);
    }

    public static String ck_platform_uid(SystemPlatformType userPlatform, String userAccount) {
        return CacheKeyGenerator.generateCacheKey(DataReportApply.class,
                new String[]{"platform", "uid"},
                new Object[]{userPlatform, userAccount});
    }

    @Override
    public String generateSummary() {
        return StringUtils.formatMessage("{}:{}申请", reportLevel == 1 ? cityName : reportLevel == 2 ? countyName : schoolName, timeDimensionality());
    }

    public String firstDocumentName() {
        return StringUtils.formatMessage("{},{}报告", reportLevel == 1 ? cityName : reportLevel == 2 ? cityName + " " + countyName : schoolName, timeDimensionality());
    }

    public String secondDocumentName() {
        return StringUtils.formatMessage("样本校:{}报告", sampleSchoolName);
    }

    public String timeDimensionality() {
        Integer reportType = this.getReportType();
        String result = "";
        if (reportType == 1) {
            if(this.reportTerm == 1){
                result = "学期报告 2016年9月-12月";
            }else if(this.reportTerm == 2){
                result = "学期报告 2017年1月-6月";
            }else if(this.reportTerm == 3){
                result = "学期报告 2017年7月-12月";
            }else if(this.reportTerm == 4){
                result = "学期报告 2018年1月-6月";
            }
        } else {
            Integer reportMonth = this.getReportMonth();
            Date date = DateUtils.stringToDate(SafeConverter.toString(reportMonth), FORMAT_TIME);
            result = StringUtils.formatMessage("月度报告 {}", DateUtils.dateToString(date, FORMAT_SHOW_TIME));
        }
        return result;
    }
}
