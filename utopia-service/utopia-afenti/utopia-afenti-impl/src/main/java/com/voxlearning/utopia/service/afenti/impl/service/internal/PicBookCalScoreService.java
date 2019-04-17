package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.voxlearning.alps.lang.convert.SafeConverter.toBoolean;
import static com.voxlearning.alps.lang.convert.SafeConverter.toDouble;

/**
 * 绘本计算分数的服务，临时使用。
 * FIXME 后面要把PictureBookListener中计算分数部分拆成实时同步的
 */
@Named
public class PicBookCalScoreService {

    @Inject private QuestionLoaderClient questionLoaderCli;

    // 区间和分数等级的对照
    private static final Map<Integer,String> RANGE_LEVEL = new LinkedHashMap<>();
    // 分数等级和比例系数的对照
    private static final Map<String, Double> LEVEL_PERCENT = new HashMap<>();

    static {
        RANGE_LEVEL.put(60,  "D");
        RANGE_LEVEL.put(75,  "C");
        RANGE_LEVEL.put(90,  "B");
        RANGE_LEVEL.put(100, "A");

        LEVEL_PERCENT.put("A", 1d);
        LEVEL_PERCENT.put("B", 0.9);
        LEVEL_PERCENT.put("C", 0.75);
        LEVEL_PERCENT.put("D", 0.6);
    }

    public BigDecimal getSingleQuestionScore(int totalMarks,PictureBookPlus book){
        return Optional.ofNullable(book)
                .map(b -> {
                    int qNum = b.getOralQuestions().size() + b.getPracticeQuestions().size();
                    return qNum == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(totalMarks).divide(BigDecimal.valueOf(qNum), 10,RoundingMode.HALF_UP);
                })
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal convertVoiceScore(int orgScore,BigDecimal qScore){
        String lvl = null;
        for(Map.Entry<Integer,String> rl : RANGE_LEVEL.entrySet()){
            if(orgScore <= rl.getKey()){
                lvl = rl.getValue();
                break;
            }
        }

        Double percent = LEVEL_PERCENT.get(lvl);
        return qScore.multiply(BigDecimal.valueOf(percent));
    }

    public BigDecimal calPractiseScore(String questionId,List<List<String>> answer,BigDecimal qScore){
        NewQuestion question = questionLoaderCli.loadQuestions(Collections.singletonList(questionId)).get(questionId);
        if(question != null){
            if(Objects.equals(question.getAnswers(),answer))
                return qScore;
        }

        return BigDecimal.ZERO;
    }
}
