package com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.data.TalkResultInfo;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import org.apache.http.message.BasicHeader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Named
public class AUTI_LoadResponse extends AbstractAiSupport implements IAITask<AITalkLessonInteractContext> {
    @Inject
    private AICacheSystem aiCacheSystem;



    @Override
    public void execute(AITalkLessonInteractContext context) {
        try{
            String response = HttpRequestExecutor.defaultInstance()
                    .post(context.getRequestUrl())
                    .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
                    .addParameter(context.getRequestParameter())
                    .execute().getResponseString();
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUser().getId(),
                    "mod1", context.getInput(),
                    "mod2", JsonUtils.fromJson(response),
                    "mod3", context.getType(),
                    "mod4", context.getUsercode(),
                    "mod5", context.getRequestParameter(),
                    "mod6", context.getRequestUrl(),
                    "op", "aiUserTalkInteract"
            ));
            if (RuntimeMode.le(Mode.STAGING)) {
                logger.info("AITalkLessonInteractContext, userId:{}, input:{}, response: {}, type:{}, usercode:{}, request:{}, url:{}",
                        context.getUser().getId(), context.getInput(), JsonUtils.fromJson(response), context.getType(), context.getUsercode(), context.getRequestParameter(), context.getRequestUrl());
            }
            Map<String, Object> map = JsonUtils.fromJson(response);
            if(MapUtils.isEmpty(map)){
                context.errorResponse("talk response empty");
                return;
            }
            context.getResult().putAll(map);

            if (!"success".equals(map.get("result"))) {
                context.terminateTask();
                return;
            }

            if (StringUtils.isBlank(context.getLessonId())) {
                context.terminateTask();
                return;
            }

            String qid = getQid(map);

            // calc question id
            context.setQid(qid);
            switch (context.getType()) {
                case Dialogue:
                    aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().addRecord(context, false, response);
                    break;
                case video_conversation:
                    aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().addRecord(context, false, response);
                    break;
                case task_conversation:
                    aiCacheSystem.getUserTaskTalkSceneResultCacheManager().addRecord(context, false, response);
                    break;
                case Task:
                    aiCacheSystem.getUserTaskTalkSceneResultCacheManager().addRecord(context, false, response);
                    break;
                default:
                    context.errorResponse("type error.");
                    return;
            }
            context.setResultInfo(JsonUtils.fromJson(response, TalkResultInfo.class));

        } catch (Exception e){
            logger.error("http request post error url:{}, paramter:{}", context.getRequestUrl(), context.getRequestParameter(), e);
            context.errorResponse("talk response error");
        }

    }


    private String getQid(Map<String, Object> map) {
        if (map.get("data") == null || ((Collection) map.get("data")).isEmpty()) {
            logger.warn("AITalkLessonInteractContext question result collect response data is null or empty");
            return "";
        }
        try {
            List data = ((List) map.get("data"));
            Map data0 = (Map) data.get(0);


            String path = String.valueOf(data0.get("path"));
            String qid = "";

            if (StringUtils.isNotBlank(path)) {

                int idx = path.indexOf("-contents");
                if (idx > 0) {
                    qid = path.substring(0, idx);
                }
            }
            return qid;
        } catch (Exception e) {
            logger.error("AITalkLessonInteractContext get qid failed,case: {}", e.getMessage());
        }


        return "";


    }

}
