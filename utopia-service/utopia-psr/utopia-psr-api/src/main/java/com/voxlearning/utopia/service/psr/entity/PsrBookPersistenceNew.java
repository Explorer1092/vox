package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2016/3/29.
 */
@Data
public class PsrBookPersistenceNew {
    private String bookId;
    private String name;
    private String seriesId; // 出版社ID
    private String status;
    private Integer subjectId;
    private Integer clazzLevel;
    private Integer latestVersion; // 版本0:老版本,1:新版本

    // 奇葩, book->module->unit->lesson 这样结构的教材, 用户传进的unitId 有可能是unitId或moduleId
    private Map<String/*unitid*/, PsrUnitPersistenceNew> unitPersistenceMap;       // todo 获取单元列表下面的API
    private Map<String/*moduleId*/, PsrUnitPersistenceNew> modulePersistenceMap;   // todo 获取单元列表下面的API
    private Map<String/*moduleId*/, List<String/*unitId*/>> moduleToUnitsMap;

    // 返回moduleId对应的unitId 列表中 最大单元的rank值
    public Map<String/*moduleId*/, Integer/*module_rank*/> getModulesRanks() {
        if (MapUtils.isEmpty(moduleToUnitsMap))
            return new HashMap<>();

        Map<String, Integer> retMap = new HashMap<>();
        for (String moduleId : moduleToUnitsMap.keySet()) {
            List<String> units = moduleToUnitsMap.get(moduleId);
            Integer rank = 1;
            for (String unitId : units) {
                if (!unitPersistenceMap.containsKey(unitId)) continue;
                if (unitPersistenceMap.get(unitId).getRank() > rank)
                    rank = unitPersistenceMap.get(unitId).getRank();
            }
            retMap.put(moduleId, rank);
        }

        return retMap;
    }

    // return true : unitId is moduleId, default:false
    public boolean isModuleId(String unitId) {
        if (MapUtils.isEmpty(modulePersistenceMap))
            return false;
        if (StringUtils.isBlank(unitId) || unitId.equals("-1"))
            return true;

        return modulePersistenceMap.containsKey(unitId)
                || !(MapUtils.isNotEmpty(unitPersistenceMap) && unitPersistenceMap.containsKey(unitId));
    }

    // 传入的 unitId用来判断是否为module推荐, todo 获取单元信息列表的时候用这个API
    public Map<String, PsrUnitPersistenceNew> getPsrUnitPersistenceMap(String unitId) {
        return isModuleId(unitId) ? modulePersistenceMap : unitPersistenceMap;
    }

    public PsrUnitPersistenceNew getUnitPersistenceByUnitId(String unitId) {
        Map<String, PsrUnitPersistenceNew> tmpMap = getPsrUnitPersistenceMap(unitId);
        if (MapUtils.isNotEmpty(tmpMap)&& tmpMap.containsKey(unitId))
            return tmpMap.get(unitId);

        return null;
    }

    public PsrUnitPersistenceNew getUnitPersistenceByUnitRankId(Integer rankId) {
        Map<String, PsrUnitPersistenceNew> tmpMap = getPsrUnitPersistenceMap("-1");
        if (MapUtils.isEmpty(tmpMap))
            return null;

        for (PsrUnitPersistenceNew p : tmpMap.values()) {
            if (p.getRank().equals(rankId))
                return p;
        }

        return null;
    }

    public List<String> getLessonIdsByBookUnit(String unitId) {
        if (StringUtils.isBlank(unitId))
            return Collections.emptyList();

        Map<String, PsrUnitPersistenceNew> psrUnitPersistenceNewMap = getPsrUnitPersistenceMap(unitId);
        if (MapUtils.isEmpty(psrUnitPersistenceNewMap))
            return Collections.emptyList();

        List<PsrUnitPersistenceNew> tmpList = new ArrayList<>();
        if (psrUnitPersistenceNewMap.containsKey(unitId))
            tmpList.add(psrUnitPersistenceNewMap.get(unitId));
        else
            tmpList.addAll(psrUnitPersistenceNewMap.values());
        if (CollectionUtils.isEmpty(tmpList))
            return Collections.emptyList();

        List<String> retList = new ArrayList<>();
        tmpList.stream().forEach(p -> retList.addAll(p.getLessonPersistenceMap().keySet()));

        return retList;
    }

    public List<Integer> getRanks() {
        List<Integer> retRanks = new ArrayList<>();
        Map<String, PsrUnitPersistenceNew> tmpMap = getPsrUnitPersistenceMap("-1");
        if (MapUtils.isEmpty(tmpMap))
            return retRanks;

        retRanks.addAll(tmpMap.values().stream().map(PsrUnitPersistenceNew::getRank).collect(Collectors.toList()));
        return retRanks;
    }

    public Integer getMaxRankId() {
        Map<String, PsrUnitPersistenceNew> tmpMap = getPsrUnitPersistenceMap("-1");
        if (MapUtils.isEmpty(tmpMap))
            return 0;

        Integer retRank = 0;
        for (PsrUnitPersistenceNew p : tmpMap.values()) {
            if (p.getRank() > retRank)
                retRank = p.getRank();
        }

        return retRank;
    }
}
