/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.impl.support.apppush.publisher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.api.entities.crm.PushTask;
import com.voxlearning.utopia.service.crm.impl.dao.crm.PushTaskDao;
import com.voxlearning.utopia.service.crm.impl.loader.crm.CrmAppPushLoaderImpl;
import com.voxlearning.utopia.service.crm.impl.support.apppush.AppPushWorkflowContext;
import com.voxlearning.utopia.service.crm.tools.AppPushWorkFlowUtils;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import org.apache.poi.ss.usermodel.*;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

abstract public class AppPushPublisher extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;

    // inject in alphabetical order
    @Inject protected AppMessageServiceClient appMessageServiceClient;
    @Inject protected MessageCommandServiceClient messageCommandServiceClient;
    @Inject private PushTaskDao pushTaskDao;
    @Inject private CrmAppPushLoaderImpl crmAppPushLoader;


    public MapMessage publish(AppPushWorkflowContext context) {
        AppPushWfMessage wfMsg = context.getWorkflowMessage();
        MapMessage validation = AppPushWorkFlowUtils.validateMessage(wfMsg);
        if (!validation.isSuccess()) {
            return validation;
        }

        if (!wfMsg.isFast()) {
            // 发送之前再次检查一下状态是不是更新过了的
            AppPushWfMessage appPushMsg = crmAppPushLoader.findByRecord(wfMsg.getRecordId());
            if (appPushMsg == null) {
                return MapMessage.errorMessage("批量发送AppPush消息失败 admin_send_app_push:{} because WechatWfMessage is null", wfMsg.getRecordId());
            }
            // 如果已经处理过就直接越过吧
            // FIXME todo checkout later
//            if ("success".equals(appPushMsg.getSendStatus()) && "processed".equals(appPushMsg.getStatus())) {
//                return MapMessage.successMessage();
//            }
        }

        // 记录错误信息
        StringBuilder errorMsg = new StringBuilder();

        // 先发送系统消息
        try {
            if (wfMsg.canSendMsg()) {
                MapMessage appResult = sendAppMessage(context);
                if (!appResult.isSuccess()) errorMsg.append("系统消息发送失败：").append(appResult.getInfo());
            }
        } catch (Exception ex) {
            logger.error("AppMessageWorkFlow: Failed Publish App Message", ex);
            errorMsg.append("系统消息发送失败：").append(StringUtils.firstLine(ex.getMessage()));
        }

        // 再发送Push
        try {
            if (wfMsg.canSendPush()) {
                MapMessage pushResult = sendPushMessage(context);
                if (!pushResult.isSuccess()) errorMsg.append("Push消息发送失败：").append(pushResult.getInfo());
            }
        } catch (Exception ex) {
            logger.error("AppMessageWorkFlow: Failed Publish Push Message", ex);
            errorMsg.append("Push消息发送失败：").append(StringUtils.firstLine(ex.getMessage()));
        }

        if (errorMsg.toString().length() > 0) {
            return MapMessage.errorMessage(errorMsg.toString());
        }
        return MapMessage.successMessage();

    }

    /**
     * 发送Push消息
     */
    public abstract MapMessage sendPushMessage(AppPushWorkflowContext context) throws Exception;

    /**
     * 发送系统消息
     */
    public abstract MapMessage sendAppMessage(AppPushWorkflowContext context) throws Exception;

    /**
     * 生成用于JPUSH 发送的 TAG
     * JPushTag.orTag  --> tag
     * JpushTag.andTag --> tag_and
     */
    public abstract JPushTag jpushTag(AppPushWfMessage wfMsg);

    protected List<Long> fetchSendUserList(AppPushWfMessage wfMsg) {
        if (StringUtils.isNotBlank(wfMsg.getFileUrl())) {
            Set<Long> userToSend = new HashSet<>();
            try {
                // Read From Excel
                URL fileUrl = new URL(wfMsg.getFileUrl());
                InputStream in = fileUrl.openStream();
                Workbook workbook = WorkbookFactory.create(in);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 0;
                while (true) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        break;
                    }
                    Long uid = getCellValueLong(row.getCell(0));
                    if (uid == null) break;
                    if (uid != 0L) userToSend.add(uid);
                    rowIndex++;
                }
                return new ArrayList<>(userToSend);
            } catch (Exception ex) {
                logger.error("Failed to Read AppPush User Excel Info, fileUrl={}", wfMsg.getFileUrl(), ex);
                return Collections.emptyList();
            }
        }
        if (CollectionUtils.isNotEmpty(wfMsg.getTargetUser())) {
            return wfMsg.getTargetUser();
        }
        return Collections.emptyList();
    }

    /**
     * 将地区拼成TAG
     */
    String genRegionTag(Integer regionCode) {
        if (regionCode == null) {
            return null;
        }
        return buildRegionTag(raikouSystem.loadRegion(regionCode));
    }

    /**
     * 将地区拼成TAG
     */
    List<String> genRegionTags(Collection<Integer> regions) {
        regions = CollectionUtils.toLinkedHashSet(regions);
        if (CollectionUtils.isEmpty(regions)) {
            return Collections.emptyList();
        }
        return raikouSystem.getRegionBuffer()
                .loadRegions(regions).values()
                .stream()
                .map(this::buildRegionTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    void recordTaskId(Map<String, Object> extInfo) {
        PushTask pushTask = new PushTask();
        Date date = new Date();
        ObjectId objectId = new ObjectId();
        String id = SafeConverter.toString(date.getTime()) + "-" + objectId.toString();
        pushTask.setId(id);
        pushTaskDao.insert(pushTask);
        extInfo.put("taskId", pushTask.getId());
    }

    String i7TinyUrl(String longUrl) {
        if (StringUtils.isBlank(longUrl)) {
            return longUrl;
        }
        String apiUrl = "http://17zyw.cn/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            apiUrl = "http://d.test.17zuoye.net/";
        }
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post(apiUrl + "crt")
                .addParameter("url", longUrl)
                .execute()
                .getResponseString();
        if (StringUtils.isNotBlank(responseStr)) {
            return apiUrl + responseStr;
        } else {
            return longUrl;
        }
    }

    private String buildRegionTag(ExRegion region) {
        if (region == null) {
            return null;
        }
        if (region.fetchRegionType() == RegionType.PROVINCE) {
            return JpushUserTag.PROVINCE.generateTag(region.getId().toString());
        } else if (region.fetchRegionType() == RegionType.CITY) {
            return JpushUserTag.CITY.generateTag(region.getId().toString());
        } else if (region.fetchRegionType() == RegionType.COUNTY) {
            return JpushUserTag.COUNTY.generateTag(region.getId().toString());
        }
        return null;
    }

    protected Long getCellValueLong(Cell cell) {
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return new Double(cell.getNumericCellValue()).longValue();
            }
            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                return ConversionUtils.toLong(cell.getStringCellValue().trim());
            }
        } catch (Exception ignored) {
            return 0L;
        }
        return null;
    }

    public String getUserKtwelvePushTag(String ktwelve) {
        if ("i".equals(ktwelve)) {
            return JpushUserTag.INFANT_SCHOOL.tag;
        } else if ("j".equals(ktwelve)) {
            return JpushUserTag.PRIMARY_SCHOOL.tag;
        } else if ("m".equals(ktwelve)) {
            return JpushUserTag.JUNIOR_SCHOOL.tag;
        } else if ("s".equals(ktwelve)) {
            return JpushUserTag.SENIOR_SCHOOL.tag;
        }

        throw new IllegalArgumentException("unknown ktwelve value " + ktwelve);
    }

}
