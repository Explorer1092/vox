package com.voxlearning.utopia.agent.service.signin;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.service.mobile.SchoolClueService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInType;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.signin.SignInRecord;
import com.voxlearning.utopia.service.crm.api.service.agent.signin.SignInRecordService;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.signin.SignInRecordLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.signin.SignInRecordServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 签到
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Named
public class SignInService {

    @Inject
    private SignInRecordServiceClient signInRecordServiceClient;

    @Inject
    private SignInRecordLoaderClient signInRecordLoaderClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolClueService schoolClueService;
    @Inject
    private WorkRecordService workRecordService;


    public MapMessage signIn(SignInBusinessType businessType, SignInType signInType, String coordinateType, String longitude, String latitude, String photoUrl, Long userId, String userName){

        if(businessType == null || signInType == null){
            return MapMessage.errorMessage("业务类型或签到类型有误！");
        }

        if(signInType == SignInType.PHOTO && StringUtils.isBlank(photoUrl)){
            return MapMessage.errorMessage("请上传照片!");
        }

        if(StringUtils.isBlank(coordinateType) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)){
            return MapMessage.errorMessage("位置信息有误！");
        }

        if(userId == null){
            return MapMessage.errorMessage("签到的用户信息有误！");
        }

        // 会将坐标转换为高德坐标系
        MapMessage addressMap = AmapMapApi.getAddress(latitude, longitude, coordinateType);
        if(!addressMap.isSuccess()){
            return MapMessage.errorMessage("签到失败！");
        }

        SignInRecord signInRecord = new SignInRecord();
        signInRecord.setBusinessType(businessType);
        signInRecord.setSignInType(signInType);
        signInRecord.setCoordinateType("autonavi");
        signInRecord.setLatitude(ConversionUtils.toString(addressMap.get("latitude")));
        signInRecord.setLongitude(ConversionUtils.toString(addressMap.get("longitude")));
        signInRecord.setAddress(ConversionUtils.toString(addressMap.get("address")));

        if(signInType == SignInType.PHOTO){
            signInRecord.setPhotoUrl(photoUrl);
        }

        signInRecord.setUserId(userId);
        signInRecord.setUserName(userName);
        signInRecord.setSignInTime(new Date());

        String signInId = signInRecordServiceClient.insert(signInRecord);
        return MapMessage.successMessage().add("signInRecordId", signInId).add("address", signInRecord.getAddress());
    }

    public boolean checkSignIn(String signInRecordId){
        if(StringUtils.isBlank(signInRecordId)){
            return false;
        }
        SignInRecord signInRecord = signInRecordLoaderClient.load(signInRecordId);
        return signInRecord != null;
    }


    public boolean judgeDistance(String coordinateType1, String lng1, String lat1, String coordinateType2, String lng2, String lat2, long distance){
        if(StringUtils.isBlank(coordinateType1) || StringUtils.isBlank(lng1) || StringUtils.isBlank(lat1)
                || StringUtils.isBlank(coordinateType2) || StringUtils.isBlank(lng2) || StringUtils.isBlank(lat2)
                || distance < 1){
            return false;
        }
        MapMessage msg = AmapMapApi.GetDistance(lng1, lat1, coordinateType1, lng2, lat2, coordinateType2);
        if (!msg.isSuccess()) {
            return false;
        }

        if (msg.get("res") != null && ConversionUtils.toLong(msg.get("res")) < distance) {
            return true;
        }
        return false;
    }

    // 判断指定的位置和多个目标位置的距离是否在指定的距离之内
    public boolean judgeDistance(List<Map<String, String>> targetLocationList, String longitude, String latitude, String coordinateType, long distance){
        if(CollectionUtils.isEmpty(targetLocationList) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude) || StringUtils.isBlank(coordinateType)){
            return false;
        }

        boolean result = false;
        for (Map<String, String> info : targetLocationList) {
            MapMessage msg = AmapMapApi.GetDistance(longitude, latitude, coordinateType, info.get("longitude"), info.get("latitude"), info.get("coordinateType"));
            if (!msg.isSuccess()) {
                continue;
            }

            if (msg.get("res") != null && ConversionUtils.toLong(msg.get("res")) < distance) {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * 获取学校的坐标位置（从schoolExtInfo及学校位置申请记录中获取）
     * @param schoolId
     * @return
     */
    public List<Map<String, String>> getSchoolLocationList(Long schoolId) {
        List<Map<String, String>> locationList = new ArrayList<>();
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        if (schoolExtInfo != null && StringUtils.isNotBlank(schoolExtInfo.getLatitude()) && StringUtils.isNotBlank(schoolExtInfo.getLongitude())) {
            Map<String, String> info = new HashMap<>();
            info.put("latitude", schoolExtInfo.getLatitude());
            info.put("longitude", schoolExtInfo.getLongitude());
            info.put("coordinateType", schoolExtInfo.getCoordinateType());
            locationList.add(info);
        }

        // 获取待审核的审核信息（审核通过的会更新到schoolExtInfo中，所以只需获取待审核的数据就可以了）
        List<CrmSchoolClue> schoolClues = schoolClueService.loads(schoolId).stream()
                .filter(p -> p.getAuthenticateType() != null && p.getAuthenticateType() == 5 && p.getStatus() != null && p.getStatus() == CrmSchoolClueStatus.待审核.getCode())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(schoolClues)) {
            schoolClues.forEach(p -> {
                Map<String, String> info = new HashMap<>();
                info.put("latitude", p.getLatitude());
                info.put("longitude", p.getLongitude());
                info.put("coordinateType", p.getCoordinateType());
                locationList.add(info);
            });
        }
        return locationList;
    }

    public Map<String, String> loadWorkRecordLocation(String workRecordId,AgentWorkRecordType workRecordType){
        WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(workRecordId, workRecordType);
        if(workRecordData != null && StringUtils.isNotBlank(workRecordData.getCoordinateType()) && StringUtils.isNotBlank(workRecordData.getLatitude())&& StringUtils.isNotBlank(workRecordData.getLongitude())){
            Map<String, String> info = new HashMap<>();
            info.put("latitude", workRecordData.getLatitude());
            info.put("longitude", workRecordData.getLongitude());
            info.put("coordinateType", workRecordData.getCoordinateType());
            return info;
        }
        return null;
    }

}
