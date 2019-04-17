package com.voxlearning.utopia.service.newexam.api.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newexam.api.NewExamStorageService;
import lombok.Getter;

public class NewExamStorageServiceClient {

    @Getter
    @ImportService(interfaceClass = NewExamStorageService.class)
    private NewExamStorageService newExamStorageService;

}
