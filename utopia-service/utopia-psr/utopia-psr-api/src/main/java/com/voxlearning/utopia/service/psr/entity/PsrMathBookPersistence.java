package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PsrMathBookPersistence {

    private Long bookId;
    private String cname;
    /** 出版社 */
    private String press;
    /** 学期 */
    private Integer term;
    /** units */
    private Map<Long, PsrMathUnitPersistence> mathUnitPersistenceMap;
    /** 记录unit的学习顺序 */
    private List<Long> mathUnitList;

    public PsrMathBookPersistence() {
        mathUnitPersistenceMap = new HashMap<>();
        mathUnitList = new ArrayList<>();
    }

    public PsrMathUnitPersistence getUnitPersistenceByUnitId(Long unitId) {
        if (mathUnitPersistenceMap.containsKey(unitId)) return mathUnitPersistenceMap.get(unitId);
        return null;
    }

    // 调试使用
    public String formatToString() {
        String retStr = "\n bookId:" + bookId.toString() + " cname:" + cname + " press:" + press + " term:" + term.toString() + "\n";
        for (Long unit : mathUnitList) {
            if (mathUnitPersistenceMap.containsKey(unit))
                retStr += mathUnitPersistenceMap.get(unit).formatToString() + "\n";
        }

        retStr += "\n";
        return retStr;
    }
}
