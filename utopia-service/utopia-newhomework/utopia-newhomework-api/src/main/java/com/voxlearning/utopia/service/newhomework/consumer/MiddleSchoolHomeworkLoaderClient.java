package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.MiddleSchoolHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.middleschool.MiddleSchoolHomeworkCrmHistory;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import java.util.Collection;
import java.util.List;

/**
 * @author yaguang.wang
 * @since 2016/12/8
 */
public class MiddleSchoolHomeworkLoaderClient implements MiddleSchoolHomeworkLoader {

    @ImportService(interfaceClass = MiddleSchoolHomeworkLoader.class)
    private MiddleSchoolHomeworkLoader remoteReference;

    @Override
    public List<MiddleSchoolHomeworkCrmHistory> loadGroupRecentHomeworkList(Collection<GroupMapper> groups, Integer day) {
        return remoteReference.loadGroupRecentHomeworkList(groups, day);
    }
}
