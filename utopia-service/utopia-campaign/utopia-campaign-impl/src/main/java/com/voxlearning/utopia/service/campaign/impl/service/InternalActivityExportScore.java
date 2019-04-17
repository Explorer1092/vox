package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.campaign.impl.service.excel.*;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class InternalActivityExportScore extends SpringContainerSupport {

    private static final Logger log = LoggerFactory.getLogger(InternalActivityExportScore.class);

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private StudentActivityServiceImpl studentActivityService;

    private SudokuActivityExportScore sudokuActivityExportScore;
    private TwentyFourActivityExportScore twentyFourActivityExportScore;
    private TwentyFourAllActivityExportScore twentyFourAllActivityExportScore;
    private TangramActivityExportScore tangramActivityExportScore;
    private TangramAllActivityExportScore tangramAllActivityExportScore;

    @Override
    public void afterPropertiesSet() {
        sudokuActivityExportScore = new SudokuActivityExportScore(
                activityConfigServiceClient,
                emailServiceClient,
                studentLoaderClient,
                studentActivityService,
                raikouSystem
        );
        twentyFourActivityExportScore = new TwentyFourActivityExportScore(
                activityConfigServiceClient,
                emailServiceClient,
                studentLoaderClient,
                studentActivityService,
                raikouSystem
        );
        twentyFourAllActivityExportScore = new TwentyFourAllActivityExportScore(
                activityConfigServiceClient,
                emailServiceClient,
                studentLoaderClient,
                studentActivityService,
                raikouSystem
        );
        tangramActivityExportScore = new TangramActivityExportScore(
                activityConfigServiceClient,
                emailServiceClient,
                studentLoaderClient,
                studentActivityService,
                raikouSystem
        );
        tangramAllActivityExportScore = new TangramAllActivityExportScore(
                activityConfigServiceClient,
                emailServiceClient,
                studentLoaderClient,
                studentActivityService,
                raikouSystem
        );
    }

    void exportSudokuScore(String activityId, String email) {
        AlpsThreadPool.getInstance().submit(() -> sudokuActivityExportScore.exportExcel(activityId, email));
    }

    public void exportTwentyFourScore(String activityId, String email) {
        AlpsThreadPool.getInstance().submit(() -> {
            twentyFourActivityExportScore.exportExcel(activityId, email);
            twentyFourAllActivityExportScore.exportExcel(activityId, email);
        });
    }

    public void exportTangramScore(String activityId, String email) {
        AlpsThreadPool.getInstance().submit(() -> {
            tangramActivityExportScore.exportExcel(activityId, email);
            tangramAllActivityExportScore.exportExcel(activityId, email);
        });
    }
}
