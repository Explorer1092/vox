package com.voxlearning.utopia.service.crm.impl.service.agent.signin;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.signin.SignInRecordService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.signin.SignInRecordDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * SignInRecordServiceImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = SignInRecordService.class)
@ExposeService(interfaceClass = SignInRecordService.class)
public class SignInRecordServiceImpl extends SpringContainerSupport implements SignInRecordService {
    @Inject
    SignInRecordDao signInRecordDao;

    @Override
    public String insert(SignInRecord signInRecord){
        signInRecordDao.insert(signInRecord);
        return signInRecord.getId();
    }

    @Override
    public void upsert(SignInRecord signInRecord){
        signInRecordDao.upsert(signInRecord);
    }
}
