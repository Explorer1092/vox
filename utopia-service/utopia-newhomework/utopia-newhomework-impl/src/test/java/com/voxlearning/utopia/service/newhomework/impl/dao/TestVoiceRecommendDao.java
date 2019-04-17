package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.VoiceRecommendV1;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.VoiceRecommendV2;
import com.voxlearning.utopia.service.newhomework.api.mapper.VoiceRecommend;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.VoiceRecommendV1Dao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.VoiceRecommendV2Dao;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@DropMongoDatabase
public class TestVoiceRecommendDao extends NewHomeworkUnitTestSupport {
    @Test
    @MockBinder(
            type = VoiceRecommendV1.class,
            jsons = {
                    "{\n" +
                            "  \"id\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"homeworkId\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"teacherId\": 126287,\n" +
                            "  \"clazzId\": 1001557,\n" +
                            "  \"groupId\": 9917\n" +
                            "}",
                    "{\n" +
                            "  \"id\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"homeworkId\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"teacherId\": 11378,\n" +
                            "  \"clazzId\": 37493,\n" +
                            "  \"groupId\": 4577,\n" +
                            "  \"recommendVoiceList\": [\n" +
                            "    {\n" +
                            "      \"studentId\": 333900813,\n" +
                            "      \"studentName\": \"看的了\",\n" +
                            "      \"practiceName\": \"单词跟读\",\n" +
                            "      \"voiceList\": [\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/B57B6951-8EDF-40CF-B95D-3973DCE47F17\\/1465208541715979027\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/9C6C1EF8-8B46-4E66-977A-E9A6C8535A86\\/1465208548805584105\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/2B63B629-E398-4FF6-89B1-8A458422DAEE\\/1465208556184792673\\/bj\"\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"recommendComment\": \"这些同学读得很不错！\"\n" +
                            "}"
            },
            persistence = VoiceRecommendV1Dao.class
    )
    public void testLoadFromV1() {
        Map<String, VoiceRecommend> voiceRecommendMap = voiceRecommendDao.loads(Collections.singleton("575547b41b06767a1fcfefe9"));
        assertEquals(voiceRecommendMap.size(), 1);
        voiceRecommendMap = voiceRecommendDao.loads(Collections.singleton("575547b41b06767a1fcfefe9"));
    }

    @Test
    @MockBinder(
            type = VoiceRecommendV2.class,
            jsons = {
                    "{\n" +
                            "  \"id\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"homeworkId\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"teacherId\": 126287,\n" +
                            "  \"clazzId\": 1001557,\n" +
                            "  \"groupId\": 9917\n" +
                            "}",
                    "{\n" +
                            "  \"id\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"homeworkId\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"teacherId\": 11378,\n" +
                            "  \"clazzId\": 37493,\n" +
                            "  \"groupId\": 4577,\n" +
                            "  \"recommendVoiceList\": [\n" +
                            "    {\n" +
                            "      \"studentId\": 333900813,\n" +
                            "      \"studentName\": \"看的了\",\n" +
                            "      \"practiceName\": \"单词跟读\",\n" +
                            "      \"voiceList\": [\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/B57B6951-8EDF-40CF-B95D-3973DCE47F17\\/1465208541715979027\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/9C6C1EF8-8B46-4E66-977A-E9A6C8535A86\\/1465208548805584105\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/2B63B629-E398-4FF6-89B1-8A458422DAEE\\/1465208556184792673\\/bj\"\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"recommendComment\": \"这些同学读得很不错！\"\n" +
                            "}"
            },
            persistence = VoiceRecommendV2Dao.class
    )
    public void testLoadFromV2() {
        Map<String, VoiceRecommend> voiceRecommendMap = voiceRecommendDao.loads(Collections.singleton("575547b41b06767a1fcfefe9"));
        assertEquals(voiceRecommendMap.size(), 1);
        voiceRecommendMap = voiceRecommendDao.loads(Collections.singleton("575547b41b06767a1fcfefe9"));
    }

    @Test
    @MockBinder(
            type = VoiceRecommendV2.class,
            jsons = {
                    "{\n" +
                            "  \"id\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"homeworkId\": \"57578c171b06760dafda7abe\",\n" +
                            "  \"teacherId\": 126287,\n" +
                            "  \"clazzId\": 1001557,\n" +
                            "  \"groupId\": 9917\n" +
                            "}",
                    "{\n" +
                            "  \"id\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"homeworkId\": \"575547b41b06767a1fcfefe9\",\n" +
                            "  \"teacherId\": 11378,\n" +
                            "  \"clazzId\": 37493,\n" +
                            "  \"groupId\": 4577,\n" +
                            "  \"recommendVoiceList\": [\n" +
                            "    {\n" +
                            "      \"studentId\": 333900813,\n" +
                            "      \"studentName\": \"看的了\",\n" +
                            "      \"practiceName\": \"单词跟读\",\n" +
                            "      \"voiceList\": [\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/B57B6951-8EDF-40CF-B95D-3973DCE47F17\\/1465208541715979027\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/9C6C1EF8-8B46-4E66-977A-E9A6C8535A86\\/1465208548805584105\\/bj\",\n" +
                            "        \"http:\\/\\/edu.hivoice.cn:80\\/WebAudio-1.0-SNAPSHOT\\/audio\\/play\\/2B63B629-E398-4FF6-89B1-8A458422DAEE\\/1465208556184792673\\/bj\"\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ],\n" +
                            "  \"recommendComment\": \"这些同学读得很不错！\"\n" +
                            "}"
            },
            persistence = VoiceRecommendV2Dao.class
    )
    public void testUpsert() {
        FlightRecorder.dot("1111111111111111111111111111111111111");
        VoiceRecommend voiceRecommend1 = voiceRecommendDao.load("57578c171b06760dafda7abe");
        FlightRecorder.dot("2222222222222222222222222222222222222");
        VoiceRecommend voiceRecommend2 = voiceRecommendDao.load("575547b41b06767a1fcfefe9");
        FlightRecorder.dot("3333333333333333333333333333333333333");
        assertNotNull(voiceRecommend1);
        assertNotNull(voiceRecommend2);
        voiceRecommend1.setRecommendVoiceList(voiceRecommend2.getRecommendVoiceList());
        voiceRecommend1.setRecommendComment(voiceRecommend2.getRecommendComment());
        voiceRecommendDao.upsert(voiceRecommend1);
        FlightRecorder.dot("4444444444444444444444444444444444444");
        voiceRecommendDao.load("57578c171b06760dafda7abe");
        FlightRecorder.dot("5555555555555555555555555555555555555");
    }
}
