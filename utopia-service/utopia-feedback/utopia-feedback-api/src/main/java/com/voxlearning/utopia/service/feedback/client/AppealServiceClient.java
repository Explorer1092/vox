package com.voxlearning.utopia.service.feedback.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.feedback.api.AppealService;
import lombok.Getter;

public class AppealServiceClient {

    @Getter
    @ImportService(interfaceClass = AppealService.class)
    private AppealService appealService;
}
