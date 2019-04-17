package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.ChipsActiveServiceType;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceTemplate;
import com.voxlearning.utopia.service.ai.entity.ActiveServiceUserTemplate;
import com.voxlearning.utopia.service.ai.entity.ChipsActiveServiceRecord;
import com.voxlearning.utopia.service.ai.entity.ChipsOtherServiceTemplate;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 主动服务 service api
 */
@ServiceVersion(version = "20190401")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsActiveService extends IPingable {

    /**
     * 筛选本班的主动服务记录
     *
     * @param classId
     * @param status
     * @param unitId
     * @param date
     * @param userId
     * @param pageNum
     * @return
     */
    MapMessage obtainActiveServiceInfos(ChipsActiveServiceType serviceType, Long classId, int status, String unitId, Date date, Long userId, int pageNum, Date updateBeginDate,String level);

    /**
     * 获取本班暂未服务的数量
     *
     * @param clazzId
     * @return
     */
    @Deprecated
    long obtainActiveServiceRemained(ChipsActiveServiceType serviceType, Long clazzId);

    /**
     * 查询班级对应的 bookId
     *
     * @param classId
     * @return
     */
    String loadBookIdByClassId(Long classId);

    /**
     * 更新到已完成状态
     *
     * @param classId
     * @param unitId
     * @param userId
     * @return
     */
    MapMessage updateToReminded(Long classId, String unitId, Long userId);

    /**
     * 一对一视频下载更新服务状态
     * @param classId
     * @param unitId
     * @param userId
     * @return
     */
    MapMessage updateServiced(Long classId, String unitId, Long userId);
    MapMessage saveActiveServiceTemplate(ActiveServiceTemplate template);

    MapMessage saveActiveServiceUserTemplate(ActiveServiceUserTemplate template);

    /**
     * 保存用户模板，并更新vox_chips_english_ai_unit_service_record
     * @param template
     * @param bookId
     * @param unitId
     * @return
     */
    MapMessage saveActiveServiceUserTemplate(ActiveServiceUserTemplate template, String bookId, String unitId);

    ActiveServiceTemplate loadActiveServiceTemplateById(String id);

    Map<String, ActiveServiceTemplate> loadActiveServiceTemplateByIds(Collection<String> ids);

    ActiveServiceUserTemplate buildActiveServiceUserTemplate(Long userId, String qid, Collection<String> aids);
    MapMessage buildActiveServiceUserTemplateMapMessage(Long userId, String qid, Collection<String> aids, String aid);

    /**
     * 是否关注薯条英文微信公众号
     */
    Map<Long, Boolean> registeredInWeChatSubscription(List<Long> userIdList);

    /**
     * 保存其他类型的用户模板结果
     * @param serviceType   BINDING("绑定公众号"),USEINSTRUCTION("薯条英语开课指导"),RENEWREMIND("续费提醒")
     */
    MapMessage saveOtherServiceTypeUserTemplate(String serviceType, long userId, long clazzId, String renewType);

    /**
     * @param serviceType BINDING("绑定公众号"),USEINSTRUCTION("薯条英语开课指导"),RENEWREMIND("续费提醒")   REMIND("未完课提醒"),
     * @return success 时 对应的templateList 为模板数据
     */
    MapMessage loadOtherServiceTypeTemplateList(ChipsActiveServiceType serviceType);

    ChipsOtherServiceTemplate loadOtherServiceTypeTemplate(String id);

    MapMessage saveOtherServiceTypeTemplate(ChipsOtherServiceTemplate template);

    /**
     * 如果有templateId 有值，代表通用模板，否则用户模板
     * @param serviceType 其他模板类型
     * @param userId 用户id
     * @param templateId 通用模板id
     * @return
     */
    MapMessage loadPreviewTemplate(String serviceType, long userId, String templateId, long clazzId,String renewType);

    MapMessage loadPageShareTitle(String serviceType, long userId, long clazzId, String renewType);

    MapMessage loadActiveServicePreviewTemplate(long userId, String qid, String bookId);

    MapMessage deleteChipsActiveServiceRecord(long userId, long clazzId);

    /**
     * 是否是测试或者退款用户
     * @return userIdCol为空时 返回一个Collections.emptyMap()，其他情况map的size与userIdCol 一致
     */
    Map<Long, Boolean> isTestOrRefundUser(Collection<Long> userIdCol, String productId);

    /**
     * 是否添加微信
     * @return userIdCol为空时 返回一个Collections.emptyMap()，其他情况map的size与userIdCol 一致
     */
    Map<Long, Boolean> isRegisterWxUser(Collection<Long> userIdCol);

    /**
     * 是否进行完课点评
     * @param userIdCol
     * @return userIdCol为空时 返回一个Collections.emptyMap()，其他情况map的size与userIdCol 一致
     */
    Map<Long, Integer> isActiveServiced(Collection<Long> userIdCol, Long clazzId);

    MapMessage handleActiveServiceData(Long clazzId, List<Long> userIdList, String unitId);

    MapMessage modifyRemindData(Long clazzId, List<Long> userIdList, String unitId);

    MapMessage handleActiveServiceStatus(Long clazzId, List<Long> userIdList, String bookId, String unitId);

    Map<String,Integer> obtainAllActiveServiceRemained(Long clazzId);

    /**
     * 如果有templateId 有值，代表通用模板，否则用户模板
     * @param userId 用户id
     * @return
     */
    MapMessage loadOtherServiceRenewUserData(long userId, long clazzId);

    MapMessage loadGradeReport(Long userId, String bookId);

    List<ChipsActiveServiceRecord> loadChipsActiveServiceRecord(ChipsActiveServiceType serviceType, Date begin);

}
