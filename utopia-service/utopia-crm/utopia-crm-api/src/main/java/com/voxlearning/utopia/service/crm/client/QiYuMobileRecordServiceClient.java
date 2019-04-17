package com.voxlearning.utopia.service.crm.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.QiYuMobileRecordService;
import lombok.Getter;

public class QiYuMobileRecordServiceClient {
    @Getter
    @ImportService(interfaceClass = QiYuMobileRecordService.class)
    private QiYuMobileRecordService qiYuMobileRecordService;
}
