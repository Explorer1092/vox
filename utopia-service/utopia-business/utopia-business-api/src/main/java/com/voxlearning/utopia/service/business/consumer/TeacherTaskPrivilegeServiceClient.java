package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import com.voxlearning.utopia.mapper.TeacherTaskPrivilegeMapper;
import com.voxlearning.utopia.service.business.api.TeacherTaskPrivilegeService;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeacherTaskPrivilegeServiceClient {

    @ImportService(interfaceClass = TeacherTaskPrivilegeService.class)
    private TeacherTaskPrivilegeService remoteReference;

    public MapMessage isHavePrivilege(Long teacherId, Long type) {
        if (teacherId == null) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        if (type == null) {
            return MapMessage.errorMessage("特权类型不能为空");
        }
        return remoteReference.isHavePrivilege(teacherId, type);
    }

    public MapMessage getPrivilegeTimes(Long teacherId, Long type) {
        if (teacherId == null) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        if (type == null) {
            return MapMessage.errorMessage("特权类型不能为空");
        }
        return remoteReference.getPrivilegeTimes(teacherId, type);
    }

    public MapMessage getPrivilege(Long teacherId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        return remoteReference.getTeacherALLPrivilege(teacherId);
    }

    public MapMessage cousumerPrivilege(Long teacherId, Long type, String system) {
        return this.cousumerPrivilege(teacherId, type, system, null);
    }

    public MapMessage cousumerPrivilege(Long teacherId, Long type, String system, String couponRefId) {
        if (teacherId == null) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        if (type == null) {
            return MapMessage.errorMessage("特权类型不能为空");
        }
        if (system == null) {
            return MapMessage.errorMessage("消费的功能不能为空");
        }
        if (system.length() > 100) {
            return MapMessage.errorMessage("system不能超过100个字符");
        }
        return remoteReference.cousumerPrivilege(teacherId, type, system, couponRefId);
    }

    public MapMessage getCoursewareDownloadTimes(Long teacherId) {
        return remoteReference.getCoursewareDownloadTimes(teacherId);
    }


    public MapMessage get17ClassTimes(Long teacherId) {
        return remoteReference.get17ClassTimes(teacherId);
    }

    public Map<Long, TeacherTaskPrivilegeTpl> getAllTeacherTaskPrivilegeTplMap() {
        return remoteReference.getAllTeacherTaskPrivilegeTplMap();
    }

    public void pushDeleteCoupon(Long teacherId, Long tplId, List<String> refIds) {
        remoteReference.pushDeleteCoupon(teacherId, tplId, refIds);
    }

    public Map<Long, TeacherTaskPrivilegeTpl> getAllTplsMap() {
        List<TeacherTaskPrivilegeTpl> tpls = remoteReference.getAllTeacherTaskPrivilegeTpl();
        Map<Long, TeacherTaskPrivilegeTpl> tplsMap = tpls.stream().collect(Collectors.toMap(TeacherTaskPrivilegeTpl::getId, t -> t));
        return tplsMap;
    }

    public boolean isInitPrivilege(Long teacherId) {
        return remoteReference.isInitPrivilege(teacherId);
    }

    public MapMessage getTeacherNowAndNextPrivilege(TeacherDetail td){
        if (td == null) {
            return MapMessage.errorMessage("老师ID不能为空");
        }
        MapMessage getPrivilegeMessage = this.getPrivilege(td.getId());
        if (!getPrivilegeMessage.isSuccess()) {
            return getPrivilegeMessage;
        }
        List<TeacherTaskPrivilegeTpl> tpls = remoteReference.getAllTeacherTaskPrivilegeTpl();
        Map<Long, TeacherTaskPrivilegeTpl> tplsMap = tpls.stream().collect(Collectors.toMap(TeacherTaskPrivilegeTpl::getId, t -> t));
        TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)getPrivilegeMessage.get("teacherTaskPrivilege");

        boolean hasBirthday = false;
        if (td.getProfile() != null &&
                td.getProfile().getYear() != null && td.getProfile().getYear() > 0 &&
                td.getProfile().getMonth() != null && td.getProfile().getMonth() > 0 &&
                td.getProfile().getDay() != null && td.getProfile().getDay() > 0) {
            hasBirthday = true;
        }

        List<TeacherTaskPrivilegeMapper> nowMapper = new ArrayList<>();
        List<String> hasTplNames = new ArrayList<>();
        for (TeacherTaskPrivilege.Privilege privilege : teacherTaskPrivilege.getPrivileges()) {
            TeacherTaskPrivilegeTpl tpl = tplsMap.get(privilege.getId());
            if (null == tpl || tpl.getIsShow() == false) {//暂时下线不显示的特权，不显示
                continue;
            }
            TeacherTaskPrivilegeMapper mapper = new TeacherTaskPrivilegeMapper();
            mapper.setId(tpl.getId());
            mapper.setName(tpl.getName());
            mapper.setSubName(tpl.getSubName());
            mapper.setType(tpl.getType().toLowerCase());
            mapper.setSkip(tpl.getSkip());
            mapper.setLoop(tpl.getLoop());
            mapper.setCycleUnit(tpl.getCycleUnit());
            mapper.setTimesLimit(tpl.getTimesLimit());
            if (privilege.getTimes() != null && privilege.getTimes() > 0) {//兼容那种次数为0的特权信息
                mapper.setTimes(privilege.getTimes());
                mapper.setUseTime(privilege.getUseTimes());
                mapper.setQuantifier(tpl.getQuantifier());
            }
            mapper.setInstruction(tpl.getInstruction());
            mapper.setSort(tpl.getSort());
            if (TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId().equals(privilege.getId())) {
                if (hasBirthday) {
                    mapper.setInstruction(MessageFormat.format(tpl.getInstruction(), ""));
                } else {
                    mapper.setInstruction(MessageFormat.format(tpl.getInstruction(), "<li class=\"taskExplain\">2.您还没有填写生日信息，<span class=\"taskJump\">去填写</span></li>"));
                }
            }
            hasTplNames.add(tpl.getName());
            nowMapper.add(mapper);
        }
        Collections.sort(nowMapper, (m1, m2) -> m1.getSort() == m2.getSort() ? 0 : m1.getSort() > m2.getSort() ? 1 : -1);

        List<TeacherTaskPrivilegeMapper> nextMapper = new ArrayList<>();
        List<TeacherTaskPrivilegeTpl> privilegeByTeacherIdExcludeLevel = remoteReference.getPrivilegeByTeacherIdExcludeLevel(td.getId());
        for (TeacherTaskPrivilegeTpl tpl : privilegeByTeacherIdExcludeLevel) {
            if (hasTplNames.contains(tpl.getName())) {//已经拥有的特权，则不再暂时
                continue;
            }
            if (tpl.getIsShow() == false) {//暂时下线不显示的特权，不显示
                continue;
            }
            TeacherTaskPrivilegeMapper mapper = new TeacherTaskPrivilegeMapper();
            mapper.setId(tpl.getId());
            mapper.setName(tpl.getName());
            mapper.setSubName(tpl.getSubName());
            mapper.setType(tpl.getType().toLowerCase());
            mapper.setSkip(tpl.getSkip());
            mapper.setLoop(tpl.getLoop());
            mapper.setCycleUnit(tpl.getCycleUnit());
            mapper.setTimesLimit(tpl.getTimesLimit());
            mapper.setInstruction(tpl.getInstruction());
            mapper.setQuantifier(tpl.getQuantifier());
            if (TeacherTaskPrivilegeTpl.Privilege.BIRTHDAY_INTEGRAL.getId().equals(tpl.getId())) {
                if (hasBirthday) {
                    mapper.setInstruction(MessageFormat.format(tpl.getInstruction(), ""));
                } else {
                    mapper.setInstruction(MessageFormat.format(tpl.getInstruction(), "<li class=\"taskExplain\">2.您还没有填写生日信息，<span class=\"taskJump\">去填写</span></li>"));
                }
            }
            mapper.setSort(tpl.getSort());
            hasTplNames.add(tpl.getName());
            nextMapper.add(mapper);
        }
        Collections.sort(nextMapper, (m1, m2) -> m1.getSort() == m2.getSort() ? 0 : m1.getSort() > m2.getSort() ? 1 : -1);


        if (TeacherExtAttribute.NewLevel.SUPER.getLevel().equals(teacherTaskPrivilege.getLevel())) {
            return MapMessage.successMessage().add("now", nowMapper);
        } else {
            return MapMessage.successMessage().add("now", nowMapper).add("next", nextMapper);
        }
    }

    public void removeTeacherTaskPrivilegeUserLog(Long time) {
        remoteReference.removeTeacherTaskPrivilegeUserLog(time);
    }

    public void upsertTeacherTaskPrivilege(TeacherTaskPrivilege teacherTaskPrivilege) {
        remoteReference.upsertTeacherTaskPrivilege(teacherTaskPrivilege);
    }

}
