/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.RSOralPaperAnalysisReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author changyuan.liu
 * @since 2015/5/19
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSOralPaperAnalysisReportDao {
    @Autowired private RSOralPaperAnalysisReportDao rsOralPaperAnalysisReportDao;

    @Test
    public void testFindByPushId() throws Exception {
        RSOralPaperAnalysisReport report = new RSOralPaperAnalysisReport();
        report.setPushId(1L);
        rsOralPaperAnalysisReportDao.insert(report);

        assertEquals(1, rsOralPaperAnalysisReportDao.findByPushId(1L).size());
    }

    @Test
    public void testFindByPushIds() throws Exception {
        RSOralPaperAnalysisReport report = new RSOralPaperAnalysisReport();
        report.setPushId(1L);
        rsOralPaperAnalysisReportDao.insert(report);
        report = new RSOralPaperAnalysisReport();
        report.setPushId(2L);
        rsOralPaperAnalysisReportDao.insert(report);
        report = new RSOralPaperAnalysisReport();
        report.setPushId(3L);
        rsOralPaperAnalysisReportDao.insert(report);

        assertEquals(3, rsOralPaperAnalysisReportDao.findByPushIds(Arrays.asList(1L, 2L, 3L)).size());
    }

    @Test
    public void test() {
        String jsonStr = "[{\"questionId\": \"54898af0a3109de8b1f720f3\", \"score\": 2}, {\"questionId\": \"54898b34a3109de8b1f720fa\", \"score\": 2}, {\"questionId\": \"54898b73a3109de8b1f72101\", \"score\": 2}, {\"questionId\": \"54898bbba3109de8b1f72108\", \"score\": 2}, {\"questionId\": \"54898c12a3109de8b1f7210f\", \"score\": 2}, {\"questionId\": \"54898cb2a3109de8b1f72116\", \"score\": 2}, {\"questionId\": \"54898d1ba3109de8b1f7211d\", \"score\": 2}, {\"questionId\": \"54898d61a3109de8b1f72124\", \"score\": 2}, {\"questionId\": \"54898d9da3109de8b1f7212b\", \"score\": 2}, {\"questionId\": \"54898ddaa3109de8b1f72132\", \"score\": 2}, {\"questionId\": \"54898e65a3109de8b1f72136\", \"score\": 3}, {\"questionId\": \"54898ea1a3109de8b1f7213a\", \"score\": 3}, {\"questionId\": \"54898ebea3109de8b1f7213e\", \"score\": 3}, {\"questionId\": \"54898ee6a3109de8b1f72142\", \"score\": 3}, {\"questionId\": \"54898f04a3109de8b1f72146\", \"score\": 3}, {\"questionId\": \"548990aca3109de8b1f7215f\", \"score\": 35}, {\"questionId\": \"548990f1a3109de8b1f72163\", \"score\": 30}]";
        List<HashMap> hashMaps = JsonUtils.fromJsonToList(jsonStr, HashMap.class);

        Map<String, Integer> map = new HashMap<>();
        hashMaps.forEach(m -> {
            map.put(m.get("questionId").toString(), Integer.valueOf(m.get("score").toString()));
        });
        int a = 1;
    }

    @Test
    public void testUpdateFlagReportTime() throws Exception {
        String id = rsOralPaperAnalysisReportDao.insert(new RSOralPaperAnalysisReport());
        rsOralPaperAnalysisReportDao.updateFlagReportTime(id, new Date(10000));
        assertEquals(10000, rsOralPaperAnalysisReportDao.load(id).getCreateAt().getTime());
    }
}
