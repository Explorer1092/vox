/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.service.site;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Report Status维护功能
 * Created by yaguang.wang on 2016/10/18.
 */
@Named
public class SiteReportStatusService {

    @Inject private ReportStatusServiceClient reportStatusServiceClient;

    // 取出summary  的顺序
    private static List<String> SUMMARY_LIST = Arrays.asList("vox_teacher_summary", "vox_class_summary", "vox_school_summary", "vox_group_summary",
            "xx_question_statistics", "math_report", "english_report", "unit_wrong_question_stat", "redmine10961_psr_wrong_exam_report",
            "redmine9523_student_total_and_ek_correct_report", "redmine10050_afenti_experience_report", "redmine9523_final_report_four_weeks",
            "redmine9523_city_total_and_ek_correct_no_detail", "redmine9523_city_total_and_ek_correct_report");

    public List<Map<String, String>> loadReportStatus() {
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return Collections.emptyList();
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (MapUtils.isEmpty(latestCollection)) {
            return Collections.emptyList();
        }
        List<String> summaryKeyList = new ArrayList<>();
        summaryKeyList.addAll(SUMMARY_LIST);
        removeRepetition(summaryKeyList, latestCollection.keySet());
        List<Map<String, String>> result = new ArrayList<>();
        summaryKeyList.forEach(p -> {
            Map map = (Map) latestCollection.get(p);
            if (map == null) {
                return;
            }
            String collectionName = String.valueOf(map.get("collection_name"));
            Map<String, String> data = new HashMap<>();
            data.put("summary_name", p);
            data.put("collection_name", collectionName);
            result.add(data);
        });
        return result;
    }

    public MapMessage updateReportStatus(List<Map<String, String>> status) {
        Map<String, String> summaryStatus = createSummaryStatus(status);
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return MapMessage.errorMessage("Report status 没有找到");
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (MapUtils.isEmpty(latestCollection)) {
            return MapMessage.errorMessage("Report status 中无可用数据");
        }
        List<String> summaryKeyList = new ArrayList<>();
        summaryKeyList.addAll(SUMMARY_LIST);
        removeRepetition(summaryKeyList, latestCollection.keySet());
        summaryKeyList.forEach(p -> {
            Map map = (Map) latestCollection.get(p);
            if (map == null) {
                return;
            }
            String summaryValue = summaryStatus.get(p);
            if (StringUtils.isBlank(summaryValue)) {
                return;
            }
            map.put("collection_name", summaryValue);
        });
        reportStatus = reportStatusServiceClient.getReportStatusService()
                .updateReportStatus(reportStatus)
                .getUninterruptibly();
        if (reportStatus == null) {
            return MapMessage.errorMessage("更新失败");
        } else {
            return MapMessage.successMessage();
        }
    }

    private Map<String, String> createSummaryStatus(List<Map<String, String>> status) {
        if (CollectionUtils.isEmpty(status)) {
            return Collections.emptyMap();
        }
        Map<String, String> summaryStatus = new HashMap<>();
        status.forEach(p -> {
            String summary_name = SafeConverter.toString(p.get("summary_name"));
            String collection_name = SafeConverter.toString(p.get("collection_name"));
            if (StringUtils.isBlank(collection_name)) {
                return;
            }
            summaryStatus.put(summary_name, collection_name);
        });
        return summaryStatus;
    }

    private void removeRepetition(List<String> summaryKeyList, Collection<String> repetitionList) {
        if (CollectionUtils.isEmpty(repetitionList)) {
            return;
        }
        repetitionList.forEach(p -> {
            if (!summaryKeyList.contains(p)) {
                summaryKeyList.add(p);
            }
        });
    }
}
