package com.voxlearning.utopia.service.piclisten.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.piclisten.api.JztReadingService;
import com.voxlearning.utopia.service.piclisten.impl.dao.UserReadingRefPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

/**
 * @author malong
 * @since 2016/12/22
 */
@Named
@Service(interfaceClass = JztReadingService.class)
@ExposeService(interfaceClass = JztReadingService.class)
public class JztReadingServiceImpl implements JztReadingService {
    @Inject
    private UserReadingRefPersistence userReadingRefPersistence;

    @Override
    public MapMessage upsertUserReadingRef(UserReadingRef userReadingRef) {
        if (userReadingRef == null) {
            return MapMessage.errorMessage("数据不存在");
        }
        return userReadingRefPersistence.upsertUserReadingRef(userReadingRef);

    }

    @Override
    public MapMessage deleteUserReadingRefs(Long userId, Set<String> pictureBookIds) {
        if (userId == 0 || CollectionUtils.isEmpty(pictureBookIds)) {
            return MapMessage.errorMessage("用户id或者绘本id出错");
        }
        return userReadingRefPersistence.deleteReadingRefsByUserIdAndReadingIds(userId, pictureBookIds);
    }
}
