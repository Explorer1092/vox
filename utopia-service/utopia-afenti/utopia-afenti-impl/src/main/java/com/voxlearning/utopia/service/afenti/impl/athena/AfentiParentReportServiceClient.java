package com.voxlearning.utopia.service.afenti.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.afenti.AfentiParentReportService;
import lombok.Getter;

import javax.inject.Named;

/**
 * Created by Summer on 2017/7/4.
 */
@Named("com.voxlearning.utopia.service.afenti.impl.athena.AfentiParentReportServiceClient")
public class AfentiParentReportServiceClient {

    @Getter
    @ImportService(interfaceClass = AfentiParentReportService.class)
    private AfentiParentReportService afentiParentReportService;
}
