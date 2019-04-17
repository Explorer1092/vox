package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicPosition;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 专题
 *
 * @author xiang.lv
 * @date 2016/10/18   10:32
 */
@ServiceVersion(version = "20161018")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface SpecialTopicLoader extends IPingable {

    List<SpecialTopic> loadAllSpecialTopic();

    List<SpecialTopic> loadActiveSpecialTopic();

    SpecialTopic loadSpecialTopicById(final String id);

    List<SpecialTopic> loadSpecialTopicList(final SpecialTopicPosition specialTopicPosition);
}
