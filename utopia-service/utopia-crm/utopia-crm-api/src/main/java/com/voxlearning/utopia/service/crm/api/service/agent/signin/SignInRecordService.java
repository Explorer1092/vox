package com.voxlearning.utopia.service.crm.api.service.agent.signin;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;

import java.util.concurrent.TimeUnit;

/**
 * SignInRecordService
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface SignInRecordService extends IPingable {

    String insert(SignInRecord signInRecord);

    void upsert(SignInRecord signInRecord);
}
