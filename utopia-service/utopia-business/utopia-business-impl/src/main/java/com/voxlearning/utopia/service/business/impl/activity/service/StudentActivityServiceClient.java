package com.voxlearning.utopia.service.business.impl.activity.service;


import com.voxlearning.alps.core.calendar.StopWatch;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.entity.activity.SudokuUserRecord;
import com.voxlearning.utopia.entity.activity.TangramEntryRecord;
import com.voxlearning.utopia.entity.activity.TwoFourPointEntityRecord;
import com.voxlearning.utopia.service.business.impl.activity.dao.SudokuUserRecordDao;
import com.voxlearning.utopia.service.business.impl.activity.dao.TangramEntryRecordDao;
import com.voxlearning.utopia.service.business.impl.activity.dao.TwoFourPointEntityRecordDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Named("com.voxlearning.utopia.service.business.impl.activity.service.StudentActivityServiceClient")
public class StudentActivityServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(StudentActivityServiceClient.class);

    @Inject
    private TangramEntryRecordDao tangramEntryRecordDao;
    @Inject
    private TwoFourPointEntityRecordDao twoFourPointEntityRecordDao;
    @Inject
    private SudokuUserRecordDao sudokuUserRecordDao;

    public Long loadTangramRecordCount(String code) {
        return tangramEntryRecordDao.loadAllCount(code);
    }

    public List<TangramEntryRecord> loadTangramRecord(String code) {
        return tangramEntryRecordDao.loadAll(code);
    }

    public Page<TangramEntryRecord> loadTangramRecordPage(String code, Integer page, Integer pageSize) {
        StopWatch stopWatch = new StopWatch(true);
        Page<TangramEntryRecord> result = tangramEntryRecordDao.loadPage(code, page, pageSize);
        stopWatch.stop();
        logger.info("ActivityReportListener loadTangramRecordPage activityId:{} time:{}", code, stopWatch.getTime(TimeUnit.SECONDS));
        return result;
    }

    public Long loadSudokuRecordCount(String code) {
        return sudokuUserRecordDao.loadAllCountByActivityId(code);
    }

    public List<SudokuUserRecord> loadSudokuRecord(String code) {
        return sudokuUserRecordDao.loadAllByActivityId(code);
    }

    public Page<SudokuUserRecord> loadSudokuRecordPage(String code, Integer page, Integer pageSize) {
        StopWatch stopWatch = new StopWatch(true);
        Page<SudokuUserRecord> result = sudokuUserRecordDao.loadPage(code, page, pageSize);
        stopWatch.stop();
        logger.info("ActivityReportListener loadSudokuRecordPage activityId:{} time:{}", code, stopWatch.getTime(TimeUnit.SECONDS));
        return result;
    }

    public Long loadTwentyFourRecordCount(String code) {
        return twoFourPointEntityRecordDao.loadAllCount(code);
    }

    public List<TwoFourPointEntityRecord> loadTwentyFourRecord(String code) {
        return twoFourPointEntityRecordDao.loadAll(code);
    }

    public Page<TwoFourPointEntityRecord> loadTwentyFourRecordPage(String code, Integer page, Integer pageSize) {
        StopWatch stopWatch = new StopWatch(true);
        Page<TwoFourPointEntityRecord> result = twoFourPointEntityRecordDao.loadPage(code, page, pageSize);
        stopWatch.stop();
        logger.info("ActivityReportListener loadTwentyFourRecordPage activityId:{} time:{}", code, stopWatch.getTime(TimeUnit.SECONDS));
        return result;
    }
}
