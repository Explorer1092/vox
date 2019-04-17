package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.AiTodayLessonService;
import lombok.Getter;

public class AiTodayLessonClient {

    @Getter
    @ImportService(interfaceClass = AiTodayLessonService.class)
    private AiTodayLessonService remoteReference;
}
