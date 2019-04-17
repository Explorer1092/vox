package com.voxlearning.utopia.service.crm.impl.loader.agent.signin;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.signin.SignInRecordLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.signin.SignInRecordDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

/**
 * SignInRecordLoaderImpl
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@Service(interfaceClass = SignInRecordLoader.class)
@ExposeService(interfaceClass = SignInRecordLoader.class)
public class SignInRecordLoaderImpl extends SpringContainerSupport implements SignInRecordLoader {

    @Inject
    SignInRecordDao signInRecordDao;

    @Override
    public SignInRecord load(String id){
        return signInRecordDao.load(id);
    }

    @Override
    public Map<String,SignInRecord> loads(Collection<String> ids){
        return signInRecordDao.loads(ids);
    }

}
