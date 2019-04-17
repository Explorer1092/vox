/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by xinxin on 1/2/2016.
 */
@Controller
@RequestMapping(value = "/site/parent")
public class SiteParentController extends SiteAbstractController {
    private final String STUDENT_PARENT_DUPLICATE_ROWS = "duplicated";
    private final String STUDENT_PARENT_DUPLICATE_KEY_PARENT = "keyparent";
    private final String STUDENT_PARENT_DUPLICATE_CONFLICT_GENDER = "gender";

    @Inject private UserManagementClient userManagementClient;

    @RequestMapping(value = "/clean.vpage", method = RequestMethod.GET)
    public String cleanDuplicatedRef() {
        return "/site/parent/clean";
    }

    //先消息重复数据,再消除重复关键家长,再消除身份性别冲突
    @RequestMapping(value = "/loadduplicated.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadDuplicatedRef() {
        MapMessage message = MapMessage.successMessage();

        Map<String, Map<String, List<Map<String, Object>>>> rows = getDuplicatedRows(100);

        if (MapUtils.isEmpty(rows)) {
            rows = getDuplicateKeyParents(100);

            if (MapUtils.isEmpty(rows)) {
                rows = getConflictGender(100);
                if (!MapUtils.isEmpty(rows)) {
                    message.add("type", STUDENT_PARENT_DUPLICATE_CONFLICT_GENDER);
                }
            } else {
                message.add("type", STUDENT_PARENT_DUPLICATE_KEY_PARENT);
            }
        } else {
            message.add("type", STUDENT_PARENT_DUPLICATE_ROWS);
        }

        return message.add("rows", rows);
    }

    //disable同一对家长孩子的重复数据
    @RequestMapping(value = "/resetrefs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage disableRef(@RequestParam String ids, @RequestParam String type) {
        if (StringUtils.isBlank(ids)) return MapMessage.errorMessage("请选择记录");

        List<String> arrIds = StringUtils.toList(ids, String.class);
        if (CollectionUtils.isEmpty(arrIds)) return MapMessage.errorMessage();

        List<String> refIds = new ArrayList<>();
        Set<Long> pids = new HashSet<>();

        arrIds.forEach(id -> {
            String[] arr = id.split("_");
            refIds.add(arr[0]);
            pids.add(Long.valueOf(arr[1]));
        });

        Map<Long, List<StudentParentRef>> refMap = parentLoaderClient.loadParentStudentRefs(pids);
        if (MapUtils.isEmpty(refMap)) return MapMessage.errorMessage("未查到关联数据");

        refMap.forEach((k, v) -> {
            v.forEach(ref -> {
                if (refIds.contains(ref.getId())) {
                    if (type.equals(STUDENT_PARENT_DUPLICATE_ROWS)) {
                        parentServiceClient.disableStudentParentRef(ref);

                        // 记录 UserServiceRecord
                        UserServiceRecord userServiceRecord = new UserServiceRecord();
                        userServiceRecord.setUserId(ref.getStudentId());
                        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                        userServiceRecord.setOperationContent("解除学生与家长的重复关联");
                        userServiceRecord.setComments("学生:" + ref.getStudentId() + ", 家长:" + ref.getParentId());
                        userServiceClient.saveUserServiceRecord(userServiceRecord);

                    } else if (type.equals(STUDENT_PARENT_DUPLICATE_KEY_PARENT)) {
                        ref.setKeyParentFlag(0);
                        parentServiceClient.updateStudentParentRef(ref.getId(), ref);

                        // 记录 UserServiceRecord
                        UserServiceRecord userServiceRecord = new UserServiceRecord();
                        userServiceRecord.setUserId(ref.getStudentId());
                        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                        userServiceRecord.setOperationContent("解除学生与重复的关键家长关联");
                        userServiceRecord.setComments("学生:" + ref.getStudentId() + ", 家长:" + ref.getParentId());
                        userServiceClient.saveUserServiceRecord(userServiceRecord);

                    } else if (type.equals(STUDENT_PARENT_DUPLICATE_CONFLICT_GENDER)) {
                        ref.setCallName("");
                        parentServiceClient.updateStudentParentRef(ref.getId(), ref);

                        // 记录 UserServiceRecord
                        UserServiceRecord userServiceRecord = new UserServiceRecord();
                        userServiceRecord.setUserId(ref.getStudentId());
                        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                        userServiceRecord.setOperationContent("解除学生与家长性别冲突的身份");
                        userServiceRecord.setComments("学生:" + ref.getStudentId() + ", 家长:" + ref.getParentId());
                        userServiceClient.saveUserServiceRecord(userServiceRecord);
                    }
                }
            });
        });

        return MapMessage.successMessage();
    }

