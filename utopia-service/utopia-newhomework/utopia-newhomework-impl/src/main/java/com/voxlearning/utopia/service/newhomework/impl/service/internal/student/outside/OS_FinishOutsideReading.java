package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.OutsideReadingDynamicCacheMapper;
import com.voxlearning.utopia.service.newhomework.consumer.cache.OutsideReadingDynamicCacheManager;
import com.voxlearning.utopia.service.newhomework.impl.dao.outside.OutsideReadingAchievementDao;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ReadingOTOBook;
import com.voxlearning.utopia.service.question.api.entity.stone.data.ReadingOTOMission;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author majianxin
 */
@Named
public class OS_FinishOutsideReading extends SpringContainerSupport implements OutsideReadingResultTask {

    @Inject private OutsideReadingAchievementDao outsideReadingAchievementDao;
    @Inject private StoneDataLoaderClient stoneDataLoaderClient;
    @Inject private NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(OutsideReadingContext context) {
        AlpsThreadPool.getInstance().submit(() -> finishOutsideReading(context));
    }

    //字数奖励&发送动态
    private void finishOutsideReading(OutsideReadingContext context) {
        if (!context.isMissionFinished()) {
            return;
        }

        // 第一次完成添加一条动态
        addOutsideReadingDynamicCache(context);

        // 第一次获得三星奖励字数
        if (context.isAddReadingCount()) {
            String missionId = context.getMissionId();
            LinkedHashMap<String, ReadingOTOMission> missionMap = stoneDataLoaderClient.loadReadingOTOMissionByIds(Collections.singleton(missionId));
            ReadingOTOMission readingOTOMission = missionMap.get(missionId);
            if (readingOTOMission != null && readingOTOMission.getTotalNum() != null) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(context.getUserId());
                outsideReadingAchievementDao.addReadingCount(context.getUserId(), studentDetail.getClazzLevelAsInteger(), readingOTOMission.getTotalNum());
            }
        }
    }

    private void addOutsideReadingDynamicCache(OutsideReadingContext context) {
        OutsideReadingDynamicCacheManager cacheManager = newHomeworkCacheService.getOutsideReadingDynamicCacheManager();
        OutsideReadingDynamicCacheMapper mapper = new OutsideReadingDynamicCacheMapper();
        mapper.setReadingId(context.getReadingId());
        mapper.setStudentId(context.getUserId());
        User user = context.getUser();
        mapper.setStudentName(user.fetchRealnameIfBlankId());
        mapper.setStudentImage(user.fetchImageUrl());

        mapper.setBookId(context.getBookId());
        ReadingOTOBook readingOTOBook = stoneDataLoaderClient.loadReadingOTOBookByIds(Collections.singleton(context.getBookId())).get(context.getBookId());
        ReadingOTOMission readingOTOMission = stoneDataLoaderClient.loadReadingOTOMissionByIds(Collections.singleton(context.getMissionId())).get(context.getMissionId());
        if (readingOTOBook == null || readingOTOMission == null) {
            return;
        }
        mapper.setBookName(readingOTOBook.getBookName());
        mapper.setCoverPic(readingOTOBook.getCoverPic());
        mapper.setAuthor(readingOTOBook.getAuthor());
        mapper.setMissionId(context.getMissionId());
        mapper.setMissionName(readingOTOMission.getMissionName());
        if (context.isAddReadingCount()) {
            mapper.setAddReadingCount(readingOTOMission.getTotalNum());
        }
        mapper.setFinishAt(new Date());
        cacheManager.addDynamic(context.getReadingId(), mapper);
    }
}
