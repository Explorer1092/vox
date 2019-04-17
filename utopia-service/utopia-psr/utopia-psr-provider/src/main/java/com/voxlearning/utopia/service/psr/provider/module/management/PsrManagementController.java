/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.provider.module.management;

//import com.voxlearning.alps.annotation.management.UrlPath;

import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.api.context.ApplicationContextScanner;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.psr.entity.*;
import com.voxlearning.utopia.service.psr.entity.termreport.*;
import com.voxlearning.utopia.service.psr.impl.appen.PsrAppEnController;
import com.voxlearning.utopia.service.psr.impl.appen.PsrAppEnMatchController;
import com.voxlearning.utopia.service.psr.impl.appen.PsrAppEnNewController;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.appmath.PsrAppMathController;
import com.voxlearning.utopia.service.psr.impl.appmath.PsrAppMathNewController;
import com.voxlearning.utopia.service.psr.impl.competition.CompetitionQids;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContext;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamPaperData;
import com.voxlearning.utopia.service.psr.impl.examcn.PsrExamCnController;
import com.voxlearning.utopia.service.psr.impl.examcn.PsrExamCnCore;
import com.voxlearning.utopia.service.psr.impl.examen.PsrExamEnController;
import com.voxlearning.utopia.service.psr.impl.exammath.PsrExamMathController;
import com.voxlearning.utopia.service.psr.impl.exammath.PsrExamMathCore;
import com.voxlearning.utopia.service.psr.impl.selfstudy.PsrSelfStudyRecomByKp;
import com.voxlearning.utopia.service.psr.impl.selfstudy.PsrSelfStudySimilar;
import com.voxlearning.utopia.service.psr.impl.similarity.PsrExamEnSimilarExController;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.psr.impl.termreport.service.PsrTermReportIPackageService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PSR management controller implementation.
 *
 * @author Xiaohai Zhang
 * @since Dec 12, 2014
 */
@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/psr")
public class PsrManagementController {

    public static final PsrManagementController INSTANCE = new PsrManagementController();

    private PsrExamCnController getPsrExamCnController() {
        return ApplicationContextScanner.getInstance().getBean(PsrExamCnController.class);
    }
    private PsrAppEnController getPsrAppEnController() {
        return ApplicationContextScanner.getInstance().getBean(PsrAppEnController.class);
    }
    private PsrAppMathController getPsrAppMathController() {
        return ApplicationContextScanner.getInstance().getBean(PsrAppMathController.class);
    }
    private PsrExamEnController getPsrExamEnController() {
        return ApplicationContextScanner.getInstance().getBean(PsrExamEnController.class);
    }
    private PsrExamMathController getPsrExamMathController() {
        return ApplicationContextScanner.getInstance().getBean(PsrExamMathController.class);
    }
    private PsrAppEnMatchController getPsrAppEnMatchController() {
        return ApplicationContextScanner.getInstance().getBean(PsrAppEnMatchController.class);
    }
    private PsrExamEnSimilarExController getPsrExamEnSimilarExController() {
        return ApplicationContextScanner.getInstance().getBean(PsrExamEnSimilarExController.class);
    }
    private NewContentLoaderClient getNewContentLoaderClient() {
        return ApplicationContextScanner.getInstance().getBean(NewContentLoaderClient.class);
    }
    private PsrTermReportIPackageService getPsrTermReportIPackageService() {
        return ApplicationContextScanner.getInstance().getBean(PsrTermReportIPackageService.class);
    }

    @RequestMapping(value = "index.do", method = RequestMethod.GET)
    public String index() {
        return "psr_management";
    }

