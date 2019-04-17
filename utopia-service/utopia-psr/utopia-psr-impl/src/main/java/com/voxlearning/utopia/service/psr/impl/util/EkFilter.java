package com.voxlearning.utopia.service.psr.impl.util;

/**
 * Created by ChaoLi lee on 14-6-28.
 * 不超出该用户所读Book的知识点范围
 */

// fixme 由PsrExamEnFilter代替

@Deprecated
public class EkFilter {
    /*
    @Getter @Setter private PsrExamContext psrExamContext;

    private List<String> eksPointRefs;
    private Map<String, List<Map.Entry<Double, String>>> ekTypeMap;

    public EkFilter() {
        psrExamContext = null;
    }

    // 用户请求 book中没有的知识点 不推荐
    public EksTypeListContent doFilter(List<UserEkContent> ekList, List<UnitKnowledgePointRef> unitKnowledgePointRefs, ExamResultsInfo examResultsInfo, Long unitId) {
        if (psrExamContext == null || ekList == null || ekList.size() <= 0 || unitKnowledgePointRefs == null)
            return null;

        EksTypeListContent eksTypeListContent = new EksTypeListContent();

        if (psrExamContext.isFilterByBook() && eksPointRefs == null) {
            // 根据unitId判断获取知识点范围
            // fixme 下次上线修改 unitId >= 0
            // 新教材结构下用groupId表示unitId
            PsrBookPersistence psrBookPersistence = null;
            if (psrExamContext != null)
                psrBookPersistence = psrExamContext.getPsrBookPersistence();
            List<Long> units = null;

            if (psrExamContext.isFilterByUnit() && unitId > 0) {
                if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1) {
                    units = psrBookPersistence.getUnitIdsByGroupId(unitId);
                    for (Long unit : units) {
                        List<String> eksTmp = PsrTools.getEksFromPointRefsByUnitId(unitKnowledgePointRefs, unit, psrExamContext.isPointId());
                        if (eksPointRefs == null)
                            eksPointRefs = new ArrayList<>();
                        if (eksTmp != null)
                            eksPointRefs.addAll(eksTmp);
                    }
                }
                else
                    eksPointRefs = PsrTools.getEksFromPointRefsByUnitId(unitKnowledgePointRefs, unitId, psrExamContext.isPointId());
            }
            else
                eksPointRefs = PsrTools.getEksFromPointRefs(unitKnowledgePointRefs, psrExamContext.isPointId());
        }
        // 根据book过滤知识点=true 并且book没有知识点 则返回
        if (psrExamContext.isFilterByBook() && (eksPointRefs == null || eksPointRefs.size() <= 0))
            return eksTypeListContent;

        List<UserEkContent> tmpAllEkList = new ArrayList<>();
        List<String> tmpEks = new ArrayList<>();
        for (UserEkContent userEkContent : ekList) {
            if (tmpEks.contains(userEkContent.getEk()))
                continue;
            tmpAllEkList.add(userEkContent);
            tmpEks.add(userEkContent.getEk());
        }

        Double tmpMaster = 0.1111111111D;
        for (String tmpEk : eksPointRefs) {
            if (tmpEks.contains(tmpEk))
                continue;
            UserEkContent tmpContent = new UserEkContent();
            tmpContent.setEk(tmpEk);
            tmpContent.setCount((short)0);
            tmpContent.setLevel((short)0);
            tmpContent.setMaster(tmpMaster);  // 默认是0.1111111111 - 0.0000000001 均等
            tmpAllEkList.add(tmpContent);
            tmpEks.add(tmpEk);
            tmpMaster -= 0.0000000001D;
        }

        for (UserEkContent userEkContent : tmpAllEkList) {
            if (psrExamContext.isFilterByBook() && !eksPointRefs.contains(userEkContent.getEk())) {
                eksTypeListContent.getEkAboveLevelList().add(userEkContent);
                eksTypeListContent.setEkAboveLevelWeightSum(
                        eksTypeListContent.getEkAboveLevelWeightSum() + userEkContent.getMaster());
                continue;
            }

            // 记录 降权集合
            if (examResultsInfo != null
                    && examResultsInfo.getEidAllCount(userEkContent.getEk()) >= psrExamContext.getEkCountByUserDone()
                    && (examResultsInfo.rightRate(userEkContent.getEk()) > psrExamContext.getDownSetRangeHigh()
                            || examResultsInfo.rightRate(userEkContent.getEk()) < psrExamContext.getDownSetRangeLow())) {
                eksTypeListContent.getEkDownWeightList().add(userEkContent);
                eksTypeListContent.setEkDownWeightSum(
                        eksTypeListContent.getEkDownWeightSum() + userEkContent.getMaster());

                // 算法修改，先剔除 需要降权的 eks,因 降权后的计算 eks 归一化算法有所改动
                continue;
            }

            switch (userEkContent.getLevel()) {
                case 0:
                    eksTypeListContent.getEkLevelZeroList().add(userEkContent);
                    eksTypeListContent.setEkLevelZeroWeightSum(
                            eksTypeListContent.getEkLevelZeroWeightSum() + userEkContent.getMaster());
                    break;
                case 1:
                    eksTypeListContent.getEkLevelOneList().add(userEkContent);
                    eksTypeListContent.setEkLevelOneWeightSum(
                            eksTypeListContent.getEkLevelOneWeightSum() + userEkContent.getMaster());
                    break;
                case 2:
                    // level = 2 真掌握, level = 3 预判掌握, 处理逻辑相同
                case 3:
                    eksTypeListContent.getEkLevelTwoList().add(userEkContent);
                    eksTypeListContent.setEkLevelTwoWeightSum(
                            eksTypeListContent.getEkLevelTwoWeightSum() + userEkContent.getMaster());
                    break;
                default:
                    break;
            }
        }

        return eksTypeListContent;
    }

    public List<Map.Entry<Double, String>> getEksSortByWeightFromMap(EksTypeListContent eksTypeListContent, String ekType) {
        if (ekTypeMap == null) {
            //ekTypeMap = new HashMap<>();
            ekTypeMap = new LinkedHashMap<>();
        }

        if (eksTypeListContent == null || StringUtils.isEmpty(ekType))
            return null;

        if (ekTypeMap.containsKey(ekType))
            return ekTypeMap.get(ekType);

        List<Map.Entry<Double, String>> list = getEksSortByWeight(eksTypeListContent, ekType);

        if (list == null)
            return null;

        ekTypeMap.put(ekType, list);

        return list;
    }

    / *
     * 将ek-list 按权重归一化 并排序
     * /
    public List<Map.Entry<Double, String>> getEksSortByWeight(EksTypeListContent eksTypeListContent, String ekType) {
        if (psrExamContext == null || eksTypeListContent == null || StringUtils.isEmpty(ekType))
            return null;

        List<UserEkContent> userEkContents = null;

        double weightSum = 0.0;

        if (ekType.equals("levelzero")) {
            weightSum = eksTypeListContent.getEkLevelZeroWeightSum();
            userEkContents = eksTypeListContent.getEkLevelZeroList();
        } else if (ekType.equals("levelone")) {
            weightSum = eksTypeListContent.getEkLevelOneWeightSum();
            userEkContents = eksTypeListContent.getEkLevelOneList();
        } else if (ekType.equals("leveltwo")) {
            weightSum = eksTypeListContent.getEkLevelTwoWeightSum();
            userEkContents = eksTypeListContent.getEkLevelTwoList();
        } else if (ekType.equals("abovelevel")) {
            weightSum = eksTypeListContent.getEkAboveLevelWeightSum();
            userEkContents = eksTypeListContent.getEkAboveLevelList();
        } else if (ekType.equals("down")) {
            weightSum = eksTypeListContent.getEkDownWeightSum();
            userEkContents = eksTypeListContent.getEkDownWeightList();
        } else
            return null;

        // 权重归一化
        //Map<Double, String> mapQueue = new HashMap<>();
        Map<Double, String> mapQueue = new LinkedHashMap<>();
        for (int i = 0; userEkContents != null && i < userEkContents.size(); i++) {
            UserEkContent userEkContent = userEkContents.get(i);
            if (userEkContent == null)
                continue;

            double tmp = 0.0;
            if (weightSum > 0)
                tmp = userEkContent.getMaster() / weightSum * psrExamContext.getBaseNumberForWeight();   // 此算法,跟 降权方式 有关

            mapQueue.put(tmp, userEkContent.getEk());
        }

        List<Map.Entry<Double, String>> eksIds = new LinkedList<Map.Entry<Double, String>>(mapQueue.entrySet());
        Collections.sort(eksIds, new Comparator<Map.Entry<Double, String>>() {
            @Override
            public int compare(Map.Entry<Double, String> o1, Map.Entry<Double, String> o2) {
                // 升序排列
                int n = 0;
                if (o2.getKey() - o1.getKey() < 0.0)
                    n = 1;
                else if (o2.getKey() - o1.getKey() > 0.0)
                    n = -1;
                return n;
            }
        });

        return eksIds;
    }
    */
}
