package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2015/12/23.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAppMessageServiceImpl {


    @Inject
    private AppMessageServiceImpl appMessageService;

    @Test
    public void testSendByTagDuration() {

        String content = "content";
        AppMessageSource source = AppMessageSource.PARENT;
        List<String> tags = new ArrayList<>();
        tags.add("tags");
        List<String> tagsAnd = new ArrayList<>();
        tagsAnd.add("tagAnd");
        Map<String, Object> extInfo = new HashMap();
        extInfo.put("taskId","xxoo");
        extInfo.put("s", "s");
        extInfo.put("link", "link");
        extInfo.put("t", "h5");
        extInfo.put("key", "key");
        appMessageService.sendAppJpushMessageByTags(content,source,tags,tagsAnd,extInfo,100);
    }

    @Test
    public void testSendByTag() {

        String content = "content";
        AppMessageSource source = AppMessageSource.PARENT;
        List<String> tags = new ArrayList<>();
        tags.add("tags");
        List<String> tagsAnd = new ArrayList<>();
        tagsAnd.add("tagAnd");
        Map<String, Object> extInfo = new HashMap();
        extInfo.put("taskId","xxoo");
        extInfo.put("s", "s");
        extInfo.put("link", "link");
        extInfo.put("t", "h5");
        extInfo.put("key", "key");
        appMessageService.sendAppJpushMessageByTags(content,source,tags,tagsAnd,extInfo);
    }



    @Test
    public void testSendByIds() {

        String content = "content";
        AppMessageSource source = AppMessageSource.PARENT;
        List<Long> userIds = new ArrayList<>();
        userIds.add(1l);
        userIds.add(2l);
        Map<String, Object> extInfo = new HashMap();
        extInfo.put("taskId","xxoo");
        extInfo.put("s", "s");
        extInfo.put("link", "link");
        extInfo.put("t", "h5");
        extInfo.put("key", "key");
        appMessageService.sendAppJpushMessageByIds(content,source,userIds,extInfo);
    }

    @Test
    public void testSendByIdsDuration() {

        String content = "content";
        AppMessageSource source = AppMessageSource.PARENT;
        List<Long> userIds = new ArrayList<>();
        userIds.add(1l);
        userIds.add(2l);
        Map<String, Object> extInfo = new HashMap();
        extInfo.put("taskId","xxoo");
        extInfo.put("s", "s");
        extInfo.put("link", "link");
        extInfo.put("t", "h5");
        extInfo.put("key", "key");
        appMessageService.sendAppJpushMessageByIds(content,source,userIds,extInfo,100l);
    }





}
