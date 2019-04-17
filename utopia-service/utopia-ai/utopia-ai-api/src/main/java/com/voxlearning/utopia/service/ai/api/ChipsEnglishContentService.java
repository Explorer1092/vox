package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionBO;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 薯条英语V2版接口
 */
@ServiceVersion(version = "20190214")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishContentService extends IPingable {

    /**
     * 处理用户做题结果
     */
    MapMessage processQuestionResult(Long userId, ChipsQuestionResultRequest chipsQuestionResultRequest);


    /**
     * 处理用户视频
     */
    MapMessage processUserVideo(Long userId, String qid, String lessonId, String unitId, String bookId, List<String> videos);


    /**
     * 收集用户做题
     */
    MapMessage collectData(Long uid, ChipsQuestionType type, ChipsQuestionBO question, String input, String userVideo);

    /**
     * 处理问答题等级
     */
    MapMessage processInterlocutionLevel(Long uid, String bookId, String lessonId, String unitId, String qId, String input);


    /**
     * 加载并记录情景对话
     */
    MapMessage processDialogueTalk(User user, String usercode, String input, String lessonId, String unitId, String bookId);

    /**
     * 加载并记录任务对话
     */
    MapMessage processTaskTalk(User user, String usercode, String input, String roleName, String lessonId, String unitId, String bookId);

    /**
     * 处理模考视频等级
     * @param userId
     * @param question
     * @param input
     * @param userVideo
     * @return
     */
    MapMessage processMockQAQuestionLevel(Long userId, ChipsQuestionBO question, String input, String userVideo);

    // 记录用户今日总结打卡次数
    MapMessage recordUserShare(String bookId, String unitId, Long userId);

    MapMessage upsertChipsEnglishProductTimetable(ChipsEnglishProductTimetable chipsEnglishProductTimetable);

    MapMessage updateUserWxInfo(Long userId, String wxCode, String wxName);

    boolean existUserWxInfo(Long userId,String productId);

    boolean existUserRecipientInfo(Long userId,String productId);

    MapMessage updateUserRecipientInfo(Long userId, String recipientName, String recipientTel, String recipientAddr);

    MapMessage processDialogueTalkV2(Long user, String input, String qid, String userVideo, ChipsLessonRequest request);

    MapMessage processTaskTalkV2(Long user, String usercode, String input, String roleName,
                                 String qid, String lessonId, String unitId, String bookId);


}
