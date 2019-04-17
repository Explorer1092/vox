package com.voxlearning.utopia.agent.service.qrcode;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.remote.hydra.client.generic.json.HydraJsonClient;
import com.voxlearning.utopia.agent.constants.QRCodeBusinessType;
import com.voxlearning.utopia.agent.dao.mongo.qrcode.UserQrCodeDao;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.persist.entity.qrcode.UserQrCode;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentRemoteClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Named
public class UserQrCodeService extends AbstractAgentService {


    @Inject
    private UserQrCodeDao userQrCodeDao;

    public MapMessage getQRCode(Long userId, QRCodeBusinessType businessType){
        UserQrCode userQrCode = userQrCodeDao.loadByTypeAndUser(businessType, userId);
        if(userQrCode == null || StringUtils.isBlank(userQrCode.getQrCode())){
            Map<String, Object> dataMap = generateQrCode(userId, businessType);
            if(MapUtils.isEmpty(dataMap) || StringUtils.isBlank((String)dataMap.get("qrCode")) || dataMap.get("relatedId") == null){
                return MapMessage.errorMessage("获取二维码失败");
            }
            if(userQrCode == null){
                userQrCode = new UserQrCode();
                userQrCode.setUserId(userId);
                userQrCode.setBusinessType(businessType);
            }
            userQrCode.setQrCode((String)dataMap.get("qrCode"));
            userQrCode.setRelatedId(SafeConverter.toString(dataMap.get("relatedId")));
            userQrCodeDao.upsert(userQrCode);
        }
        return MapMessage.successMessage().add("qrCode", userQrCode.getQrCode());
    }

    private Map<String, Object> generateQrCode(Long userId, QRCodeBusinessType businessType){
        Map<String, Object> resultMap = new HashMap<>();
        if(businessType == QRCodeBusinessType.LIVE_ENROLLMENT){
            MapMessage message = LiveEnrollmentRemoteClient.loadQrCode(userId);
            if(message.isSuccess()){
                Map<String, Object> dataMap = (Map<String, Object>)message.get("data");
                resultMap.put("qrCode", dataMap.get("qrcode"));
                resultMap.put("relatedId", dataMap.get("deliveryId"));
            }
        }
        return resultMap;
    }

    public String getRelatedId(Long userId, QRCodeBusinessType businessType){
        UserQrCode userQrCode = userQrCodeDao.loadByTypeAndUser(businessType, userId);
        if(userQrCode != null){
            return userQrCode.getRelatedId();
        }
        return "";
    }

}
