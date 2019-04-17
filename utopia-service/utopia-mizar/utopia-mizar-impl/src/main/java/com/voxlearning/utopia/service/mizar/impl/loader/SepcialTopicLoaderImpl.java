package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.constants.SpecialTopicPosition;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.loader.SpecialTopicLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.SpecialTopicDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiang.lv
 * @date 2016/10/18   10:48
 */
@Named
@Service(interfaceClass = SpecialTopicLoader.class)
@ExposeService(interfaceClass = SpecialTopicLoader.class)
public class SepcialTopicLoaderImpl extends SpringContainerSupport implements SpecialTopicLoader {
    @Inject
    SpecialTopicDao specialTopicDao;

    @Override
    public List<SpecialTopic> loadAllSpecialTopic() {
        return specialTopicDao.loadAllSpecialTopic();
    }

    @Override
    public List<SpecialTopic> loadActiveSpecialTopic() {
        return specialTopicDao.loadActiveSpecialTopic();
    }

    @Override
    public SpecialTopic loadSpecialTopicById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return specialTopicDao.load(id);
    }

    @Override
    public List<SpecialTopic> loadSpecialTopicList(SpecialTopicPosition specialTopicPosition) {
        List<SpecialTopic> allList = loadAllSpecialTopic();
        if (CollectionUtils.isEmpty(allList)) {
            return Collections.emptyList();
        }
        return allList.stream().filter(o -> StringUtils.equalsIgnoreCase(o.getPosition(), specialTopicPosition.name()))
                .filter(o -> StringUtils.isNotBlank(o.getUrl())).collect(Collectors.toList());
    }
}
