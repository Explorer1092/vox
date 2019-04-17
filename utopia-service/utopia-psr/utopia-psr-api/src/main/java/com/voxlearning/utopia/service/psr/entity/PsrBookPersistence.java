package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;

/*
 * 新教材结构中unitid意义发生了变化,并由groupId表示老结构中的unitid
 * 该类做了前后兼容
 * 在查询时候 根据bookId即可判断是否为新教材,新教材则把传进的unitid 转换为 groupId 继续查
 */

@Data
public class PsrBookPersistence {

    /** 单元ID */
    private Long bookId;
    /** 中文名称 */
    private String cname;
    /** 英文名称 */
    private String ename;
    /** 课本教材类型  教材结构 1: Book-Unit-Lesson-Part(单元测验在GROUP上) 2:Book-Module-Unit-part 3:Book-Unit-Part（湘少）*/
    private Integer bookStructure;
    /** units */
    private Map<Long/*unitid*/, PsrUnitPersistence> unitPersistenceMap;
    /** groupid(=>units) 教材结构 1: Book-Unit-Lesson-Part(单元测验在GROUP上) */
    private Map<Long/*groupid*/, PsrUnitPersistence> unitGroupIdPersistenceMap;
    /** unitid -> groupid, 提供反查数据 教材结构 1: Book-Unit-Lesson-Part(单元测验在GROUP上)*/
    private Map<Long/*unitid*/, Long/*groupid*/> unitToGroupIdMap;
    /** groupid -> unitid, 提供反查数据 教材结构 1: Book-Unit-Lesson-Part(单元测验在GROUP上)*/
    private Map<Long/*groupid*/, List<Long/*unitid*/>> groupIdToUnitIdsMap;

    public Map<Long, PsrUnitPersistence> getUnitMap() {
        if (bookStructure == 1)
            return unitGroupIdPersistenceMap;
        else
            return unitPersistenceMap;
    }

    public List<Long> getLessonIdsByBookUnit(Long bookId, Long unitId) {
        List<Long> retLessonIds = new ArrayList<>();

        Map<Long/*groupid*/, PsrUnitPersistence> unitMap = getUnitMap();
        if (unitMap == null || unitMap.size() <= 0)
            return retLessonIds;

        for (Map.Entry<Long, PsrUnitPersistence> entry : unitMap.entrySet()) {
            // unitId!=-1 说明按unit获取Lesson
            if (!unitId.equals(-1L) && unitId.equals(entry.getKey()))
                continue;
            if (entry.getValue().getLessonPersistenceMap() == null)
                continue;
            for (Long lessonId : entry.getValue().getLessonPersistenceMap().keySet()) {
                if (!retLessonIds.contains(lessonId))
                    retLessonIds.add(lessonId);
            }
        }

        return retLessonIds;
    }

    public PsrUnitPersistence getUnitPersistenceByUnitId(Long unitId) {
        if (bookStructure == 1) {
            // 教材结构 1: Book-Unit-Lesson-Part(单元测验在GROUP上), 根据传近来的UnitId反推出GroupId 在进行查询
            Long groupId = getGroupIdByUnitId(unitId);
            if (unitGroupIdPersistenceMap != null && unitGroupIdPersistenceMap.containsKey(groupId))
                return unitGroupIdPersistenceMap.get(groupId);
        } else {
            if (unitPersistenceMap != null && unitPersistenceMap.containsKey(unitId))
                return unitPersistenceMap.get(unitId);
        }

        return null;
    }

    public Long getGroupIdByUnitId(Long unitId) {
        if (unitToGroupIdMap != null && unitToGroupIdMap.containsKey(unitId))
            return unitToGroupIdMap.get(unitId);

        return -1L;
    }

    public List<Long> getUnitIdsByGroupId(Long groupId) {
        if (groupIdToUnitIdsMap != null && groupIdToUnitIdsMap.containsKey(groupId))
            return groupIdToUnitIdsMap.get(groupId);

        return Collections.emptyList();
    }
}

