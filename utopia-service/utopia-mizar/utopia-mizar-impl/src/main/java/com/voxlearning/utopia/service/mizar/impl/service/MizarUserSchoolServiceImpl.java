package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.api.service.MizarUserSchoolService;
import com.voxlearning.utopia.service.mizar.impl.dao.user.MizarUserSchoolDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-06-22 16:22
 **/
@Named
@Service(interfaceClass = MizarUserSchoolService.class)
@ExposeService(interfaceClass = MizarUserSchoolService.class)
public class MizarUserSchoolServiceImpl implements MizarUserSchoolService {

    @Inject private MizarUserSchoolDao mizarUserSchoolDao;

    @Override
    public MapMessage addUserSchools(Collection<MizarUserSchool> userSchools) {
        MapMessage mapMessage = new MapMessage();
        List<Long> bindedSchoolIds = new ArrayList<>();

        Integer currentYearMonth = Integer.valueOf(DateUtils.nowToString("yyyyMM"));
        userSchools.forEach(p -> {
            MizarUserSchool mizarUserSchool = mizarUserSchoolDao.loadBySchoolId(p.getSchoolId());
            if (null == mizarUserSchool) {
                p.setDisabled(false);
                mizarUserSchoolDao.insert(p);
            } else if (mizarUserSchool.getUserId().equals(p.getUserId()) && mizarUserSchool.getContractEndMonth() >= currentYearMonth) {
                p.setId(mizarUserSchool.getId());
                mizarUserSchoolDao.upsert(p);
            } else {
                bindedSchoolIds.add(p.getSchoolId());
            }
        });
        if (bindedSchoolIds.size() > 0) {
            mapMessage.set("bindedSchoolIds", bindedSchoolIds);
        }
        return mapMessage;
    }

    @Override
    public MapMessage deleteUserSchool(String userId, Long schoolId) {
        if (userId == null && schoolId == null) {
            return MapMessage.errorMessage("参数异常");
        }
        mizarUserSchoolDao.disableUserSchool(userId, schoolId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteUserSchool(String userId) {
        return deleteUserSchool(userId, null);
    }
}
