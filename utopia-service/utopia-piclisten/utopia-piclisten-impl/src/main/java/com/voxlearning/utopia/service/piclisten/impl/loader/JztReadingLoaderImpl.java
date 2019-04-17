package com.voxlearning.utopia.service.piclisten.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.piclisten.api.JztReadingLoader;
import com.voxlearning.utopia.service.piclisten.impl.dao.UserReadingRefPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.UserReadingRef;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author malong
 * @since 2016/12/22
 */
@Named
@Service(interfaceClass = JztReadingLoader.class)
@ExposeService(interfaceClass = JztReadingLoader.class)
public class JztReadingLoaderImpl implements JztReadingLoader {
    @Inject
    private UserReadingRefPersistence userReadingRefPersistence;

    @Override
    public Map<Long, List<UserReadingRef>> getUserReadingRefsByUserIds(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return userReadingRefPersistence.getUserReadingRefsByUserIds(userIds);
    }

    @Override
    public List<UserReadingRef> getUserReadingRefsByUserId(@CacheParameter(value = "UID") Long userId) {
        if (userId == 0L) {
            return Collections.emptyList();
        }
        return userReadingRefPersistence.getUserReadingRefsByUserId(userId);
    }
}
