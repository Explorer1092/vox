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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendDataSentenceRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = VoiceRecommendDataSentenceRef.class)
public class TestVoiceRecommendDataSentenceRefPersistence {

    @Inject private VoiceRecommendDataSentenceRefPersistence voiceRecommendDataSentenceRefPersistence;

    @Test
    public void testVoiceRecommendDataSentenceRefPersistence() throws Exception {
        long sentenceId = RandomUtils.nextLong();
        VoiceRecommendDataSentenceRef ref = new VoiceRecommendDataSentenceRef();
        ref.setSentenceId(sentenceId);
        voiceRecommendDataSentenceRefPersistence.insert(ref);
        ref = voiceRecommendDataSentenceRefPersistence.load(sentenceId);
        assertNotNull(ref);
    }

}
