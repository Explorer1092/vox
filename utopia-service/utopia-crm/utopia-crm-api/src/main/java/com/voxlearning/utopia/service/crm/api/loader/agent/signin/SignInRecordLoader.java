package com.voxlearning.utopia.service.crm.api.loader.agent.signin;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SignInRecordLoader
 *
 * @author deliang.che
 * @since 2018/12/17
 */
@ServiceVersion(version = "2018.12.17")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface SignInRecordLoader extends IPingable{

    SignInRecord load(String id);

    Map<String,SignInRecord> loads(Collection<String> ids);
}
