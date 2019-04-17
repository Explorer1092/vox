package com.voxlearning.utopia.service.user.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.user.api.constants.ThirdPartyGroupType;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.api.mappers.third.ThirdPartyGroupMapper;
import com.voxlearning.utopia.service.user.api.service.thirdparty.ThirdPartyGroupLoader;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
public class ThirdPartyGroupLoaderClient implements ThirdPartyGroupLoader {

    @ImportService(interfaceClass = ThirdPartyGroupLoader.class)
    private ThirdPartyGroupLoader remoteReference;

    @Override
    public Map<Long, ThirdPartyGroup> loadThirdPartyGroupsIncludeDisabled(Collection<Long> groupIds) {
        return remoteReference.loadThirdPartyGroupsIncludeDisabled(groupIds);
    }

    @Override
    public Map<Long, List<ThirdPartyGroupMapper>> loadTeacherGroups(Collection<Long> teacherIds, ThirdPartyGroupType groupType) {
        return remoteReference.loadTeacherGroups(teacherIds, groupType);
    }

    @Override
    public Map<Long, List<ThirdPartyGroupMapper>> loadStudentGroups(Collection<Long> studentIds, ThirdPartyGroupType groupType) {
        return remoteReference.loadStudentGroups(studentIds, groupType);
    }

    @Override
    public Map<Long, List<Long>> loadGroupStudentIds(Collection<Long> groupIds) {
        return remoteReference.loadGroupStudentIds(groupIds);
    }
}
