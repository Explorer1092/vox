package com.voxlearning.utopia.service.afenti.impl.listener.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxlearning.utopia.service.afenti.api.annotations.AfentiQueueMessageTypeIdentification;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;
import com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewFamilyClassRankingCacheManager;
import com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewFamilySchoolRankingCacheManager;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;


@Named
@Lazy(false)
@AfentiQueueMessageTypeIdentification(AfentiQueueMessageType.FAMILY_JOIN)
public class FamilyJoinHandler extends AbstractAfentiQueueMessageHandler {

    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private AfentiReviewFamilyClassRankingCacheManager afentiReviewFamilyClassRankingCacheManager;

    @Inject
    private AfentiReviewFamilySchoolRankingCacheManager afentiReviewFamilySchoolRankingCacheManager;

    @Override
    public void handle(ObjectMapper mapper, JsonNode root) throws Exception {
        Long studentId = root.get("S").asLong();
        int number = root.get("N").asInt();

        asyncAfentiCacheService.getAfentiReviewFamilyJoinCacheManager().addRecordOrIncreaseNumber(studentId, number);

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        afentiReviewFamilyClassRankingCacheManager.addRecord(studentDetail.getClazzId(), studentId);

        Long schoolId = studentDetail.getClazz().getSchoolId();
        afentiReviewFamilySchoolRankingCacheManager.addRecord(schoolId, studentId);

    }
}
