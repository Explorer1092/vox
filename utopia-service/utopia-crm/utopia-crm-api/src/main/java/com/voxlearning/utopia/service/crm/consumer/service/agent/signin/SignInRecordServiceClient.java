package com.voxlearning.utopia.service.crm.consumer.service.agent.signin;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.signin.SignInRecordService;

/**
 * SignInRecordServiceClient
 *
 * @author deliang.che
 * @since  2018/12/17
 */
public class SignInRecordServiceClient implements SignInRecordService {

    @ImportService(interfaceClass = SignInRecordService.class)
    private SignInRecordService remoteReference;


    @Override
    public String insert(SignInRecord signInRecord) {
        return remoteReference.insert(signInRecord);
    }

    @Override
    public void upsert(SignInRecord signInRecord){
        remoteReference.upsert(signInRecord);
    }

}
