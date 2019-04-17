package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyType;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarUserNotify;
import com.voxlearning.utopia.service.mizar.api.loader.MizarNotifyLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarNotifyMapper;
import com.voxlearning.utopia.service.mizar.impl.dao.notify.MizarNotifyDao;
import com.voxlearning.utopia.service.mizar.impl.dao.notify.MizarUserNotifyDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mizar Notify Loader Implementation Class
 * Created by Yuechen Wang on 2016/12/05.
 */
@Named
@Service(interfaceClass = MizarNotifyLoader.class)
@ExposeService(interfaceClass = MizarNotifyLoader.class)
public class MizarNotifyLoaderImpl implements MizarNotifyLoader {

    @Inject private MizarNotifyDao mizarNotifyDao;
    @Inject private MizarUserNotifyDao mizarUserNotifyDao;

    @Override
    public List<MizarNotifyMapper> loadAllUserNotify(String userId) {
        List<MizarUserNotify> notifyRefs = loadUserNotifyRefs(userId);
        if (CollectionUtils.isEmpty(notifyRefs)) {
            return Collections.emptyList();
        }
        List<String> notifyIds = notifyRefs.stream()
                .map(MizarUserNotify::getNotifyId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, MizarNotify> notifyMap = mizarNotifyDao.loads(notifyIds);
        List<MizarNotifyMapper> notifyList = new LinkedList<>();
        for (MizarUserNotify ref : notifyRefs) {
            if (!notifyMap.containsKey(ref.getNotifyId())) {
                continue;
            }
            MizarNotify notify = notifyMap.get(ref.getNotifyId());
            MizarNotifyMapper mapper = new MizarNotifyMapper();
            mapper.setId(ref.getId());
            mapper.setRead(ref.isRead());
            mapper.setNotifyId(notify.getId());
            mapper.setTitle(notify.getTitle());
            mapper.setType(MizarNotifyType.safeParse(notify.getType()));
            mapper.setContent(notify.getContent());
            mapper.setUrl(notify.getUrl());
            mapper.setFiles(notify.getFiles());
            mapper.setCreateAt(notify.getCreateAt());
            notifyList.add(mapper);
        }
        notifyList.sort((n1, n2) -> {
            long c1 = n1.getCreateAt().getTime();
            long c2 = n2.getCreateAt().getTime();
            return Long.compare(c2, c1);
        });
        return notifyList;
    }

    private List<MizarUserNotify> loadUserNotifyRefs(String userId) {
        return mizarUserNotifyDao.loadByUser(userId)
                .stream()
                .filter(n -> !n.isDisabledTrue())
                .collect(Collectors.toList());
    }

}
