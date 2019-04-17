package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarUserNotify;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.service.MizarNotifyService;
import com.voxlearning.utopia.service.mizar.impl.dao.notify.MizarNotifyDao;
import com.voxlearning.utopia.service.mizar.impl.dao.notify.MizarUserNotifyDao;
import com.voxlearning.utopia.service.mizar.impl.dao.user.MizarUserDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by wangshichao on 16/9/6.
 */

@Named
@Service(interfaceClass = MizarNotifyService.class)
@ExposeService(interfaceClass = MizarNotifyService.class)
public class MizarNotifyServiceImpl implements MizarNotifyService {

    @Inject private MizarNotifyDao mizarNotifyDao;
    @Inject private MizarUserNotifyDao mizarUserNotifyDao;
    @Inject private MizarUserDao mizarUserDao;

    @Override
    public MapMessage sendNotify(MizarNotify notify, Collection<String> users) {
        if (notify == null) {
            return MapMessage.errorMessage("参数无效");
        }
        // 过滤掉无用的userId
        Set<String> validUsers = mizarUserDao.loads(users).values()
                .stream()
                .filter(MizarUser::isValid)
                .map(MizarUser::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(validUsers)) {
            return MapMessage.errorMessage("无效的消息接收用户");
        }
        // 先保存消息实体
        MizarNotify upsert = mizarNotifyDao.upsert(notify);
        if (upsert == null || StringUtils.isBlank(upsert.getId())) {
            return MapMessage.errorMessage("生成消息失败");
        }
        // 再保存消息与用户关联
        String notifyId = upsert.getId();
        List<MizarUserNotify> receivers = validUsers.stream()
                .map(user -> new MizarUserNotify(user, notifyId))
                .collect(Collectors.toList());
        try {
            mizarUserNotifyDao.inserts(receivers);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("发送消息发生异常：" + ex.getMessage());
        }
    }

    @Override
    public MapMessage readNotify(String refId) {
        if (StringUtils.isBlank(refId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MizarUserNotify ref = mizarUserNotifyDao.load(refId);
        // 如果该条消息被删除了 或者 已经设置已读，直接返回成功
        if (ref == null || ref.isDisabledTrue() || ref.isRead()) {
            return MapMessage.successMessage();
        }
        boolean read = mizarUserNotifyDao.updateFlag(refId, "flag");
        MapMessage retMsg = new MapMessage();
        retMsg.setSuccess(read);
        retMsg.setInfo(read ? "设置已读成功" : "设置已读失败");
        return retMsg;
    }

    @Override
    public MapMessage removeNotify(String refId) {
        if (StringUtils.isBlank(refId)) {
            return MapMessage.errorMessage("无效的参数");
        }
        MizarUserNotify ref = mizarUserNotifyDao.load(refId);
        // 如果该条消息被删除了,直接返回成功
        if (ref == null || ref.isDisabledTrue()) {
            return MapMessage.successMessage();
        }
        boolean read = mizarUserNotifyDao.updateFlag(refId, "disabled");
        MapMessage retMsg = new MapMessage();
        retMsg.setSuccess(read);
        retMsg.setInfo(read ? "删除消息成功" : "删除消息失败");
        return retMsg;
    }
}