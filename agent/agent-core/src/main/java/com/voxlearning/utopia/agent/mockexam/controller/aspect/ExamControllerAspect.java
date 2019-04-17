package com.voxlearning.utopia.agent.mockexam.controller.aspect;

import com.voxlearning.utopia.agent.mockexam.controller.ViewBuilder;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * contoller前后都要做点什么
 *
 * @author xiaolei.li
 * @version 2018/8/18
 */
@Slf4j
@Aspect
@Component
public class ExamControllerAspect {

    /**
     * 环切,处理异常
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(public com.voxlearning.alps.lang.util.MapMessage com.voxlearning.utopia.agent.mockexam.controller.*.*(..)) " +
            "&& " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "&& " +
            "@annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (BusinessException e) {
            return ViewBuilder.error(e);
        } catch (Exception e) {
            log.error("未能处理的异常", e);
            return ViewBuilder.error(e);
        }
    }
}
