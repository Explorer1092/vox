package com.voxlearning.utopia.service.crm.consumer.loader.agent.signin;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.loader.agent.signin.SignInRecordLoader;

import java.util.Collection;
import java.util.Map;

/**
 * SignInRecordLoaderClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class SignInRecordLoaderClient implements SignInRecordLoader {

    @ImportService(interfaceClass = SignInRecordLoader.class)
    private SignInRecordLoader remoteReference;

    @Override
    public SignInRecord load(String id) {
        return remoteReference.load(id);
    }

    @Override
    public Map<String,SignInRecord> loads(Collection<String> ids){
        return remoteReference.loads(ids);
    }
}
