package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuan.zhu
 * @date 2018/8/23 19:37
 * 薯条英语用户 service 接口
 */
@ServiceVersion(version = "20190315")
@ServiceTimeout(timeout = 120, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsEnglishUserLoader extends IPingable {

    /**
     * 根据产品id 和 班级id 查找用户简短信息
     */
    MapMessage loadSimpleUserByClassId(String productId, Long classId, String excludeUserId, int minCost, int maxCost, int pageNum);

    /**
     * 查询用户详细信息
     */
    AIUserInfoDetail loadUserDetailByUserId(Long userId, String operator);

    /**
     * 查询某个班级内的某批用户的成绩信息
     */
    Map<Long, List<AIUserInfoWithScore>> loadClassSingleUserInfoWithScore(Long classId, List<Long> userIds);

    /**
     * 查询某个用户的某个薯条英语产品成绩
     *
     * @param userId    用户id
     * @param productId 产品id
     * @return 成绩信息
     */
    List<AIUserInfoWithScore> loadUserAllClassScore(Long userId, String productId);

    /**
     * 查询某个用户的运营信息
     */
    List<AiUserOperationInfo> loadUserOperationInfoList(Long userId);

    /**
     * 薯条英语用户数据同步任务
     */
    void transferUserExtToUserExtSplit();

    MapMessage loadQuestionResult4Crm(Long userId, String lessonId);

    MapMessage loadQuestionResultByUnit4Crm(Long userId,String bookId, String unitId);

    MapMessage loadUserChipsBookIds(Long parentId);

    MapMessage loadUserChipsLessonIds(Long parentId, String bookId, ChipsUnitType unitType, String unitId);

    List<ChipsUserCourseMapper> loadUserEffectiveCourse(Long userId);

    /**
     * 用户单元成绩编辑
     */
    void editUnitResultOperationLog(Long userId, String unitId, String operationLog);

    /**
     * 计算用户的成绩简单信息， crm 用
     */
    Map<Long, List<ScoreSimpleInfo>> loadUserResultSimpleInfo(String productId, List<Long> userIdList);


    MapMessage loadLessonConfigExpend();

    /**
     * 用户主动服务页面展示的用户回答项
     *
     * @param qid questionId
     * @param aid answerId
     * @return
     */
    AIActiveServiceUserTemplateItem buildUserAnswer(String unitId, String lessonId, String qid, String aid, String name);

    /**
     * 加载用户邮寄信息
     *
     * @param userId
     * @return
     */
    ChipsUserMailInfo loadUserMailInfo(long userId);

    /**
     * 用户邮寄信息保存
     *
     * @param userId
     * @param editType
     * @param value
     * @return
     */
    boolean updateUserMailInfo(long userId, String editType, String value);

    /**
     * 保存邮寄地址问卷
     * @param userId
     * @param recipientName
     * @param recipientTel
     * @param recipientAddr
     * @param courseLevel
     * @return
     */
    MapMessage updateMailAddrAndCourseLevel(long userId, String recipientName,String recipientTel,String recipientAddr,String courseLevel);

    /**
     * 更新微信号
     *
     * @param userId
     * @param wxNum
     * @return
     */
    boolean updateUserWxNum(long userId, String wxNum);

    /**
     * 更新微信昵称
     *
     * @param userId
     * @param wxName
     * @return
     */
    boolean updateUserWxName(long userId, String wxName);

    Collection<ChipsEnglishClass> loadMyClazz(Long userId);
}
