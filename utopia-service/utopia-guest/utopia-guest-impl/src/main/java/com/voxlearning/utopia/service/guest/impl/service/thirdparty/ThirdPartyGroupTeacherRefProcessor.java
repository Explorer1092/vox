package com.voxlearning.utopia.service.guest.impl.service.thirdparty;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.guest.impl.dao.ThirdPartyGroupTeacherRefPersistence;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroupTeacherRef;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 先不做log记录了，目前也用不着，不过processor还是留着吧
 *
 * @author xuesong.zhang
 * @since 2016/9/27
 */
@Slf4j
@Named
public class ThirdPartyGroupTeacherRefProcessor {

    @Inject private ThirdPartyGroupTeacherRefPersistence thirdPartyGroupTeacherRefPersistence;

    public Long persist(ThirdPartyGroupTeacherRef thirdPartyGroupTeacherRef) {
        if (thirdPartyGroupTeacherRef == null) {
            return null;
        }
        return MiscUtils.firstElement(persist(Collections.singleton(thirdPartyGroupTeacherRef)));
    }

    public Collection<Long> persist(Collection<ThirdPartyGroupTeacherRef> thirdPartyGroupTeacherRefs) {
        if (CollectionUtils.isEmpty(thirdPartyGroupTeacherRefs)) {
            return Collections.emptyList();
        }
        return thirdPartyGroupTeacherRefPersistence.persist(thirdPartyGroupTeacherRefs);
    }

    /**
     * disable by groupId
     *
     * @param groupId
     * @return
     */
    public int disableByGroupId(Long groupId) {
        if (groupId == null) {
            return 0;
        }
        return disableByGroupIds(Collections.singleton(groupId));
    }

    /**
     * disable by groupIds
     *
     * @param groupIds
     */
    public int disableByGroupIds(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return 0;
        }
        List<ThirdPartyGroupTeacherRef> oldValues = thirdPartyGroupTeacherRefPersistence.findByGroupIds(groupIds).values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(oldValues)) {
            return 0;
        }
        Date date = new Date();
        return thirdPartyGroupTeacherRefPersistence.disable(oldValues, date);
    }
}