    @RequestMapping(value = "examcn.do", method = RequestMethod.GET)
    public String examcn(Model model,
                         @RequestParam(name = "userId", defaultValue = "0") Long userId,
                         @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
                         @RequestParam(name = "bookId", defaultValue = "") String bookId,
                         @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                         @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                         @RequestParam(name = "grade", defaultValue = "3") Integer grade,
                         @RequestParam(name = "minP", defaultValue = "0.8") Float minP,
                         @RequestParam(name = "maxP", defaultValue = "0.95") Float maxP,
                         @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String product = "TEST";
        String uType = "student";
        String retMsg = "return empty";

        Map<String, String> para = new HashMap<>();

        if (Long.compare(userId, 0L) != 0) {
            Date dtB = new Date();

            PsrExamContent ret = getPsrExamCnController().deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            if (ret != null)
                retMsg = ret.formatList("ExamCn") + "[TotalTime:" + uTAll.toString() + "]";
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("minP", Float.toString(minP));
        model.addAttribute("maxP", Float.toString(maxP));
        model.addAttribute("grade", Integer.toString(grade));
        model.addAttribute("action", "examcn");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_examen";
    }

    @RequestMapping(value = "appen.do", method = RequestMethod.GET)
    public String appen(Model model,
                        @RequestParam(name = "userId", defaultValue = "0") Long userId,
                        @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
                        @RequestParam(name = "bookId", defaultValue = "") String bookId,
                        @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                        @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                        @RequestParam(name = "matchCount", defaultValue = "3") Integer matchCount,
                        @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String product = "TEST";
        String uType = "student";
        String retMsg = "return empty";

        if (Long.compare(userId, 0L) != 0) {
            PsrAppEnNewController psrAppEnNewController = ApplicationContextScanner.getInstance().getBean(PsrAppEnNewController.class);
            PsrPrimaryAppEnContent ret = psrAppEnNewController.deal(product, userId, regionCode, bookId, unitId, eCount, "");
            if (ret != null)
                retMsg = ret.formatList();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("matchCount", Integer.toString(matchCount));
        model.addAttribute("action", "appen");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_appmathen";
    }

    @RequestMapping(value = "appmath.do", method = RequestMethod.GET)
    public String appmath(Model model,
                          @RequestParam(name = "userId", defaultValue = "0") Long userId,
                          @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
                          @RequestParam(name = "bookId", defaultValue = "") String bookId,
                          @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                          @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                          @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult,
                          @RequestParam(name = "matchCount", defaultValue = "4") Integer matchCount) {
        String product = "TEST";
        String uType = "student";

        String retMsg = "return empty";

        if (Long.compare(userId, 0L) != 0) {
            PsrAppMathNewController psrAppEnNewController = ApplicationContextScanner.getInstance().getBean(PsrAppMathNewController.class);
            PsrPrimaryAppMathContent ret = psrAppEnNewController.deal(product, userId, regionCode, bookId, unitId, eCount);
            if (ret != null)
                retMsg = ret.formatList();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("matchCount", Long.toString(matchCount));
        model.addAttribute("action", "appmath");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_appmathen";
    }


    @RequestMapping(value = "appenmatch.do", method = RequestMethod.GET)
    public String appenmatch(Model model,
                 @RequestParam(name = "userId", defaultValue = "0") Long userId,
                 @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
                 @RequestParam(name = "bookId", defaultValue = "") String bookId,
                 @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                 @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                 @RequestParam(name = "matchCount", defaultValue = "3") Integer matchCount,
                 @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String product = "TEST";
        String uType = "student";
        String retMsg = "return empty";

        if (Long.compare(userId, 0L) != 0) {
            PsrPrimaryAppEnMatchContent ret = getPsrAppEnMatchController().deal(product, userId, regionCode, bookId, unitId, eCount, "", matchCount);
            if (ret != null)
                retMsg = ret.formatList();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("matchCount", Integer.toString(matchCount));
        model.addAttribute("action", "appenmatch");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_appmathen";
    }

    @RequestMapping(value = "examen.do", method = RequestMethod.GET)
    public String examen(Model model,
            @RequestParam(name = "userId", defaultValue = "0") Long userId,
            @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
            @RequestParam(name = "bookId", defaultValue = "") String bookId,
            @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
            @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
            @RequestParam(name = "grade", defaultValue = "3") Integer grade,
            @RequestParam(name = "minP", defaultValue = "0.8") Float minP,
            @RequestParam(name = "maxP", defaultValue = "0.95") Float maxP,
            @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String product = "TEST";
        String uType = "student";

        if (!StringUtils.isBlank(bookId) && StringUtils.isNumeric(bookId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(bookId));
            if (newBookProfile != null)
                bookId = newBookProfile.getId();
        }

        if (!StringUtils.isBlank(unitId) && StringUtils.isNumeric(unitId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(unitId));
            if (newBookProfile != null)
                unitId = newBookProfile.getId();
        }

        String retMsg = "return empty";
        Map<String, String> para = new HashMap<>();
        if (Long.compare(userId, 0L) != 0 && !StringUtils.isBlank(bookId)) {
            Date dtB = new Date();
            PsrExamContent ret = getPsrExamEnController().deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            if (ret != null)
                retMsg = ret.formatList() + "[TotalTime:" + uTAll.toString() + "]";
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("minP", Float.toString(minP));
        model.addAttribute("maxP", Float.toString(maxP));
        model.addAttribute("grade", Integer.toString(grade));
        model.addAttribute("action", "examen");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_examen";
    }

    /**
     * PSR management controller implementation.
     * Controller to test monthly term report
     * @author Dongxue ZHAO
     * @since Jul 21, 2017
     */
    //Long groupId, String unitId, String kpId
    @RequestMapping(value = "termreportMonth.do", method = RequestMethod.GET)
    public String termreportMonth(Model model,
                                  @RequestParam(name = "yearId", defaultValue = "2016") Integer yearId,
                                  @RequestParam(name = "termId", defaultValue = "1") Integer termId,
                                  @RequestParam(name = "group_id", defaultValue = "1000546") Integer group_id,
                                  @RequestParam(name = "subjectId", defaultValue = "ENGLISH") String subjectId) {
        String retMsg = "False input; Return empty";
        if (group_id > 0 && subjectId != null && subjectId.length() > 0 && yearId > 0) {
            TermReportPackage retObject =  getPsrTermReportIPackageService().loadTermtReportPackage(yearId, termId, group_id, subjectId);
            if (retObject != null) {
                List<StudentTermReport> retStudentInfos = retObject.getStudentTermReports();
                List<MonthLayoutInfo> retMonthInfos = retObject.getMonthLayoutInfos();
                retMsg = "TermReportPackage(<p>" +
                        "monthLayoutInfos=[<p>";
                for(MonthLayoutInfo monthInfo : retMonthInfos) {
                    retMsg += "&nbsp;&nbsp;&nbsp;&nbsp;" + monthInfo.toString() + ",<p>";
                }
                retMsg += "],<br>StudentTermReports=[<p>";
                for(StudentTermReport studentInfo : retStudentInfos) {
                    retMsg += "&nbsp;&nbsp;&nbsp;&nbsp;" + studentInfo.toString() + ",<p>";
                }
                retMsg += "]<p>)<p>";
                model.addAttribute("retMsg", retMsg);
            } else {
                retMsg = "No result for " + "yearId:" + yearId.toString() +
                        "&nbsp;&nbsp;termId:" + termId.toString() +
                        "&nbsp;&nbsp;groupId:" + group_id +
                        "&nbsp;&nbsp;subjectId:" + subjectId;
            }
        }
        model.addAttribute("retMsg", retMsg);
        model.addAttribute("group_id", group_id);
        model.addAttribute("yearId", yearId);
        model.addAttribute("termId", termId);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("action", "termreportMonth");
        return "termreportMonth";
    }
    /**
     * PSR management controller implementation.
     * Controller to test daily term report
     * @author Dongxue ZHAO
     * @since Jul 21, 2017
     */
    //Long groupId, String unitId, String kpId
    @RequestMapping(value = "termreportDaily.do", method = RequestMethod.GET)
    public String termreportDaily(Model model,
                                  @RequestParam(name = "group_id", defaultValue = "1000001") Integer group_id,
                                  @RequestParam(name = "unit_id", defaultValue = "BKC_10300106812399") String unit_id) {
        String retMsg = "False input; Return empty";
        if (group_id > 0 && unit_id != null && unit_id.length() > 0) {
            GroupUnitReportPackage retObject =  getPsrTermReportIPackageService().loadGroupUnitReportPackage(group_id, unit_id);
            if (retObject != null && retObject.getLayoutHomeworkTimes() != null && retObject.getStudentGroupUnitReport() != null) {
                Integer homeworkTimes = retObject.getLayoutHomeworkTimes();
                List<StudentGroupUnitReport> retStudentInfos = retObject.getStudentGroupUnitReport();
                retMsg = "GroupUnitReportPackage(<p>" +
                        "LayoutHomeworkTimes=" + homeworkTimes.toString();
                retMsg += ",<br>StudentGroupUnitReport[" + retStudentInfos.size() +"]=[<p>";
                for(StudentGroupUnitReport studentInfo : retStudentInfos) {
                    retMsg += "&nbsp;&nbsp;&nbsp;&nbsp;" + studentInfo.toString() + ",<p>";
                }
                retMsg += "]<p>)<p>";
                model.addAttribute("retMsg", retMsg);
            } else {
                retMsg = "No result for: " + "group_id:" + group_id + "&nbsp;&nbsp;unit_id:" + unit_id
                    + "<p>" + retObject.toString();
            }
        }
        model.addAttribute("retMsg", retMsg);
        model.addAttribute("group_id", group_id);
        model.addAttribute("unit_id", unit_id);
        model.addAttribute("action", "termreportDaily");
        return "termreportDaily";
    }















    @RequestMapping(value = "exammath.do", method = RequestMethod.GET)
    public String exammath(Model model,
            @RequestParam(name = "userId", defaultValue = "0") Long userId,
            @RequestParam(name = "regionCode", defaultValue = "0") Integer regionCode,
            @RequestParam(name = "bookId", defaultValue = "") String bookId,
            @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
            @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
            @RequestParam(name = "grade", defaultValue = "3") Integer grade,
            @RequestParam(name = "minP", defaultValue = "0.8") Float minP,
            @RequestParam(name = "maxP", defaultValue = "0.95") Float maxP,
            @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String product = "TEST";
        String uType = "student";
        String retMsg = "return empty";

        if (Long.compare(userId, 0L) != 0) {
            Date dtB = new Date();

            PsrExamContent ret = getPsrExamMathController().deal(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
            Date dtE = new Date();
            Long uTAll = dtE.getTime() - dtB.getTime();
            if (ret != null)
                retMsg = ret.formatList("ExamMath") + "[TotalTime:" + uTAll.toString() + "]";
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", Integer.toString(regionCode));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("minP", Float.toString(minP));
        model.addAttribute("maxP", Float.toString(maxP));
        model.addAttribute("grade", Integer.toString(grade));
        model.addAttribute("action", "exammath");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_examen";
    }

    @RequestMapping(value = "similar.do", method = RequestMethod.GET)
    public String similar(Model model,
            @RequestParam(name = "userId", defaultValue = "0") Long userId,
            @RequestParam(name = "regionCode", defaultValue = "requiredEids") String regionCode,
            @RequestParam(name = "bookId", defaultValue = "") String bookId,
            @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
            @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
            @RequestParam(name = "grade", defaultValue = "3") Integer grade,
            @RequestParam(name = "minP", defaultValue = "0.8") Float minP,
            @RequestParam(name = "maxP", defaultValue = "0.95") Float maxP,
            @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String eidStr = regionCode;
        String[] eids = eidStr.split(",");

        String product = "TEST";
        String uType = "student";
        String retMsg = "return empty";

        if (Long.compare(userId, 0L) != 0 && eids.length > 0 && !eidStr.equals("requiredEids")) {
            PsrExamEnSimilarContentEx ret = getPsrExamEnSimilarExController().deal(product, userId, Arrays.asList(eids), eCount);
            if (ret != null)
                retMsg = ret.formatList();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("regionCode", eidStr);
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("minP", Float.toString(minP));
        model.addAttribute("maxP", Float.toString(maxP));
        model.addAttribute("grade", Integer.toString(grade));
        model.addAttribute("action", "similar");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_examen";
    }

    @RequestMapping(value = "bookinfo.do", method = RequestMethod.GET)
    public String bookinfo(Model model,
                         @RequestParam(name = "bookId", defaultValue = "") String bookId,
                         @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                         @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        if (!StringUtils.isBlank(bookId) && StringUtils.isNumeric(bookId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(bookId));
            if (newBookProfile != null)
                bookId = newBookProfile.getId();
        }

        if (!StringUtils.isBlank(unitId) && StringUtils.isNumeric(unitId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(unitId));
            if (newBookProfile != null)
                unitId = newBookProfile.getId();
        }

        // product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade
        PsrExamContext psrExamContext = new PsrExamContext("test", "student", 33333L, 110100, bookId, unitId, 9, 0.75f, 0.89f, 3);

        Map<String, Object> bookInfo = new HashMap<>();
        if ( ! StringUtils.isBlank(bookId)) {
            Map<String, Integer> unitKps = new HashMap<>();

            PsrExamEnController psrExamEnController = ApplicationContextScanner.getInstance().getBean(PsrExamEnController.class);
            PsrBooksSentencesNew psrBooksSentencesNew = ApplicationContextScanner.getInstance().getBean(PsrBooksSentencesNew.class);
            PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(bookId);
            if (psrBookPersistenceNew != null) {
                List<Object> modules = formatPsrUnitPersistence(psrBookPersistenceNew.getModulePersistenceMap(), onlyResult);
                List<Object> units = formatPsrUnitPersistence(psrBookPersistenceNew.getUnitPersistenceMap(), onlyResult);
                if (!CollectionUtils.isEmpty(modules)) {
                    psrBookPersistenceNew.getModulePersistenceMap().values().stream().forEach(p -> unitKps.put(p.getUnitId(), p.getSentences().size()));
                } else if (!CollectionUtils.isEmpty(units)) {
                    psrBookPersistenceNew.getUnitPersistenceMap().values().stream().forEach(p -> unitKps.put(p.getUnitId(), p.getSentences().size()));
                }

                bookInfo.put("curBookId", psrBookPersistenceNew.getBookId());
                bookInfo.put("curBookName", psrBookPersistenceNew.getName());
                bookInfo.put("curBookStatus", psrBookPersistenceNew.getStatus());
                bookInfo.put("curBookSubjectId", psrBookPersistenceNew.getSubjectId());
                bookInfo.put("curSeriesId", psrBookPersistenceNew.getSeriesId());
                bookInfo.put("curBookKpsNum", unitKps.values().stream().reduce((sum, kps) -> sum + kps).get());
                if (onlyResult >= 2) {
                    bookInfo.put("modules", modules);
                    bookInfo.put("units", units);
                    PaperLoaderClient paperLoaderClient = ApplicationContextScanner.getInstance().getBean(PaperLoaderClient.class);
                    PsrExamPaperData psrExamPaperData = ApplicationContextScanner.getInstance().getBean(PsrExamPaperData.class);
                    if (onlyResult >= 3) {
                        List<NewPaper> papers = paperLoaderClient.loadPaperAsListByNewBookIds(Collections.singleton(psrExamContext.getBookId()), null);
                        bookInfo.put("curBookPaperNum", CollectionUtils.isEmpty(papers) ? 0 : papers.size());
                        Map<String, Boolean> paperQids = psrExamPaperData.getPaperQidsByBookId(psrExamContext);
                        bookInfo.put("curBookPaperQidNum", MapUtils.isEmpty(paperQids) ? 0 : paperQids.size());
                    }
                }
            }

            // 前一本教材
            Map<String, Object> preBook = new HashMap<>();
            NewBookProfile preBookProfile = psrBooksSentencesNew.getPreBookWithSameSeriesByBookId(bookId);
            if (preBookProfile != null) {
                preBook.put("bookId", preBookProfile.getId());
                preBook.put("bookName", preBookProfile.getName());
            }
            bookInfo.put("preBook", preBook);
        }
        String retMsg = MapUtils.isEmpty(bookInfo) ? "return empty" : JsonUtils.toJson(bookInfo);

        if (onlyResult >= 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("action", "bookinfo");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_bookinfo";
    }

    @RequestMapping(value = "bookunitqidnum.do", method = RequestMethod.GET)
    public String bookunitqidnum(Model model,
                           @RequestParam(name = "bookId", defaultValue = "") String bookId,
                           @RequestParam(name = "unitId", defaultValue = "-1") String unitId,
                           @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        if (!StringUtils.isBlank(bookId) && StringUtils.isNumeric(bookId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(bookId));
            if (newBookProfile != null)
                bookId = newBookProfile.getId();
        }

        if (!StringUtils.isBlank(unitId) && StringUtils.isNumeric(unitId)) {
            NewBookProfile newBookProfile = getNewContentLoaderClient().loadNewBookProfileByOldId(Subject.ENGLISH, SafeConverter.toLong(unitId));
            if (newBookProfile != null)
                unitId = newBookProfile.getId();
        }

        // product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade
        PsrExamContext psrExamContext = new PsrExamContext("test", "student", 33333L, 110100, bookId, unitId, 9, 0.75f, 0.89f, 3);

        Map<String, Object> bookInfo = new HashMap<>();
        if ( ! StringUtils.isBlank(bookId)) {
            Map<String, Integer> unitKpss = new HashMap<>();

            EkCouchbaseDao ekCouchbaseDao = ApplicationContextScanner.getInstance().getBean(EkCouchbaseDao.class);
            PsrExamEnController psrExamEnController = ApplicationContextScanner.getInstance().getBean(PsrExamEnController.class);
            PsrBooksSentencesNew psrBooksSentencesNew = ApplicationContextScanner.getInstance().getBean(PsrBooksSentencesNew.class);
            PsrExamCnCore psrExamCnCore = ApplicationContextScanner.getInstance().getBean(PsrExamCnCore.class);
            PsrBookPersistenceNew psrBookPersistenceNew = psrBooksSentencesNew.getBookPersistenceByBookId(bookId);
            if (psrBookPersistenceNew != null) {
                psrExamContext.setSubject(Subject.fromSubjectId(psrBookPersistenceNew.getSubjectId()));
                psrExamContext.setGrade(psrBookPersistenceNew.getClazzLevel());

                // 获取所有知识点
                Map<String/*kp*/, List<String>> kpToQids = new HashMap<>();
                Map<String/*moduleid*/, Map<String/*kp*/, Integer>> moduleKpsNum = new HashMap<>();
                Map<String/*unitid*/, Map<String/*kp*/, Integer>> unitKpsNum = new HashMap<>();
                Map<String, Integer> moduleQidNum = new HashMap<>();
                Map<String, Integer> unitQidNum = new HashMap<>();

                initBookUnitInfo(psrExamContext, psrBookPersistenceNew, kpToQids, moduleKpsNum, unitKpsNum, moduleQidNum, unitQidNum);

                bookInfo.put("curBookId", psrBookPersistenceNew.getBookId());
                bookInfo.put("curBookName", psrBookPersistenceNew.getName());
                bookInfo.put("curBookStatus", psrBookPersistenceNew.getStatus());
                bookInfo.put("curBookSubjectId", psrBookPersistenceNew.getSubjectId());
                bookInfo.put("curSeriesId", psrBookPersistenceNew.getSeriesId());
                bookInfo.put("moduleQidNum", moduleQidNum);
                bookInfo.put("unitQidNum", unitQidNum);

                if (onlyResult >= 2) {
                    bookInfo.put("curBookModuleKps", moduleKpsNum);
                    bookInfo.put("curBookUnitKps", unitKpsNum);
                    if (onlyResult >= 3) {
                        bookInfo.put("curBookKpsQids", kpToQids);
                    }
                }
            }
        }
        String retMsg = MapUtils.isEmpty(bookInfo) ? "return empty" : JsonUtils.toJson(bookInfo);

        if (onlyResult >= 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitId", unitId);
        model.addAttribute("action", "bookunitqidnum");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_bookinfo";
    }

    private Map<String, List<String>> getUnitQidsFromString(String line) {
        Map<String, List<String>> retMap = new HashMap<>();
        if (StringUtils.isBlank(line))
            return retMap;

        String[] unitInfoArr = line.split(";");
        if (unitInfoArr.length > 0) {
            for (String unitInfo : unitInfoArr) {
                String[] tmpArr = unitInfo.split(":");
                if (tmpArr.length < 2)
                    continue;
                String unitId = tmpArr[0];
                List<String> qids = new ArrayList<>();
                for (int i = 1; i< tmpArr.length; i++) {
                    qids.add(tmpArr[i]);
                }
                retMap.put(unitId, qids);
            }
        }
        return retMap;
    }

    @RequestMapping(value = "selfstudysimilar.do", method = RequestMethod.GET)
    public String selfstudysimilar(Model model,
                          @RequestParam(name = "userId", defaultValue = "0") Long userId,
                          @RequestParam(name = "bookId", defaultValue = "") String bookId,
                          @RequestParam(name = "unitQids", defaultValue = "") String unitQids,
                          @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                          @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {

        String retMsg = "return empty";
        Map<String, List<String>> unitInfoArr = getUnitQidsFromString(unitQids);
        if (StringUtils.isNotBlank(bookId) && MapUtils.isNotEmpty(unitInfoArr)) {
            Map<String, List<String>> ret = ApplicationContextScanner.getInstance().getBean(PsrSelfStudySimilar.class).deal(bookId, unitInfoArr, eCount);
            if (MapUtils.isNotEmpty(ret))
                retMsg = ret.toString();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitQids", unitQids);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("action", "selfstudysimilar");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_selfstudy";
    }

    @RequestMapping(value = "selfstudykprecomm.do", method = RequestMethod.GET)
    public String selfstudykprecomm(Model model,
                          @RequestParam(name = "userId", defaultValue = "0") Long userId,
                          @RequestParam(name = "bookId", defaultValue = "") String bookId,
                          @RequestParam(name = "unitQids", defaultValue = "") String unitQids,
                          @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                          @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {


        String retMsg = "return empty";
        Map<String, List<String>> unitInfoArr = getUnitQidsFromString(unitQids);
        if (StringUtils.isNotBlank(bookId) && MapUtils.isNotEmpty(unitInfoArr)) {
            Map<String, List<String>> ret = ApplicationContextScanner.getInstance().getBean(PsrSelfStudyRecomByKp.class).deal(userId, bookId, unitInfoArr, eCount);
            if (MapUtils.isNotEmpty(ret))
                retMsg = ret.toString();
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("bookId", bookId);
        model.addAttribute("unitQids", unitQids);
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("action", "selfstudykprecomm");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_selfstudy";
    }

    /*
     * Engish or Math
     * DayCompetition, //资格赛
     * WeekCompetition, //周排位赛
     * MonthCompetition, //月排位赛
     */
    @RequestMapping(value = "competitionqids.do", method = RequestMethod.GET)
    public String selfstudykprecomm(Model model,
                                    @RequestParam(name = "userId", defaultValue = "0") Long userId,
                                    @RequestParam(name = "subjectId", defaultValue = "102") Integer subjectId,
                                    @RequestParam(name = "grade", defaultValue = "1") Integer grade,
                                    @RequestParam(name = "term", defaultValue = "1") Integer term,
                                    @RequestParam(name = "eCount", defaultValue = "1") Integer eCount,
                                    @RequestParam(name = "type", defaultValue = "DayCompetition") String type,
                                    @RequestParam(name = "onlyResult", defaultValue = "0") Integer onlyResult) {


        String retMsg = "return empty";

        Subject subject = Subject.fromSubjectId(subjectId);

        if (Subject.ENGLISH.equals(subject) || Subject.MATH.equals(subject)) {
            if (userId > 0 && Arrays.asList("DayCompetition", "WeekCompetition", "MonthCompetition").contains(type)) {
                Set<String> ret = ApplicationContextScanner.getInstance().getBean(CompetitionQids.class).deal(userId, Subject.fromSubjectId(subjectId), grade, term, eCount, type);
                if (CollectionUtils.isNotEmpty(ret))
                    retMsg = ret.toString();
            }
        }

        if (onlyResult == 1) {
            model.addAttribute("retMsg", retMsg);
            return "psr_result";
        }

        model.addAttribute("retMsg", retMsg);
        model.addAttribute("userId", Long.toString(userId));
        model.addAttribute("subjectId", Integer.toString(subjectId));
        model.addAttribute("grade", Integer.toString(grade));
        model.addAttribute("term", Integer.toString(term));
        model.addAttribute("eCount", Long.toString(eCount));
        model.addAttribute("type", type);
        model.addAttribute("action", "competitionqids");
        model.addAttribute("onlyResult", Integer.toString(onlyResult));

        return "psr_competitionqids";
    }

    private void initBookUnitInfo(PsrExamContext psrExamContext, PsrBookPersistenceNew psrBookPersistenceNew,
                                  Map<String/*kp*/, List<String>> kpToQids,
                                  Map<String/*moduleid*/, Map<String/*kp*/, Integer>> moduleKpsNum,
                                  Map<String/*unitid*/, Map<String/*kp*/, Integer>> unitKpsNum,
                                  Map<String, Integer> moduleQidNum, Map<String, Integer> unitQidNum ) {

        if (Subject.CHINESE.equals(psrExamContext.getSubject())) {
            getChineseBookUnitInfo(psrBookPersistenceNew, kpToQids, moduleKpsNum, unitKpsNum);
        } else {
            getEnAndMathBookUnitInfo(psrBookPersistenceNew, kpToQids, moduleKpsNum, unitKpsNum);
        }

        // 获取知识点挂载的题目
        getKPFromCouchbase(psrExamContext, psrBookPersistenceNew, kpToQids);

        // 按Unit/Module归类
        getTogether(kpToQids, moduleKpsNum, unitKpsNum, moduleQidNum, unitQidNum);
    }


    private void getKPFromCouchbase(PsrExamContext psrExamContext, PsrBookPersistenceNew psrBookPersistenceNew, Map<String/*kp*/, List<String>> kpToQids) {
        EkCouchbaseDao ekCouchbaseDao = ApplicationContextScanner.getInstance().getBean(EkCouchbaseDao.class);
        PsrExamCnCore psrExamCnCore = ApplicationContextScanner.getInstance().getBean(PsrExamCnCore.class);
        PsrExamMathCore psrExamMathCore = ApplicationContextScanner.getInstance().getBean(PsrExamMathCore.class);

        EkEidListContent ekEidListTmp = psrExamMathCore.getMathNewKpQids(psrExamContext, kpToQids.keySet().stream().collect(Collectors.toList()), 500);
        Map<String, EkToEidContent> mathNewKpEids = new HashMap<>();
        ekEidListTmp.getEkList().stream().forEach(p -> {
            mathNewKpEids.put(p.getEk(), p);
        });

        // 获取知识点挂载的题目
        for (String kpId : kpToQids.keySet()) {
            EkToEidContent ekToEidContent = null;

            // from couchbase
            if (ekCouchbaseDao != null) {
                if (Subject.CHINESE.equals(psrExamContext.getSubject())) {
                    ekToEidContent = psrExamCnCore.getEidsByLessons(Collections.singletonList(kpId));
                } else {
                    EkEidListContent ekEidListContentEx = null;
                    if (psrBookPersistenceNew.getSubjectId() == Subject.MATH.getId() &&  psrBookPersistenceNew.getLatestVersion() == 1) {
                        ekToEidContent = mathNewKpEids.containsKey(kpId) ? mathNewKpEids.get(kpId) : null;
                    } else {
                        ekEidListContentEx = ekCouchbaseDao.getEkEidListContentFromCouchbaseByEk(kpId, psrExamContext.getGrade());
                        if (ekEidListContentEx == null || CollectionUtils.isEmpty(ekEidListContentEx.getEkList())) {
                            // 该知识点 没有取到 题, 重新获取知识点 , continue for {get ek, get eids}
                            continue;
                        }
                        ekToEidContent = ekEidListContentEx.getEkList().get(0);
                    }
                }
            }

            if (ekToEidContent == null || CollectionUtils.isEmpty(ekToEidContent.getEidList()))
                continue;

            kpToQids.get(kpId).addAll(
                    ekToEidContent.getEidList().stream().map(EidItem::getEid).collect(Collectors.toList())
            );
        }
    }

    private void getTogether(Map<String/*kp*/, List<String>> kpToQids,
                             Map<String/*moduleid*/, Map<String/*kp*/, Integer>> moduleKpsNum,
                             Map<String/*unitid*/, Map<String/*kp*/, Integer>> unitKpsNum,
                             Map<String, Integer> moduleQidNum, Map<String, Integer> unitQidNum) {
        // 按Unit/Module归类
        for (Map.Entry<String/*moduleid*/, Map<String/*kp*/, Integer>> entry : moduleKpsNum.entrySet()) {
            moduleQidNum.put(entry.getKey(), 0);
            entry.getValue().keySet().stream().filter(kpToQids::containsKey)
                    .forEach(kp -> {
                        moduleQidNum.put(entry.getKey(), moduleQidNum.get(entry.getKey()) + kpToQids.get(kp).size());
                        entry.getValue().put(kp, kpToQids.get(kp).size());
                    });
        }
        for (Map.Entry<String/*moduleid*/, Map<String/*kp*/, Integer>> entry : unitKpsNum.entrySet()) {
            unitQidNum.put(entry.getKey(), 0);
            entry.getValue().keySet().stream().filter(kpToQids::containsKey)
                    .forEach(kp -> {
                        unitQidNum.put(entry.getKey(), unitQidNum.get(entry.getKey()) + kpToQids.get(kp).size());
                        entry.getValue().put(kp, kpToQids.get(kp).size());
                    });
        }
    }
    private void getChineseBookUnitInfo(PsrBookPersistenceNew psrBookPersistenceNew, Map<String/*kp*/, List<String>> kpToQids,
                                        Map<String/*moduleid*/, Map<String/*kp*/, Integer>> moduleKpsNum,
                                        Map<String/*unitid*/, Map<String/*kp*/, Integer>> unitKpsNum) {

        EkCouchbaseDao ekCouchbaseDao = ApplicationContextScanner.getInstance().getBean(EkCouchbaseDao.class);
        PsrExamEnController psrExamEnController = ApplicationContextScanner.getInstance().getBean(PsrExamEnController.class);
        PsrBooksSentencesNew psrBooksSentencesNew = ApplicationContextScanner.getInstance().getBean(PsrBooksSentencesNew.class);
        if (MapUtils.isNotEmpty(psrBookPersistenceNew.getModulePersistenceMap())) {
            psrBookPersistenceNew.getModulePersistenceMap().values().stream().filter(p -> {return MapUtils.isNotEmpty(p.getLessonPersistenceMap());})
                    .forEach(p -> {
                        moduleKpsNum.put(p.getUnitId(), new HashMap<>());
                        p.getLessonPersistenceMap().keySet().forEach(m -> {
                            moduleKpsNum.get(p.getUnitId()).put(m, 0);
                            kpToQids.put(m, new ArrayList<>());
                        });
                    });
        }
        if (MapUtils.isNotEmpty(psrBookPersistenceNew.getUnitPersistenceMap())) {
            psrBookPersistenceNew.getUnitPersistenceMap().values().stream().filter(p -> {return MapUtils.isNotEmpty(p.getLessonPersistenceMap());})
                    .forEach(p -> {
                        unitKpsNum.put(p.getUnitId(), new HashMap<>());
                        p.getLessonPersistenceMap().keySet().forEach(m -> {
                            unitKpsNum.get(p.getUnitId()).put(m, 0);
                            kpToQids.put(m, new ArrayList<>());
                        });
                    });
        }
    }

    private void getEnAndMathBookUnitInfo(PsrBookPersistenceNew psrBookPersistenceNew, Map<String/*kp*/, List<String>> kpToQids,
                                          Map<String/*moduleid*/, Map<String/*kp*/, Integer>> moduleKpsNum,
                                          Map<String/*unitid*/, Map<String/*kp*/, Integer>> unitKpsNum) {

        EkCouchbaseDao ekCouchbaseDao = ApplicationContextScanner.getInstance().getBean(EkCouchbaseDao.class);
        PsrExamEnController psrExamEnController = ApplicationContextScanner.getInstance().getBean(PsrExamEnController.class);
        PsrBooksSentencesNew psrBooksSentencesNew = ApplicationContextScanner.getInstance().getBean(PsrBooksSentencesNew.class);
        PsrExamCnCore psrExamCnCore = ApplicationContextScanner.getInstance().getBean(PsrExamCnCore.class);
        if (MapUtils.isNotEmpty(psrBookPersistenceNew.getModulePersistenceMap())) {
            psrBookPersistenceNew.getModulePersistenceMap().values().stream().filter(p -> {
                return MapUtils.isNotEmpty(p.getSentences());
            }).forEach(p -> {
                moduleKpsNum.put(p.getUnitId(), new HashMap<>());
                p.getSentences().keySet().stream().forEach(m -> {
                    moduleKpsNum.get(p.getUnitId()).put(m, 0);
                    kpToQids.put(m, new ArrayList<>());
                });
            });
        }
        if (MapUtils.isNotEmpty(psrBookPersistenceNew.getUnitPersistenceMap())) {
            psrBookPersistenceNew.getUnitPersistenceMap().values().stream().filter(p -> {
                return MapUtils.isNotEmpty(p.getSentences());
            }).forEach(p -> {
                unitKpsNum.put(p.getUnitId(), new HashMap<>());
                p.getSentences().keySet().stream().forEach(m -> {
                    unitKpsNum.get(p.getUnitId()).put(m, 0);
                    kpToQids.put(m, new ArrayList<>());
                });
            });
        }
    }

    private List<Object> formatPsrUnitPersistence(Map<String/*moduleId*/, PsrUnitPersistenceNew> unitPersistenceNewMap, Integer onlyResult) {
        List<Object> retList = new ArrayList<>();
        if (MapUtils.isEmpty(unitPersistenceNewMap))
            return retList;

        for (PsrUnitPersistenceNew unit : unitPersistenceNewMap.values()) {
            Map<String, Object> unitJsonMap = new HashMap<>();
            unitJsonMap.put("unitId", unit.getUnitId());
            unitJsonMap.put("unitKpNum", unit.getSentences().size());
            unitJsonMap.put("unitRank", unit.getRank());

            if (onlyResult >= 3) {
                unitJsonMap.put("unitKps", unit.getSentences().keySet());
                if (onlyResult >= 4) {
                    List<Object> lessonList = new ArrayList<>();
                    for (PsrLessonPersistenceNew lesson : unit.getLessonPersistenceMap().values()) {
                        Map<String, Object> lessonJsonMap = new HashMap<>();
                        lessonJsonMap.put("lessonId", lesson.getLessonId());
                        lessonJsonMap.put("lessonKpNum", lesson.getSentences().size());
                        lessonJsonMap.put("kps", lesson.getSentences().keySet());

                        lessonList.add(lessonJsonMap);
                    }
                    unitJsonMap.put("lessons", lessonList);
                }
            }

            retList.add(unitJsonMap);
        }

        return retList;
    }
}
