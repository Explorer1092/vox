package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 能力测试service
 *
 * @author lei.liu
 * @version 18-10-31
 */
@ServiceVersion(version = "20181031")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
public interface AbilityExamService extends IPingable {

    // 获取首页数据
    MapMessage index(Long studentId);

    //
    MapMessage doData(Long studentId);

    // 获取题目信息
    Map<String, Object> loadQuestion(Long studentId);

    // 获取答案信息
    Map<String, Object> loadQuestionAnswer(Long studentId);

    // 提交问题
    AbilityExamAnswerContext postQuestionAnswer(AbilityExamAnswerContext context);

    /**
     * 获取基本信息
     */
    @CacheMethod(type = AbilityExamBasic.class, writeCache = false, cacheName = "utopia-homework-cache")
    AbilityExamBasic loadAbilityExamBasic(@CacheParameter String id);

}
