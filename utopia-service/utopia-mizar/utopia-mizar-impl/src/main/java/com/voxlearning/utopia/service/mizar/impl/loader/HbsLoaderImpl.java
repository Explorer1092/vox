package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsContestant;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;
import com.voxlearning.utopia.service.mizar.api.loader.HbsLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.hbs.HbsContestantDao;
import com.voxlearning.utopia.service.mizar.impl.dao.hbs.HbsUserDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by haitian.gan on 2017/2/15.
 */
@Named
@Service(interfaceClass = HbsLoader.class)
@ExposeServices({
        @ExposeService(interfaceClass = HbsLoader.class,version = @ServiceVersion(version = "20170321")),
        @ExposeService(interfaceClass = HbsLoader.class,version = @ServiceVersion(version = "20170301"))
})
public class HbsLoaderImpl implements HbsLoader{

    @Inject private HbsUserDao hbsUserDao;
    @Inject private HbsContestantDao hbsContestantDao;

    @Override
    public HbsUser loadUser(Long userId) {
        HbsUser user = hbsUserDao.loadUser(userId);
        // 关联参数选手信息
        HbsContestant contestant = hbsContestantDao.loadByStudentId(user.getId());
        user.setContestant(contestant);

        return user;
    }

    @Override
    public HbsUser loadUserByName(String userName) {
        HbsUser user = hbsUserDao.loadUserByName(userName);
        if(user != null){
            // 关联参数选手信息
            HbsContestant contestant = hbsContestantDao.loadByStudentId(user.getId());
            user.setContestant(contestant);
        }

        return user;
    }

    @Override
    public HbsContestant getContestant(Long studentId) {
        return hbsContestantDao.getByStudentId(studentId);
    }

    @Override
    public HbsContestant loadContestant(Long studentId) {
        HbsContestant contestant = hbsContestantDao.loadByStudentId(studentId);
        fillUserData(contestant);

        return contestant;
    }

    @Override
    public HbsContestant findStudentByIdCardNo(String idCardNo) {
        HbsContestant contestant = hbsContestantDao.loadByIdCardNo(idCardNo);
        fillUserData(contestant);

        return contestant;
    }

    @Override
    public HbsContestant findStudentByPhoneNumber(String phoneNumber) {
        HbsContestant contestant = hbsContestantDao.loadByPhoneNumber(phoneNumber);
        fillUserData(contestant);

        return contestant;
    }

    private void fillUserData(HbsContestant contestant){
        if(contestant != null){
            HbsUser user = hbsUserDao.loadUser(contestant.getUserId());
            contestant.setUser(user);
        }
    }


}
