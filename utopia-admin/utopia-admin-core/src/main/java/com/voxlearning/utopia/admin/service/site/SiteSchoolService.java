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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.service.site.SiteBatchInputHandler.UnitTransformer;
import com.voxlearning.utopia.entity.crm.CrmGroupSummary;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.user.api.entities.UserTag;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yuechen wang
 * @since 16/4/11
 */
@Named
public class SiteSchoolService {

    @Inject private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject private UserTagLoaderClient userTagLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;

    private final static String DEFAULT_WORD_SEPARATOR = "\\t";
    private final static int DEFAULT_QPS_INTERVAL = 500;
    private final static String GET_SCHOOL_INFO_API_URL = "http://tiku.17zuoye.net/service/get-school-info";
    private final static String BIND_STUDENT_TAG_KEY = "S_BIND_MOBILE_OR_HIS_P_BIND_MOBILE";

    private UtopiaSql utopiaSql = UtopiaSqlFactory.instance().getDefaultUtopiaSql();


    @Getter
    @Setter
    private static class BatchQuerySchoolObj {
        Long schoolId;
        String schoolName;

        @Override
        public String toString() {
            return StringUtils.join(SafeConverter.toString(schoolId), ":", schoolName);
        }

        public static final UnitTransformer<String, BatchQuerySchoolObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 2) {
                return null;
            }
            BatchQuerySchoolObj batchQuerySchoolObj = new BatchQuerySchoolObj();
            batchQuerySchoolObj.setSchoolId(SafeConverter.toLong(words[0]));
            batchQuerySchoolObj.setSchoolName(words[1]);
            return batchQuerySchoolObj;
        };
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class QuerySchoolResult {
        private Long schoolId;        // 学校ID
        private String schoolName;    // 学校名称
        private String count;         // 百度结果个数
        private boolean foundAmap;    // 是否有高德地图结果
        private boolean foundBdMap;   // 是否有百度地图结果
        private boolean hasBaike;     // 是否有百度百科页面
        private String bindRate;      // 认证手机号绑定率
    }

    public MapMessage batchQuerySchool(String content) {
        content = content.replaceAll("\\r", "");
        DefaultExcelStringSiteBatchInputHandler<BatchQuerySchoolObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<SiteBatchInputHandler.FailedData<String>> failedRows = new ArrayList<>();
        List<BatchQuerySchoolObj> batchQuerySchools = defaultExcelStringInputHandler.handleInput(
                content, failedRows, BatchQuerySchoolObj.rowTransformer);

        List<QuerySchoolResult> querySchoolResults = new ArrayList<>();
        for (Iterator<BatchQuerySchoolObj> it = batchQuerySchools.iterator(); it.hasNext(); ) {
            BatchQuerySchoolObj schoolObj = it.next();
            // 调用API查询学校综合信息
            String URL = UrlUtils.buildUrlQuery(GET_SCHOOL_INFO_API_URL, MiscUtils.m("name", schoolObj.getSchoolName()));
            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(URL).execute();
            try {
                // FIXME QPS用什么方式处理更加合适
                Thread.sleep(DEFAULT_QPS_INTERVAL);
            } catch (Exception e) {
                failedRows.add(new SiteBatchInputHandler.FailedData<>(schoolObj.toString(), e.getMessage()));
                continue;
            }
            String rs = response.getResponseString();
            if (StringUtils.isBlank(rs)) {
                failedRows.add(new SiteBatchInputHandler.FailedData<>(schoolObj.toString(), "接口调用失败,CODE=" + response.getStatusCode()));
                continue;
            }
            Map<String, Object> returnMap = JsonUtils.convertJsonObjectToMap(rs);

            QuerySchoolResult newSchoolResult = new QuerySchoolResult();
            newSchoolResult.setSchoolId(schoolObj.getSchoolId());
            newSchoolResult.setSchoolName(schoolObj.getSchoolName());
            if (SafeConverter.toBoolean(returnMap.get("success"))) {
                newSchoolResult.setCount(SafeConverter.toString(returnMap.get("count")));
                newSchoolResult.setFoundAmap(SafeConverter.toBoolean(returnMap.get("found_amap")));
                newSchoolResult.setFoundBdMap(SafeConverter.toBoolean(returnMap.get("found_bd_map")));
            } else {
                newSchoolResult.setCount("0");
                newSchoolResult.setFoundBdMap(false);
                newSchoolResult.setFoundAmap(false);
            }

            // 计算手机号绑定率
            List<CrmGroupSummary> groupSummaries = crmSummaryLoaderClient.loadSchoolGroupSummary(schoolObj.getSchoolId());
            CrmSchoolSummary schoolSummary = crmSummaryLoaderClient.loadSchoolSummary(schoolObj.getSchoolId());

            Set<Long> authStudentSet = new HashSet<>();
            if(schoolSummary != null && schoolSummary.getSchoolLevel() != null && CollectionUtils.isNotEmpty(groupSummaries)){
                Set<Long> groupIdSet = groupSummaries.stream().map(CrmGroupSummary::getGroupId).collect(Collectors.toSet());
                Map<Long, List<Long>> groupStudentMap = studentLoaderClient.loadGroupStudentIds(groupIdSet);
                Set<Long> studentSet = groupStudentMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
                List<Long> authedStudents = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(studentSet)){
                    authedStudents = userAuthQueryServiceClient.filterAuthedStudents(studentSet, schoolSummary.getSchoolLevel());
                }
                if(CollectionUtils.isNotEmpty(authedStudents)){
                    authStudentSet.addAll(authedStudents);
                }
            }
//            for (CrmGroupSummary summary : groupSummaries) {
//                authStudentSet.addAll(summary.fetchStudentAuthedList().keySet().stream().map(SafeConverter::toLong).collect(Collectors.toSet()));
//            }
            DecimalFormat df = new DecimalFormat("00.00%");
            if (CollectionUtils.isNotEmpty(authStudentSet)) {
                // 再根据学生ID获得所有绑定手机号的学生数
                Map<Long, UserTag> studentTags = userTagLoaderClient.loadUserTags(authStudentSet);
                Map<Long, List<UserTag>> bindStudents = studentTags.values()
                        .stream()
                        .filter(this::filterBoundMobileStudentByTag)
                        .collect(Collectors.groupingBy(UserTag::getUserId, Collectors.toList()));

                newSchoolResult.setBindRate(df.format(bindStudents.size() * 1.0 / authStudentSet.size()));
            } else {
                newSchoolResult.setBindRate(df.format(0));
            }

            querySchoolResults.add(newSchoolResult);
        } // 至此学校信息处理完毕

        //导出结果
        try {
            XSSFWorkbook xssfWorkbook = convertToXSSF(querySchoolResults, failedRows);
            String filename = "导出学校综合信息-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE) + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    filename,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
        } catch (IOException ex) {
            return MapMessage.errorMessage("导出EXCEL失败:{}", ex.getMessage());
        }

        return MapMessage.successMessage().add("successCnt", querySchoolResults.size()).add("failedCnt", failedRows.size());
    }

    //////////////////////////////////////////////Private Methods///////////////////////////////////////////////////

    private XSSFWorkbook convertToXSSF(List<QuerySchoolResult> succeededList, List<SiteBatchInputHandler.FailedData<String>> failedList) {
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();

        if (CollectionUtils.isNotEmpty(succeededList)) {
            XSSFSheet xssfSheet = xssfWorkbook.createSheet("成功列表");
            for (int i = 0; i < 10; i++) {
                xssfSheet.setColumnWidth(i, 400 * 15);
            }
            XSSFRow firstRow = xssfSheet.createRow(0);
            firstRow.setHeightInPoints(20);
            firstRow.createCell(0).setCellValue("学校ID");
            firstRow.createCell(1).setCellValue("学校名称");
            firstRow.createCell(2).setCellValue("认证手机号绑定率");
            firstRow.createCell(3).setCellValue("百度绝对搜索返回值");
            firstRow.createCell(4).setCellValue("是否有百度百科");
            firstRow.createCell(5).setCellValue("百度地图是否有返回位置");
            firstRow.createCell(6).setCellValue("高德地图是否有返回位置");

            int rowNum = 1;
            for (QuerySchoolResult data : succeededList) {
                XSSFRow xssfRow = xssfSheet.createRow(rowNum++);
                xssfRow.setHeightInPoints(20);
                xssfRow.createCell(0).setCellValue(data.getSchoolId());
                xssfRow.createCell(1).setCellValue(data.getSchoolName());
                xssfRow.createCell(2).setCellValue(data.getBindRate());
                xssfRow.createCell(3).setCellValue(data.getCount());
                xssfRow.createCell(4).setCellValue(data.isHasBaike() ? "是" : "否");
                xssfRow.createCell(5).setCellValue(data.isFoundBdMap() ? "是" : "否");
                xssfRow.createCell(6).setCellValue(data.isFoundAmap() ? "是" : "否");
            }
        }

        if (CollectionUtils.isNotEmpty(failedList)) {
            XSSFSheet xssfFailedSheet = xssfWorkbook.createSheet("失败列表");
            xssfFailedSheet.setColumnWidth(1, 400 * 60);
            XSSFRow firstRow = xssfFailedSheet.createRow(0);
            firstRow.setHeightInPoints(20);
            firstRow.createCell(0).setCellValue("序号");
            firstRow.createCell(1).setCellValue("错误数据");
            firstRow.createCell(2).setCellValue("错误信息");
            int rowNum = 1;
            for (SiteBatchInputHandler.FailedData<String> failed : failedList) {
                XSSFRow xssfRow = xssfFailedSheet.createRow(rowNum);
                xssfRow.setHeightInPoints(20);
                xssfRow.createCell(0).setCellValue(rowNum);
                xssfRow.createCell(1).setCellValue(failed.getUnitData());
                xssfRow.createCell(2).setCellValue(failed.getErrorMsg());
                rowNum++;
            }
        }
        return xssfWorkbook;
    }

    private boolean filterBoundMobileStudentByTag(UserTag userTag) {
        boolean isBound = false;
        if (userTag != null) {
            UserTag.Tag tag = userTag.fetchTag(BIND_STUDENT_TAG_KEY);
            if (tag != null) {
                isBound = SafeConverter.toBoolean(tag.getValue());
            }
        }
        return isBound;
    }

}
