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

package com.voxlearning.washington.controller.rstaff;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.washington.support.AbstractController;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/rstaff/oral")
public class ResearchStaffOralController extends AbstractController {

    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private NewExamLoaderClient newExamLoaderClient;

    /**
     * NEW 教研员首页
     * 教研员首页  组卷统考 --> 口语测试
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String oralindex() {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff != null && researchStaff.isResearchStaffForProvince()) {
            // 知识数据
            return "redirect:/rstaff/report/knowledgedata.vpage";
        }
        return "rstaffv3/oral/indexNew";
    }

    private double reservedDecimal(double data, int n) {
        return new BigDecimal(data).setScale(n, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 组卷统考 --> 口语测试-->区统计数据
     *
     * @return 跳转页面
     */

    @RequestMapping(value = "regionStatistic.vpage", method = RequestMethod.GET)
    public String oralRegionStatistic(Model model) {
        String examId = getRequestString("exam_id");
        String[] fieldNames = {"exam_id", "paper_id"};
        handlePass(model, fieldNames);
        NewExam exam = newExamLoaderClient.load(examId);
        model.addAttribute("papers", exam.getPapers());
        return "rstaffv3/oral/regionStatistic";
    }

    private void handlePass(Model model, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            model.addAttribute(fieldName, this.getRequestString(fieldName));
        }
    }


    /**
     * 组卷统考 --> 口语测试-->学校统计数据
     *
     * @return 跳转学校统计页面
     */

    @RequestMapping(value = "schoolStatistic.vpage", method = RequestMethod.GET)
    public String oralSchoolStatistic(Model model) {
        String examId = getRequestString("exam_id");
        String[] fieldNames = {"paper_id", "exam_id", "county_id"};
        handlePass(model, fieldNames);
        NewExam exam = newExamLoaderClient.load(examId);
        model.addAttribute("papers", exam.getPapers());
        return "rstaffv3/oral/schoolStatistic";
    }

    /**
     * 组卷统考 --> 口语测试-->班级
     *
     * @return 班级统计页面
     */
    @RequestMapping(value = "classStatistic.vpage", method = RequestMethod.GET)
    public String oralClassStatistic(Model model) {
        String examId = getRequestString("exam_id");
        String[] fieldNames = {"school_id", "paper_id", "exam_id", "county_id"};
        handlePass(model, fieldNames);
        NewExam exam = newExamLoaderClient.load(examId);
        model.addAttribute("papers", exam.getPapers());
        return "rstaffv3/oral/classStatistic";
    }


    /**
     * 获取口语试卷列表（新）
     */
    @RequestMapping(value = "oralpaperNewData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralPaperNewList() {
        ResearchStaff researchStaff = currentResearchStaff();
        MapMessage mapMessage = MapMessage.successMessage();
        if (researchStaff == null) {
            mapMessage.add("refList", Collections.emptyList());
        } else {
            ExRegion _exRegion = process(researchStaff);
            if (!researchStaff.getRegionType().equals(RegionType.PROVINCE)) {
                SchoolLevel schoolLevel = SchoolLevel.safeParse(researchStaff.getKtwelve().getLevel());
                Subject subject = researchStaff.getSubject();
                if (SchoolLevel.MIDDLE.equals(schoolLevel)) {
                    subject = Subject.of("J" + researchStaff.getSubject().name());
                }
                mapMessage.add("refList", newExamLoaderClient.getPageOralByResearchStaffInfo(subject, _exRegion));
            } else {
                mapMessage.add("refList", Collections.emptyList());
            }
        }
        return mapMessage;
    }

    /**
     * 获取区统计数据
     */
    @RequestMapping(value = "oralRegionStatisticData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralRegionStatisticData(@RequestBody Map<String, Object> req) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (req.get("exam_id") == null) {
            return MapMessage.errorMessage("考试ID为空");
        }
        if (req.get("paper_id") == null) {
            return MapMessage.errorMessage("试卷ID为空");
        }
        String examId = req.get("exam_id").toString();
        String paperId = req.get("paper_id").toString();
        try {
            ExRegion exRegion = process(researchStaff);
            if (!researchStaff.getRegionType().equals(RegionType.PROVINCE)) {
                List<Map<String, Object>> regions = newExamReportLoaderClient.getRegionStatistic(
                        examId,
                        exRegion,
                        paperId
                );
                return MapMessage.successMessage().add("refList", regions);
            } else {
                return MapMessage.successMessage().add("refList", Collections.emptyList());
            }
        } catch (Exception ex) {
            logger.error("Get oralRegionStatisticData error, the error is {}", ex.getMessage());
            return MapMessage.errorMessage("获取区统计数据异常");
        }
    }

    private ExRegion process(ResearchStaff researchStaff) {
        RegionType regionType = researchStaff.getRegionType();
        ExRegion exRegion = researchStaff.getRegion();
        ExRegion _exRegion = new ExRegion();
        _exRegion.setProvinceCode(exRegion.getProvinceCode());
        _exRegion.setCityCode(exRegion.getCityCode());
        _exRegion.setCountyCode(exRegion.getCountyCode());
        //将区设置成零
        if (regionType == RegionType.CITY) {
            _exRegion.setCountyCode(0);
        }
        return _exRegion;
    }

    /**
     * 获取学校统计数据
     */
    @RequestMapping(value = "oralSchoolStatisticData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralSchoolStatisticData(@RequestBody Map<String, Object> req) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (req.get("exam_id") == null) {
            return MapMessage.errorMessage("考试ID为空");
        }
        if (req.get("paper_id") == null) {
            return MapMessage.errorMessage("试卷ID为空");
        }
        if (req.get("county_id") == null) {
            return MapMessage.errorMessage("区域ID为空");
        }
        String examId = req.get("exam_id").toString();
        String paperId = req.get("paper_id").toString();
        String countyId = req.get("county_id").toString();
        try {
            ExRegion exRegion = process(researchStaff);
            if (!researchStaff.getRegionType().equals(RegionType.PROVINCE)) {
                List<Map<String, Object>> schools = newExamReportLoaderClient.getSchoolStatistic(
                        examId,
                        paperId,
                        exRegion,
                        countyId
                );
                return MapMessage.successMessage().add("refList", schools);
            } else {
                return MapMessage.successMessage().add("refList", Collections.emptyList());
            }
        } catch (Exception ex) {
            logger.error("Get oralSchoolStatisticData error, the error is {}", ex.getMessage());
            return MapMessage.errorMessage("获取学校统计数据异常");
        }
    }

    /**
     * 获取班级统计
     */
    @RequestMapping(value = "oralClassStatisticData.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralClassStatisticData(@RequestBody Map<String, Object> req) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null) {
            return MapMessage.errorMessage("用户不存在");
        }
        if (req.get("exam_id") == null) {
            return MapMessage.errorMessage("考试ID为空");
        }
        if (req.get("paper_id") == null) {
            return MapMessage.errorMessage("试卷ID为空");
        }
        if (req.get("school_id") == null) {
            return MapMessage.errorMessage("学校ID为空");
        }
        String examId = req.get("exam_id").toString();
        String paperId = req.get("paper_id").toString();
        String schoolId = req.get("school_id").toString();
        try {
            ExRegion exRegion = process(researchStaff);
            if (!researchStaff.getRegionType().equals(RegionType.PROVINCE)) {
                List<Map<String, Object>> classes = newExamReportLoaderClient.getClassStatistic(
                        examId,
                        paperId,
                        exRegion,
                        schoolId
                );
                return MapMessage.successMessage().add("refList", classes);
            } else {
                return MapMessage.successMessage().add("refList", Collections.emptyList());
            }
        } catch (Exception ex) {
            logger.error("Get oralClassStatisticData error, the error is {}", ex.getMessage());
            return MapMessage.errorMessage("获取班级统计数据异常");
        }
    }

    /**
     * 获取口语试卷列表
     */
    @RequestMapping(value = "oralpaperlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage oralPaperList() {
        return MapMessage.errorMessage("功能已下线");
    }


    // 预览口语题
    @RequestMapping(value = "oralpreview.vpage", method = RequestMethod.GET)
    public String previewOralQuestion(HttpServletRequest request, Model model) {
        String paperId = request.getParameter("paperId");
        List<String> questionIds = new LinkedList<>();
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("displayHeadline", false);  //是否显示标题
        if (StringUtils.isNotBlank(paperId)) {
            NewPaper newPaper = paperLoaderClient.loadPaperIncludeDisabled(paperId);
            if (newPaper != null && newPaper.getQuestions() != null) {
                vars.put("displayHeadline", true);
                vars.put("paperId", paperId);
                vars.put("paperName", newPaper.getTitle());
                vars.put("questionCnt", newPaper.getQuestions().size());
                vars.put("totalScore", newPaper.getTotalScore() == null ? 0 : newPaper.getTotalScore());

                Map<String, String> map = newPaper.getQuestionMap();
                //<题号，题ID> ,题号都是数字的字符串
                TreeMap<String, String> qtMap = new TreeMap<>((o1, o2) -> SafeConverter.toInt(o1) - SafeConverter.toInt(o2));
                for (String index : map.keySet()) {
                    qtMap.put(index, map.get(index));
                }
                questionIds.addAll(qtMap.values());
            }
        }
        //数组格式的字符串
        vars.put("questionIds", JsonUtils.toJson(questionIds));
        //增加额外的flashvars
        gameFlashLoaderConfigManager.setupFlashUrl(vars, request, "OralExamPreview", "OralExamPreview");
        model.addAttribute("flashVars", JsonUtils.toJson(vars));
        model.addAttribute("loadQuestionUrl", "/rstaff/oral/loadquestions.vpage");

        //前端
        return "rstaffv3/oral/oralpreview";
    }

    // flash load口语测试题
    @RequestMapping(value = "loadquestions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadOralQuestions() {
        List<String> questionIds = JsonUtils.fromJsonToList(getRequestParameter("questionIds", ""), String.class);
        if (CollectionUtils.isEmpty(questionIds)) {
            return MapMessage.errorMessage("试题ID未找到");
        }
        List<NewQuestion> questionList = questionLoaderClient.loadQuestionsIncludeDisabledAsList(questionIds);
        return MapMessage.successMessage().add("allQuestions", questionList);
    }

    /**
     * NEW 教研员首页
     * 教研员首页 --> 组卷统考 --> 口语测试 --> 查看报告
     */
    @RequestMapping(value = "oralreport.vpage", method = RequestMethod.GET)
    public String getOralReport(Model model) {
        return "redirect:/rstaff/oral/index.vpage";
    }


    /**
     * 下载学生成绩
     */
    @RequestMapping(value = "loadStudentAchievement.vpage", method = RequestMethod.GET)
    public void loadStudentAchievement(HttpServletResponse response) {
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff == null || StringUtils.isBlank(getRequestString("exam_id"))) {
            return;
        }
        String examId = getRequestString("exam_id");
        // type = (system:系统成绩,teacher:批改成绩)
        String type = getRequestString("type");
        try {
            List<RptMockNewExamStudent> rptMockNewExamStudents = newExamReportLoaderClient.getStudentAchievement(
                    researchStaff.getRegion(),
                    examId
            );
            HSSFWorkbook hSSFWorkbook = this.createStudentAchievementExcel(examId, rptMockNewExamStudents, type);
            @Cleanup ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            assert hSSFWorkbook != null;
            hSSFWorkbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            String fileName = researchStaff.formatManagedRegionStr() + researchStaff.getSubject().getValue() + "报告.xls";
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        byteArrayOutputStream.toByteArray()
                );
            } catch (IOException ignored) {
                response.getWriter().write("不能下载");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception ex) {
            logger.error("学生成绩下载失败!", ex.getMessage(), ex);
        }
    }

    /**
     * 生成学生的成绩表格
     *
     * @return 返回对应的Excel
     */
    private HSSFWorkbook createStudentAchievementExcel(String examId, List<RptMockNewExamStudent> rptMockNewExamStudents, String type) {
        Map<String,String> paperTypeMap = new TreeMap<>();
        NewExam newExam = newExamLoaderClient.load(examId);
        if (newExam != null) {
            List<NewExam.EmbedPaper> papers = newExam.getPapers();
            if (CollectionUtils.isNotEmpty(papers)) {
                papers.stream()
                        .filter(e -> StringUtils.isNotBlank(e.getPaperId()))
                        .forEach(e -> paperTypeMap.put(e.getPaperId(),e.getPaperName()));
            }
        }
        // 创建Excel的工作书册workbook,对应到一个excel文档
        HSSFWorkbook workbook = new HSSFWorkbook();
        if (MapUtils.isNotEmpty(paperTypeMap)) {
            for (Map.Entry<String, String> entry : paperTypeMap.entrySet()) {
                createSheet(rptMockNewExamStudents, type, workbook, entry);
            }
        }
        return workbook;
    }

    private void createSheet(List<RptMockNewExamStudent> rptMockNewExamStudents, String type,
                             HSSFWorkbook workbook, Map.Entry<String,String> paperType) {
        // 创建单元格样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 创建Excel的工作sheet,对应到一个excel文档的tab
        HSSFSheet sheet = workbook.createSheet(paperType.getValue());

        // 创建Excel的sheet的一行
        int rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum++);
        // 创建一个Excel的单元格
        int columnNum = 0;
        HSSFCell cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("学生ID");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("学生姓名");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("学校名称");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("班级名称");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("教师姓名");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("试卷类型(多试卷)");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("答题时长");
        cell = row.createCell(columnNum++);
        cell.setCellStyle(style);
        cell.setCellValue("总成绩");

        if (CollectionUtils.isNotEmpty(rptMockNewExamStudents)) {
            List<Map.Entry<String, Integer>> questionRanks = new ArrayList<>();
            // map<题id，题号>
            Map<String, Integer> questionRankMap = new HashMap<>();
            // map<题id，小题数>
            Map<String, Integer> subQuestionSizeMap = new HashMap<>();
            String paperId = paperType.getKey();

            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paperId);
            List<NewPaperQuestion> questionList = newPaper.getQuestions();
            if (CollectionUtils.isNotEmpty(questionList)) {
                for (NewPaperQuestion newPaperQuestion : questionList) {
                    String questionId = newPaperQuestion.getId();
                    if (StringUtils.isNotBlank(questionId)) {
                        String[] docIdStr = questionId.split("-");
                        if (docIdStr.length == 2) {
                            questionRankMap.put(docIdStr[0], newPaperQuestion.getNumber());
                        }
                    }
                }
            }
            // 试卷的题目排序
            List<Map.Entry<String, Integer>> questionNumberList = new ArrayList<>(questionRankMap.entrySet());
            questionNumberList.sort(
                    (e1, e2) -> {
                        if (e1.getValue() != null
                                && e2.getValue() != null
                                && SafeConverter.toInt(e1.getValue()) - SafeConverter.toInt(e2.getValue()) > 0) {
                            return 1;
                        } else {
                            return -1;
                        }
                    });
            questionRanks.addAll(questionNumberList);

            Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadLatestQuestionByDocIds(questionRankMap.keySet());
            if (MapUtils.isNotEmpty(newQuestionMap)) {
                for (Map.Entry<String, NewQuestion> entry : newQuestionMap.entrySet()) {
                    String questionId = entry.getKey();
                    NewQuestion newQuestion = entry.getValue();
                    Integer subSize = 0;
                    if (newQuestion != null) {
                        NewQuestionsContent content = newQuestion.getContent();
                        if (content != null) {
                            List<NewQuestionsSubContents> subContents = content.getSubContents();
                            if (CollectionUtils.isNotEmpty(subContents)) {
                                subSize = subContents.size();
                            }
                        }
                    }
                    subQuestionSizeMap.put(questionId, subSize);
                }
            }

            // 设置小题列标题
            if (CollectionUtils.isNotEmpty(questionRanks)) {
                int questionNum = 1;
                for (Map.Entry<String, Integer> entry : questionRanks) {
                    String questionId = entry.getKey();
                    int subSize = subQuestionSizeMap.get(questionId);
                    if (subSize == 1) {
                        cell = row.createCell(columnNum++);
                        cell.setCellStyle(style);
                        cell.setCellValue("第" + questionNum + "题分数");
                    } else {
                        int subQuestionNum = 1;
                        for (; subQuestionNum <= subSize; subQuestionNum++) {
                            cell = row.createCell(columnNum++);
                            cell.setCellStyle(style);
                            cell.setCellValue("第" + questionNum + "题-" + subQuestionNum + "小题分数");
                        }
                    }
                    questionNum++;
                }
            }

            // 设置excel列宽度
            for (int i = 0; i < columnNum; i++) {
                sheet.setColumnWidth(i, 4000);
            }

            // 开始准备写数据
            for (RptMockNewExamStudent rptMockNewExamStudent : rptMockNewExamStudents) {
                if (rptMockNewExamStudent.getPaperDocId().equals(paperType.getKey())) {
                    int colNum = 0;
                    // 生成行
                    row = sheet.createRow(rowNum++);
                    // 生成第colNum个单元格
                    cell = row.createCell(colNum++);
                    // 设置样式
                    cell.setCellStyle(style);
                    // 学生ID
                    cell.setCellValue(rptMockNewExamStudent.getStudentId() == null ? 0 : rptMockNewExamStudent.getStudentId());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 学生姓名
                    cell.setCellValue(StringUtils.isBlank(rptMockNewExamStudent.getStudentName()) ? "" : rptMockNewExamStudent.getStudentName());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 学校名称
                    cell.setCellValue(StringUtils.isBlank(rptMockNewExamStudent.getSchoolName()) ? "" : rptMockNewExamStudent.getSchoolName());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 班级名称
                    cell.setCellValue(StringUtils.isBlank(rptMockNewExamStudent.getClassName()) ? "" : rptMockNewExamStudent.getClassName());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 教师姓名
                    cell.setCellValue(StringUtils.isBlank(rptMockNewExamStudent.getTeacherName()) ? "" : rptMockNewExamStudent.getTeacherName());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 试卷类型(多试卷)
                    cell.setCellValue(StringUtils.isBlank(rptMockNewExamStudent.getPaperName()) ? "" : rptMockNewExamStudent.getPaperName());
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 答题时长
                    Double totalDuration = rptMockNewExamStudent.getTotalDuration() == null ? 0 : rptMockNewExamStudent.getTotalDuration() / 1000;
                    int minute = (int) (totalDuration / 60);
                    int second = (int) (totalDuration - minute * 60);
                    cell.setCellValue(minute + "分" + second + "秒");
                    cell = row.createCell(colNum++);
                    cell.setCellStyle(style);
                    // 总成绩
                    if ("system".equals(type)) {
                        cell.setCellValue(rptMockNewExamStudent.getTotalScore() == null ? 0D : rptMockNewExamStudent.getTotalScore());
                    } else if ("teacher".equals(type)) {
                        cell.setCellValue(rptMockNewExamStudent.getTotalCorrectScore() == null ? 0D : rptMockNewExamStudent.getTotalCorrectScore());
                    }

                    // 各模块子题分数
                    Map<String, List<Double>> subQuestionScore;
                    String paper = rptMockNewExamStudent.getPaperJson();
                    if (StringUtils.isNotBlank(paper)) {
                        paper = paper.replaceAll("'", "\"");
                    }
                    List<Map> partQuestions = JsonUtils.fromJsonToList(paper, Map.class);
                    subQuestionScore = getAllQuestionScore(partQuestions, type);
                    if (CollectionUtils.isNotEmpty(questionRanks)) {
                        for (Map.Entry<String, Integer> entry : questionRanks) {
                            String questionId = entry.getKey();
                            if (subQuestionSizeMap.get(questionId) != null && CollectionUtils.isNotEmpty(subQuestionScore.get(questionId))) {
                                if (subQuestionSizeMap.get(questionId) == subQuestionScore.get(questionId).size()) {
                                    for (Double subScore : subQuestionScore.get(questionId)) {
                                        cell = row.createCell(colNum++);
                                        cell.setCellStyle(style);
                                        cell.setCellValue(subScore);
                                    }
                                } else {
                                    for (int i = 0; i < subQuestionSizeMap.get(questionId); i++) {
                                        cell = row.createCell(colNum++);
                                        cell.setCellStyle(style);
                                        cell.setCellValue(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, List<Double>> getAllQuestionScore(List<Map> partQuestions, String type) {
        Map<String, List<Double>> questionsMap = new TreeMap<>();
        if (CollectionUtils.isNotEmpty(partQuestions)) {
            for (Map partPaper : partQuestions) {
                List<Map> questions = (List<Map>) partPaper.get("questions");
                if (CollectionUtils.isNotEmpty(questions)) {
                    for (Map question : questions) {
                        String questionId = question.get("question_id") == null ? "" : question.get("question_id").toString();
                        String subScoreJson = "";
                        // 系统分数
                        if ("system".equals(type)) {
                            subScoreJson = question.get("subscore") == null ? "" : question.get("subscore").toString();
                        } else if ("teacher".equals(type)) {
                            // 批改分数
                            subScoreJson = question.get("correctsubscore") == null ? "" : question.get("correctsubscore").toString();
                        }
                        List<Double> subScores = JsonUtils.fromJsonToList(subScoreJson, Double.class);
                        questionsMap.put(questionId, subScores);
                    }
                }
            }
        }
        return questionsMap;
    }
}
