package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.SpecialTopic;
import com.voxlearning.utopia.service.mizar.api.service.SpecialTopicService;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.SpecialTopicDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xiang.lv
 * @date 2016/10/18   10:48
 */
@Named
@Service(interfaceClass = SpecialTopicService.class)
@ExposeService(interfaceClass = SpecialTopicService.class)
public class SepcialTopicServiceImpl extends SpringContainerSupport implements SpecialTopicService {

    @Inject
    private SpecialTopicDao specialTopicDao;

    @Override
    public SpecialTopic save(final SpecialTopic specialTopic) {
        SpecialTopic upsert = null;
        if (null != specialTopic) {
            upsert = specialTopicDao.upsert(specialTopic);
        }
        return upsert;
    }


}
