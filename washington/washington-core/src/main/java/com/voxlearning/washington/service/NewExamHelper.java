package com.voxlearning.washington.service;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.entity.RptMockNewExamStudent;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.TikuStrategy;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

@Named
public class NewExamHelper extends SpringContainerSupport {
    @Inject
    private PaperLoaderClient paperLoaderClient;
    @Inject
    private TikuStrategy tikuStrategy;
    @Inject
    private QuestionLoaderClient questionLoaderClient;

    @Getter
    @Setter
    public static class QuestionDetail implements Serializable {
        private static final long serialVersionUID = 5569465843171648342L;
        private String qid;
        //小题数
        private int subQuestionSize;
    }

    @Getter
    @Setter
    public static class PaperPart implements Serializable {
        private static final long serialVersionUID = -8896233101369554583L;
        private Map<String, QuestionDetail> questionDetailMap = new LinkedHashMap<>();
        private HSSFSheet sheet;
        private int rowNum = 0;
        private int columnNum = 0;

    }

    public HSSFWorkbook fetchHSSFWorkbook(NewExam newExam, Integer type, List<RptMockNewExamStudent> rptMockNewExamStudents, Map<Long, User> allUser) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
        Set<String> qids = new LinkedHashSet<>();
        Map<String, PaperPart> paperPartMap = new LinkedHashMap<>();
        //每份试卷创建一个sheet,以及这份数据的题目
        for (NewExam.EmbedPaper paper : papers) {
            NewPaper newPaper = tikuStrategy.loadLatestPaperByDocId(paper.getPaperId());
            if (newPaper == null)
                continue;
            if (newPaper.getQuestions() == null)
                continue;
            HSSFCellStyle style = workbook.createCellStyle();
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            // 创建Excel的工作sheet,对应到一个excel文档的tab
            HSSFSheet sheet = workbook.createSheet(paper.getPaperName());
            PaperPart paperPart = new PaperPart();
            paperPartMap.put(paper.getPaperId(), paperPart);
            paperPart.setSheet(sheet);
            newPaper.getQuestions().sort(Comparator.comparingInt(NewPaperQuestion::getNumber));
            for (NewPaperQuestion newPaperQuestion : newPaper.getQuestions()) {
                QuestionDetail questionDetail = new QuestionDetail();
                questionDetail.setQid(newPaperQuestion.getId());
                paperPart.getQuestionDetailMap().put(questionDetail.getQid(), questionDetail);
                qids.add(newPaperQuestion.getId());
            }
        }
        Map<String, NewQuestion> newQuestionMap = tikuStrategy.loadQuestionsIncludeDisabled(qids, newExam.getSchoolLevel());
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        //创建表头
        for (PaperPart paperPart : paperPartMap.values()) {
            HSSFSheet sheet = paperPart.getSheet();
            // 创建单元格样式
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
            int questionNum = 1;
            for (QuestionDetail questionDetail : paperPart.getQuestionDetailMap().values()) {
                if (newQuestionMap.containsKey(questionDetail.getQid())) {
                    NewQuestion newQuestion = newQuestionMap.get(questionDetail.getQid());
                    if (newQuestion.getContent() != null) {
                        if (newQuestion.getContent().getSubContents() != null) {
                            questionDetail.setSubQuestionSize(newQuestion.getContent().getSubContents().size());
                        }
                    }
                }
                if (questionDetail.getSubQuestionSize() == 0)
                    continue;
                if (questionDetail.getSubQuestionSize() == 1) {
                    cell = row.createCell(columnNum++);
                    cell.setCellStyle(style);
                    cell.setCellValue("第" + questionNum + "题分数");
                } else {
                    for (int i = 1; i <= questionDetail.getSubQuestionSize(); i++) {
                        cell = row.createCell(columnNum++);
                        cell.setCellStyle(style);
                        cell.setCellValue("第" + questionNum + "题-" + i + "小题分数");
                    }
                }
                questionNum++;
            }
        }
        HSSFRow row;
        HSSFCell cell;
        Set<Long> doUserIds = new LinkedHashSet<>();
        for (RptMockNewExamStudent rptMockNewExamStudent : rptMockNewExamStudents) {
            if (!paperPartMap.containsKey(rptMockNewExamStudent.getPaperDocId()))
                continue;
            doUserIds.add(SafeConverter.toLong(rptMockNewExamStudent.getStudentId()));
            PaperPart paperPart = paperPartMap.get(rptMockNewExamStudent.getPaperDocId());
            HSSFSheet sheet = paperPart.getSheet();
            int colNum = 0;
            //第几行
            paperPart.setRowNum(1 + paperPart.getRowNum());
            row = sheet.createRow(paperPart.getRowNum());
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
            Double totalDuration = rptMockNewExamStudent.getTotalDuration() == null ? 0 : rptMockNewExamStudent.getTotalDuration();
            totalDuration = totalDuration / 1000;
            int minute = (int) (totalDuration / 60);
            int second = (int) (totalDuration - minute * 60);
            cell.setCellValue(minute + "分" + second + "秒");
            cell = row.createCell(colNum++);
            cell.setCellStyle(style);
            // 总成绩
            cell.setCellValue(SafeConverter.toDouble(type == 1 ? rptMockNewExamStudent.getTotalScore() : rptMockNewExamStudent.getTotalCorrectScore()));
            String paper = rptMockNewExamStudent.getPaperJson();
            if (StringUtils.isNotBlank(paper)) {
                paper = paper.replaceAll("'", "\"");
            }
            List<Map> partQuestions = JsonUtils.fromJsonToList(paper, Map.class);

            Map<String, List<Double>> questionsMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(partQuestions)) {
                for (Map partPaper : partQuestions) {
                    List<Map> questions = (List<Map>) partPaper.get("questions");
                    if (CollectionUtils.isNotEmpty(questions)) {
                        for (Map question : questions) {
                            String questionId = question.get("question_id") == null ? "" : question.get("question_id").toString();
                            String key = type == 1 ? "subscore" : "correctsubscore";
                            String subScoreJson = question.get(key) == null ? "" : question.get(key).toString();
                            List<Double> subScores = JsonUtils.fromJsonToList(subScoreJson, Double.class);
                            subScores = subScores != null ? subScores : Collections.emptyList();
                            questionsMap.put(questionId, subScores);
                        }
                    }
                }
            }
            for (QuestionDetail questionDetail : paperPart.getQuestionDetailMap().values()) {
                if (!newQuestionMap.containsKey(questionDetail.getQid()))
                    continue;
                NewQuestion newQuestion = newQuestionMap.get(questionDetail.getQid());
                //是否数据全，用来后面需不需要补数据
                boolean flag = false;
                List<Double> subScores = new LinkedList<>();
                if (questionsMap.containsKey(newQuestion.getDocId())) {
                    subScores = questionsMap.get(newQuestion.getDocId());
                    if (subScores.size() == questionDetail.getSubQuestionSize()) {
                        flag = true;
                    }
                }
                if (flag) {
                    for (Double subScore : subScores) {
                        cell = row.createCell(colNum++);
                        cell.setCellStyle(style);
                        cell.setCellValue(SafeConverter.toDouble(subScore));
                    }
                } else {
                    //写零数据
                    for (int i = 0; i < questionDetail.getSubQuestionSize(); i++) {
                        cell = row.createCell(colNum++);
                        cell.setCellStyle(style);
                        cell.setCellValue(0);
                    }
                }
            }
        }
        if (allUser != null) {
            HSSFSheet sheet = workbook.createSheet("缺考学生");
            // 创建Excel的sheet的一行
            row = sheet.createRow(0);
            // 创建一个Excel的单元格
            cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue("学生id");
            cell = row.createCell(1);
            cell.setCellStyle(style);
            cell.setCellValue("学生姓名");
            int rowNum = 1;
            for (User user : allUser.values()) {
                if (!doUserIds.contains(user.getId())) {
                    row = sheet.createRow(rowNum++);// 生成行
                    cell = row.createCell(0);
                    cell.setCellStyle(style);
                    cell.setCellValue(user.getId());
                    cell = row.createCell(1);
                    cell.setCellStyle(style);
                    cell.setCellValue(user.fetchRealnameIfBlankId());
                }
            }
        }
        return workbook;
    }
}
