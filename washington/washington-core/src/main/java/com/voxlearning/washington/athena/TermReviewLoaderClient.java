/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.athena.api.recom.loader.TermReviewLoader;
import com.voxlearning.washington.support.WorkbookUtils;
import lombok.Getter;
import org.apache.poi.ss.usermodel.*;

import javax.inject.Named;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Named("com.voxlearning.washington.athena.TermReviewLoaderClient")
public class TermReviewLoaderClient {

    @Getter
    @ImportService(interfaceClass = TermReviewLoader.class)
    private TermReviewLoader termReviewLoader;

    public static void main(String[] args) throws Exception {
        InputStream resource = TermReviewLoaderClient.class.getResourceAsStream("/activity/feature_50111_school_dict.xlsx");
        Workbook workbook = WorkbookFactory.create(resource);
        Sheet sheet = workbook.getSheetAt(0);

        int lastRowNum = sheet.getLastRowNum();
        System.out.println("total " + lastRowNum + " found");

        Map<Long, String> t = new LinkedHashMap<>(lastRowNum);

        for (int i = 0; i < lastRowNum; ++i) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell schoolCell = row.getCell(0);
            Cell levelCell  = row.getCell(1);

            t.put(SafeConverter.toLong(WorkbookUtils.getCellValue(schoolCell)), WorkbookUtils.getCellValue(levelCell));
        }

        System.out.println(
                JsonUtils.toJson(t).getBytes().length / 1024 + "KB"
        );

    }


}
