package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsRank;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer on 2018/3/27
 */
@ServiceVersion(version = "20181115")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiLoader extends IPingable {

    MapMessage loadDailyClass(User user, String unitId);

    MapMessage loadClassDetail(User user, String unitId);

    MapMessage loadQuestions(User user, String lessonId);

    MapMessage loadUnitResult(Long userId, String unitId);

    MapMessage loadLessonResult(User user, String lessonId);

    MapMessage loadUserMapList(Long userId);

    @Deprecated
    MapMessage loadVideo(Long userId);

    @Deprecated
    MapMessage loadHandoutsList(Long userId);

    /**
     * @see ChipsOrderProductLoader#loadOnSaleShortLevelProductInfo
     * @param userId
     * @return
     */
    @Deprecated
    MapMessage loadOrderStatus(Long userId);

    MapMessage loadInvitationInfoByUserId(Long userId);

    /**
     * 查询试用单元内容
     * (试用单元已经是配置了，不能在后端写死)
     */
    @Deprecated
    MapMessage loadTrialCourseUnitInfo();

    /**
     * 查询试用课程的题目等信息
     *
     */
    MapMessage loadTrialCourseUnitLessonInfo(String lessonId);

    /**
     * @see ChipsEnglishContentLoader#loadCourseStudyPlanInfo(Long, String, String)
     * @param userId
     * @param unitId
     * @return
     */
    @Deprecated
    MapMessage loadCourseStudyPlanInfo(Long userId, String unitId);

    /**
     * 查看整本做题结果 wechat用 FIXME BOOKID
     */
    @Deprecated
    MapMessage loadBookResultInfo(Long userId);

    List<ChipEnglishInvitation> loadInvitationByInviterId(Long inviterId);
    /**
     * @see ChipsEnglishContentLoader#loadLessonPlay(Long, String, String)
     */
    @Deprecated
    MapMessage loadLessonPlay(String unitId);

    /**
     * 查看课程列表，wechat用，FIXME BOOKId
     */
    MapMessage loadCourseList();

    @Deprecated
    Long loadMyVirtualClazz(Long userId);

    /**
     * 获取分享的视频排名
     *
     * @param clazz  虚拟班级id， 1或者2
     * @param unitId 单元
     */
    List<ChipsRank> loadShareVideoRanking(String clazz, String unitId);

    List<AIUserUnitResultPlan> loadUnitStudyPlan(String unitId);

    List<AIUserUnitResultPlan> loadUnitStudyPlan(Long userId);

    Map<Long, List<AIUserUnitResultPlan>> loadUnitStudyPlan(Collection<Long> userIds);

    /**
     * 查询今日学习的单元 FIXME BookId
     */
    @Deprecated
    NewBookCatalog loadTodayStudyUnit();

    /**
     * @see ChipsUserVideoLoader#loadById(String)
     */

    @Deprecated
    AIUserVideo loadUserVideoById(String id);

    /**
     * @see ChipsUserVideoLoader#loadByUnitId
     */
    @Deprecated
    List<AIUserVideo> loadUserVideoListByUnitId(String unitId, AIUserVideo.ExamineStatus examineStatus);

    /**
     * @see ChipsUserVideoLoader#loadByUserId(Long)
     */
    @Deprecated
    List<AIUserVideo> loadUserVideoListByUserId(Long userId);

    /**
     * 预览用户的整本书的结果，暂时只支持旅行口语，crm用 FIXME bookId
     *
     * @see ChipsEnglishContentLoader#loadPreviewUserBookResult(Collection, String)
     */
    @Deprecated
    Map<Long, AIUserBookResult> loadPreviewUserBookResult(Collection<Long> userId);

    Collection<Long> loadOfficialProductUser(Integer courseGrade);

    Map<String, Object> loadUserShareRecords(Long userId, String bookId);

    /**
     * 查询所有的有效的unit(排序Trial_Unit)
     */
    List<String> loadAllValidUnitIdByBookIdSortWithRank(String bookId);

    /**
     * 查询所有的有效的并且已经开课的unit(排除Trial_Unit)
     */
    @Deprecated
    List<NewBookCatalog> loadValidBeginUnitByBookIdSortWithRank(OrderProduct orderProduct, String bookId);

    /**
     * 发送 userId 对应的bookId 的定级报告
     */
    MapMessage sendGradingReportTemplateMessage(Long userId, String bookId);

    /**
     * 发送毕业证书模板消息
     */
    MapMessage sendGraduationCertificateTemplateMessage(Long userId);

    /**
     * 发送电子教材 url
     */
    MapMessage sendElectronictTextBookTextMessage(Long userId);

    /**
     * 发送今日学习模板消息
     */
    MapMessage sendDailyLessonTemplateMessage(Long userId, StoneUnitData unit, OrderProduct orderProduct, String bookId);

    /**
     * 查询所有的有效的并且已经开课的unit(排除Trial_Unit)
     */
    @Deprecated
    List<NewBookCatalog> loadAllValidUnitByBookIdSortWithRank(String bookId);

    Date loadUnitBeginTime(OrderProduct product, String bookId, String unitId);

    /**
     * 判断此教材的全部单元是否全部完成（排除试用单元）
     *
     * @param userId 用户id
     * @param bookId 教材id
     */
    boolean ifAllUnitFinished(Long userId, String bookId);

    /**
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=38912463 第11条
     * 发送薯条课程每日排行榜
     * 去除NewBookCatalog对象的依赖
     */
    MapMessage sendChipsCourseDailyRankTemplateMessage(String unitId, ChipsEnglishClass chipsClazz, Collection<Long> userList, int count);

}
