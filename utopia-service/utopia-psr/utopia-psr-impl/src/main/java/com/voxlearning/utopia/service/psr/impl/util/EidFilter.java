package com.voxlearning.utopia.service.psr.impl.util;


import lombok.extern.slf4j.Slf4j;

/*
 * 先初始化过滤条件
 * initBookEids
 *
 */

// fixme 由PsrExamEnFilter代替

@Slf4j
@Deprecated
public class EidFilter {
/*
    @Getter @Setter private PsrExamContext psrExamContext;

    / *
     * 记录某book 对应的Eid-list,用于判断 题目是否超纲
     * todo 改成 HashMap
     * /
    @Getter private List<String> bookEids;
    @Getter private AboveLevelBookUnitEids bookUnitEids;
    @Getter private AboveLevelBookUnitEids bookGroupIdEids;

    public EidFilter() {
        if (bookEids == null)
            bookEids = new ArrayList<>();
        if (bookUnitEids == null)
            bookUnitEids = new AboveLevelBookUnitEids();
        if (bookGroupIdEids == null)
            bookGroupIdEids = new AboveLevelBookUnitEids();
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    / *
     * 过滤 超纲 和 已经做对过的题
     * 根据 predictRate\weightet 计算权重newWeight, 学生能力不同 计算方法不同
     * predictRate: 预估通过率
     * weightet: 该题题型在本地区热度
     * 高能力：irt-theta > 1.5  , 从小往大选择（选难策略）：依据weight*（1-weightet）进行排序选择
     * 中能力：irt-theta [0~1.5], 从中位数往两边选择（平衡策略）：依据weight*weightet进行排序选择,暂时不用中位数,用list中间位置
     * 低能力：irt-theta < 0    , 从大往小选择（选易策略）：依据weight*weightet进行排序选择
     *
     * ekCount : Ek 对应取题个数
     * /
    public List<Map.Entry<Double, EidItem>> getEidsPredictRate(EkToEidContent ekToEidContent, List<String> recommendEids, int ekCount) {

        if (psrExamContext == null)
            return null;

        UserExamContent userExamContent = psrExamContext.getUserExamContentId();

        // 获取 该用户 做题记录
        PsrUserHistoryEid psrUserHistoryEid = null; //psrExamContext.getPsrExamEnData().getPsrUserHistoryEid(psrExamContext);

        Map<Double, EidItem> outOfRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> rightRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> aboveRangeMap = new LinkedHashMap<>();
        Map<Double, EidItem> allRangeMap = new LinkedHashMap<>();   // 所有的题

        int notAboveLevelEids = 0; // 不超纲 eid-list, 用于记录日志

        if (ekToEidContent == null)
            return null;

        double weightet = 0.0;
        double predictRate = 0.0;
        double newWeight = 0.0;

        for (int i = 0; !ekToEidContent.isEidListNull() && i < ekToEidContent.getEidList().size(); i++) {
            weightet = 0.0;
            predictRate = 0.0;
            newWeight = 0.0;

            EidItem eidItem = ekToEidContent.getEidList().get(i);

            if (eidItem == null)
                continue;

            // 已经存在推题列表的题 不重复推
            if (recommendEids != null && recommendEids.contains(eidItem.getEid()))
                continue;

            // 提取代码 获取predictRate 和newWeight，只要传入 eid 即可算出
            // 分为 有Uc 和 无Uc两种
            // 最终形成eid列表

            if (userExamContent.getUserInfoLevel() == 1) {
                double ucW = 0.5;
                newWeight = userExamContent.getUc() * ucW + eidItem.getAccuracyRate() * (1 - ucW);
                // 输出权重
                eidItem.setPredictRate(newWeight);
            } else if (userExamContent.getUserInfoLevel() == 2) {
                newWeight = eidItem.getAccuracyRate();
                // 输出权重
                eidItem.setPredictRate(newWeight);
            } else {
                predictRate = PsrIrtPredictEx.predictEx(userExamContent.getIrtTheta(), eidItem.getAccuracyRate(), eidItem.getIrtA(), eidItem.getIrtB());
                // 输出权重
                eidItem.setPredictRate(predictRate);

                EtRegionItem etRegionItem = null; // fixme PsrEtRegions.getEtRegionItemByEt(eidItem.getEt(), userExamContent.getRegionCode());

                if (etRegionItem != null)
                    weightet = etRegionItem.getHotLevel();

                newWeight = predictRate * weightet;
                if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta())
                    newWeight = predictRate * (1 - weightet);
            }

            // 记录 所有的 eid列表
            allRangeMap.put(newWeight, eidItem);

            // 历史上做过的题,或者psr最近几天推荐过的题 不推
            if (isDoItRightEid(eidItem.getEid()))
                continue;

            // 超纲的题
            aboveRangeMap.put(newWeight, eidItem);
            if (isAboveLevelEid(eidItem.getEid()))
                continue;
            notAboveLevelEids++;

            // poo3 和 a001 向下(minP)扩展的时候加限度 >= minP - *
            if (userExamContent.getUserInfoLevel() >= 2)
                outOfRangeMap.put(newWeight, eidItem);
            else {
                if (eidItem.getPredictRate() >= psrExamContext.getMinP() - psrExamContext.getMinPDown())
                    outOfRangeMap.put(newWeight, eidItem);
            }

            // 难度区间 判断
            if (eidItem.getPredictRate() < psrExamContext.getMinP() || eidItem.getPredictRate() > psrExamContext.getMaxP())
                continue;

            // 题量 足够的时候 选取 本地区 题型热度 top3 的eid
            // 暂时 推迟 实现, 因数据库中是按 Et存储,如果做topN排行 需要遍历库中的所有Et数据
            // 实现逻辑,添加一个 topN热度的 eid-list, 如果该题 题型属于 topN,则加入eid-list,并且此list题量够的话,优先提取数据

            // 如果 合适的列表不是 rightRangeMap,说明过滤后的题量不够
            rightRangeMap.put(newWeight, eidItem);
        }

        Map<Double, EidItem> pMap = rightRangeMap;

        if (rightRangeMap.size() > 0)
            pMap = rightRangeMap;
        else if (outOfRangeMap.size() > 0)
            pMap = outOfRangeMap;
        else if (!psrExamContext.isPointId() && psrExamContext.isLastAdaptive() && psrExamContext.isExamEnUseAdapt()) {  // 旧知识点体系自适应算法去掉 超纲判断逻辑
            if (aboveRangeMap.size() > 0) pMap = aboveRangeMap;
            else pMap = allRangeMap;
        }

        if (notAboveLevelEids < psrExamContext.getMinNotAboveLevelEids()) {

            String strLog = "[state:aboveLevel book:" + psrExamContext.getBookId().toString() + " unit:" + psrExamContext.getUnitId().toString();
            strLog += " ek:" + ekToEidContent.getEk();
            strLog += " eidCount:" + notAboveLevelEids;
            strLog += "<" + psrExamContext.getMinNotAboveLevelEids() + "]";

            // log.info(strLog);
        }

        if (pMap.isEmpty())
            return null;

        List<Map.Entry<Double, EidItem>> retSortList = new LinkedList<>(pMap.entrySet());

        boolean desc = true;  // 默认降序

        if (userExamContent.getIrtTheta() > psrExamContext.getHighIrtTheta()) {
            // 升序
            desc = false;
        }

        final boolean finalDesc = desc;
        Collections.sort(retSortList, new Comparator<Map.Entry<Double, EidItem>>() {
            @Override
            public int compare(Map.Entry<Double, EidItem> o1, Map.Entry<Double, EidItem> o2) {
                int n = 0;
                if (o2.getKey() - o1.getKey() < 0.0) {
                    n = -1;   // 降序
                    if (!finalDesc)
                        n = 1; // 升序
                } else if (o2.getKey() - o1.getKey() > 0.0) {
                    n = 1;
                    if (!finalDesc)
                        n = -1;
                }
                return n;
            }
        });

        return retSortList;
    }

    public void initBookEids() {
        PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao = psrExamContext.getPsrAboveLevelBookEidsDao();
        if (psrAboveLevelBookEidsDao == null)
            return;

        AboveLevelBookUnitEids aboveLevelBookUnitEids = psrAboveLevelBookEidsDao.findUnitsEidsByBookId(psrExamContext.getBookId());
        if (aboveLevelBookUnitEids == null || aboveLevelBookUnitEids.getUnits() == null || aboveLevelBookUnitEids.getUnits().size() <= 0)
            return;

        this.bookEids = psrAboveLevelBookEidsDao.getEidsByBookUnitEids(aboveLevelBookUnitEids);
        this.bookUnitEids = aboveLevelBookUnitEids;

        if (!psrExamContext.isFilterByUnit())
            return;

        PsrBookPersistence psrBookPersistence = psrExamContext.getPsrBookPersistence();
        if (psrBookPersistence != null && psrBookPersistence.getBookStructure() == 1 && psrExamContext.getUnitId() > 0) {
            this.bookGroupIdEids.setId(this.bookUnitEids.getId());
            this.bookGroupIdEids.setBookid(this.bookUnitEids.getBookid());
            Long groupId = -1L;
            for (Map.Entry<Long,List<String>> entry : this.bookUnitEids.getUnits().entrySet()) {
                groupId = psrBookPersistence.getGroupIdByUnitId(entry.getKey());
                if ( !this.bookGroupIdEids.getUnits().containsKey(groupId) )
                    this.bookGroupIdEids.getUnits().put(groupId, this.bookUnitEids.getUnits().get(entry.getKey()));
                else
                    this.bookGroupIdEids.getUnits().get(groupId).addAll(this.bookUnitEids.getUnits().get(entry.getKey()));
            }
        }
    }

    / *
     * 判断是否超纲
     * true ：超纲, false: 反之
     * /
    public boolean isAboveLevelEid(String eid) {
        if (StringUtils.isEmpty(eid))
            return true;

        // fixme 第一版默认unitId=0,第二版(启用unit超纲)默认值unitId=-1 不用unit超纲, unitId>=0 为使用unit超纲过滤;
        // fixme 因默认值不同所以加入过度流程,先 unitId>0,等调用者都更新到第二版的时候,在修改成unitId>=0.
        if (psrExamContext.isFilterByUnit() && psrExamContext.getUnitId() > 0) {
            AboveLevelBookUnitEids bookUnitEidsTmp = this.bookUnitEids;
            if (psrExamContext.getPsrBookPersistence() != null && psrExamContext.getPsrBookPersistence().getBookStructure() == 1)
                bookUnitEidsTmp = this.bookGroupIdEids;

            // 根据单元过滤
            if (bookUnitEidsTmp == null || bookUnitEidsTmp.getUnits() == null)
                return false;
            if (!bookUnitEidsTmp.getUnits().containsKey(psrExamContext.getUnitId()))
                return false;
            List<String> tmpEids = bookUnitEidsTmp.getUnits().get(psrExamContext.getUnitId());
            if (tmpEids == null)
                return false;
            return (!tmpEids.contains(eid));
        }

        if (!psrExamContext.isFilterByBook() || bookEids == null || bookEids.size() <= 0)
            return false;

        return (!bookEids.contains(eid));
    }

    / *
     * 该用户 历史上做过的题 不推
     * true: 做对过(不推)，false：反之
     * 该用户 历史上做对的题 不重做,
     * /
    public boolean isDoItRightEid(String eid) {
        if (StringUtils.isEmpty(eid) || psrExamContext.getPsrUserHistoryEid() == null)
            return false;

        if (psrExamContext.getPsrUserHistoryEid().isMasterByEid(eid))
            return true;

        if (psrExamContext.getPsrUserHistoryEid().isPsrByEid(eid, psrExamContext.getEidPsrDays()))
            return true;

        return false;
    }

    */
}
