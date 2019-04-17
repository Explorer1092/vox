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

package com.voxlearning.washington.controller.babel;

import com.voxlearning.washington.support.AbstractGameSupportController;

/**
 * Abstract BABEL controller implementation for providing some common utilities.
 *
 * @author Xiaohai Zhang
 * @since Oct 22, 2014
 */
abstract public class AbstractBabelController extends AbstractGameSupportController {

//    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
//
//    /**
//     * Parse request instance from specified request parameter value.
//     * Make sure parseRequest method available in specified request class.
//     */
//    @SuppressWarnings("unchecked")
//    protected final <R> R parseRequestFromParameter(String parameter, Class<R> requestClass) {
//        if (requestClass == null) {
//            throw new IllegalArgumentException("Request class must not be null");
//        }
//        String parameterValue = getRequestParameter(parameter, "");
//        try {
//            Method parseMethod = requestClass.getMethod("parseRequest", String.class);
//            return (R) parseMethod.invoke(null, parameterValue);
//        } catch (NoSuchMethodException ex) {
//            throw new IllegalStateException("Make sure requestClass " + requestClass.getName() +
//                    " has parseRequest(String) method");
//        } catch (Throwable ex) {
//            if (RuntimeMode.lt(Mode.PRODUCTION)) {
//                if (ex instanceof InvocationTargetException) {
//                    ex = ((InvocationTargetException) ex).getTargetException();
//                }
//                logger.error("FAILED TO PARSE ILLEGAL REQUEST PARAMETER TO {}: {}",
//                        requestClass.getName(), parameterValue, ex);
//            } else {
//                logger.error("FAILED TO PARSE ILLEGAL REQUEST PARAMETER TO {}: {}",
//                        requestClass.getName(), parameterValue);
//            }
//            return null;
//        }
//    }
//
//    /**
//     * Check if specified user has or not permission to play BABEL.
//     */
//    protected final boolean hasPermission(Long userId) {
//        if (userId == null) {
//            return false;
//        }
//        if (RuntimeMode.current() == Mode.STAGING) {
//            School school = asyncStudentServiceClient.getAsyncStudentService()
//                    .loadStudentSchool(userId)
//                    .getUninterruptibly();
//            if (school == null || !Objects.equals(353246L, school.getId())) {
//                logger.warn("User {} has no permission to play BABEL under STAGING environment", userId);
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//    /**
//     * Load PK role information of specified id from PK service.
//     */
//    protected final RoleInfo loadPkRole(Long roleId) {
//        if (roleId == null) {
//            return null;
//        }
//        MapMessage message = pkLoaderClient.getRoleInfos(Collections.singleton(roleId));
//        if (!message.isSuccess()) {
//            return null;
//        }
//        Object roleInfos = message.get("roleInfos");
//        if (roleInfos == null || !(roleInfos instanceof Collection)) {
//            logger.warn("No 'roleInfos' returned after PkLoaderClient.getRoleInfos invoked, " +
//                    "please contact PK developer");
//            return null;
//        }
//        return (RoleInfo) MiscUtils.firstElement((Collection) roleInfos);
//    }
//
//    protected final Integer currentStudentClazzLevel(StudentDetail student) {
//        if (student == null) {
//            student = currentStudentDetail();
//        }
//        Integer clazzLevel = student.getClazzLevelAsInteger();
//        if (clazzLevel == null) {
//            logger.error("Student {} has no clazz level", student.getId());
//            return null;
//        }
//        if (clazzLevel > ClazzLevel.SIXTH_GRADE.getLevel()) {
//            logger.warn("Student {} clazz level {} is illegal, change it to max value {}",
//                    student.getId(), clazzLevel, ClazzLevel.SIXTH_GRADE.getLevel());
//            clazzLevel = ClazzLevel.SIXTH_GRADE.getLevel();
//        }
//        return clazzLevel;
//    }
//
//    /**
//     * Load BABEL role of current user.
//     */
//    protected final BabelRole currentBabelRole() {
//        return babelLoaderClient.loadRole(currentUserId());
//    }
//
//    /**
//     * 领取指定的外部奖励
//     */
//    protected final BabelReward claimExternalReward(BabelRole role, BabelReward reward) {
//        if (role == null) {
//            logger.error("BABEL role must not be null");
//            return null;
//        }
//        if (reward == null || reward.getRewardType() == null) {
//            return null;
//        }
//        if (reward.getRewardType().isProcessInternal()) {
//            return reward;
//        }
//        switch (reward.getRewardType()) {
//            case PK_ITEM: {
//                boolean addPkSuccess;
//                try {
//                    MapMessage addEqMsg = pkServiceClient.addEquipment(role.getRoleId(), reward.getItemId(), reward.getCount());
//                    addPkSuccess = addEqMsg.isSuccess();
//                } catch (Exception e) {
//                    logger.error("", e.getMessage(), e);
//                    addPkSuccess = false;
//                }
//                if (!addPkSuccess) {
//                    logger.error("pkServiceClient.addEquipment failed.roleId{},rewardId:{},count:{}", role.getRoleId(), reward.getItemId(), reward.getCount());
//                    reward.setCount(5);
//                    reward.setRewardType(RewardType.BABEL_STAR);
//                    reward.setItemId("");
//                    babelServiceClient.useStar(role, -reward.getCount(), BabelStarChange.STAR_REWARD, "PK道具奖励失败，补偿5个星星，让用户以为奖励的就是星星");
//                }
//                break;
//            }
//            default: {
//                logger.error("Unsupported external BABEL reward type: {}", reward.getRewardType());
//                break;
//            }
//        }
//        return reward;
//    }
//
//    protected List<Question> buildExamQuestion(long bookId, String appId, int questionCount, boolean withBackup) {
//        List<Question> responseQuestion = new ArrayList<>();
//        StudentDetail sd = currentStudentDetail();
//        //PSR first
//        Set<String> pushedEids = new HashSet<>();
//        PsrExamContent psrRs = utopiaPsrServiceClient.getPsrExam(appId, "student", sd.getId(), null == sd.getCityCode() ? 0 : sd.getCityCode(), bookId, -1L, questionCount, (float) 0.7, (float) 0.85, null == sd.getClazzLevelAsInteger() ? 0 : sd.getClazzLevelAsInteger());
//        if (psrRs.getErrorContent().equals("success")) {
//            for (PsrExamItem eitem : psrRs.getExamList()) {
//                Question q = buildQuestion(eitem.getAlogv(), String.valueOf(bookId), eitem.getEid(), eitem.getEk(), eitem.getWeight());
//                responseQuestion.add(q);
//                pushedEids.add(eitem.getEid());
//            }
//        }
//        if (responseQuestion.size() >= questionCount || !withBackup) {
//            return responseQuestion;
//        }
//
//        if (responseQuestion.size() < questionCount) {
//            logger.warn("FAILED TO fetch enough english exam question,bookId {},required {},fetched {}", bookId, questionCount, responseQuestion.size());
//        }
//        return responseQuestion;
//    }
//
//    protected Question buildQuestion(String aglov, String bookId, String eid, String ek, double weight) {
//        Question q = new Question();
//        q.alogv = aglov;
//        q.bookId = bookId;
//        q.cid = String.valueOf(currentStudentDetail().getClazzId());
//        if (q.cid.equals("null")) {
//            q.cid = "";
//        }
//        q.eid = eid;
//        q.ek = ek.replace(":", "#");
//        q.weight = weight;
//        return q;
//    }
//
//    /**
//     * 根据psr推出的知识点，获取其在指定book中所对应的sentence。用于组装应用题
//     * 由于书-单元-课-知识点的数据存储既定结构，只能遍历所有单元-所有课-所有词汇类知识点
//     *
//     * @param wordList
//     * @param book
//     * @return
//     */
//    protected Map<Sentence, Unit> loadSentenceFromWordListAndBook(List<String> wordList, long book, int requerCount) {
//        Map<Sentence, Unit> allWordSentence = getAllSentenceOrdrBySentence(book);
//        Map<Sentence, Unit> rtn = new LinkedHashMap<>();
//        Map<Sentence, Unit> backup = new LinkedHashMap<>();
//        for (Map.Entry<Sentence, Unit> entry : allWordSentence.entrySet()) {
//            if (wordList.contains("word#" + entry.getKey().getEnText())) {
//                rtn.put(entry.getKey(), entry.getValue());
//                if (rtn.size() == wordList.size()) {//已经全部命中，可以退出了
//                    break;
//                }
//                //如果单词已找到对应的sentence，就删除，从而防止不同sentenceId不同但entext字段相同的sentence发给flash，从而导致flash组题出现重复选项
//                wordList.remove("word#" + entry.getKey().getEnText());
//            } else {
//                if (backup.size() < requerCount) {
//                    backup.put(entry.getKey(), entry.getValue());
//                }
//            }
//        }
//        if (rtn.size() < requerCount) {
//            for (Map.Entry<Sentence, Unit> entry : backup.entrySet()) {
//                rtn.put(entry.getKey(), entry.getValue());
//                if (rtn.size() == requerCount) {
//                    break;
//                }
//            }
//        }
//
//        return rtn;
//    }
//
//    protected GetBossBattleRankResponse fillLastTimeRankInfo(GetBossBattleRankResponse input, boolean fillLastTop) throws Exception {
//        long userId = currentUserId();
//        if (fillLastTop) {
//            input.lastTimeTopList = getFixedBossBattleTop();
//        }
//
//        input.lastTimePlayedCount = Long.toString(babelLoaderClient.countBossFightRanks());
//        BabelBossFightRank myRank = babelLoaderClient.loadBossFightRank(userId);
//        if (null == myRank) {//上次没打过BOSS战
//            return input;
//        }
//
//        input.lastTimeRank = Integer.toString(SafeConverter.toInt(myRank.getRank()));
//        input.lastTimePosition = Integer.toString(SafeConverter.toInt(myRank.getPosition()));
//        input.lastTimeMyScore = SafeConverter.toInt(myRank.getTotalScore());
//        return input;
//    }
//
//    protected GetBossBattleRankResponse fillLastTimeRankInfo(GetBossBattleRankResponse input) throws Exception {
//        return fillLastTimeRankInfo(input, false);
//    }
//
//    /**
//     * 获取最近一次完成的BOSS战top排名
//     *
//     * @return
//     */
//    protected List<BossBattleTopInfo> getFixedBossBattleTop() throws Exception {
//        String cacheKey = BossBattleTopInfo.class.getName() + "_top_" + String.valueOf(BabelConstants.MAX_BOSS_FIGHT_TOP_COUNT) + BabelBossBattleConf.getLatestFinishedSuffix().toString();
//        List<BossBattleTopInfo> fromCache = BabelCache.getBabelCache().load(cacheKey);
//        if (CollectionUtils.isNotEmpty(fromCache)) {
//            return fromCache;
//        }
//
//        Map<Long, BabelBossFightRank> rankList = babelLoaderClient.loadBossFightTopRanks();
//        if (MapUtils.isEmpty(rankList)) {
//            return Collections.emptyList();
//        }
//
//        Map<Long, User> topUser = userLoaderClient.loadUsers(new ArrayList(rankList.keySet()));
//        List<BossBattleTopInfo> rtn = new ArrayList<>(rankList.size());
//        for (Map.Entry<Long, User> entry : topUser.entrySet()) {
//            BossBattleTopInfo topInfo = new BossBattleTopInfo();
//            BabelBossFightRank rank = rankList.get(entry.getKey());
//            topInfo.score = SafeConverter.toInt(rank.getTotalScore());
//            topInfo.imgUrl = getUserAvatarImgUrl(entry.getValue().fetchImageUrl());
//            topInfo.lastTimeFinishTime = rank.getFightHistory().get(rank.getFightHistory().size() - 1).getFightFinishTime();
//            topInfo.userId = String.valueOf(rank.getUserId());
//            topInfo.userName = entry.getValue().fetchRealname();
//            rtn.add(topInfo);
//        }
//        Collections.sort(rtn, topInfoComparator);
//        BabelCache.getBabelCache().add(cacheKey, BabelCacheKey.BABEL_BOSS_BATTLE_CACHE_EXPIRE, rtn);
//        return rtn;
//    }
//
//    /**
//     * 算出给出名次在全部人中的百分比
//     *
//     * @param total
//     * @param rank
//     * @return
//     */
//    protected String calcMyPosition(final int total, final int rank) {
//        if (0 == total) {
//            return String.valueOf(99);
//        }
//
//        BigDecimal totalDecimal = new BigDecimal(String.valueOf(total) + ".00");
//        BigDecimal passedDecimal = new BigDecimal(String.valueOf(rank) + ".00");
//        BigDecimal divideRs = passedDecimal.divide(totalDecimal, BigDecimal.ROUND_UP);
//        int position = divideRs.multiply(new BigDecimal(100)).intValue();
//        if (position == 100) {
//            position--;
//        }
//
//        return String.valueOf(position);
//    }
//
//    protected Map<Unit, List<Sentence>> getAllSentenceOrdrByUnit(long bookId) {
//        List<Unit> allUnit = englishContentLoaderClient.loadEnglishBookUnits(bookId);
//        Map<Unit, List<Sentence>> mp = new LinkedHashMap<>();
//        Map<Long, Unit> unitIdMap = new LinkedHashMap<>();
//        for (Unit unit : allUnit) {
//            unitIdMap.put(unit.getId(), unit);
//        }
//        Map<Long, List<Lesson>> unitLessonMap = englishContentLoaderClient.loadEnglishUnitLessons(new ArrayList<>(unitIdMap.keySet()));
//        Map<Long, Lesson> bookAllLessonIdMap = new LinkedHashMap<>();
//        for (Map.Entry<Long, List<Lesson>> entry : unitLessonMap.entrySet()) {
//            for (Lesson ls : entry.getValue()) {
//                bookAllLessonIdMap.put(ls.getId(), ls);
//            }
//        }
//        Map<Long, List<Sentence>> lessonSentenceMap = englishContentLoaderClient.loadEnglishLessonSentences(new ArrayList<>(bookAllLessonIdMap.keySet()));
//        for (Map.Entry<Long, List<Sentence>> entry : lessonSentenceMap.entrySet()) {
//            Lesson lesson = bookAllLessonIdMap.get(entry.getKey());
//            Unit unit = unitIdMap.get(lesson.getUnitId());
//            List<Sentence> unitSentenceList = mp.get(unit);
//            if (CollectionUtils.isEmpty(unitSentenceList)) {
//                unitSentenceList = new ArrayList<>();
//                mp.put(unit, unitSentenceList);
//            }
//            for (Sentence sentence : entry.getValue()) {
//                if (null != sentence.getType()) {
//                    switch (sentence.getType().intValue()) {
//                        case 1:
//                        case 11:
//                            unitSentenceList.add(sentence);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }
//        }
//        Iterator<Map.Entry<Unit, List<Sentence>>> iter = mp.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry<Unit, List<Sentence>> next = iter.next();
//            if (CollectionUtils.isEmpty(next.getValue())) {
//                iter.remove();
//            }
//        }
//        return mp;
//    }
//
//    protected Map<Sentence, Unit> getAllSentenceOrdrBySentence(long bookId) {
//        Map<Unit, List<Sentence>> unitSentence = this.getAllSentenceOrdrByUnit(bookId);
//        if (null == unitSentence) {
//            return Collections.emptyMap();
//        }
//        Map<Sentence, Unit> rtn = new LinkedHashMap<>();
//        for (Map.Entry<Unit, List<Sentence>> entry : unitSentence.entrySet()) {
//            for (Sentence st : entry.getValue()) {
//                rtn.put(st, entry.getKey());
//            }
//        }
//        return rtn;
//    }
//
//    /**
//     * 把我的实时得分插入到排名中，形成伪实时排名
//     *
//     * @param rank
//     * @return
//     */
//    protected TreeSet<StudentClazzRankInfo> insertMeIntoRank(TreeSet<StudentClazzRankInfo> rank) {
//        BabelRole me = babelLoaderClient.loadRole(currentUserId());
//        StudentClazzRankInfo rankInfoMe = new StudentClazzRankInfo();
//        if (null != me) {
//            rankInfoMe.starCount = me.getStarCount();
//            rankInfoMe.floor = me.getFloor();
//            rankInfoMe.stageIndex = me.getStageIndex();
//        }
//
//        StudentDetail meDetail = currentStudentDetail();
//        rankInfoMe.userId = String.valueOf(currentUserId());
//        rankInfoMe.imgUrl = getUserAvatarImgUrl(meDetail.fetchImageUrl());
//        rankInfoMe.userName = meDetail.fetchRealname();
//        Iterator<StudentClazzRankInfo> iter = rank.iterator();
//        while (iter.hasNext()) {
//            if (iter.next().userId.equals(currentUserId().toString())) {
//                iter.remove();
//                break;
//            }
//
//        }
//
//        rank.add(rankInfoMe);
//        return rank;
//    }
//
//    protected List<AttackBuffInfo> getAttackBuff() {
//        if (attackBuff.isEmpty()) {
//            for (AttackType atk : AttackType.values()) {
//                for (int i = 0; i < atk.getBuff().length; i++) {
//                    attackBuff.add(new AttackBuffInfo(atk, AttackType.values()[i], atk.getBuff()[i]));
//                }
//            }
//        }
//        return attackBuff;
//    }
//
//    /**
//     * 推题数量。
//     */
//    protected int pushQuestionCount = 5;
//    protected int pushEnglishAppEkCount = 10;
//    protected BabelBossBattleComparator battleScoreComparator = new BabelBossBattleComparator();
//    /**
//     * boss战排名比较器。算法：分数降序——打成时间降序——userid升序
//     */
//    protected Comparator<BossBattleTopInfo> topInfoComparator = new Comparator<BossBattleTopInfo>() {
//        @Override
//        public int compare(BossBattleTopInfo o1, BossBattleTopInfo o2) {
//            if (o1.score != o2.score) {
//                return o2.score - o1.score;
//            }
//
//            if (o1.lastTimeFinishTime != o2.lastTimeFinishTime) {
//                return o1.lastTimeFinishTime - o2.lastTimeFinishTime;
//            }
//
//            return (int) (NumberUtils.toLong(o1.userId) - NumberUtils.toLong(o2.userId));
//        }
//
//    };
//    protected static final Integer VITALITY_CACHE_EXPIRE = 86400;
//    protected List<AttackBuffInfo> attackBuff = new ArrayList<>(16);
//
//    protected class AttackBuffInfo implements Serializable {
//        protected static final long serialVersionUID = 2603905660686052194L;
//
//        public AttackBuffInfo(AttackType attack, AttackType defense, double buff) {
//            this.attack = attack;
//            this.defense = defense;
//            this.buff = buff;
//        }
//
//        protected AttackType attack;
//        protected AttackType defense;
//        protected double buff;
//    }
}
