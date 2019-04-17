/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.business.api.BusinessManagement;
import com.voxlearning.utopia.service.business.api.entity.RSOralReportStat;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.api.entity.UserBalanceLog;
import com.voxlearning.utopia.service.business.api.mapper.ChipsRedPack;
import com.voxlearning.utopia.service.business.api.mapper.RedPackMapper;
import com.voxlearning.utopia.service.business.impl.dao.RSOralReportStatDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.business.impl.dao.UserBalanceLogPersistence;
import com.voxlearning.utopia.service.business.impl.support.Certification;
import com.voxlearning.utopia.service.business.impl.support.EnglishResearchStaffBehaviorDataGenerator;
import com.voxlearning.utopia.service.business.impl.support.MathResearchStaffBehaviorDataGenerator;
import com.voxlearning.utopia.service.business.impl.utils.HongBaoUtils;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.RedPackCategory;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatRedPackHistory;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Spring
@Named
@Service(interfaceClass = BusinessManagement.class)
@ExposeService(interfaceClass = BusinessManagement.class)
public class BusinessManagementImpl extends SpringContainerSupport implements BusinessManagement {

    @Inject private CertificationManagementImpl certificationManagement;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private UserBalanceLogPersistence userBalanceLogPersistence;
    @Inject private RSOralReportStatDao rsOralReportStatDao;
    @Inject private EnglishResearchStaffBehaviorDataGenerator englishResearchStaffBehaviorDataGenerator;
    @Inject private MathResearchStaffBehaviorDataGenerator mathResearchStaffBehaviorDataGenerator;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @AlpsQueueProducer(queue = "utopia.ai.invitation.send.redpack.queue")
    private MessageProducer sendChipsRedPackQueue;

    @Override
    public int sendRedPacks(List<RedPackMapper> dataList) {
        int errorCount = 0;
        for (RedPackMapper mapper : dataList) {
            //进行红包发送
            SortedMap<String, String> sortedMap = HongBaoUtils.createMap(
                    HongBaoUtils.createBillNo(mapper.getUserId().toString()),
                    mapper.getOpenId(),
                    mapper.getAmount(),
                    RedPackCategory.get(mapper.getRedPackType()).getWishingText());
            //设置签名
            HongBaoUtils.sign(sortedMap);
            boolean res = doSendRedPack(sortedMap, certificationManagement.getAmbassadorCertificationRawData(), mapper.getUserId(), mapper.getOpenId(), mapper.getAmount());
            if (!res) {
                errorCount ++;
            }
        }
        return errorCount;
    }

