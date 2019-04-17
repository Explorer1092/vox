package com.voxlearning.utopia.agent.controller.mobile.signin;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.workrecord.WorkRecordData;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.signin.SignInService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.core.helper.AmapMapApi;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInBusinessType;
import com.voxlearning.utopia.service.crm.api.constants.agent.SignInType;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SignInController
 *
 * @author song.wang
 * @date 2018/12/17
 */
@Controller
@RequestMapping("/mobile/signin")
public class SignInController extends AbstractAgentController {

    @Inject
    private AgentRequestSupport agentRequestSupport;
    @Inject
    private SignInService signInService;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private WorkRecordService workRecordService;

    @RequestMapping("signin.vpage")
    @ResponseBody
    public MapMessage signIn(HttpServletRequest request){

        AuthCurrentUser user = getCurrentUser();

        SignInBusinessType businessType = SignInBusinessType.nameOf(getRequestString("businessType"));
        SignInType signInType = SignInType.nameOf(getRequestString("signInType"));

        if(businessType == null){
            return MapMessage.errorMessage("业务类型有误,签到失败！");
        }

        if(signInType == null){
            return MapMessage.errorMessage("签到类型有误,签到失败！");
        }

        String longitude = getRequestString("longitude");
        String latitude = getRequestString("latitude");
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";//getRequestString("coordinateType");
        } else {
            coordinateType = "autonavi";
        }

        if(StringUtils.isBlank(coordinateType) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)){
            return MapMessage.errorMessage("GPS位置获取失败！");
        }

        String photoUrl = getRequestString("photoUrl");

        List<Map<String, String>> locationList = new ArrayList<>();
        if (businessType == SignInBusinessType.SCHOOL || businessType == SignInBusinessType.ACCOMPANY){
            if (businessType == SignInBusinessType.SCHOOL){
                Long schoolId = getRequestLong("schoolId");
                // 获取学校的坐标位置
                locationList = signInService.getSchoolLocationList(schoolId);
            }else {
                String targetWorkRecordId = getRequestString("targetWorkRecordId");
                String targetWorkRecordType = getRequestString("targetWorkRecordType");
                Map<String, String> locationMap = signInService.loadWorkRecordLocation(targetWorkRecordId, AgentWorkRecordType.nameOf(targetWorkRecordType));
                if (MapUtils.isNotEmpty(locationMap)) {
                    locationList.add(locationMap);
                }

                //如果是陪访进校，获取学校位置坐标信息
                WorkRecordData workRecordData = workRecordService.getWorkRecordDataByIdAndType(targetWorkRecordId, AgentWorkRecordType.nameOf(targetWorkRecordType));
                if (workRecordData != null && AgentWorkRecordType.nameOf(targetWorkRecordType) == AgentWorkRecordType.SCHOOL){
                    locationList.addAll(signInService.getSchoolLocationList(SafeConverter.toLong(workRecordData.getSchoolId())));
                }
            }
            if(signInType == SignInType.GPS && CollectionUtils.isEmpty(locationList)){
                return MapMessage.errorMessage().add("noLocation", true);
            }
        }

        // 位置签到的情况下，判断距离
        if(signInType == SignInType.GPS){
            // 判断距离
            boolean withinDistance = judgeDistance(businessType, coordinateType, longitude, latitude,locationList);
            if(!withinDistance){
                return MapMessage.errorMessage("距离过远").add("farAway", true);
            }
        }


        return signInService.signIn(businessType, signInType, coordinateType, longitude, latitude, photoUrl, user.getUserId(), user.getRealName());
    }

    private boolean judgeDistance(SignInBusinessType businessType, String coordinateType, String longitude, String latitude,List<Map<String, String>> locationList){
        if(businessType == null || StringUtils.isBlank(coordinateType) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)){
            return false;
        }
        Integer distance = getRequestInt("distance", 2000);  // 默认2000米
        // TODO: 2018/12/17 根据不同的类型，进行相应业务的距离判断
        if(businessType == SignInBusinessType.SCHOOL || businessType == SignInBusinessType.ACCOMPANY){
            // 判断距离是否在2000米内
            boolean withinDistance = signInService.judgeDistance(locationList, longitude, latitude, coordinateType, distance);
            if (!withinDistance) {
                return false;
            }
        }else if(businessType == SignInBusinessType.LIVE_ENROLLMENT || businessType == SignInBusinessType.MEETING || businessType == SignInBusinessType.RESOURCE_EXTENSION){   // 不进行距离校验
            return true;
        }
        return true;
    }

    @RequestMapping("distance.vpage")
    @ResponseBody
    public MapMessage getDistance(HttpServletRequest request){

        SignInBusinessType businessType = SignInBusinessType.nameOf(getRequestString("businessType"));
        if(businessType == null){
            return MapMessage.errorMessage("业务类型有误,获取距离失败！");
        }

        String longitude = getRequestString("longitude");
        String latitude = getRequestString("latitude");
        String coordinateType;
        if (agentRequestSupport.isIOSRequest(request)) {
            coordinateType = "wgs84ll";//getRequestString("coordinateType");
        } else {
            coordinateType = "autonavi";
        }

        if(StringUtils.isBlank(coordinateType) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)){
            return MapMessage.errorMessage("GPS位置获取失败！");
        }

        return getDistance(businessType, coordinateType, longitude, latitude);
    }

    private MapMessage getDistance(SignInBusinessType businessType, String coordinateType, String longitude, String latitude){
        if(businessType == null || StringUtils.isBlank(coordinateType) || StringUtils.isBlank(longitude) || StringUtils.isBlank(latitude)){
            return MapMessage.errorMessage();
        }
        String targetCoordinateType = "";
        String targetLongitude = "";
        String targetLatitude = "";
        if(businessType == SignInBusinessType.LIVE_ENROLLMENT){
            Long schoolId = getRequestLong("schoolId");
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (schoolExtInfo != null && StringUtils.isNotBlank(schoolExtInfo.getLatitude()) && StringUtils.isNotBlank(schoolExtInfo.getLongitude())) {
                targetCoordinateType = schoolExtInfo.getCoordinateType();
                targetLongitude = schoolExtInfo.getLongitude();
                targetLatitude = schoolExtInfo.getLatitude();
            }
        }
        if(StringUtils.isBlank(targetCoordinateType) || StringUtils.isBlank(targetLongitude) || StringUtils.isBlank(targetLatitude)){
            return MapMessage.errorMessage();
        }
        MapMessage distanceMap = AmapMapApi.GetDistance(longitude, latitude, coordinateType, targetLongitude, targetLatitude, targetCoordinateType);
        if(!distanceMap.isSuccess()){
            return distanceMap;
        }
        return MapMessage.successMessage().add("distance", SafeConverter.toLong(distanceMap.get("res")));
    }

}
