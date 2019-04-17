package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.service.SpecialTopicService;

/**
 * Created by xiang.lv on 2016/10/18.
 * <p>
 * 专题相关
 *
 * @author xiang.lv
 * @date 2016/10/18   13:52
 */
public class SpecialTopicServiceClient {

    @ImportService(interfaceClass = SpecialTopicService.class)
    private SpecialTopicService specialTopicService;


    public SpecialTopic save(SpecialTopic specialTopic) {
        return specialTopicService.save(specialTopic);
    }
}
