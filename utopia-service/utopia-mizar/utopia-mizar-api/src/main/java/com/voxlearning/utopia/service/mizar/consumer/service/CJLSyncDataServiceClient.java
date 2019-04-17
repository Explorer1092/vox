package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.entity.cjlschool.CJLTeacher;
import com.voxlearning.utopia.service.mizar.api.service.cjlschool.CJLSyncDataService;
import lombok.Getter;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
public class CJLSyncDataServiceClient {

    @Getter
    @ImportService(interfaceClass = CJLSyncDataService.class)
    private CJLSyncDataService syncDataService;

}