    @Override
    public void sendChipsRedPacks(Collection<ChipsRedPack> userRedPacks) {
        if (CollectionUtils.isNotEmpty(userRedPacks)) {
            return;
        }
        Map<Long, List<UserWechatRef>> userWechatMap = wechatLoaderClient.loadUserWechatRefs(userRedPacks.stream().map(ChipsRedPack::getUserId).collect(Collectors.toList()), WechatType.CHIPS);

        for(ChipsRedPack redPack : userRedPacks) {
            if (MapUtils.isEmpty(userWechatMap) || CollectionUtils.isEmpty(userWechatMap.get(redPack.getUserId()))) {
                logger.warn("user has no wechat info. userId:{}", redPack.getUserId());
                continue;
            }
            AppPayMapper appPayMapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.ChipsEnglish.name(), redPack.getUserId(), true);
            if (!(appPayMapper.isActive() && appPayMapper.containsProductId(redPack.getProductId()))) {
                continue;
            }
            UserWechatRef userWechatRef = userWechatMap.get(redPack.getUserId()).stream().filter(e -> StringUtils.isNotBlank(e.getOpenId()))
                    .filter(e -> !Boolean.TRUE.equals(e.getDisabled()))
                    .sorted((e1, e2) -> {
                        Long o1 = e1.getUpdateDatetime() != null ? e1.getUpdateDatetime().getTime() : 0L;
                        Long o2 = e2.getUpdateDatetime() != null ? e2.getUpdateDatetime().getTime() : 0L;
                        return o2.compareTo(o1);
                    })
                    .findFirst().orElse(null);
            if (userWechatRef == null) {
                logger.warn("user has no wechat info. userId:{}", redPack.getUserId());
                continue;
            }
            SortedMap<String, String> sortedMap = HongBaoUtils.createChipsMap(
                        HongBaoUtils.createChipsBillNo(redPack.getUserId().toString()),
                        userWechatRef.getOpenId(),
                        redPack.getAmount(),
                        redPack.getProductName() + "推荐奖励");
            HongBaoUtils.chipsSign(sortedMap);
            boolean res = doSendRedPack(sortedMap, Certification.getChipsEnglishCertificationContent(), redPack.getUserId(), userWechatRef.getOpenId(), redPack.getAmount());
            if (res) {
                Map<String, Object> message = new HashMap<>();
                message.put("UID", redPack.getUserId());
                message.put("PID", redPack.getProductId());
                sendChipsRedPackQueue.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            }
        }
    }

    private boolean doSendRedPack(SortedMap<String, String> sortedMap, byte[] ambassadorCertificationRawData, Long userId, String openId, int amount) {
        boolean res = false;
        try {
            InputStream inStream = new ByteArrayInputStream(ambassadorCertificationRawData);
            String responseResult = HongBaoUtils.post(HongBaoUtils.getRequestXml(sortedMap), inStream);
            Document document = XmlUtils.parseDocument(new ByteArrayInputStream(responseResult.getBytes()));
            String return_code = XmlUtils.getChildElementText(document.getDocumentElement(), "return_code");
            String return_msg = XmlUtils.getChildElementText(document.getDocumentElement(), "return_msg");
            //记录历史
            WechatRedPackHistory packHistory = new WechatRedPackHistory();
            packHistory.setPackCategory(RedPackCategory.AMBASSADOR_BACK_FLOW.name());
            packHistory.setUserId(userId);
            packHistory.setOpenId(openId);
            packHistory.setAmount(amount);
            if (HongBaoUtils.SUCCESS.equals(return_code)) {
                String result_code = XmlUtils.getChildElementText(document.getDocumentElement(), "result_code");
                String err_code = XmlUtils.getChildElementText(document.getDocumentElement(), "err_code");
                String err_code_des = XmlUtils.getChildElementText(document.getDocumentElement(), "err_code_des");
                if (HongBaoUtils.SUCCESS.equals(result_code)) {
                    //记录下发送结果， 存到表里吧 涉及到钱的
                    String mch_id = XmlUtils.getChildElementText(document.getDocumentElement(), "mch_id");
                    String wxappid = XmlUtils.getChildElementText(document.getDocumentElement(), "wxappid");
                    String send_listid = XmlUtils.getChildElementText(document.getDocumentElement(), "send_listid");
                    packHistory.setSuccess(true);
                    packHistory.setMchId(mch_id);
                    packHistory.setAppId(wxappid);
                    packHistory.setSendListId(send_listid);
                    wechatServiceClient.saveWechatRedPackHistory(packHistory);
                    res = true;
                } else {
                    packHistory.setSuccess(false);
                    packHistory.setErrorCode(err_code);
                    packHistory.setErrorMsg(err_code_des);
                    wechatServiceClient.saveWechatRedPackHistory(packHistory);
                    logger.error("add wechat red bag error, user is {}, amount is {}, errorCode is {}, errorMsg is {}",
                            userId, amount, err_code, err_code_des);
                }
            } else {
                packHistory.setSuccess(false);
                packHistory.setErrorMsg(return_msg);
                wechatServiceClient.saveWechatRedPackHistory(packHistory);
                logger.error("add wechat red bag error, user is {}, amount is {}, returnCode is {}, returnMsg is {}",
                        userId, amount, return_code, return_msg);
            }
        } catch (Exception e) {
            logger.error("add wechat red bag error, user is {}, amount is {}",
                    userId, amount, e);
        }
        return res;
    }
    @Override
    public Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviterIds(Collection<Long> inviterIds) {
        return teacherActivateTeacherHistoryDao.findByInviterIds(inviterIds);
    }

    @Override
    public Map<Long, List<TeacherActivateTeacherHistory>> findTeacherActivateTeacherHistoryMapByInviteeIds(Collection<Long> inviteeIds) {
        return teacherActivateTeacherHistoryDao.findByInviteeIds(inviteeIds);
    }

    @Override
    public List<UserBalanceLog> findUserBalanceLogListByUserId(Long userId) {
        return userBalanceLogPersistence.findByUserId(userId);
    }

    @Override
    public List<RSOralReportStat> findRSOralReportStatListExcludeSchool(String taskId, String docId, Integer acode, Long schoolId, Date searchDate) {
        return rsOralReportStatDao.findExcludeSchool(taskId, docId, acode, schoolId, searchDate);
    }

    @Override
    public List<RSOralReportStat> findAreaRSOralReportStatList(String taskId, String docId, Integer ccode, Long schoolId, Date searchDate) {
        return rsOralReportStatDao.findAreaReport(taskId, docId, ccode, schoolId, searchDate);
    }

    @Override
    public void scheduleAutoResearchStaffBehaviorDataJob(String fromDateStr1, String endDateStr1, String jobOnly1, boolean needClear1) {
        AlpsThreadPool.getInstance().submit(() -> {
            String fromDateStr = fromDateStr1;
            String endDateStr = endDateStr1;
            String jobOnly = jobOnly1;
            boolean needClear = needClear1;

            if (StringUtils.isEmpty(endDateStr)) {
                Date date = DateUtils.nextDay(new Date(), -1);
                endDateStr = formatDate(date);
            }
            Date endDate = DateUtils.stringToDate(endDateStr + " 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

            if (StringUtils.isEmpty(fromDateStr)) {
                Date date = DateUtils.nextDay(endDate, -1);
                fromDateStr = formatDate(date);
            }
            Date fromDate = DateUtils.stringToDate(fromDateStr + " 23:59:59", DateUtils.FORMAT_SQL_DATETIME);

            if (needClear) {
                SchoolYear schoolYear = SchoolYear.newInstance();
                logger.info("clear data for year {}, term {}", schoolYear.year(), schoolYear.currentTerm().getKey());
                englishResearchStaffBehaviorDataGenerator.clearData(schoolYear.year(), schoolYear.currentTerm());
                mathResearchStaffBehaviorDataGenerator.clearData(schoolYear.year(), schoolYear.currentTerm());
            }

            for (; fromDate.before(endDate); fromDate = DateUtils.nextDay(fromDate, 1)) {
                // 这里按天执行
                Date nextDate = DateUtils.nextDay(fromDate, 1);
                if (jobOnly == null || jobOnly.equals("ENGLISH")) {
                    logger.info(" generate research staff english behavior data start: [" + formatDate(fromDate) + " ~ " + formatDate(endDate) + "]");
                    englishResearchStaffBehaviorDataGenerator.generate(fromDate, nextDate);
                    logger.info(" generate research staff english behavior data end: [" + formatDate(fromDate) + " ~ " + formatDate(endDate) + "]");
                }
                if (jobOnly == null || jobOnly.equals("MATH")) {
                    logger.info(" generate research staff math behavior data start: [" + formatDate(fromDate) + " ~ " + formatDate(endDate) + "]");
                    mathResearchStaffBehaviorDataGenerator.generate(fromDate, nextDate);
                    logger.info(" generate research staff math behavior data end: [" + formatDate(fromDate) + " ~ " + formatDate(endDate) + "]");
                }
            }
        });
    }

    private String formatDate(Date fromDate) {
        return DateUtils.dateToString(fromDate, DateUtils.FORMAT_SQL_DATE);
    }
}
