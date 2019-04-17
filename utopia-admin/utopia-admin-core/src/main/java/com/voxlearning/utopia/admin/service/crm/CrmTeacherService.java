/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.entity.ucenter.CertificationApplication;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午12:15,13-11-22.
 */
@Named
public class CrmTeacherService extends AbstractAdminService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private CertificationServiceClient certificationServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;

    /**
     * 查询老师换班历史
     */
    public List<Map<String, Object>> getChangeClazzHistoryList(Long teacherId) {
        if (teacherId == null || teacherId == 0)
            return Collections.emptyList();

        List<Map<String, Object>> retList = new ArrayList<>();
        List<GroupTeacherTuple> gtrList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getGroupTeacherTupleService()
                .dbFindByTeacherIdIncludeDisabled(teacherId)
                .getUninterruptibly();
        Set<Long> groupIds = gtrList.stream().map(GroupTeacherTuple::getGroupId).collect(Collectors.toSet());
        Map<Long, Group> groupInfo = groupLoaderClient.getGroupLoader().loadGroupsIncludeDisabled(groupIds).getUninterruptibly();

        Set<Long> clazzIds = groupInfo.values().stream().map(Group::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzInfo = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzsIncludeDisabled(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        for (GroupTeacherTuple gtr : gtrList) {
            Group group = groupInfo.get(gtr.getGroupId());
            if (group == null) {
                continue;
            }

            Clazz clazz = clazzInfo.get(group.getClazzId());
            if (clazz == null) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("updateDatetime", gtr.getUpdateTime());
            item.put("disabled", gtr.getDisabled());
            item.put("groupId", gtr.getGroupId());
            item.put("clazzId", clazz.getId());
            item.put("clazzName", clazz.formalizeClazzName());

            retList.add(item);
        }

        retList.sort((o1, o2) -> {
            boolean cl1 = SafeConverter.toBoolean(o1.get("disabled"));
            boolean cl2 = SafeConverter.toBoolean(o2.get("disabled"));
            if (Boolean.compare(cl1, cl2) != 0) {
                return Boolean.compare(cl1, cl2);
            }
            Date cn1 = SafeConverter.toDate(o1.get("updateDatetime"));
            Date cn2 = SafeConverter.toDate(o2.get("updateDatetime"));
            return cn2.compareTo(cn1);
        });

        return retList;
    }

    /**
     * 查询班级任课历史
     */
    public List<Map<String, Object>> getChangeTeacherHistoryList(Long groupId) {
        if (groupId == null || groupId == 0)
            return Collections.emptyList();

        List<Map<String, Object>> retList = new ArrayList<>();
        List<GroupTeacherTuple> gtrList = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .getGroupTeacherTupleService()
                .dbFindByGroupIdIncludeDisabled(groupId)
                .getUninterruptibly();
        Set<Long> teacherIds = gtrList.stream().map(GroupTeacherTuple::getTeacherId).collect(Collectors.toSet());
        Map<Long, User> userInfo = userLoaderClient.loadUsers(teacherIds);
        for (GroupTeacherTuple gtr : gtrList) {
            if (!userInfo.containsKey(gtr.getTeacherId())) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("updateDatetime", gtr.getUpdateTime());
            item.put("disabled", gtr.getDisabled());
            item.put("teacherId", gtr.getTeacherId());
            item.put("teacherName", userInfo.get(gtr.getTeacherId()).fetchRealname());

            retList.add(item);
        }

        retList.sort((o1, o2) -> {
            boolean cl1 = SafeConverter.toBoolean(o1.get("disabled"));
            boolean cl2 = SafeConverter.toBoolean(o2.get("disabled"));
            if (Boolean.compare(cl1, cl2) != 0) {
                return Boolean.compare(cl1, cl2);
            }
            Date cn1 = SafeConverter.toDate(o1.get("updateDatetime"));
            Date cn2 = SafeConverter.toDate(o2.get("updateDatetime"));
            return cn2.compareTo(cn1);
        });

        return retList;
    }

    //删除老师的认证申请记录，只能删除等待认证状态下的申请记录
    public boolean deleteTeacherCertificationApplication(Long teacherId) {
        if (teacherId <= 0) {
            throw new RuntimeException("TeacherId is not valid");
        }
        User user = userLoaderClient.loadUser(teacherId);
        if (null != user && user.getAuthenticationState() != AuthenticationState.WAITING.getState()) {
            throw new RuntimeException("只能删除等待认证状态下的认证申请记录");
        }
        CertificationApplication certificationApplication = certificationServiceClient.getRemoteReference()
                .findCertificationApplication(teacherId)
                .getUninterruptibly();
        if (null == certificationApplication) {
            throw new RuntimeException("要删除的认证申请记录已不存在");
        }
        if (certificationApplication.fetchCertificationState() != AuthenticationState.WAITING) {
            throw new RuntimeException("只能删除等待认证状态下的认证申请记录");
        }
        return certificationServiceClient.getRemoteReference()
                .deleteCertificationApplication(teacherId)
                .getUninterruptibly();
    }

    /**
     * 判断老师是否可以绑定手机
     */
    public boolean canBindMobile(Long teacherId) {
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(teacherId);
        return userAuthentication == null || !userAuthentication.isMobileAuthenticated();
    }

    public Page<PossibleCheatingHomework> pageGetByDateRange(DateRange range, Pageable pageable) {
        return newHomeworkLoaderClient.pageFindPossibleCheatingHomeworkByDateRange(range, pageable);
    }

    public PossibleCheatingHomework loadPossibleCheatingHomework(String cheatId) {
        if (StringUtils.isEmpty(cheatId)) {
            return null;
        }
        return newHomeworkLoaderClient.loadPossibleCheatingHomeworkById(cheatId);
    }

    public void updatePossibleCheatingHomeworkIntegral(String id) {
        newHomeworkServiceClient.updatePossibleCheatingHomeworkIntegral(id);
    }


//    //读取完成作业列表
//    public List<StudentHomeworkAccomplishment> getHomeworkAccomplishmentList(String homeworkId, HomeworkType type) {
//        if (null == homeworkId || null == type) {
//            return Collections.emptyList();
//        }
//        NewHomework newhomework = newHomeworkLoaderClient.loadNewHomeworkIncludeDisabled(homeworkId);
//        if (newhomework == null) {
//            return Collections.emptyList();
//        }
//        NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(newhomework.toLocation());
//        List<StudentHomeworkAccomplishment> list = new LinkedList<>();
//        if (accomplishment != null && accomplishment.getDetails() != null) {
//            for (Map.Entry<String, NewAccomplishment.Detail> entry : accomplishment.getDetails().entrySet()) {
//                long studentId = NumberUtils.toLong(entry.getKey());
//                NewAccomplishment.Detail detail = entry.getValue();
//                StudentHomeworkAccomplishment a = new StudentHomeworkAccomplishment();
//                a.setStudentId(studentId);
//                a.setHomeworkId(homeworkId);
//                a.setHomeworkType(HomeworkType.valueOf(newhomework.getNewHomeworkType().name()));
//                a.setSubject(newhomework.getSubject());
//                a.setAccomplishTime(detail.getAccomplishTime());
//                a.setIp(detail.getIp());
//                a.setRepair(detail.getRepair());
//                list.add(a);
//            }
//        }
//        Collections.sort(list, (o1, o2) -> {
//            long a1 = o1.getAccomplishTime() == null ? 0 : o1.getAccomplishTime().getTime();
//            long a2 = o2.getAccomplishTime() == null ? 0 : o2.getAccomplishTime().getTime();
//            return Long.compare(a1, a2);
//        });
//        return list;
//    }

    /**
     * 加载老师名下所有学生及家长的手机绑定情况
     */
    public List<Map<String, Object>> loadStudentAndParentAuthentication(Long teacherId) {
        if (null == teacherId) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> clazzIds = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream().map(Clazz::getId).collect(Collectors.toList());
        List<User> students = userAggregationLoaderClient.loadTeacherStudentsByClazzIds(clazzIds, teacherId).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        for (User student : students) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(student.getId());
            UserAuthentication studentAuthentication = userLoaderClient.loadUserAuthentication(student.getId());

            Map<String, Object> map = new HashMap<>();
            if (clazz != null) {
                map.put("clazzName", clazz.getClassName());
                map.put("clazzId", clazz.getId());
            }
            map.put("studentId", student.getId());
            map.put("studentName", student.getProfile().getRealname());
            if (null != studentAuthentication) {
                String authenticatedMobile = sensitiveUserDataServiceClient.showUserMobile(student.getId(), "loadStudentAndParentAuthentication", SafeConverter.toString(student.getId()));
                map.put("studentBindMobile", authenticatedMobile == null ? null : authenticatedMobile);
            }

            List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
            if (parents.size() > 0) {
                for (StudentParent parent : parents) {
                    UserAuthentication parentAuthentication = userLoaderClient.loadUserAuthentication(parent.getParentUser().getId());
                    if ((null == studentAuthentication || !studentAuthentication.isMobileAuthenticated())
                            && (null == parentAuthentication || !parentAuthentication.isMobileAuthenticated())) {
                        continue; //没有绑定的数据过滤掉
                    }

                    Map<String, Object> parentMap = new HashMap<>();
                    parentMap.putAll(map);
                    parentMap.put("parentId", parent.getParentUser().getId());
                    parentMap.put("parentName", parent.getParentUser().getProfile().getRealname());

                    if (null != parentAuthentication) {
                        parentMap.put("parentBindMobile", parentAuthentication.getSensitiveMobile());
                    }
                    result.add(parentMap);
                }
            } else {
                if (null != studentAuthentication && studentAuthentication.isMobileAuthenticated()) {
                    result.add(map);
                }
            }
        }
        return result;
    }

}
