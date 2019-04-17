package com.voxlearning.utopia.service.newhomework.impl.service.processor.index;

import com.lambdaworks.redis.api.sync.RedisListCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
@Named
public class AbilityExamQuestionCacheManager extends SpringContainerSupport {

    // 默认题库
    private static final String DEFAULT_LEVEL_KEY = "P{}0000";

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("eval_data_collect");
    }

    public AbilityExamBasic getQuestionInfo(Long studentId, Integer level) {
        RedisListCommands<String, Object> redisListCommands = redisCommands.sync().getRedisListCommands();
        List<Object> studentQuestionSetKey = redisListCommands.lrange(String.valueOf(studentId), 0, -1);
        String paperId;
        // 不存在该学生对应的题库
        if (studentQuestionSetKey == null || CollectionUtils.isEmpty(studentQuestionSetKey)) {
            paperId = StringUtils.formatMessage(DEFAULT_LEVEL_KEY, level);
        } else {
            paperId = String.valueOf(studentQuestionSetKey.get(0));
        }
        List<String> questionIdList = redisListCommands.lrange(paperId, 0, -1).stream()
                .filter(Objects::nonNull)
                .map(o -> (String) o)
                .collect(Collectors.toList());
        AbilityExamBasic basic = new AbilityExamBasic();
        basic.setPaperId(paperId);
        basic.setQuestionIds(questionIdList);
        return basic;
    }
}
