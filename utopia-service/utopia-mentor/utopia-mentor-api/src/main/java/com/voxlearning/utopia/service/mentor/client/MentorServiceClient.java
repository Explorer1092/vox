package com.voxlearning.utopia.service.mentor.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mentor.api.MentorService;
import lombok.Getter;

public class MentorServiceClient {

    @Getter
    @ImportService(interfaceClass = MentorService.class) private MentorService remoteReference;
}
