package com.voxlearning.utopia.service.crm.consumer.loader.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmProductFeedbackRecordLoader;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */
public class CrmProductFeedbackRecordLoaderClient implements CrmProductFeedbackRecordLoader{

    @ImportService(interfaceClass = CrmProductFeedbackRecordLoader.class)
    private CrmProductFeedbackRecordLoader remoteReference;
}
