package com.voxlearning.utopia.service.push.impl.base;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.voxlearning.utopia.core.utils.LoggerUtils;

import java.util.concurrent.TimeUnit;

/**
 * 通用业务处理模板
 *
 * @author Wenlong Meng
 * @since Mar 13, 2019
 * @param <T>
 */
public abstract class BizTemplate<T> {

    protected String biz;
    protected Stopwatch watch;

    /**
     * 构建指定业务的处理模板
     *
     * @param biz
     */
    public BizTemplate(String biz) {
        this.biz = biz;
    }

    /**
     * 检查参数
     */
    protected void checkParams(){

    }

    /**
     * 业务处理
     *
     * @return
     */
    protected abstract T process();

    /**
     * 后处理，紧跟{@link #onError(Throwable)} or {@link #onSuccess()} 后
     */
    protected void afterProcess() {
    }

    /**
     * 成功后处理
     */
    protected void onSuccess() {
    }

    /**
     * 异常后处理
     * @param e
     */
    protected void onError(Throwable e) {
    }

    /**
     * 执行
     * @return
     */
    public T execute() {
        watch = Stopwatch.createStarted();
        try {
            checkParams();
            T result = process();
            onSuccess();
            return result;
        } catch (Throwable e) {
            onError(e);
            LoggerUtils.info(biz + ".error", e);
            Throwables.propagate(e);
        } finally {
            afterProcess();
            LoggerUtils.debug(biz, watch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
        return null;
    }
}
