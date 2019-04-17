package com.voxlearning.utopia.service.crm.impl.loader.crm;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.crm.api.loader.crm.CrmProductFeedbackRecordLoader;

import javax.inject.Named;

/**
 * Created by yaguang.wang
 * on 2017/3/3.
 */

@Named
@Service(interfaceClass = CrmProductFeedbackRecordLoader.class)
@ExposeService(interfaceClass = CrmProductFeedbackRecordLoader.class)
public class CrmProductFeedbackRecordLoaderImpl implements CrmProductFeedbackRecordLoader {
}
