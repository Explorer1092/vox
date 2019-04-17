package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombUserData;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombUser;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombUserDao;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class HoneycombUserService {

    @Inject
    private HoneycombUserDao honeycombUserDao;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private AgentPartnerService agentPartnerService;

    public List<Long> getHoneycombUserIds(Long agentUserId){
        Map<Long, List<HoneycombUser>> dataMap = honeycombUserDao.loadByAgentUserIds(Collections.singleton(agentUserId));
        if(MapUtils.isEmpty(dataMap)){
            return Collections.emptyList();
        }
        return dataMap.values().stream().flatMap(List::stream).map(HoneycombUser::getId).collect(Collectors.toList());
    }

    public Map<Long, List<Long>> getHoneycombUserIds(Collection<Long> agentUserIds){
        if(CollectionUtils.isEmpty(agentUserIds)){
            return Collections.emptyMap();
        }
        Map<Long, List<HoneycombUser>> dataMap = honeycombUserDao.loadByAgentUserIds(agentUserIds);
        if(MapUtils.isEmpty(dataMap)){
            return Collections.emptyMap();
        }
        Map<Long, List<Long>> resultMap = new HashMap<>();
        dataMap.forEach((k, v) -> resultMap.put(k, v.stream().map(HoneycombUser::getId).collect(Collectors.toList())));
        return resultMap;
    }

    public MapMessage saveHoneycombUser(Long honeycombUserId){
        HoneycombUser honeycombUser = honeycombUserDao.load(honeycombUserId);
        if(honeycombUser != null){
            return MapMessage.successMessage();
        }
        honeycombUser = new HoneycombUser();
        honeycombUser.setId(honeycombUserId);
        honeycombUserDao.insert(honeycombUser);
        return MapMessage.successMessage();
    }

    private MapMessage bindAgentUser(Long honeycombUserId, Long agentUserId){
        if(honeycombUserId == null || agentUserId == null){
            return MapMessage.errorMessage();
        }

        HoneycombUser honeycombUser = honeycombUserDao.load(honeycombUserId);
        if(honeycombUser == null){
            honeycombUser = new HoneycombUser();
            honeycombUser.setId(honeycombUserId);
        }

        if(honeycombUser.getAgentUserId() != null && !Objects.equals(honeycombUser.getAgentUserId(), agentUserId)){
            agentPartnerService.removeLinkManByUserId(honeycombUser.getAgentUserId());
        }

        honeycombUser.setAgentUserId(agentUserId);
        honeycombUserDao.upsert(honeycombUser);
        return MapMessage.successMessage();
    }

    public MapMessage unbindAgentUser(Long agentUserId){
        List<Long> honeycombUserIds = getHoneycombUserIds(agentUserId);
        if(CollectionUtils.isNotEmpty(honeycombUserIds)){
            honeycombUserIds.forEach(p -> honeycombUserDao.unsetAgentUserId(p));
        }
        return MapMessage.successMessage();
    }

    public MapMessage bindAgentUser(String mobile, Long agentUserId){
        Long honeycombId = registerOrGetHoneycombUser(mobile);
        if(SafeConverter.toLong(honeycombId) < 1){
            return MapMessage.errorMessage("蜂巢账号绑定失败");
        }
        return bindAgentUser(honeycombId, agentUserId);
    }

    private Long registerOrGetHoneycombUser(String mobile){
        Long id = null;
        String domain = getDomainUrl();
        String url = "/v1/agent/register.vpage";
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("mobile", mobile);
        dataMap.put("app_key", "HoneyComb");
        String sig = AgentApiAuth.generateAppKeySig(dataMap, getHoneycombSecretKey());

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(domain + url)
                .addParameter("mobile", mobile)
                .addParameter("app_key", "HoneyComb")
                .addParameter("sig", sig)
                .execute();
        MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (resultMap != null && resultMap.isSuccess() && resultMap.containsKey("id")) {
            Long honeycombId = SafeConverter.toLong(resultMap.get("id"));
            if(honeycombId > 0){
                id = honeycombId;
                smsServiceClient.createSmsMessage(mobile)
                        .content("您已成功注册蜂巢，戳链接http://t.cn/EMw7m5l下载体验吧")
                        .send();
            }
        }
        return id;
    }

    public String getDomainUrl(){
        String domain = "http://honeycomb.test.17zuoye.net";
        if(RuntimeMode.isTest()){
            domain = "http://honeycomb.test.17zuoye.net";
        }else if(RuntimeMode.isStaging()){
            domain = "http://honeycomb.staging.17zuoye.net";
        }else if(RuntimeMode.isProduction()){
            domain = "http://honeycomb.oaloft.com";
        }
        return domain;
    }





    public String getHoneycombSecretKey(){
        if(RuntimeMode.lt(Mode.STAGING)){
            return "ASDF78XF";
        }else {
            return "0zns1UZTljhH";
        }
    }


    // 获取蜂巢用户的基本数据
    public List<HoneycombUserData> getHoneycombUserData(Collection<Long> honeycombIds){
        List<HoneycombUserData> dataList = new ArrayList<>();
        if(CollectionUtils.isEmpty(honeycombIds)){
            return dataList;
        }

        String url = "/v1/agent/user_info.vpage";
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("userIds", StringUtils.join(honeycombIds, ","));
        dataMap.put("app_key", "HoneyComb");
        String sig = AgentApiAuth.generateAppKeySig(dataMap, getHoneycombSecretKey());

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(getDomainUrl() + url)
                .addParameter("userIds", StringUtils.join(honeycombIds, ","))
                .addParameter("app_key", "HoneyComb")
                .addParameter("sig", sig)
                .execute();
        MapMessage resultMap = JsonUtils.fromJson(response.getResponseString(), MapMessage.class);
        if (resultMap != null && resultMap.isSuccess() && resultMap.containsKey("data")) {
            HashMap map = (HashMap)resultMap.get("data");
            map.forEach((k, v) -> {
                HashMap userMap = (HashMap) v;
                Long id = SafeConverter.toLong(userMap.get("id"));
                if(id < 1){
                    return;
                }
                HoneycombUserData userData = new HoneycombUserData();
                userData.setHoneycombId(id);
                userData.setNickName(SafeConverter.toString(userMap.get("nickName")));
                userData.setMobile(SafeConverter.toString(userMap.get("mobile")));
                userData.setHeadPortrait(SafeConverter.toString(userMap.get("headPortrait")));
                dataList.add(userData);
            });
        }
        return dataList;
    }

}
