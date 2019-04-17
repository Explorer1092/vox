package com.voxlearning.utopia.service.crm.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.QiYuSessionRecordService;
import lombok.Getter;

public class QiYuSessionRecordServiceClient {
    @Getter
    @ImportService(interfaceClass = QiYuSessionRecordService.class)
    private QiYuSessionRecordService qiYuSessionRecordService;
}
