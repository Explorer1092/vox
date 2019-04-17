package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.service.HbsService;
import com.voxlearning.utopia.service.mizar.impl.dao.hbs.HbsContestantDao;
import com.voxlearning.utopia.service.mizar.impl.dao.hbs.HbsUserDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by ganhaitian on 2017/2/15.
 */
@Named
@Service(interfaceClass = HbsService.class)
@ExposeService(interfaceClass = HbsService.class)
public class HbsServiceImpl implements HbsService{

    @Inject private HbsUserDao hbsUserDao;
    @Inject private HbsContestantDao hbsContestantDao;

    @Override
    public MapMessage updateUserPhoneNumber(Long userId, String phoneNumber) {
        int rows = hbsUserDao.updateUserName(userId,phoneNumber);
        // rows += hbsContestantDao.updatePhoneNumber(userId,phoneNumber);
        if (rows > 0) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("没有数据被更新");
        }
    }

    @Override
    public MapMessage updateUserPwd(Long userId, String clearText) {
        String cipherText = DigestUtils.md5Hex(clearText);
        int rows = hbsUserDao.updateUserPassword(userId,cipherText);
        if (rows > 0) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("没有数据被更新");
        }
    }
}
