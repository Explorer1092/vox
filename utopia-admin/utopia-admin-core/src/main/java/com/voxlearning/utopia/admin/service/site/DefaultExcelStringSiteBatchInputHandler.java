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

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author changyuan
 * @since 2016/3/31
 */
@Getter
@Setter
public class DefaultExcelStringSiteBatchInputHandler<R> implements SiteBatchInputHandler<String, String, R> {

    private final static String DEFAULT_ROW_SEPARATOR = "\\n";


    private String rowSeparator = DEFAULT_ROW_SEPARATOR;

    @Override
    public List<R> handleInput(String input, List<FailedData<String>> failedDatas, UnitTransformer<String, R> unitTransformer) {
        if (StringUtils.isEmpty(input)) {
            return Collections.emptyList();
        }

        List<R> result = new ArrayList<>();

        String[] rows = input.split(rowSeparator);
        for (String row : rows) {
            R ret = unitTransformer.transform(row);
            if (ret == null) {
                failedDatas.add(new FailedData<>(row, "错误的数据格式"));
            } else {
                result.add(ret);
            }
        }
        return result;
    }

}
