package com.voxlearning.utopia.agent.mockexam.service;

import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * service layer exception aspect
 *
 * @author xiaolei.li
 * @version 2018/8/18
 */
@Slf4j
@Aspect
@Component
public class ExamServiceExceptionAspect {

    /**
     * 环切
     *
     * @param joinPoint 切点
     * @return 结果
     * @throws Throwable
     */
    @Around("execution(public " +
            "com.voxlearning.utopia.agent.mockexam.service.dto.Result " +
            "com.voxlearning.utopia.agent.mockexam.service.*.*(..)) ")
    public Object round(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BusinessException e) {
            return Result.error(e);
        } catch (Exception e) {
            log.error("未能处理的异常", e);
            return Result.error(e);
        }
    }
}
