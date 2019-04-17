package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.ClazzZoneCommonService;
import com.voxlearning.utopia.service.zone.impl.service.message.MessageHandler;
import com.voxlearning.utopia.service.zone.api.constant.Protocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author : kai.sun
 * @version : 2018-11-30
 * @description :
 **/

@Service("com.voxlearning.utopia.service.zone.impl.service.ClazzZoneCommonServiceImpl")
@ExposeService(interfaceClass = ClazzZoneCommonService.class, version = @ServiceVersion(version = "20181130"))
@Slf4j
public class ClazzZoneCommonServiceImpl extends MessageHandler implements ClazzZoneCommonService {

    public ClazzZoneCommonServiceImpl() {
        super(Protocol.COMMON_SERVICE_START, Protocol.COMMON_SERVICE_END);
    }

    @Override
    public MapMessage handle(int protocol,Map<String, Object> map) {
        switch (protocol){
            case Protocol.REQUEST_COMMON_SERVICE:
                return common(map);
            default:
                return MapMessage.errorMessage("传入参数错误");
        }
    }

    private MapMessage common(Map<String, Object> map){
        return MapMessage.of(map).setSuccess(true);
    }

}
