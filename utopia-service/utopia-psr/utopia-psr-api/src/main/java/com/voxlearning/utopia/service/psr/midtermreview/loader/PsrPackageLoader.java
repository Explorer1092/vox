package com.voxlearning.utopia.service.psr.midtermreview.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.psr.midtermreview.client.IPsrPackageLoaderClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/10/9.
 */
@ServiceVersion(version = "20161009")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface PsrPackageLoader extends IPsrPackageLoaderClient {
}
