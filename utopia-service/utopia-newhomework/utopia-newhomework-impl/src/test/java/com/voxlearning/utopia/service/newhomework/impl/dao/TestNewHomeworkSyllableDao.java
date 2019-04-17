package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/2/8 20:43
 */
@DropMongoDatabase
public class TestNewHomeworkSyllableDao extends NewHomeworkUnitTestSupport {
    @Test
    public void testSaveSyllable() throws Exception {
        String day = timeStamp2Date(String.valueOf(Integer.parseInt("58510894", 16)), "yyyyMMdd");
        Long studentId = 333803086L;
        String homeworkId = "201612_58510894e92b1b1f28c9f394";
        String audio = "9D017DDF-00C1-414B-8BEB-91ECB534AA86_1485533793761953087_sh";
        String dataStr = "{\n" +
                "    \"homeworkId\":\"201612_58510894e92b1b1f28c9f394\",\n" +
                "    \"objectiveConfigType\":\"BASIC\",\n" +
                "    \"bookId\":\"111\",\n" +
                "    \"unitId\":\"111\",\n" +
                "    \"lessonId\":\"111\",\n" +
                "    \"sentenceId\":\"111\",\n" +
                "    \"questionId\":\"111\",\n" +
                "    \"clientType\":\"mobile\",\n" +
                "    \"clientName\":\"17studentapp\",\n" +
                "    \"ipImei\":\"111\",\n" +
                "    \"audio\":\"http://edu.hivoice" +
                ".cn:80/WebAudio-1.0-SNAPSHOT/audio/play/9D017DDF-00C1-414B-8BEB-91ECB534AA86/1485533793761953087/sh\",\n" +
                "    \"lines\":[\n" +
                "        {\n" +
                "            \"sample\":\"nice\",\n" +
                "            \"usertext\":\"nice\",\n" +
                "            \"begin\":0,\n" +
                "            \"end\":1.941,\n" +
                "            \"score\":42.738,\n" +
                "            \"fluency\":76.304,\n" +
                "            \"integrity\":100,\n" +
                "            \"pronunciation\":40.972,\n" +
                "            \"words\":[\n" +
                "                {\n" +
                "                    \"text\":\"nice\",\n" +
                "                    \"type\":2,\n" +
                "                    \"score\":3.706,\n" +
                "                    \"subwords\":[\n" +
                "                        {\n" +
                "                            \"subtext\":\"n\",\n" +
                "                            \"score\":3.706\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"subtext\":\"aɪ\",\n" +
                "                            \"score\":3.706\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"subtext\":\"s\",\n" +
                "                            \"score\":3.706\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        Map<String, Object> contentMap = JsonUtils.fromJson(dataStr);
        contentMap.put("userId", studentId);
//        newHomeworkSyllableDao.saveSyllable(contentMap);
//        NewHomeworkSyllable.ID id = new NewHomeworkSyllable.ID(day, studentId, homeworkId, audio);
//        NewHomeworkSyllable result = newHomeworkSyllableDao.load(id.toString());
//        Assert.assertEquals(result.getHomeworkId(), "201612_58510894e92b1b1f28c9f394");
    }

    private static String timeStamp2Date(String timestampString, String formats) {
        Long timestamp = Long.parseLong(timestampString) * 1000;
        return new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
    }
}
