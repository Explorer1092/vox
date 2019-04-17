package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzQuestionContext;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract public class ClazzQuestionAnswerHandlerTemplate extends NewExamSpringBean {
    abstract NewExamQuestionType getNewExamQuestionType();

    abstract public void processSubQuestion(NewExamClazzQuestionContext newExamClazzQuestionContext);


    abstract public void prepareSubQuestion(NewExamClazzPrepareQuestionContext newExamClazzPrepareQuestionContext);

    public String pressAnswer(NewQuestionsSubContents qsc, List<String> subContentAnswers) {
        String answer = "暂时无法查看答案";
        if (qsc.getSubContentTypeId() != QuestionConstants.LianXianTi && qsc.getSubContentTypeId() != QuestionConstants.GuiLeiTi) {
            if (qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DanXuan
                    || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_DuoXuan
                    || qsc.getSubContentTypeId() == QuestionConstants.XuanZe_BuDingXiang
                    || qsc.getSubContentTypeId() == QuestionConstants.PanDuanTi) {
                List<String> ans = subContentAnswers
                        .stream()
                        .map(an -> StringUtils.isNotBlank(an) ?
                                Character.valueOf((char) (conversionService.convert(an, Integer.class) + 65)).toString() :
                                "未作答")
                        .collect(Collectors.toList());
                answer = StringUtils.join(ans, ";");
            } else if (qsc.getSubContentTypeId() == QuestionConstants.XuanCiTianKong) {
                // 排序题和一级题型为选词填空，答案的处理
                List<String> ans = new ArrayList<>();
                for (String an : subContentAnswers) {
                    List<String> as = new ArrayList<>();
                    for (String a : StringUtils.split(an, ";")) {
                        if (StringUtils.isNumeric(a)) {
                            as.add(conversionService.convert(conversionService.convert(a, Integer.class) + 1, String.class));
                        }
                    }
                    ans.add(StringUtils.join(as, ";"));
                }
                answer = StringUtils.join(ans, ";");
            } else if (qsc.getSubContentTypeId() == QuestionConstants.PaiXuTi) {
                List<String> ans = new ArrayList<>();
                for (String an : subContentAnswers) {
                    List<String> as = new ArrayList<>();

                    Map<Integer, Integer> map = new LinkedHashMap<>();
                    int value = 0;
                    String[] split = StringUtils.isNotBlank(an) ? an.split(";") : new String[0];
                    for (String a : split) {
                        if (StringUtils.isNumeric(a)) {
                            int ann = SafeConverter.toInt(a);
                            map.put(ann, value);
                        }
                        value++;
                    }
                    for (int p = 0; p < split.length; p++) {
                        if (map.containsKey(p)) {
                            as.add((map.get(p) + 1) + "");
                        } else {
                            as.add("");
                        }
                    }
                    ans.add(StringUtils.join(as, ";"));
                }
                answer = StringUtils.join(ans, ";");
            } else {
                answer = StringUtils.join(subContentAnswers, ";");
            }
        }
        return answer;
    }
}
