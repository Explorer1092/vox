package com.voxlearning.utopia.service.ai.impl.support;

import com.mongodb.annotations.NotThreadSafe;

/**
 * @author xuan.zhu
 * @date 2018/9/11 11:37
 * lambda 内的自增工具
 */
@NotThreadSafe
public class LambdaCounter {
    private int count = 1;

    public int getAndIncrement() {
        return count++;
    }
}
