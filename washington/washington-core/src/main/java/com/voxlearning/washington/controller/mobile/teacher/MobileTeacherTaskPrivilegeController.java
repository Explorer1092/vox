package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskPrivilegeServiceClient;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;

/**
 * Created by zhouwei on 2018/9/17
 **/
@Named
@RequestMapping("/teacherMobile/teacherTask/privilege")
public class MobileTeacherTaskPrivilegeController extends AbstractMobileTeacherController {

    @Inject
    private TeacherTaskPrivilegeServiceClient teacherTaskPrivilegeServiceClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;

    @Inject
    private CouponServiceClient couponServiceClient;

    @Inject
    private CouponLoaderClient couponLoaderClient;

    /**
     * 老师特权首页
     */
    @RequestMapping("/index.vpage")
    @ResponseBody
    public MapMessage index() {
        try {
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());
            Long newExpTime = new Date().getTime();
            if (teacherExtAttribute != null && teacherExtAttribute.getInitNewExpTime() != null && teacherExtAttribute.getInitNewExpTime() > 0) {
                newExpTime = teacherExtAttribute.getInitNewExpTime();
            }
            Integer level = 1;
            if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
                level = teacherExtAttribute.getNewLevel();
            }
            Integer exp = 0;
            if (null != teacherExtAttribute && teacherExtAttribute.getExp() != null && teacherExtAttribute.getExp() > 0) {
                exp = teacherExtAttribute.getExp();
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (levelEnum == null) {
                return errorMessage("老师等级信息错误");
            }
            Integer nextLevel = levelEnum.getLevel() + 1 >= TeacherExtAttribute.NewLevel.SUPER.getLevel() ? TeacherExtAttribute.NewLevel.SUPER.getLevel() : levelEnum.getLevel() + 1;
            TeacherExtAttribute.NewLevel nextLevelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(nextLevel);

            MapMessage mapMessage = teacherTaskPrivilegeServiceClient.getTeacherNowAndNextPrivilege(td);
            if (!mapMessage.isSuccess()) {
                Boolean lock = SafeConverter.toBoolean(mapMessage.get("lock"));
                if (lock != null && lock == true) {//前端因为壳的关系，可能会连续调两次。如果是因为lock返回的错误，则不再返回了
                    return MapMessage.successMessage();
                }
                return mapMessage;
            }
            Map<String, Object> userExp = new HashMap<>();//用户的基本信息
            mapMessage.add("userExp", userExp);
            Long expireDate = newExpTime + 180 * 24 * 60 * 60 * 1000L;
            userExp.put("expireTime", DateUtils.dateToString(new Date(expireDate), DateUtils.FORMAT_SQL_DATE));
            userExp.put("level_name", levelEnum.getValue());
            userExp.put("level_id", levelEnum.getLevel());
            userExp.put("level_name_up", nextLevelEnum.getValue());
            userExp.put("level_id_up", nextLevelEnum.getLevel());
            if (levelEnum == TeacherExtAttribute.NewLevel.SUPER) {
                userExp.put("hold_need_exp", levelEnum.getMinExp());
                userExp.put("hold_need_exp_left", levelEnum.getMinExp() - exp <= 0 ? 0 : levelEnum.getMinExp() - exp);
            } else if (levelEnum == TeacherExtAttribute.NewLevel.NORMAL || levelEnum == TeacherExtAttribute.NewLevel.PRIMARY) {
                userExp.put("up_need_exp", levelEnum.getUpExp());
                userExp.put("up_need_exp_left", levelEnum.getUpExp() - exp <= 0 ? 0 : levelEnum.getUpExp() - exp);
            } else {
                userExp.put("up_need_exp", levelEnum.getUpExp());
                userExp.put("up_need_exp_left", levelEnum.getUpExp() - exp <= 0 ? 0 : levelEnum.getUpExp() - exp);
                userExp.put("hold_need_exp", levelEnum.getMinExp());
                userExp.put("hold_need_exp_left", levelEnum.getMinExp() - exp <= 0 ? 0 : levelEnum.getMinExp() - exp);
            }
            mapMessage.add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null));
            mapMessage.add("cityName", td.getCityName());
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 获取用户的等级信息
     * @return
     */
    @RequestMapping(value = "/getTeacherLevel.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getTeacherLevel() {
        try {
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());
            Integer level = 1;
            if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
                level = teacherExtAttribute.getNewLevel();
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (levelEnum == null) {
                return MapMessage.errorMessage("老师特权等级错误");
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.set("level_id", levelEnum.getLevel());
            mapMessage.set("level_name", levelEnum.getValue());
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 用户第一次进特权弹窗
     * @return
     */
    @RequestMapping(value = "/firstPopUp.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage firstPopUp() {
        try {
            String stopString = "2018-10-31";
            Date stop = DateUtils.stringToDate(stopString, DateUtils.FORMAT_SQL_DATE);
            Date now = new Date();
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }

            Integer isFirst = (Integer)CacheSystem.CBS.getCache("persistence").get("teacher_new_change_level_first_app_" + td.getId()).getValue();
            if (isFirst != null || now.getTime() > stop.getTime()) {//之前没有弹过，并且当前日期小于截止日期
                return MapMessage.successMessage().set("first", false);
            }

            CacheSystem.CBS.getCache("persistence").set("teacher_new_change_level_first_app_" + td.getId(), 60 * 24 * 60 * 60, 1);
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());
            Integer level = 1;
            if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
                level = teacherExtAttribute.getNewLevel();
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (levelEnum == null) {
                return MapMessage.errorMessage("老师特权等级错误");
            }
            MapMessage result = MapMessage.successMessage();
            result.set("level_id", levelEnum.getLevel());
            result.set("level_name", levelEnum.getValue());
            result.set("first", true);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 等级发生变化的弹窗
     * @return
     */
    @RequestMapping(value = "/levelChangePopUpWindows.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage levelChangePopUpWindows() {
        try {
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            Integer type = (Integer)CacheSystem.CBS.getCache("persistence").get("teacher_new_change_level_app_" + td.getId()).getValue();
            Boolean isUpLevel = (Boolean)CacheSystem.CBS.getCache("persistence").get("teacher_app_personal_uplevel_" + td.getId()).getValue();
            Boolean expIsAdd = (Boolean)CacheSystem.CBS.getCache("persistence").get("exp_teacher_is_add_" + td.getId()).getValue();
            CacheSystem.CBS.getCache("persistence").delete("teacher_new_change_level_app_" + td.getId());
            CacheSystem.CBS.getCache("persistence").delete("teacher_app_personal_uplevel_" + td.getId());
            CacheSystem.CBS.getCache("persistence").delete("exp_teacher_is_add_" + td.getId());
            isUpLevel = isUpLevel == null ? false : isUpLevel;
            expIsAdd = expIsAdd == null ? false : expIsAdd;
            if (type == null) {
                return MapMessage.successMessage().set("isPop", false).set("expIsAdd", expIsAdd).set("isUpLevel", isUpLevel);
            }
            MapMessage mapMessagePrivilege = teacherTaskPrivilegeServiceClient.getPrivilege(td.getId());
            if (!mapMessagePrivilege.isSuccess()) {
                return mapMessagePrivilege.set("isPop", false).set("expIsAdd", expIsAdd).set("isUpLevel", isUpLevel);
            }
            Map<Long, TeacherTaskPrivilegeTpl> allTplsMap = teacherTaskPrivilegeServiceClient.getAllTplsMap();
            TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege)mapMessagePrivilege.get("teacherTaskPrivilege");
            List<TeacherTaskPrivilege.Privilege> privileges = teacherTaskPrivilege.getPrivileges();
            List<String> privilegeName = new ArrayList<>();
            for (TeacherTaskPrivilege.Privilege privilege : privileges) {
                TeacherTaskPrivilegeTpl teacherTaskPrivilegeTpl = allTplsMap.get(privilege.getId());
                if (teacherTaskPrivilegeTpl == null) {
                    continue;
                }
                privilegeName.add(teacherTaskPrivilegeTpl.getName() + (teacherTaskPrivilegeTpl.getSubName() == null ? "" : teacherTaskPrivilegeTpl.getSubName()));
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(teacherTaskPrivilege.getLevel());
            if (levelEnum == null) {
                return MapMessage.successMessage().set("isPop", false);
            }
            MapMessage mapMessage = MapMessage.successMessage();
            mapMessage.set("isPop", true);
            mapMessage.set("expIsAdd", expIsAdd);
            mapMessage.set("isUpLevel", expIsAdd);
            mapMessage.set("privilgeName", privilegeName);
            mapMessage.set("level_id", teacherTaskPrivilege.getLevel());
            mapMessage.set("level_name", levelEnum.getValue());
            mapMessage.set("type", type);//级别调整类型1升级，-1降级，0保级
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 获取用户的券使用情况信息
     * @return
     */
    @RequestMapping(value = "/getCashCouponInfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getCashCouponInfo() {
        try {
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            Long privilegeId = getRequestLong("privilegeId");
            if (null == privilegeId || privilegeId <= 0) {
                return errorMessage("请输入特权ID");
            }
            if (privilegeId != TeacherTaskPrivilegeTpl.Privilege.CASH_COUPON_30.getId().intValue() && privilegeId != TeacherTaskPrivilegeTpl.Privilege.CASH_COUPON_80.getId().intValue()) {
                return errorMessage("该接口只能传入兑换券的特权ID");
            }
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(td.getId());
            Integer level = 1;
            if (teacherExtAttribute != null && teacherExtAttribute.getNewLevel() != null && teacherExtAttribute.getNewLevel() > 0) {
                level = teacherExtAttribute.getNewLevel();
            }
            TeacherExtAttribute.NewLevel levelEnum = TeacherExtAttribute.NewLevel.getNewLevelByLevel(level);
            if (levelEnum == null) {
                return errorMessage("老师特权等级错误");
            }
            MapMessage mapMessage = teacherTaskPrivilegeServiceClient.getPrivilege(td.getId());
            if (mapMessage.isSuccess()) {
                TeacherTaskPrivilege teacherTaskPrivilege = (TeacherTaskPrivilege) mapMessage.get("teacherTaskPrivilege");
                TeacherTaskPrivilege.Privilege privilege = teacherTaskPrivilege.getPrivileges().stream().filter(p -> p.getId().equals(privilegeId)).findFirst().orElse(null);
                if (privilege == null) {
                    return errorMessage("老师不存在该特权信息");
                }
                Integer useTimes = privilege.getUseTimes();
                if (useTimes >= 1) {
                    String refId = privilege.getPrivilegeCoupons().get(0).getCouponUserRefId();
                    CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(refId);
                    Long studentId = couponUserRef.getUserId();
                    Student student = studentLoaderClient.loadStudent(studentId);
                    return MapMessage.successMessage().set("useTimes", useTimes)
                            .add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null))
                            .add("cityName", td.getCityName())
                            .add("name", student.fetchRealname())
                            .add("id", student.getId())
                            .add("couponId", refId)
                            .add("level_id", levelEnum.getLevel())
                            .add("level_name", levelEnum.getValue());
                } else {
                    return MapMessage.successMessage().set("useTimes", 0).add("subject", Optional.ofNullable(td.getSubject()).map(Enum::name).orElse(null)).add("cityName", td.getCityName());
                }
            }
            return mapMessage;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 获取学生信息
     * @return
     */
    @RequestMapping(value = "/getCashCouponStudentInfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getCashCouponStudentInfo() {
        try {
            TeacherDetail td = currentTeacherDetail();
            if (td == null) {
                return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
            }
            String studentToken = getRequestString("studentId");
            if (StringUtils.isBlank(studentToken)) {
                return errorMessage("请输入正确的小学生号哦~");
            }
            List<User> users = userLoaderClient.loadUsers(studentToken, UserType.STUDENT);
            if (CollectionUtils.isEmpty(users)) {
                return errorMessage("请输入正确的小学生号哦~");
            }
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(users.stream().map(User::getId).collect(Collectors.toList()));
            if (CollectionUtils.isEmpty(studentDetailMap.keySet())) {
                return errorMessage("请输入正确的小学生号哦~");
            }
            StudentDetail studentDetail = studentDetailMap.values().stream().filter(s -> s.isPrimaryStudent()).findFirst().orElse(null);
            if (studentDetail == null) {
                return errorMessage("请输入正确的小学生号哦~");
            }
            return MapMessage.successMessage().add("name", studentDetail.getProfile().getRealname()).add("id", studentDetail.getId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("系统错误，请重试");
        }
    }

    /**
     * 使用券
     * @return
     */
    @RequestMapping(value = "/useCashCoupon.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage useCashCoupon() {
        TeacherDetail td = currentTeacherDetail();
        if (td == null) {
            return errorMessage("请登录老师账号").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("useCashCoupon")
                    .keys(td.getId())
                    .callback(() -> {
                        try {
                            Long privilegeId = getRequestLong("privilegeId");
                            if (null == privilegeId || privilegeId <= 0) {
                                return errorMessage("请输入老师特权ID");
                            }
                            Map<Long, TeacherTaskPrivilegeTpl> tplMap = teacherTaskPrivilegeServiceClient.getAllTeacherTaskPrivilegeTplMap();
                            TeacherTaskPrivilegeTpl tpl = tplMap.get(privilegeId);
                            if (null == tpl) {
                                return errorMessage("特权不存在");
                            }
                            if (privilegeId != TeacherTaskPrivilegeTpl.Privilege.CASH_COUPON_30.getId().intValue() && privilegeId != TeacherTaskPrivilegeTpl.Privilege.CASH_COUPON_80.getId().intValue()) {
                                return errorMessage("只支持的特权类型为家属增值产品30元代金券30与家属增值产品30元代金券80");
                            }
                            String studentToken = getRequestString("studentId");
                            if (StringUtils.isBlank(studentToken)) {
                                return errorMessage("请输入正确的小学生号哦~");
                            }
                            List<User> users = userLoaderClient.loadUsers(studentToken, UserType.STUDENT);
                            if (CollectionUtils.isEmpty(users)) {
                                return errorMessage("请输入正确的小学生号哦~");
                            }
                            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(users.stream().map(User::getId).collect(Collectors.toList()));
                            if (CollectionUtils.isEmpty(studentDetailMap.keySet())) {
                                return errorMessage("请输入正确的小学生号哦~");
                            }
                            StudentDetail studentDetail = studentDetailMap.values().stream().filter(s -> s.isPrimaryStudent()).findFirst().orElse(null);
                            if (studentDetail == null) {
                                return errorMessage("请输入正确的小学生号哦~");
                            }
                            MapMessage mapMessage = teacherTaskPrivilegeServiceClient.getPrivilegeTimes(td.getId(), privilegeId);
                            if (!mapMessage.isSuccess()) {
                                return mapMessage;
                            }
                            Integer times = (Integer) mapMessage.get("times");
                            if (times <= 0) {
                                return errorMessage("无可用无优惠券，请联系客服反馈400-160-1717");
                            }

                            MapMessage mapMessageCreateCoupon = couponServiceClient.sendCoupon(tpl.getCouponId(), studentDetail.getId(), ChargeType.TEACHER_TASK.getDescription() + tpl.getName(), SafeConverter.toString(td.getId()));
                            if (mapMessageCreateCoupon.isSuccess()) {
                                String refId = SafeConverter.toString(mapMessageCreateCoupon.get("refId"));
                                try {
                                    MapMessage consumerMapMessage = teacherTaskPrivilegeServiceClient.cousumerPrivilege(td.getId(), privilegeId, "老师任务体系3.0", refId);
                                    if (consumerMapMessage.isSuccess()) {
                                        consumerMapMessage.add("name", studentDetail.getProfile().getRealname()).add("id", studentDetail.getId());
                                    } else {//发券失败，删除券
                                        deleteCoupon(td, privilegeId, refId);
                                    }
                                    return consumerMapMessage;
                                } catch (Exception e) {//消费，删除券
                                    deleteCoupon(td, privilegeId, refId);
                                    logger.error(e.getMessage(), e);
                                    return errorMessage("代金券兑换失败，请重试!");
                                }
                            } else {
                                return errorMessage("代金券兑换失败，请重试!");
                            }

                        } catch (Exception e) {
                            logger.error("老师特权为学生发券失败, {}", e.getMessage(), e);
                            return MapMessage.errorMessage("代金券兑换失败，请重试!");
                        }
                    })
                    .build()
                    .execute();
        } catch (Exception e) {
            return MapMessage.errorMessage("正在处理中，请勿重复操作");
        }
    }

    private void deleteCoupon(TeacherDetail td, Long privilegeId, String refId) {
        List<String> refIds = new ArrayList<>();
        refIds.add(refId);
        MapMessage mapMessage = couponServiceClient.removeCouponUserRefs(refIds);//底层调用失败，删除券
        if (!mapMessage.isSuccess()) {
            logger.error("老师特权删除券关系失败, teacherId:{}, refId:{}", td.getId(), refId);
            teacherTaskPrivilegeServiceClient.pushDeleteCoupon(td.getId(), privilegeId, Arrays.asList(refId));
        }
    }
}
