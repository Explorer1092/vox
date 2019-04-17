package com.voxlearning.utopia.service.newhomework.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;
import com.voxlearning.utopia.service.newhomework.api.service.AbilityExamService;
import com.voxlearning.utopia.service.newhomework.impl.dao.bonus.AbilityExamBasicDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.bonus.AbilityExamQuestionDao;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.answer.AbilityExamAnswerProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.doData.AbilityExamDoContext;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.doData.AbilityExamDoProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.index.AbilityExamIndexContext;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.index.AbilityExamIndexProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.question.AbilityExamQuestionProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-10-31
 */
@Named
@ExposeService(interfaceClass = AbilityExamService.class)
public class AbilityExamServiceImpl extends SpringContainerSupport implements AbilityExamService {

    @Inject private AbilityExamIndexProcessor abilityExamIndexProcessor;
    @Inject private AbilityExamDoProcessor abilityExamDoProcessor;
    @Inject private AbilityExamQuestionProcessor abilityExamQuestionProcessor;
    @Inject private AbilityExamAnswerProcessor abilityExamAnswerProcessor;

    @Inject private AbilityExamBasicDao abilityExamBasicDao;
    @Inject protected AbilityExamQuestionDao abilityExamQuestionDao;

    @Override
    public MapMessage index(Long studentId) {
        if (studentId == null) {
            return MapMessage.errorMessage("参数错误");
        }

        AbilityExamIndexContext indexContext = new AbilityExamIndexContext();
        indexContext.setStudentId(studentId);

        abilityExamIndexProcessor.index(indexContext);

        if (indexContext.isSuccessful()) {
            return MapMessage.successMessage().add("homeworkList", indexContext.getDataMap());
        } else {
            return MapMessage.errorMessage(indexContext.getMessage()).setErrorCode(indexContext.getErrorCode());
        }
    }

    @Override
    public MapMessage doData(Long studentId) {
        if (studentId == null) {
            return MapMessage.errorMessage("参数错误");
        }

        AbilityExamDoContext context = new AbilityExamDoContext();
        context.setStudentId(studentId);

        abilityExamDoProcessor.process(context);
        if (context.isSuccessful()) {
            return MapMessage.successMessage().add("data", context.getVars());
        } else {
            return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
        }
    }

    @Override
    public Map<String, Object> loadQuestion(Long studentId) {
        return abilityExamQuestionProcessor.loadQuestions(studentId);
    }

    @Override
    public Map<String, Object> loadQuestionAnswer(Long studentId) {
        return abilityExamQuestionProcessor.loadQuestionAnswer(studentId);
    }

    @Override
    public AbilityExamAnswerContext postQuestionAnswer(AbilityExamAnswerContext context) {
        try {
            return AtomicLockManager.instance()
                    .wrapAtomic(abilityExamAnswerProcessor)
                    .keyPrefix("postQuestionAnswer")
                    .keys(context.getUserId())
                    .proxy()
                    .process(context);
        } catch (CannotAcquireLockException ex) {
            context.errorResponse("正在处理，请不要重复提交");
            context.setErrorCode("104");
            return context;
        } catch (Exception ex) {
            context.errorResponse("数据异常，重试一下吧");
            context.setErrorCode("100");
            return context;
        }
    }

    @Override
    public AbilityExamBasic loadAbilityExamBasic(String id) {
        return abilityExamBasicDao.load(id);
    }

    public void delSomeForCrm(Long studentId) {
        if (studentId != null) {
            abilityExamBasicDao.remove(SafeConverter.toString(studentId));
            abilityExamQuestionDao.remove(SafeConverter.toString(studentId));
        }
    }

    public void delSomeForCrms(String studentIdStr) {
        List<String> studentIds = Arrays.asList(StringUtils.split(studentIdStr, ","));
        if (CollectionUtils.isNotEmpty(studentIds)) {
            abilityExamBasicDao.removes(studentIds);
            abilityExamQuestionDao.removes(studentIds);
        }
    }
}
