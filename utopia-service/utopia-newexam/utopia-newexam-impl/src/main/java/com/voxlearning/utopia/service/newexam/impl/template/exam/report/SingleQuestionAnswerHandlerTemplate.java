package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSinglePrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSingleQuestionContext;
import com.voxlearning.utopia.service.newexam.api.utils.NewExamUtils;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionAnswer;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOption;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract public class SingleQuestionAnswerHandlerTemplate extends NewExamSpringBean {

    abstract ExamReportAnswerStatType getExamReportAnswerStatType();

    /**
     * 准备答案统计类型
     */
    abstract public void prepareAnswerType(NewExamSinglePrepareQuestionContext newExamSinglePrepareQuestionContext);

    /**
     * 统计答案详情
     */
    abstract public void statAnswerTypeDetail(NewExamSingleQuestionContext newExamSingleQuestionContext);

    /**
     * 模考格式化答案(连线题, 归类题)
     * @param subAnswers 要格式化的答案
     * @param subContents 小题content
     * @return 格式化后的答案
     */
    public List<String> transformSubAnswer(List<String> subAnswers, NewQuestionsSubContents subContents) {
        if (CollectionUtils.isEmpty(subAnswers)) {
            return Collections.emptyList();
        }
        if (ExamReportAnswerStatType.CHOICE.getSubContentTypeIds().contains(subContents.getSubContentTypeId())) {
            if (QuestionConstants.PanDuanTi == subContents.getSubContentTypeId()) {
               return subAnswers.stream().map(a -> "0".equals(a) ? "√" : "×").collect(Collectors.toList());
            } else {
                return subAnswers.stream().map(a -> NewExamUtils.convertToCapital(SafeConverter.toInt(a))).collect(Collectors.toList());
            }
        } else if (QuestionConstants.GuiLeiTi_V2 == subContents.getSubContentTypeId() || QuestionConstants.GuiLeiTi == subContents.getSubContentTypeId()) {
            return transformGuiLeiTiV2(subAnswers, subContents);
        } else if (QuestionConstants.LianXianTi_V2 == subContents.getSubContentTypeId() || QuestionConstants.LianXianTi == subContents.getSubContentTypeId()) {
            return transformLianXianTiV2(subAnswers, subContents);
        } else if (QuestionConstants.XuanCiTianKong == subContents.getSubContentTypeId()) {
            return transformXuanCiTianKong(subAnswers, subContents);
        } else {
            return subAnswers.stream().map(o -> {
                if ("".equals(o.trim())) {
                    return "未作答";
                }
                return o;
            }).collect(Collectors.toList());
        }
    }

    private List<String> transformLianXianTiV2(List<String> subAnswers, NewQuestionsSubContents subContents) {
        List<String> formatAnswers = Lists.newLinkedList();
        if (CollectionUtils.isEmpty(subContents.getOptions()) || StringUtils.isBlank(subContents.getOptions().get(0).getOption())) {
            return subAnswers;
        }
        int halfOptionSize = subContents.getOptions().get(0).getOption().split("<!-- -->").length;
        for (String subAnswer : subAnswers) {
            String[] littleAnswers = subAnswer.split(",");
            String formatAnswer = StringUtils.join(SafeConverter.toInt(littleAnswers[0]) + 1, "-", NewExamUtils.convertToCapital(SafeConverter.toInt(littleAnswers[1]) - halfOptionSize));
            formatAnswers.add(formatAnswer);
        }
        return formatAnswers;
    }

    private List<String> transformGuiLeiTiV2(List<String> subAnswers, NewQuestionsSubContents subContents) {
        List<String> formatAnswers = Lists.newLinkedList();
        List<NewQuestionAnswer> answers = subContents.getAnswers();
        for (int i = 0; i < subAnswers.size(); i++) {
            String subAnswer = subAnswers.get(i);
            NewQuestionAnswer newQuestionAnswer = answers.get(i);
            String formatAnswer = StringUtils.join(newQuestionAnswer.getClassification(), "：");
            for (String littleAnswer : subAnswer.split(",")) {
                if ("img".equals(subContents.getOptionType())) {
                    formatAnswer = StringUtils.join(formatAnswer, "图", SafeConverter.toInt(littleAnswer) + 1, "、");
                } else {
                    NewQuestionOption option = subContents.getOptions().get(SafeConverter.toInt(littleAnswer));
                    formatAnswer = StringUtils.join(formatAnswer, filterHTMLTag(option.getOption()), "、");
                }
            }
            formatAnswers.add(formatAnswer.substring(0, formatAnswer.length() - 1));
        }
        return formatAnswers;
    }

    private List<String> transformXuanCiTianKong(List<String> subAnswers, NewQuestionsSubContents subContents) {
        List<String> formatAnswers = Lists.newLinkedList();
        for (String subAnswer : subAnswers) {
            if ("".equals(subAnswer) || !StringUtils.isNumeric(subAnswer)) {
                formatAnswers.add("未作答");
            } else {
                int answerIndex = SafeConverter.toInt(subAnswer);
                if ("word".equals(subContents.getOptionType())) {
                    NewQuestionOption option = subContents.getOptions().get(answerIndex);
                    formatAnswers.add(StringUtils.join(filterHTMLTag(option.getOption())));
                } else {
                    formatAnswers.add(NewExamUtils.convertToCapital(answerIndex));
                }
            }
        }
        return formatAnswers;
    }

    /**
     * 过滤字符串中html标签
     * @param htmlStr
     * @return
     */
    public static String filterHTMLTag(String htmlStr) {
        String regEx = "<(\\s*|/)([a-zA-Z]+)(>|\\s+([^>]*)\\s*>)";
        Pattern pSpan = Pattern.compile(regEx);
        Matcher mSpan = pSpan.matcher(htmlStr);
        htmlStr = mSpan.replaceAll(""); //过滤标签
        return htmlStr.trim(); //返回文本字符串
    }
}
