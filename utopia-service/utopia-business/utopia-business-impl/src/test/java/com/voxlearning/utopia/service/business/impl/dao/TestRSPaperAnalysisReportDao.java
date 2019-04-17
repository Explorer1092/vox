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

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSPaperAnalysisReportDao {

    @Inject private RSPaperAnalysisReportDao rsPaperAnalysisReportDao;

    @Test
    public void testFindByPaperIds() throws Exception {
        Set<String> paperIds = Arrays.asList("a", "b", "c").stream().collect(Collectors.toSet());
        paperIds.stream()
                .map(t -> {
                    RSPaperAnalysisReport report = new RSPaperAnalysisReport();
                    report.setPaperId(t);
                    return report;
                })
                .forEach(rsPaperAnalysisReportDao::save);
        List<RSPaperAnalysisReport> list = rsPaperAnalysisReportDao.findByPaperIds(paperIds);
        assertEquals(3, list.size());
    }
}
