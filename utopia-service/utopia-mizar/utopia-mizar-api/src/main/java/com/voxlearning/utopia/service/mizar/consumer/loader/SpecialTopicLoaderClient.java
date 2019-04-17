package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicPosition;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.loader.SpecialTopicLoader;

import java.util.List;

/**
 * Created by xiang.lv on 2016/10/18.
 * <p>
 * 专题相关
 *
 * @author xiang.lv
 * @date 2016/10/18   13:52
 */
public class SpecialTopicLoaderClient {

    @ImportService(interfaceClass = SpecialTopicLoader.class)
    private SpecialTopicLoader specialTopicLoader;

    public List<SpecialTopic> loadActiveSpecialTopic() {
        return specialTopicLoader.loadActiveSpecialTopic();
    }

    public List<SpecialTopic> loadAllTopics() {
        return specialTopicLoader.loadAllSpecialTopic();
    }

    public List<SpecialTopic> loadSpecialTopicList(final SpecialTopicPosition specialTopicPosition) {
        return specialTopicLoader.loadSpecialTopicList(specialTopicPosition);
    }

    public SpecialTopic loadById(final String specialTopicId) {
        return specialTopicLoader.loadSpecialTopicById(specialTopicId);
    }
}
