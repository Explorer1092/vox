package com.voxlearning.utopia.service.psr.newhomework.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.psr.newhomework.client.ISimilarQuestionLoaderClient;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20161012")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface SimilarQuestionLoader extends ISimilarQuestionLoaderClient {
}
