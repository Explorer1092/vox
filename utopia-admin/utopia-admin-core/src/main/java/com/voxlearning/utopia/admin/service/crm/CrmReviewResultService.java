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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.admin.dao.CrmReviewResultDao;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.entity.crm.CrmReviewResult;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CrmReviewResultService
 *
 * @author song.wang
 * @date 2016/7/6
 */

@Named
public class CrmReviewResultService extends AbstractAdminService {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    CrmReviewResultDao crmReviewResultDao;
    @Inject
    CrmSchoolClueService crmSchoolClueService;

    public void saveReviewResult(CrmReviewResult reviewResult) {
        if (reviewResult == null) {
            return;
        }
        crmReviewResultDao.insert(reviewResult);
    }

    public CrmReviewResult getReviewResult(Long schoolId) {
        return crmReviewResultDao.findLastestBySchoolId(schoolId);
    }

    public XSSFWorkbook exportReviewResult(List<Long> schoolIds) {
        Resource resource = new ClassPathResource("/config/templates/crm_review_result_templates.xlsx");
        if (!resource.exists()) {
            logger.error("exportSchoolTasks - template not exists");
            return null;
        }
        try {
            List<CrmReviewResult> crmReviewResults = getReviewResultBySchoolIds(schoolIds);
            @Cleanup InputStream in = resource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            XSSFSheet sheet = workbook.getSheetAt(0);
            XSSFFont font = workbook.createFont();
            font.setFontHeightInPoints((short) 14);
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            if (CollectionUtils.isNotEmpty(crmReviewResults)) {
                int index = 1;
                for (CrmReviewResult res : crmReviewResults) {
                    XSSFRow row = sheet.createRow(index++);
                    int col = 0;
                    createCell(row, col++, cellStyle, ConversionUtils.toString(res.getSchoolId()));
                    createCell(row, col++, cellStyle, ConversionUtils.toString(res.getSchoolName()));
                    Long schoolId = res.getSchoolId();
                    School school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(schoolId)
                            .getUninterruptibly();
                    if (school == null) {
                        continue;
                    }
                    CrmSchoolClue crmSchoolClue = crmSchoolClueService.findLastestAuthedSchoolClue(res.getSchoolId());
                    if (crmSchoolClue == null) {
                        continue;
                    }
                    createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.getRecorderName()));
                    createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.getRecorderPhone()));
                    createCell(row, col++, cellStyle, DateUtils.dateToString(crmSchoolClue.getUpdateTime()));
                    createCell(row, col++, cellStyle, DateUtils.dateToString(res.getCreateTime()));
                    createCell(row, col++, cellStyle, ConversionUtils.toString(res.getReviewUserName()));
                    List<CrmReviewResult.CrmReviewResultDetail> resultDetailList = res.getResultDetailList();
                    CrmReviewResult.CrmReviewResultDetail question1 = null;
                    CrmReviewResult.CrmReviewResultDetail question2 = null;
                    if (resultDetailList != null && resultDetailList.size() > 0) {
                        if (resultDetailList.get(0) != null) {
                            question1 = resultDetailList.get(0);
                            if (resultDetailList.size() > 1 && resultDetailList.get(1) != null) {
                                question2 = resultDetailList.get(1);
                            }
                        }
                    }
                    if (!res.getGradeDistributionFlag()) {
                        createCell(row, col++, cellStyle, "年级分布");
                        createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.formatGradeDistribution()));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question1 != null ? question1.getGradeDistribution() : ""));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question2 != null ? question2.getGradeDistribution() : ""));
                    }
                    if (!res.getSchoolingLengthFlag()) {
                        createCell(row, col++, cellStyle, "学制");
                        createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.getEduSystem()));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question1 != null ? question1.getSchoolingLength() : ""));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question2 != null ? question2.getSchoolingLength() : ""));
                    }
                    if (!res.getExternOrBoarderFlag()) {
                        createCell(row, col++, cellStyle, "走读住宿");
                        createCell(row, col++, cellStyle, getExternOrBoarder(crmSchoolClue.getExternOrBoarder()));
                        createCell(row, col++, cellStyle, question1 != null ? getExternOrBoarder(question1.getExternOrBoarder()) : "");
                        createCell(row, col++, cellStyle, question2 != null ? getExternOrBoarder(question2.getExternOrBoarder()) : "");
                    }
                    if (!res.getEnglishStartGradeFlag()) {
                        createCell(row, col++, cellStyle, "英语起始年级");
                        createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.getEnglishStartGrade()));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question1 != null ? question1.getEnglishStartGrade() : ""));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question2 != null ? question2.getEnglishStartGrade() : ""));
                    }
                    if (!res.getSchoolSizeFlag()) {
                        createCell(row, col++, cellStyle, "学校规模");
                        createCell(row, col++, cellStyle, ConversionUtils.toString(crmSchoolClue.sumStudentCount()));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question1 != null ? question1.getSchoolSize() : ""));
                        createCell(row, col++, cellStyle, ConversionUtils.toString(question2 != null ? question2.getSchoolSize() : ""));
                    }
                    if (!res.getBranchSchoolIdsFlag()) {
                        createCell(row, col++, cellStyle, "关联分校");
                        createCell(row, col++, cellStyle, getBranchSchoolName(crmSchoolClue.getBranchSchoolIds()));
                        createCell(row, col++, cellStyle, question1 != null ? ConversionUtils.toString(getBranchSchoolName(question1.getBranchSchoolIds())) : "");
                        createCell(row, col++, cellStyle, question2 != null ? ConversionUtils.toString(getBranchSchoolName(question2.getBranchSchoolIds())) : "");
                    }
                }
            }
            return workbook;
        } catch (Exception ex) {
            logger.error("export review re - Excp : {}; schoolIds = {}", ex, schoolIds);
            return null;
        }
    }

    private String getExternOrBoarder(Integer externOrBoarder) {
        if (externOrBoarder == null) {
            return "";
        }
        switch (externOrBoarder) {
            case 1:
                return "走读";
            case 2:
                return "住宿";
            case 3:
                return "走读并且住宿";
            default:
                return "";
        }
    }

    private String getBranchSchoolName(Set<Long> branchSchoolIds) {
        if (CollectionUtils.isEmpty(branchSchoolIds)) {
            return "";
        }
        try {
            Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                    .loadSchools(branchSchoolIds)
                    .getUninterruptibly();
            Collection<School> schools = schoolMap.values();
            List<String> schoolName = schools.stream().map(School::getCname).collect(Collectors.toList());
            return StringUtils.join(schoolName, ",");
        } catch (Exception ex) {
            return "";
        }
    }

    private XSSFCell createCell(XSSFRow row, int index, XSSFCellStyle style, String value) {
        XSSFCell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
        return cell;
    }

    private List<CrmReviewResult> getReviewResultBySchoolIds(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return crmReviewResultDao.findAll();
        }
        return crmReviewResultDao.findLastestBySchoolIds(schoolIds);
    }


}
