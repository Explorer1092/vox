package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsUserOralScheduleService;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOralTestSchedule;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishClassUserRefPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserOralTestScheduleDao;
import com.voxlearning.utopia.service.ai.util.OrderProductUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2019/3/12
 */
@Named
@ExposeService(interfaceClass = ChipsUserOralScheduleService.class)
public class ChipsUserOralScheduleServiceImpl implements ChipsUserOralScheduleService {
    @Inject
    private ChipsUserOralTestScheduleDao chipsUserOralTestScheduleDao;
    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;
    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    private ChipsEnglishClass getClazz(Long userId) {
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        Set<Long> clazzSet = userRefList.stream().map(ChipsEnglishClassUserRef::getChipsClassId).collect(Collectors.toSet());
        Map<Long, ChipsEnglishClass> clazzMap = chipsEnglishClassPersistence.loads(clazzSet);
        for (ChipsEnglishClass clazz : clazzMap.values()) {
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(clazz.getProductId());
            if (OrderProductUtil.isShortProduct(product)) {
                return clazz;
            }
        }
        return null;

    }

    public MapMessage updateChipsUserOralTestSchedule(Long userId, Date testBeginTime, Date testEndTime) {
        if (userId == null || testBeginTime == null || testBeginTime == null) {
            return MapMessage.errorMessage("参数有误");
        }

        ChipsEnglishClass clazz = getClazz(userId);
        if (clazz == null) {
            return MapMessage.errorMessage("您未在我们所服务的班级中,请更换登录账号");
//            return MapMessage.successMessage();
        }
        ChipsUserOralTestSchedule schedule = new ChipsUserOralTestSchedule();
        schedule.setId(ChipsUserOralTestSchedule.genId(userId, clazz.getProductId()));
        schedule.setTestBeginTime(testBeginTime);
        schedule.setTestEndTime(testEndTime);
        schedule.setClazzId(clazz.getId());
        schedule.setUserId(userId);
        List<ChipsUserOralTestSchedule> scheduleList = chipsUserOralTestScheduleDao.loadByClazzId(clazz.getId());
        long count = scheduleList.stream().filter(e -> e.getTestBeginTime().equals(testBeginTime)).count();
        if (count >= 4) {
            return MapMessage.errorMessage("该时段已经被约满，请重新选择");
        }
        chipsUserOralTestScheduleDao.upsert(schedule);
        return MapMessage.successMessage();
    }

    @Override
    public List<ChipsUserOralTestSchedule> loadByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0l) {
            return Collections.emptyList();
        }
        return chipsUserOralTestScheduleDao.loadByClazzId(clazzId);
    }
}


