package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.annotation.MockBinders;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendData;
import com.voxlearning.utopia.service.newhomework.api.entity.VoiceRecommendDataSentenceRef;
import com.voxlearning.utopia.service.newhomework.impl.dao.VoiceRecommendDataPersistence;
import com.voxlearning.utopia.service.newhomework.impl.dao.VoiceRecommendDataSentenceRefPersistence;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class TestVoiceRecommendLoaderImpl extends NewHomeworkUnitTestSupport {

    @Test
    @TruncateDatabaseTable()
    @MockBinders({
            @MockBinder(
                    type = VoiceRecommendData.class,
                    jsons = {
                            "{'id':1,'studentId':11,'oldVoiceUrl':'ourl1','voiceUrl':'vurl1','sentenceEntext':'ido'}",
                            "{'id':2,'studentId':22,'oldVoiceUrl':'ourl2','voiceUrl':'vurl2','sentenceEntext':'ido'}",
                            "{'id':3,'studentId':33,'oldVoiceUrl':'ourl3','voiceUrl':'vurl3','sentenceEntext':'ido'}",
                    },
                    persistence = VoiceRecommendDataPersistence.class
            ),
            @MockBinder(
                    type = VoiceRecommendDataSentenceRef.class,
                    jsons = {
                            "{'sentenceId':569300520173807,'recommendIds':'1,2,3'}",
                            "{'sentenceId':569300520173808,'recommendIds':'1,2,3'}",
                    },
                    persistence = VoiceRecommendDataSentenceRefPersistence.class
            )
    })
    public void testFindVoiceRecommendDataBySentenceIds() throws Exception {
        Long sentenceId1 = 569300520173807L;
        Long sentenceId2 = 569300520173808L;
        Long sentenceId3 = 123L;
        Map<Long, VoiceRecommendData> map = voiceRecommendLoader.findVoiceRecommendDataBySentenceIds(Arrays.asList(sentenceId1, sentenceId2, sentenceId3));
        assertNotNull(map.get(sentenceId1));
        assertNotNull(map.get(sentenceId2));
        FlightRecorder.dot(">>>>>>>> CBS <<<<<<<<");
        Map<Long, VoiceRecommendData> map1 = voiceRecommendLoader.findVoiceRecommendDataBySentenceIds(Arrays.asList(sentenceId1, sentenceId2, sentenceId3));
        assertNotNull(map1.get(sentenceId1));
        assertNotNull(map1.get(sentenceId2));
    }

}
