package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.UserAdvertisementInfoService;
import com.voxlearning.utopia.service.business.api.entity.StudentAdvertisementInfo;
import com.voxlearning.utopia.service.business.consumer.StudentAdvertisementInfoLoaderClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author malong
 * @since 2016/10/24
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestStudentAdvertisementInfoService {

    @Inject StudentAdvertisementInfoLoaderClient studentAdvertisementInfoLoaderClient;

    @Inject
    private UserAdvertisementInfoService userAdvertisementInfoService;

    @Test
    public void test() {
        StudentAdvertisementInfo studentAdvertisementInfo = new StudentAdvertisementInfo();
        studentAdvertisementInfo.setUserId(333929894L);
        studentAdvertisementInfo.setSlotId("120202");
        studentAdvertisementInfo.setMessageText("测试");
        studentAdvertisementInfo.setImgUrl("https://cdn.17zuoye.com/static/project/app/teacher_test.png");
        studentAdvertisementInfo.setClickUrl("https://www.17zuoye.com/help/jobs.vpage");
        studentAdvertisementInfo.setShowEndTime(System.currentTimeMillis() + 6000000L);
        studentAdvertisementInfo.setShowStartTime(System.currentTimeMillis() - 6000000L);
        studentAdvertisementInfo.setBtnContent("test17171717");
        userAdvertisementInfoService.insert(studentAdvertisementInfo);

        StudentAdvertisementInfo info = studentAdvertisementInfoLoaderClient
                .loadByUserId(1L)
                .stream()
                .filter(p -> "120202" .equals(p.getSlotId()))
                .findFirst()
                .orElse(null);

        assertTrue(info != null);
        assertEquals("测试", info.getMessageText());
    }

    public static void main(String[] args) {
        StudentAdvertisementInfo studentAdvertisementInfo = new StudentAdvertisementInfo();
        studentAdvertisementInfo.setUserId(333929894L);
        studentAdvertisementInfo.setSlotId("120202");
        studentAdvertisementInfo.setMessageText("测试");
        studentAdvertisementInfo.setImgUrl("https://cdn.17zuoye.com/static/project/app/teacher_test.png");
        studentAdvertisementInfo.setClickUrl("https://www.17zuoye.com/help/jobs.vpage");
        studentAdvertisementInfo.setShowEndTime(System.currentTimeMillis() + 6000000L);
        studentAdvertisementInfo.setShowStartTime(System.currentTimeMillis() - 6000000L);
        studentAdvertisementInfo.setBtnContent("测试2018928-1");
        System.out.println(JsonUtils.toJson(studentAdvertisementInfo));
    }
}
