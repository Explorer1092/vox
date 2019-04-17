package com.voxlearning.utopia.service.newhomework.impl.dao.livecast;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.UsTalkHomeworkData;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * 用于UsTalk导作业数据用的定时任务
 *
 * @author xuesong.zhang
 * @since 2017/8/8
 */
@Named
public class UsTalkHomeworkDataPersistence extends NoCacheStaticMySQLPersistence<UsTalkHomeworkData, Long> {
    public List<UsTalkHomeworkData> findAll() {
        List<UsTalkHomeworkData> list = query();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }
}
