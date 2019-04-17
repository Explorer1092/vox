/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.service.site;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 批量输入处理器
 * CT 批量输入的数据类型
 * RT 单元数据的类型
 * R 返回的处理后的单元数据类型
 *
 * @author changyuan
 * @since 2016/3/31
 */
public interface SiteBatchInputHandler<CT, RT, R> {

    /**
     * 错误数据
     *
     * @param <RT> 每行的类型
     */
    @Getter
    @Setter
    @AllArgsConstructor
    class FailedData<RT> {
        RT unitData;
        String errorMsg;
    }

    /**
     * 输入单元转为处理后单元转换器
     *
     * @param <RT>
     * @param <R>
     */
    interface UnitTransformer<RT, R> {
        R transform(RT words);
    }

    /**
     * 处理输入
     *
     * @param input 输入
     * @param failedDatas    失败的单元数据
     * @param unitTransformer    将输入的单元数据转换成处理后单元数据的transformer
     * @return 处理后单元数据列表
     */
    List<R> handleInput(CT input, List<FailedData<RT>> failedDatas, UnitTransformer<RT, R> unitTransformer);
}
