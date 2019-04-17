package com.voxlearning.utopia.service.wechat.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import com.voxlearning.utopia.service.wechat.api.entities.MiniProgramNoticeFormId;
import com.voxlearning.utopia.service.wechat.api.entities.UserMiniProgramCheck;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
@ServiceVersion(version = "20190218")
public interface UserMiniProgramCheckService extends IPingable {

    boolean isChecked(Long uid, MiniProgramType type);

    MapMessage doCheck(Long uid, MiniProgramType type);

    Long  getTodayCheckCount( MiniProgramType type);

    int getWeekContinuousCheckCount(Long uid, MiniProgramType type);

    int getTotalCheckCount(Long uid, MiniProgramType type);

    MapMessage loadCheckData(Long uid, MiniProgramType type);

    UserMiniProgramCheck loadByUid(Long uid, MiniProgramType type);

    /**
     * Redis cache for uid immediate
     * @param uid
     * @param formId
     * @param type
     */
    void addNoticeFormId(Long uid, String formId, MiniProgramType type);

    /**
     * Mongo db expire by self
     * @param openId
     * @param formId
     * @param type
     */
    void addNoticeFormId(String openId, String formId, MiniProgramType type);

    List<MiniProgramNoticeFormId> loadNoticeFormId(String openId, MiniProgramType type);

    void expireNoticeFormId(List<String> ids);

    String useNoticeFormId(String openId, MiniProgramType type);

    String getAccessToken(MiniProgramType type);
    String getAccessTokenNoCache(MiniProgramType type);
}
