package com.voxlearning.utopia.service.psr.termreport.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.psr.termreport.client.IPsrTermReportIPackageLoaderClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by mingming.zhao on 2016/10/20.
 */
@ServiceVersion(version = "20161020")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface PsrTermReportIPackageLoader extends IPsrTermReportIPackageLoaderClient {
}
