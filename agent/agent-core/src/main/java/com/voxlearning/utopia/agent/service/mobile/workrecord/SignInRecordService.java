package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInType;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.consumer.service.agent.signin.SignInRecordServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SignInRecordService extends AbstractAgentService {
    @Inject
    private SignInRecordServiceClient signInRecordServiceClient;

    /**
     * 添加签到记录
     * @param coordinateType
     * @param longitude
     * @param latitude
     * @param businessType
     * @param signType
     * @param photoUrl
     * @param userId
     * @param userName
     * @return
     */
    public String addSignInRecord(String coordinateType,String longitude, String latitude, SignInBusinessType businessType, Integer signType,String photoUrl,Long userId,String userName){
        SignInRecord signInRecord = new SignInRecord();
        SignInType signInType;
        if (signType == 1){
            signInType = SignInType.GPS;
        }else {
            signInType = SignInType.PHOTO;
            signInRecord.setPhotoUrl(photoUrl);
        }
        MapMessage address = AmapMapApi.getAddress(latitude, longitude, coordinateType);
        if(address.isSuccess()){
            signInRecord.setAddress(ConversionUtils.toString(address.get("address")));
            signInRecord.setLatitude(ConversionUtils.toString(address.get("latitude")));
            signInRecord.setLongitude(ConversionUtils.toString(address.get("longitude")));
            signInRecord.setCoordinateType(coordinateType);
        }
        signInRecord.setBusinessType(businessType);
        signInRecord.setSignInType(signInType);
        signInRecord.setUserId(userId);
        signInRecord.setUserName(userName);
        return signInRecordServiceClient.insert(signInRecord);
    }
}
