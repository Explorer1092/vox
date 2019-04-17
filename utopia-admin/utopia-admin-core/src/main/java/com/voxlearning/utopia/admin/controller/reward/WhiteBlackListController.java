package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2018/3/8
 */
@Controller
@RequestMapping("/reward/whiteblack")
@Slf4j
public class WhiteBlackListController extends RewardAbstractController {

    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        List<SchoolExtInfo> receiverList = schoolExtServiceClient.getSchoolExtService().findAllSchoolReceiveTeacher().getUninterruptibly()
                .stream().filter(e->e.getReceiveTeacher() != null).collect(Collectors.toList());
        List<Map<String, Object>> receivers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(receiverList)) {
            List<Long> schoolIds = receiverList.stream().map(SchoolExtInfo::getId).collect(Collectors.toList());
            Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader().loadSchools(schoolIds).getUninterruptibly();
            List<Long> teacherIds = receiverList.stream().map(SchoolExtInfo::getReceiveTeacher).collect(Collectors.toList());
            Map<Long, TeacherDetail> teacherMap = teacherLoaderClient.loadTeacherDetails(teacherIds);
            for(SchoolExtInfo receiver : receiverList) {
                Map<String, Object> map = new HashMap<>();
                School school = schoolMap.get(receiver.getId());
                map.put("schoolName", school != null ? school.getCmainName() : "");
                TeacherDetail teacherDetail = teacherMap.get(receiver.getReceiveTeacher());
                map.put("teacherName", teacherDetail != null ? teacherDetail.fetchRealname() : "");
                map.put("teacherId", receiver.getReceiveTeacher());
                map.put("id", receiver.getId());
                map.put("description", receiver.getExtDescription());
                map.put("date", DateUtils.dateToString(receiver.getReceiverCreateAt()));
                receivers.add(map);
            }
        }

        model.addAttribute("receivers", receivers);

        return "reward/whiteblack/index";
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add() {
        Long schoolId = getRequestLong("schoolId");
        Long teacherId = getRequestLong("teacherId");
        String description = getRequestParameter("description", "");
        MapMessage mapMessage = validate(schoolId, teacherId);
        if (mapMessage != null) {
            return mapMessage;
        }

        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.getReceiveTeacher() != null) {
            if (schoolExtInfo.getReceiveTeacher().equals(teacherId)) {
                return MapMessage.errorMessage().setInfo("老师已经在该学校的白名单里，请勿重复添加！");
            } else {
                return MapMessage.errorMessage().setInfo("已经有老师在该学校的白名单里，请检查, 如要换老师请找到该学校的记录并移除！");
            }
        }
        Boolean addRes = schoolExtServiceClient.getSchoolExtService().addOrUpdateSchoolReceiver(schoolId, teacherId, description).getUninterruptibly();
        if (addRes) {
            try {
                String content = "恭喜您成为学生奖品代收老师。";
                AppMessage message = new AppMessage();
                message.setUserId(teacherId);
                message.setMessageType(TeacherMessageType.ACTIVIY.getType());
                message.setTitle("通知");
                message.setContent(content);
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                // 发pc端信息
                teacherLoaderClient.sendTeacherMessage(teacherId, content);
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, Arrays.asList(teacherId), new HashMap<>());
            } catch (Exception e) {
                log.error("fail to send Message, userId:{}", teacherId, e);
            }
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remove() {
        Long schoolId = getRequestLong("schoolId");
        if (schoolId == null || schoolId <= 0) {
            return MapMessage.errorMessage().setInfo("参数异常！");
        }
        schoolExtServiceClient.getSchoolExtService().addOrUpdateSchoolReceiver(schoolId, null, "");
        return MapMessage.successMessage();
    }

    private MapMessage validate(Long schoolId, Long teacherId) {
        if (schoolId == null || schoolId <= 0 || teacherId == null || teacherId <= 0) {
            return MapMessage.errorMessage().setInfo("参数异常！");
        }
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage().setInfo("学校不存在请检查！");
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacherDetail == null) {
            return MapMessage.errorMessage().setInfo("老师不存在请检查！");
        }

        if (!school.getId().equals(teacherDetail.getTeacherSchoolId())) {
            return MapMessage.errorMessage().setInfo("老师和学校不匹配，请检查！");
        }

        UserShippingAddress address = userLoaderClient.loadUserShippingAddress(teacherId);
        if (address == null || StringUtils.isBlank(address.getDetailAddress())) {
            return MapMessage.errorMessage().setInfo("老师的地址为空，请检查！");
        }
        return null;
    }
}
