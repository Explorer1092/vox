package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilege;
import com.voxlearning.utopia.entity.task.TeacherTaskPrivilegeTpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ServiceVersion(version = "20181026")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface TeacherTaskPrivilegeService {

    /**
     * 获取老师是否拥有某个特权
     * @param teacherId
     * @param id 特权类型 TeacherTaskPrivilegeTpl.Privilege
     * @param teacherId
     * @return 获取key:isHave, true为拥有权限，false为没有
     * @author zhouwei
     */
    MapMessage isHavePrivilege(Long teacherId, Long id);

    /**
     * 获取某个特权的次数
     * @param teacherId
     * @param id 特权类型 TeacherTaskPrivilegeTpl.Privilege
     * @return 获取key:times，NULL表示不限制次数，否则为剩余的次数
     *         获取key:total，NULL表示不限制次数，否则为总次数
     * @autor zhouwei
     */
    MapMessage getPrivilegeTimes(Long teacherId, Long id);

    /**
     * 获取老师当前的特权信息
     * @return 获取key: teacherTaskPrivilege，获取老师的特权
     * @author zhouwei
     */
    MapMessage getTeacherALLPrivilege(Long teacherId);

    /**
     * 消费【一次】某个特权
     * @param teacherId 老师ID
     * @param id  特权类型 TeacherTaskPrivilegeTpl.Privilege
     * @param system 消费特权的功能，请输入相关系统的功能名即可，随意输入，但请清晰准确
     * @return
     * @author zhouwei
     */
    MapMessage cousumerPrivilege(Long teacherId, Long id, String system);

    /**
     * 消费【一次】某个特权
     * @param teacherId 老师ID
     * @param id  特权类型 TeacherTaskPrivilegeTpl.Privilege
     * @param system 消费特权的功能，请输入相关系统的功能名即可，随意输入，但请清晰准确
     * @param couponRefId 如果有券的关联ID，则传，没有，可以不传入
     * @return
     * @author zhouwei
     */
    MapMessage cousumerPrivilege(Long teacherId, Long id, String system, String couponRefId);

    /**
     * 获取所有的特权模板
     * @return
     */
    List<TeacherTaskPrivilegeTpl> getAllTeacherTaskPrivilegeTpl();

    /**
     * 删除券，写一条消息
     * @param teacherId
     * @param tplId
     * @param refIds
     */
    void pushDeleteCoupon(Long teacherId, Long tplId, List<String> refIds);

    /**
     * 获取下载课件的次数
     * @param teacherId
     * @return
     * times = null || total = null表示不限制次数
     * time表示剩余次数，total表示总次数
     */
    MapMessage getCoursewareDownloadTimes(Long teacherId);

    /**
     * 获取一起新讲堂免费观看的次数信息
     * @param teacherId
     * @return
     * times = null || total = null表示不限制次数
     * time表示剩余次数，total表示总次数
     */
    MapMessage get17ClassTimes(Long teacherId);

    /**
     * 老师特权是否初始化了
     * @param teacherId
     * @return false，没有初始化， true 初始化了
     */
    public boolean isInitPrivilege(Long teacherId);

    /**
     * 获取老师的特权西信息
     *
     * 注意，该方法是给任务用的，请不要调用
     *
     * @param teacherId
     * @return
     * @author zhouwei
     */
    TeacherTaskPrivilege getTeacherTaskPrivilege(Long teacherId);

    /**
     * 获取当级别是最高级时，所有可以获取的特权
     * @param teacherId
     * @return
     */
    List<TeacherTaskPrivilegeTpl> getPrivilegeByTeacherIdExcludeLevel(Long teacherId);

    /**
     * 删除特权的使用日志记录
     * @param time
     */
    void removeTeacherTaskPrivilegeUserLog(Long time);

    default Map<Long, TeacherTaskPrivilegeTpl> getAllTeacherTaskPrivilegeTplMap() {
        return this.getAllTeacherTaskPrivilegeTpl().stream().collect(Collectors.toMap(TeacherTaskPrivilegeTpl::getId, t -> t));
    }

    /**
     * 存入特权信息
     *
     * 注意，该方法是给任务用的，请不要调用
     *
     * @param teacherTaskPrivilege
     * @author zhouwei
     */
    void upsertTeacherTaskPrivilege(TeacherTaskPrivilege teacherTaskPrivilege);
}
