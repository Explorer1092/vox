package com.voxlearning.utopia.admin.service.shensz;

import com.voxlearning.alps.api.common.UpdateOption;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.admin.util.Execute;
import com.voxlearning.utopia.admin.util.ShenszDB;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.GroupKlxStudentRef;
import com.voxlearning.utopia.service.user.api.entities.KlxStudent;
import com.voxlearning.utopia.service.user.api.entities.UserLoginInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ShenszService extends AbstractAdminService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoginServiceClient userLoginServiceClient;

    /**
     * 合并账户
     *
     * @param groupId
     * @param selectStudents
     * @return selectStudent: id 17ID, klxId 快乐学Id
     */
    public MapMessage mergeStudent(Long groupId, List<Map> selectStudents) {
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupId);
        Map<String, List<KlxStudent>> nameIds = klxStudents.stream().collect(Collectors.groupingBy(KlxStudent::getName));
        Map<String, List<KlxStudent>> klxIds = klxStudents.stream().collect(Collectors.groupingBy(KlxStudent::getId));

        for (Map map : selectStudents) {

            if (map.get("klxId") == null || map.get("id") == null || StringUtils.isBlank(String.valueOf(map.get("klxId")))) {
                continue;
            }

            // 需要保留的klxIdi
            String klxId = String.valueOf(map.get("klxId"));
            Long a17Id = Long.valueOf(map.get("id") + "");
            List<KlxStudent> klxStudentList = klxIds.get(klxId);
            if (klxStudentList == null || klxStudentList.isEmpty()) {
                continue;
            }
            KlxStudent klxStudent = klxStudentList.get(0);
            Long delete17Id = null;
            if (!Objects.equals(klxStudent.getA17id(), a17Id)) {
                // 解绑原账号和klxId 的关联
                newKuailexueServiceClient.clearA17id(klxId, klxStudent.getA17id());
                delete17Id = klxStudent.getA17id();
                // 绑定此klxId 和原账号Id
                newKuailexueServiceClient.updateA17id(klxId, a17Id);
            }
            List<KlxStudent> nameStudents = nameIds.get(klxStudent.getName());
            if (nameStudents == null || nameStudents.isEmpty()) {
                continue;
            }
            // 找到不需要保留的klxId
            Optional<KlxStudent> optionalStudent = nameStudents.stream().filter(m -> !Objects.equals(m.getId(), klxId)).findFirst();
            if (optionalStudent == null || optionalStudent.get() == null) {
                continue;
            }
            KlxStudent student = optionalStudent.get();
            if (delete17Id == null) {
                delete17Id = student.getA17id();
            }
            // 从班内删除不保留的klxId
            if (student.getId() != null) {
                // 解绑原账号和klxId 的关联
                newKuailexueServiceClient.clearA17id(student.getId(), a17Id);
                List<GroupKlxStudentRef> gksrList = asyncGroupServiceClient.findKlxStudentGroupRefsWithCache(student.getId());
                if (CollectionUtils.isNotEmpty(gksrList)) {
                    for (GroupKlxStudentRef gksr : gksrList) {
                        asyncGroupServiceClient.getAsyncGroupService().disableGroupKlxStudentRefs(Collections.singleton(gksr.getId()));
                        asyncUserServiceClient.getAsyncUserService().evictUserCache(gksr.getA17id()).awaitUninterruptibly();
                    }
                }
            }
            if (delete17Id == null || delete17Id == 0L) {
                continue;
            }
            // 从班内删除不保留的 17Id
            raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .getGroupStudentTupleService()
                    .dbFindByStudentIdIncludeDisabled(delete17Id)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .forEach(e -> {
                        String id = e.getId();
                        raikouSDK.getClazzClient()
                                .getGroupStudentTupleServiceClient()
                                .getGroupStudentTupleService()
                                .disable(id)
                                .awaitUninterruptibly();
                    });

            // 迁移极算测业务数据到保留的17Id
            List<Map> shenszStudents = ShenszDB.find("select id, id_17 from users where id_17 = ?", delete17Id);
            // 如果被删除的17ID在极算没有mapping
            if (shenszStudents == null || shenszStudents.isEmpty()) {
                continue;
            }
            Map shenszStudent = shenszStudents.get(0);
            Map shenszStudent1 = null;
            List<Map> shenszStudent1s = ShenszDB.find("select id, id_17 from users where id_17 = ?", a17Id);
            if (shenszStudent1s != null && !shenszStudent1s.isEmpty()) {
                shenszStudent1 = shenszStudents.get(0);
            }
            try {
                Map finalShenszStudent = shenszStudent;
                Map finalShenszStudent1 = shenszStudent1;
                ShenszDB.withTransaction(new Execute() {
                    @Override
                    public void call() throws Exception {
                        if (finalShenszStudent1 != null) {
                            executeUpdate("update users set id_17 = NULL, access_token = NULL, session_key = NULL where id_17 = ?", a17Id);
                            // 要删除的用户是否有作业
                            List<Map> papers = ShenszDB.find("select id from `student_paper` where uid = ?", finalShenszStudent1.get("id"));
                            if (papers != null && !papers.isEmpty()) {
                                executeUpdate("update `student_paper` set uid = ? where uid = ? and paper_id not in (select paper_id from (select paper_id from `student_paper` where uid = ? and create_time > '2018-04-25') c)", finalShenszStudent.get("id"), finalShenszStudent1.get("id"), finalShenszStudent.get("id"));
                                executeUpdate("update `student_answer` set user_id = ? where user_id = ? and create_time > '2018-04-25'", finalShenszStudent.get("id"), finalShenszStudent1.get("id"));
                            }
                        }
                        executeUpdate("update users set id_17 = ?, access_token = NULL, session_key = NULL where id = ?", a17Id, finalShenszStudent.get("id"));
                    }
                });
            } catch (SQLException e) {
                logger.error("shensz merge error %s", e);
                return MapMessage.errorMessage(e.getMessage());
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 返回多账号同学
     *
     * @param groupId
     * @return
     */
    public MapMessage multiStudentInfo(Long groupId) {
        // 合并组的学生，并删除不需要的组
        if (groupId == null) {
            return MapMessage.errorMessage("参数错误 groupId {}", groupId);
        }
        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return MapMessage.errorMessage("组不存在： {}", groupId);
        }
        // 拉取改组下所有的学生 并按名字分组
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupId);
        Map<String, List<KlxStudent>> studentMap = klxStudents.stream().collect(Collectors.groupingBy(u -> u.getName()));
        Map<String, List<KlxStudent>> nameStudents = new HashMap<>();
        studentMap.forEach((k, v) -> {
            if (v.size() > 1) {
                nameStudents.put(k, v);
            }
        });
        if (nameStudents.isEmpty()) {
            return MapMessage.successMessage().add("group", group);
        }
        List<KlxStudent> studentsFilter = nameStudents.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Set<Long> studentIds = studentsFilter.stream().filter(k -> k.getA17id() != null).map(KlxStudent::getA17id).collect(Collectors.toSet());
        // 查询学生的登录信息
        Map<Long, UserLoginInfo> studentLoginInfo = userLoginServiceClient.getUserLoginService().loadUserLoginInfo(studentIds).getUninterruptibly();

        // 查询学生的快乐学信息
        List<KlxStudent> klxStudentsFilter = klxStudents.stream().filter(k -> studentIds.contains(k.getA17id())).collect(Collectors.toList());
        Map<Long, List<KlxStudent>> klxMap = klxStudentsFilter.stream().collect(Collectors.groupingBy(KlxStudent::getA17id));
        // 查询扫描信息

        /**
         *
         * http://10.6.15.28:8888/klx_scan_info?usernames=klx_58b67a892ffd4b749bb6f9bf,klx_5aa3eee5177a4b7068d3a512,blablabla
         *
         * 返回：
         *
         * {
         *     "code": 0,
         *     "msg": "ok",
         *     "items": [
         *         {
         *             "username": "klx_58b67a892ffd4b749bb6f9bf",
         *             "count": 41,
         *             "latest_time": "2018-06-13 08:10:59"
         *         },
         *         {
         *             "username": "klx_5aa3eee5177a4b7068d3a512",
         *             "count": 34,
         *             "latest_time": "2018-06-05 08:38:35"
         *         },
         *         {
         *             "username": "blablabla",
         *             "count": 0,
         *             "latest_time": ""
         *         }
         *     ]
         * }
         */
        String klxids = StringUtils.join(klxStudentsFilter.stream().map(KlxStudent::getId).collect(Collectors.toSet()), ",");
        AlpsHttpResponse res = HttpRequestExecutor.defaultInstance().get("http://10.6.13.142:8888/klx_scan_info?usernames=" + klxids).execute();
        Map<String, Object> resMap = JsonUtils.fromJson(res.getResponseString());
        if (resMap == null || resMap.get("items") == null) {
            return MapMessage.errorMessage("查询快乐学扫描失败");
        }
        List<Map> items = JsonUtils.fromJsonToList(JsonUtils.toJson(resMap.get("items")), Map.class);
        Map<String, List<Map>> klxIdMap = items.stream().collect(Collectors.groupingBy(m -> String.valueOf(m.get("username"))));
        // 查询数学作业
        List<Map> mathWorks = ShenszDB.find(String.format("SELECT COUNT(a.uid) total, b.id_17 id17 , MAX(update_time) lastTime FROM student_paper a, users b WHERE a.uid = b.id and a.status != -1 and b.id_17 IN(%s) GROUP BY uid", StringUtils.join(studentIds, ",")));

        // 以17Id分组
        Map<Long, List<Map>> mathMap = mathWorks.stream().filter(m -> m.get("id17") != null).collect(Collectors.groupingBy(m -> Long.valueOf(m.get("id17") + "")));

        // 返回显示数据
        List<Map> results = new ArrayList<>();
        studentsFilter.forEach(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName());
            map.put("id", s.getA17id());
            map.put("createTime", s.getCreateTime());
            UserLoginInfo userLoginInfo = studentLoginInfo.get(s.getA17id());
            map.put("loginTime", userLoginInfo != null ? userLoginInfo.getLoginTime() : "");
            map.put("klxId", s.getId());
            map.put("scanNumber", s.getScanNumber());
            List<Map> scanInfors = klxIdMap.get(s.getId());
            if (scanInfors != null && !scanInfors.isEmpty()) {
                Map scanInfo = scanInfors.get(0);
                map.put("scanCount", scanInfo.get("count"));
                map.put("lastTime", scanInfo.get("latest_time"));
            }
            List<Map> maths = mathMap.get(s.getA17id());
            if (maths != null && !maths.isEmpty()) {
                Map math = maths.get(0);
                map.put("mathCount", math.get("total"));
                map.put("mathLastTime", math.get("lastTime"));
            }
            results.add(map);
        });
        // 按名字分组
        Map<String, List<Map>> resultMap = results.stream().collect(Collectors.groupingBy(m -> String.valueOf(m.get("name"))));
        return MapMessage.successMessage().add("results", resultMap).add("group", group);
    }

    /**
     * 恢复极算数据
     *
     * @param userId
     * @param phone
     * @return
     */
    public MapMessage recoverStudentData(Long userId, String phone, String adminName) {
        if (userId == null || phone == null) {
            return MapMessage.errorMessage("参数不正确");
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        if (studentDetail == null) {
            return MapMessage.errorMessage("学生不存在");
        }
        if (!studentDetail.isJuniorStudent()) {
            return MapMessage.errorMessage("不是中学生");
        }
        // 先查询一下 userID 在极算时候有mapping
        // 手机号加密 前3位后4位
        String mdPhone = String.format("%s****%s", phone.substring(0, 3), phone.substring(7));
        // 先查询一下 userID 在极算时候有mapping
        List<Map> oUsers = ShenszDB.find("select * from users where phone = ? and role = 3 and createdAt < '2018-04-25'", mdPhone);

        if (oUsers == null || oUsers.isEmpty()) {
            return MapMessage.errorMessage("没有找到原极算数据");
        }

        if (oUsers.size() > 1) {
            String realname = studentDetail.getProfile().getRealname();
            oUsers = oUsers.stream().filter(u -> String.valueOf(u.get("username")).contains(realname)).collect(Collectors.toList());
            if (oUsers.isEmpty()) {
                return MapMessage.errorMessage("没有找到原极算数据");
            }
            if (oUsers.size() > 1) {
                return MapMessage.errorMessage("找到多个极算用户，请联系技术处理");
            }
        }
        Map oUser = oUsers.get(0);
        if (oUser.get("id_17") != null && Objects.equals(Long.valueOf(oUser.get("id_17") + ""), userId)) {
            return MapMessage.errorMessage("没有要恢复的极算数据或者已经恢复过了");
        }
        Map nUser = null;
        List<Map> nUsers = ShenszDB.find("select * from users where id_17 = ?", userId);
        if (nUsers != null && !nUsers.isEmpty()) {
            nUser = nUsers.get(0);
        }

        // 这些操作在一个事务里面
        try {
            Map finalNUser = nUser;
            ShenszDB.withTransaction(new Execute() {
                @Override
                public void call() throws Exception {
                    // 如果17ID已经有关联，必须先去掉关联
                    if (finalNUser != null) {
                        executeUpdate("UPDATE `users` SET id_17 = NULL, access_token = NULL, session_key = NULL, updatedAt = now() WHERE id = ?", finalNUser.get("id"));
                    }
                    // 然后更新17ID到恢复的神算子ID
                    executeUpdate("UPDATE `users` SET id_17 = ?, access_token = NULL, session_key = NULL, updatedAt = now() WHERE id = ?", userId, oUser.get("id"));
                    // 更新班组关系
                    executeUpdate("UPDATE `teacher_student` SET `status` = 1 WHERE student_id=?", oUser.get("id"));
                    if (finalNUser != null) {
                        executeUpdate("UPDATE `teacher_student` SET `status` = 0 WHERE student_id=?", finalNUser.get("id"));
                    }
                    // 更新 student_paper
                    // 跟新 student_answer
                    if (finalNUser != null) {
                        List<Map> papers = ShenszDB.find("select * from `student_paper` where uid = ? and create_time > '2018-04-25'", finalNUser.get("id"));
                        // 如果新的账号已经有作业数据，也需要移动到老账号上
                        if (papers != null && !papers.isEmpty()) {
                            executeUpdate("update `student_paper` set uid = ? where uid = ? and create_time > '2018-04-25' and paper_id not in (select paper_id from (select paper_id from `student_paper` where uid = ? and create_time > '2018-04-25') c)", oUser.get("id"), finalNUser.get("id"), oUser.get("id"));
                            executeUpdate("update `student_answer` set user_id = ? where user_id = ? and create_time > '2018-04-25'", oUser.get("id"), finalNUser.get("id"));
                        }
                    }
                    executeUpdate("insert into recover_log (ouser_id, nuser_id, admin_name, create_time) value (?, ?, ?, ?)", oUser.get("id"), finalNUser == null ? 0 : finalNUser.get("id"), adminName, new Date());
                }
            });
        } catch (Exception e) {
            return MapMessage.errorMessage("服务器异常:{}", e);
        }

        return MapMessage.successMessage();
    }

    /**
     * 合并两个组的学生
     *
     * @param oid 原组
     * @param nid 目标组
     * @return
     */
    public MapMessage mergeGroup(Long oid, Long nid) {

        if (oid == null || nid == null || oid <= 0 || nid <= 0) {
            return MapMessage.errorMessage("参数错误 oid {}, nid{}", oid, nid);
        }

        Group oGroup = groupLoaderClient.getGroupLoader().loadGroup(oid).getUninterruptibly();
        if (oGroup == null) {
            return MapMessage.errorMessage("找不到组呢oid");
        }
        Group nGroup = groupLoaderClient.getGroupLoader().loadGroup(nid).getUninterruptibly();
        if (nGroup == null) {
            return MapMessage.errorMessage("找不到组呢nid");
        }

        List<GroupStudentTuple> ogsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(oid)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        if (ogsrList.isEmpty()) {
            return MapMessage.errorMessage("oid没有学生");
        }
        List<GroupStudentTuple> ngsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(nid)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        Set<Long> nStudentIds = ngsrList.stream().map(GroupStudentTuple::getStudentId).collect(Collectors.toSet());
        // 没有在要转移过去的组，把这些学生加入班级
        Set<GroupStudentTuple> oStudents = ogsrList.stream()
                .filter(s -> !nStudentIds.contains(s.getStudentId()))
                .collect(Collectors.toSet());

        if (oStudents.isEmpty()) {
            return MapMessage.errorMessage("没有需要转移的学生");
        }

        // 换到新组
        MapMessage mapMessage = groupServiceClient.moveStudentsBetweenGroup(oid, nid,
                oStudents.stream().map(GroupStudentTuple::getStudentId).collect(Collectors.toList()));
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage(mapMessage.getInfo());
        }

        // 跟新极算测的组关系, 把oid的17Id 更新为 nid
        List<Map> nGroups = ShenszDB.find("select id from `group` where group_id_17 = ?", nid);
        Map newGroup = null;
        if (nGroups != null && !nGroups.isEmpty()) {
            newGroup = nGroups.get(0);
        }

        try {
            Map finalNewGroup = newGroup;
            ShenszDB.withTransaction(new Execute() {
                @Override
                public void call() throws Exception {
                    if (finalNewGroup != null) {
                        executeUpdate("update  `group` set group_id_17 = NULL where id = ?", finalNewGroup.get("id"));
                    }
                    executeUpdate("update  `group` set group_id_17 = ?, clazz_id_17 = ? where group_id_17 = ?", nid, nGroup.getClazzId(), oid);
                }
            });
        } catch (SQLException e) {
            logger.error("mergeGroup error : %s", e);
            return MapMessage.errorMessage(e.getMessage());
        }

        // 同时 disable oid 和组内剩余的学生
        List<GroupStudentTuple> gsrList = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(oid)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(gsrList)) {
            for (GroupStudentTuple gsr : gsrList) {
                raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .disable(gsr.getId())
                        .awaitUninterruptibly();
            }
        }

        List<GroupKlxStudentRef> gksrList = asyncGroupServiceClient.findKlxGroupStudentRefsWithCache(oid);
        if (CollectionUtils.isNotEmpty(gksrList)) {
            for (GroupKlxStudentRef gksr : gksrList) {
                asyncGroupServiceClient.getAsyncGroupService().disableGroupKlxStudentRefs(Collections.singleton(gksr.getId()));
                asyncUserServiceClient.getAsyncUserService().evictUserCache(gksr.getA17id()).awaitUninterruptibly();
            }
        }

        // 去除组下的老师
        List<GroupTeacherTuple> gtrList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getGroupTeacherTupleService()
                .dbFindByGroupIdIncludeDisabled(oid)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(gtrList)) {
            for (GroupTeacherTuple gtr : gtrList) {
                raikouSDK.getClazzClient()
                        .getGroupTeacherTupleServiceClient()
                        .getGroupTeacherTupleService()
                        .disable(gtr.getId(), new UpdateOption().recordLog(true))
                        .awaitUninterruptibly();
                asyncUserServiceClient.getAsyncUserService().evictUserCache(gtr.getTeacherId()).awaitUninterruptibly();
            }
        }

        // 删除组
        asyncGroupServiceClient.getAsyncGroupService().disableGroup(oid);
        return MapMessage.successMessage();
    }
}
