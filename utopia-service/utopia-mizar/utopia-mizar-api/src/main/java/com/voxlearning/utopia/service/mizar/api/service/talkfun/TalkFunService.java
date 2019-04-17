package com.voxlearning.utopia.service.mizar.api.service.talkfun;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.TalkFunCourse;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 欢拓云直播 接口
 *
 * @author yuechen.wang
 * @date 2017/01/09
 */
@ServiceVersion(version = "20170315")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TalkFunService extends IPingable {

    /**
     * 添加 OR 更新课程
     *
     * @param periodId Mizar中维护的课时ID
     * @return 失败返回失败原因，成功时 data = KT_CourseData，更新成功直接返回success
     */
    MapMessage registerCourse(String periodId);

    /**
     * 手动注册课程
     */
    MapMessage manualRegisterCourse(String periodId, String funTalkCourse);

    /**
     * 删除课程
     *
     * @param periodId Mizar中维护的课时ID
     * @return 成功 / 失败
     */
    MapMessage deleteCourse(String periodId);

    /**
     * 进入课程，根据课时维护的时间决定是进入直播还是回放
     *
     * @param periodId  Mizar中维护的课时ID
     * @param userId    进入课程的用户ID
     * @param userName  进入课程的用户名称
     * @param role      用户身份(user/admin/spadmin/guest，分别对应普通用户/管理员/超级管理员/游客)
     * @param optionMap 其它可选项，gender：性别(1为男性，2为女性)，avatar：头像链接，gid：分组ID，ssl：是否使用https(true为使用，false为不使用)
     * @return live:是否直播 liveUrl:直播地址; playbackUrl:回放地址; access_token:用户的access_token
     */
    MapMessage accessCourse(String periodId, String userId, String userName, String role, Map<String, Object> optionMap);

    /**
     * 添加主播 or 更新主播
     *
     * @param userId   Mizar中维护的老师Id
     * @param password 密码明文
     */
    MapMessage registerTeacher(String userId, String password);

    /**
     * 获取直播器启动协议
     *
     * @param periodId Mizar中维护的课时ID
     */
    MapMessage launch(String periodId);

    /**
     * 课程列表(将返回开始时间在区间内的课程)
     *
     * @param periodId Mizar中维护的课时ID
     * @param start    开始时间
     * @param end      结束时间
     * @param pager    分页参数，页码（1~100），每页个数（1~1000）
     * @param live     直播 OR 回放
     */
    MapMessage report(String periodId, Date start, Date end, Pageable pager, boolean live);

    /**
     * 获取到 课时在欢拓后台的参数
     */
    Map<String, TalkFunCourse> loadTalkFunCourses(Collection<String> periodIds);

    default TalkFunCourse loadTalkFunCourse(String periodId) {
        return loadTalkFunCourses(Collections.singletonList(periodId)).get(periodId);
    }

    /**
     * 标志欢拓课程下课
     *
     * @param courseId 欢拓课程ID
     */
    MapMessage finishClazz(String courseId);

    /**
     * 标志欢拓课程回放已经生成
     *
     * @param courseId 欢拓课程ID
     */
    MapMessage replayDone(String courseId);

    boolean checkClassFinished(String periodId);

    boolean checkHasReplay(String periodId);

    MapMessage changeTalkFunStatus(String periodId, TalkFunCourse.Status status);

    MapMessage courseEntrance(String periodId, String userId, String userName, String role, Map<String, Object> optionMap);

    /**
     * 获取欢拓Token前生成Url
     *
     * @param periodId
     * @param userId
     * @param userName
     * @param role
     * @param options
     * @return
     */
    MapMessage generateUrl(String periodId, String userId, String userName, String role, Map<String, Object> options);

    /**
     * 生成客户端需要的调起欢拓的accessKey
     * @param periodId
     * @param userId
     * @param userName
     * @param role
     * @param options
     * @return
     */
    MapMessage fetchAccessKey(String periodId, String userId, String userName, String role, Map<String, Object> options);
}
