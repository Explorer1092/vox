package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.remote.hydra.client.generic.json.HydraJsonClient;
import com.voxlearning.alps.remote.hydra.client.generic.json.JsonResultMapping;
import com.voxlearning.alps.runtime.RuntimeMode;
import org.slf4j.Logger;

import javax.validation.constraints.Max;
import java.util.Collection;
import java.util.Map;

/**
 * LiveEnrollmentRemoteClient
 *
 * @author song.wang
 * @date 2018/12/18
 */
public class LiveEnrollmentRemoteClient {

    private static final Logger logger = LoggerFactory.getLogger(LiveEnrollmentRemoteClient.class);

    private static final String GROUP = "alps-hydra-" + (RuntimeMode.current().le(Mode.TEST) ? Mode.TEST.getStageMode() : RuntimeMode.getCurrentStage());
    private static final String VERSION = "1.0";
    private static final String INTERFACE = "com.voxlearning.xue.service.marketing.api.XuePhecdaActivityLoader";
    private static final String INTERFACE_SERVICE = "com.voxlearning.xue.service.marketing.api.XuePhecdaActivityService";

    public static MapMessage loadQrCode(Long userId){
        return invokeRemoteMethod("getQrcode", new String[]{String.class.getName(), int.class.getName()}, new Object[]{String.valueOf(userId), 1});
    }

    public static MapMessage loadOrderList(Collection<Map<String, Object>> params){
        return invokeRemoteMethod("queryActivityPayList", new String[]{Collection.class.getName()}, new Object[]{params});
    }

    public static MapMessage receiveGifts(String phoneNo,Long userId){
        return invokeRemoteServiceMethod("receiveGift", new String[]{String.class.getName(),String.class.getName()}, new Object[]{phoneNo,String.valueOf(userId)});
    }

    public static MapMessage loadOrderCourseInfo(Collection<String> orderIds){
        return invokeRemoteMethod("queryOrderCourseInfo", new String[]{Collection.class.getName()}, new Object[]{orderIds});
    }

    private static MapMessage invokeRemoteMethod(String methodName, String[] parameterTypes, Object[] arguments){
        try {
            return HydraJsonClient.builder()
                    .serviceInterface(INTERFACE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName(methodName)
                    .parameterTypes(parameterTypes)
                    .arguments(arguments)
                    .build()
                    .invoke()
                    .mapTo(MapMessage.class);
        }catch (Exception e){
            logger.error("remote method execute error :" + INTERFACE + "." + methodName + "()", e);
            return MapMessage.errorMessage();
        }
    }
    private static MapMessage invokeRemoteServiceMethod(String methodName, String[] parameterTypes, Object[] arguments){
        try {
            return HydraJsonClient.builder()
                    .serviceInterface(INTERFACE_SERVICE)
                    .serviceGroup(GROUP)
                    .serviceVersion(VERSION)
                    .methodName(methodName)
                    .parameterTypes(parameterTypes)
                    .arguments(arguments)
                    .build()
                    .invoke()
                    .mapTo(MapMessage.class);
        }catch (Exception e){
            logger.error("remote method execute error :" + INTERFACE_SERVICE + "." + methodName + "()", e);
            return MapMessage.errorMessage();
        }
    }
}