    //取同一对学生与家长的重复数据
    private Map<String, Map<String, List<Map<String, Object>>>> getDuplicatedRows(Integer count) {
        String sql = "select REF.ID,REF.PARENT_ID,REF.STUDENT_ID,CALL_NAME,KEY_PARENT,CREATETIME,UPDATETIME FROM VOX_STUDENT_PARENT_REF REF, (SELECT PARENT_ID,STUDENT_ID FROM VOX_STUDENT_PARENT_REF WHERE DISABLED=FALSE \n" +
                "  GROUP BY PARENT_ID,STUDENT_ID HAVING COUNT(1)>1 LIMIT " + count + ") TB WHERE REF.DISABLED=FALSE AND REF.PARENT_ID=TB.PARENT_ID AND REF.STUDENT_ID=TB.STUDENT_ID";

        List<Map<String, Object>> resultRows = utopiaSql.withSql(sql).queryAll();
        if (CollectionUtils.isEmpty(resultRows)) return new HashMap<>();

        resultRows.forEach(m -> {
            m.put("reason", "数据重复");
        });

        Map<String, Map<String, List<Map<String, Object>>>> rows = groupDuplicatedRowsBy_ParentId_StudentId(resultRows);

        filterDuplicatedRowsBy_ParentId_StudentId(rows);

        return rows;
    }

    //取同一个学生的重复关键家长
    private Map<String, Map<String, List<Map<String, Object>>>> getDuplicateKeyParents(Integer count) {
        String sql = "SELECT " +
                "  REF.ID, " +
                "  REF.PARENT_ID, " +
                "  REF.STUDENT_ID, " +
                "  CALL_NAME, " +
                "  KEY_PARENT ,CREATETIME,UPDATETIME" +
                "  FROM VOX_STUDENT_PARENT_REF REF, (SELECT" +
                "                                    STUDENT_ID " +
                "                                  FROM VOX_STUDENT_PARENT_REF " +
                "                                  WHERE KEY_PARENT = TRUE AND DISABLED = FALSE " +
                "                                  GROUP BY STUDENT_ID " +
                "                                  HAVING COUNT(1) > 1 " +
                "                                  LIMIT " + count + ") TB " +
                "  WHERE REF.STUDENT_ID = TB.STUDENT_ID AND REF.DISABLED=FALSE AND REF.KEY_PARENT=TRUE";
        List<Map<String, Object>> resultRows = utopiaSql.withSql(sql).queryAll();
        if (CollectionUtils.isEmpty(resultRows)) return new HashMap<>();

        resultRows.forEach(m -> {
            m.put("reason", "关键家长重复");
        });

        Map<String, Map<String, List<Map<String, Object>>>> rows = groupDuplicatedRowsBy_StudentId_ParentId(resultRows);

        filterDuplicatedRowsBy_StudentId(rows);

        return rows;
    }

    //取身份性别冲突的家长
    private Map<String, Map<String, List<Map<String, Object>>>> getConflictGender(Integer count) {
        //step1:先查家长ID
        String sql = "SELECT TB.PARENT_ID FROM (SELECT MALE.PARENT_ID FROM (\n" +
                "  SELECT DISTINCT (PARENT_ID)\n" +
                "  FROM VOX_STUDENT_PARENT_REF\n" +
                "  WHERE DISABLED=FALSE AND CALL_NAME IS NOT NULL  AND CALL_NAME IN ('爸爸', '爷爷', '外公')\n" +
                ") AS MALE\n" +
                "INNER JOIN (\n" +
                "    SELECT DISTINCT (PARENT_ID)\n" +
                "    FROM VOX_STUDENT_PARENT_REF\n" +
                "    WHERE DISABLED=FALSE AND CALL_NAME IS NOT NULL AND CALL_NAME IN ('妈妈', '奶奶', '外婆')\n" +
                "    ) AS FAMALE\n" +
                "  ON MALE.PARENT_ID=FAMALE.PARENT_ID) TB LIMIT " + count;

        List<Map<String, Object>> parents = utopiaSql.withSql(sql).queryAll();
        if (CollectionUtils.isEmpty(parents)) return new HashMap<>();

        //step2:查详细记录
        List<Long> parentIds = new ArrayList<>();
        parents.forEach(p -> {
            parentIds.add((Long) p.get("PARENT_ID"));
        });

        String parentIdStr = StringUtils.join(parentIds, ",");
        sql = "SELECT ID,PARENT_ID,STUDENT_ID,KEY_PARENT,CALL_NAME,CREATETIME,UPDATETIME FROM VOX_STUDENT_PARENT_REF WHERE DISABLED=FALSE AND CALL_NAME IS NOT NULL AND CALL_NAME <>'' AND CALL_NAME<>'其它监护人' AND PARENT_ID IN (" + parentIdStr + ")";
        parents = utopiaSql.withSql(sql).queryAll();

        if (CollectionUtils.isEmpty(parents)) return new HashMap<>();

        parents.forEach(m -> m.put("reason", "身份性别冲突"));

        Map<String, Map<String, List<Map<String, Object>>>> rows = groupDuplicatedRowsBy_ParentId_StudentId(parents);

        filterDuplicatedRowsBy_CallName(rows);

        return rows;
    }

