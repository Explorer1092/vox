package com.voxlearning.utopia.service.psr.impl.mathnewkp.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.psr.entity.mathnewkp.MathQuestion;
import com.voxlearning.utopia.service.psr.impl.mathnewkp.service.PsrQuestionService;
import com.voxlearning.utopia.service.psr.mathnewkp.loader.PsrQuestionsLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: hui.cheng
 * Date: 2016/10/20.
 * Time: 16:03
 */
@Named
@ExposeService(interfaceClass = PsrQuestionsLoader.class)
public class PsrQuestioinLoaderImpl implements PsrQuestionsLoader {
    @Inject
    private PsrQuestionService psrQuestionService;
    private static Cache<String, List<MathQuestion>> cacheMathQuestions = CacheBuilder.newBuilder().maximumSize(10000).initialCapacity(6000).expireAfterAccess(1, TimeUnit.HOURS).build();

    private static final Integer DEFAULT_COUNT = 500;

    @Override
    public Map<String, List<MathQuestion>> loadMathQuestionsByKp(String bookId, String unitId, List<String> knowledgePoints, Integer count) {
        if (CollectionUtils.isEmpty(knowledgePoints)) return Collections.emptyMap();

        Map<String, List<MathQuestion>> result = Maps.newHashMap();
        List<String> knowledgePointsToBeProcessed = Lists.newArrayList();
        knowledgePoints.stream().forEach(q -> {
            String key = unitId + q;
            List<MathQuestion> mathQuestions = cacheMathQuestions.getIfPresent(key);
            if (mathQuestions == null) knowledgePointsToBeProcessed.add(q);
            else result.put(q, mathQuestions);
        });

        if (CollectionUtils.isNotEmpty(knowledgePointsToBeProcessed)) {
            Integer questionsCount = (count > 0) ? count : DEFAULT_COUNT;
            Map<String, List<MathQuestion>> tmpResult = psrQuestionService.getQuestions(unitId, knowledgePoints, questionsCount);
            if (tmpResult != null) {
                result.putAll(tmpResult);
                tmpResult.entrySet().stream().forEach(q -> {
                    cacheMathQuestions.put(unitId + q.getKey(), q.getValue());
                });
            }
        }
        return result;
    }


    /* 测试接口,for php dubbo*/
    //TODO 上线之后去掉
    @Override
    public Map<String, List<MathQuestion>> testLoadMathQuestionsByKp(String unitId, List<String> knowledgePoints) {
        String bookId = "BK_20300001022210";
        Integer count = 0;
        return loadMathQuestionsByKp(bookId, unitId, knowledgePoints, count);
    }
}