    //按学生分组
    private Map<String, Map<String, List<Map<String, Object>>>> groupDuplicatedRowsBy_StudentId_ParentId(List<Map<String, Object>> duplicatedRows) {
        Map<String, Map<String, List<Map<String, Object>>>> rows = new HashMap<>();
        duplicatedRows.forEach(r -> {
            if (rows.containsKey(r.get("STUDENT_ID").toString())) {
                Map<String, List<Map<String, Object>>> parentMap = rows.get(r.get("STUDENT_ID").toString());
                if (parentMap.containsKey(r.get("PARENT_ID").toString())) {
                    List<Map<String, Object>> lst = parentMap.get(r.get("PARENT_ID").toString());
                    lst.add(r);
                } else {
                    List<Map<String, Object>> lst = new ArrayList<>();
                    lst.add(r);
                    parentMap.put(r.get("PARENT_ID").toString(), lst);
                }
            } else {
                List<Map<String, Object>> lst = new ArrayList<>();
                lst.add(r);

                Map<String, List<Map<String, Object>>> parentMap = new HashMap<>();
                parentMap.put(r.get("PARENT_ID").toString(), lst);

                rows.put(r.get("STUDENT_ID").toString(), parentMap);
            }
        });


        return rows;
    }

    //按家长/学生分组
    private Map<String, Map<String, List<Map<String, Object>>>> groupDuplicatedRowsBy_ParentId_StudentId(List<Map<String, Object>> duplicatedRows) {
        Map<String, Map<String, List<Map<String, Object>>>> rows = new HashMap<>();

        duplicatedRows.forEach(r -> {
            if (rows.containsKey(r.get("PARENT_ID").toString())) {
                Map<String, List<Map<String, Object>>> studentMap = rows.get(r.get("PARENT_ID").toString());
                if (studentMap.containsKey(r.get("STUDENT_ID").toString())) {
                    List<Map<String, Object>> lst = studentMap.get(r.get("STUDENT_ID").toString());
                    lst.add(r);
                } else {
                    List<Map<String, Object>> lst = new ArrayList<>();
                    lst.add(r);
                    studentMap.put(r.get("STUDENT_ID").toString(), lst);
                }
            } else {
                List<Map<String, Object>> lst = new ArrayList<>();
                lst.add(r);

                Map<String, List<Map<String, Object>>> studentMap = new HashMap<>();
                studentMap.put(r.get("STUDENT_ID").toString(), lst);

                rows.put(r.get("PARENT_ID").toString(), studentMap);
            }
        });
        return rows;
    }

    //按家长/学生分组后处理重复数据
    private void filterDuplicatedRowsBy_ParentId_StudentId(Map<String, Map<String, List<Map<String, Object>>>> rows) {
        if (MapUtils.isEmpty(rows)) return;

        rows.forEach((firstId, map) -> {
            map.forEach((secondId, lst) -> {
                sortRef(lst);

                lst.get(0).put("del", false);
            });
        });
    }

    //按学生分组后,处理重复的关键家长
    private void filterDuplicatedRowsBy_StudentId(Map<String, Map<String, List<Map<String, Object>>>> rows) {
        if (MapUtils.isEmpty(rows)) return;

        rows.forEach((sid, map) -> {

            List<Map<String, Object>> allStudentRef = new ArrayList<>();
            map.forEach((secondId, lst) -> {
                allStudentRef.addAll(lst);
            });

            sortRef(allStudentRef);

            allStudentRef.get(0).put("del", false);
        });
    }

    //按家长/学生分组后,处理家长身份性别的冲突
    private void filterDuplicatedRowsBy_CallName(Map<String, Map<String, List<Map<String, Object>>>> rows) {
        if (MapUtils.isEmpty(rows)) return;

        rows.forEach((pid, map) -> {
            List<Map<String, Object>> allParentRef = new ArrayList<>();
            map.forEach((sid, lst) -> {
                lst.forEach(ref -> {
                    if (null != ref.get("CALL_NAME") && StringUtils.isNotBlank(ref.get("CALL_NAME").toString()) && null != CallName.of(ref.get("CALL_NAME").toString())) {
                        allParentRef.add(ref);
                    }
                });
            });

            sortRef(allParentRef);

            allParentRef.get(0).put("del", false); //这里虽然是del,但其实最后只是把CALL_NAME更新成空
            if (allParentRef.size() > 1) {
                CallName rightCallName = CallName.of(allParentRef.get(0).get("CALL_NAME").toString());
                for (int i = 1; i < allParentRef.size(); i++) {
                    CallName curCallName = CallName.of(allParentRef.get(i).get("CALL_NAME").toString());
                    if (curCallName.getGender() == rightCallName.getGender()) {
                        allParentRef.get(i).put("del", false);
                    }
                }
            }
        });
    }

    //过滤重复数据,关键家长优先于普通家长,同类型的时间早的优先于时间晚的
    private void sortRef(List<Map<String, Object>> lst) {
        lst.stream().sorted((r1, r2) -> {
            if (!r1.get("KEY_PARENT").toString().equals(r2.get("KEY_PARENT").toString())) {
                if (r1.get("KEY_PARENT").toString().equals("1")) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return r1.get("UPDATETIME").toString().compareTo(r2.get("UPDATETIME").toString());
            }
        }).forEach(m -> m.put("del", true));
    }
}
