package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.appalachian.org.apache.zookeeper.Op;
import com.voxlearning.utopia.service.ai.api.ChipsTaskLoader;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneLessonData;
import com.voxlearning.utopia.service.ai.data.StoneQuestionData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.data.vo.ChipsDrawingTaskVO;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsRedDotPage;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsUserRedDotPageRecord;
import com.voxlearning.utopia.service.ai.impl.context.ChipsDrawingTaskLoadContext;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishUserExtSplitDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.reddot.ChipsRedDotPagePersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.reddot.ChipsUserRedDotRecordPagePersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskJoinPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.drawingtask.ChipsDrawingTaskLoadProcessor;
import com.voxlearning.utopia.service.ai.impl.support.UserInfoSupport;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ChipsTaskLoader.class)
public class ChipsTaskLoaderImpl implements ChipsTaskLoader {

    @Inject
    private ChipsUserDrawingTaskPersistence chipsUserDrawingTaskPersistence;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsUserDrawingTaskJoinPersistence chipsUserDrawingTaskJoinPersistence;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private AIUserVideoDao aiUserVideoDao;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    private ChipsDrawingTaskLoadProcessor chipsDrawingTaskLoadProcessor;

    @Inject
    private ChipsRedDotPagePersistence chipsRedDotPagePersistence;

    @Inject
    private ChipsUserRedDotRecordPagePersistence chipsUserRedDotRecordPagePersistence;

    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @AlpsQueueProducer(queue = "utopia.chips.share.video.count.queue")
    private MessageProducer videoMessageProducer;

    private static String PAGE_DRAWING_TASK_TIP_CODE = "drawing_task_tip";

    private static Set<LessonType> VIDEO_LESSON_TYPES = Arrays.asList(LessonType.Dialogue, LessonType.video_conversation, LessonType.role_play_lesson, LessonType.mock_test_lesson_2)
            .stream().collect(Collectors.toSet());

    @Override
    public MapMessage loadMyDrawingTask(Long userId, Long drawingTaskId) {
        ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.load(drawingTaskId);
        if (task == null || !task.getUserId().equals(userId)) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到任务");
        }

        StoneUnitData unitData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(task.getUnitId())))
                .map(map -> map.get(task.getUnitId()))
                .map(StoneUnitData::newInstance)
                .orElse(null);

        if (unitData == null) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到单元");
        }

        StoneQuestionData questionData = Optional.ofNullable(unitData)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getReward_illust_id)
                .map(e -> {
                    Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e));
                    if (stoneDataMap != null) {
                        return stoneDataMap.get(e);
                    }
                    return null;
                })
                .map(StoneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到图鉴");
        }
        //TODO IOS的bug导致需要将升级后的内容改为升级前的
        if (task.fetchStatus() != ChipsUserDrawingTaskStatus.finished) {
            questionData.getJsonData().put("reward_pic_after_back", SafeConverter.toString(questionData.getJsonData().get("reward_pic_before_back")));
            questionData.getJsonData().put("reward_pic_after_front", SafeConverter.toString(questionData.getJsonData().get("reward_pic_before_front")));
        }

        String lessonId = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(task.getBookId())))
                .map(e -> e.get(task.getBookId()))
                .map(StoneBookData::newInstance)
                .map(StoneBookData::getJsonData)
                .map(StoneBookData.Book::getChildren)
                .map(unitNodes -> unitNodes.stream().filter(node -> node.getStone_data_id().equals(task.getUnitId())).findFirst().orElse(null))
                .map(unit -> {
                    Set<String> lessonIds = unit.getChildren().stream().map(StoneBookData.Node::getStone_data_id).collect(Collectors.toSet());
                    StoneLessonData lessonData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(lessonIds).values().stream()
                            .map(StoneLessonData::newInstance)
                            .filter(lesson -> VIDEO_LESSON_TYPES.contains(lesson.getJsonData().getLesson_type()))
                            .findFirst().orElse(null);
                    if (lessonData != null) {
                        return lessonData.getId();
                    }
                    return "";
                })
                .orElse("");

        boolean read = Optional.ofNullable(chipsRedDotPagePersistence.loadByCode(PAGE_DRAWING_TASK_TIP_CODE))
                .map(page -> {
                    List<ChipsUserRedDotPageRecord> userRecordList = chipsUserRedDotRecordPagePersistence.loadByUser(userId);
                    if (CollectionUtils.isEmpty(userRecordList)) {
                        chipsUserRedDotRecordPagePersistence.insertOrUpdate(userId, page.getId(), true);
                        return true;
                    }
                    ChipsUserRedDotPageRecord record = userRecordList.stream().filter(e -> e.getPage().equals(page.getId())).findFirst().orElse(null);
                    if (record != null) {
                        return false;
                    }
                    chipsUserRedDotRecordPagePersistence.insertOrUpdate(userId, page.getId(), true);
                    return true;
                }).orElse(true);

        int gain = chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId()).stream().mapToInt(ChipsUserDrawingTaskJoin::getEnergy).sum();
        return MapMessage.successMessage().set("drawingTaskId", task.getId())
                .set("totalEnergy", task.getTotalEnergy())
                .set("gainEnergy", gain)
                .set("unitName", unitData.getJsonData().getName())
                .set("unitId", unitData.getId())
                .set("lessonId", lessonId)
                .set("bookId", task.getBookId())
                .set("userName", "")
                .set("tipPop", read)
                .set("jsonData", questionData.getJsonData())
                .set("status", task.getStatus());
    }

    @Override
    public MapMessage loadDrawingTabList(Long userId, String labelCode, int page, int pageSize) {
        ChipsDrawingTaskLoadContext context = chipsDrawingTaskLoadProcessor.process(new ChipsDrawingTaskLoadContext(userId, labelCode, page, pageSize));
        if (!context.isSuccessful()) {
            return MapMessage.errorMessage(context.getMessage()).setErrorCode(context.getErrorCode());
        }

        return MapMessage.successMessage()
                .set("labelList", context.getLabelList())
                .set("drawingList", context.getDrawingList())
                .set("totalPage", context.getTotalPage())
                .set("popFinish", context.getPopData());
    }


    @Override
    public MapMessage loadDrawingTask(Long drawingTaskId) {
        ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.load(drawingTaskId);
        if (task == null) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到任务");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("unitId", task.getUnitId()).set("drawingTaskId", task.getId());

        StoneQuestionData questionData = Optional.ofNullable(task.getDrawingId())
                .map(e -> {
                    Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e));
                    if (stoneDataMap != null) {
                        return stoneDataMap.get(e);
                    }
                    return null;
                })
                .map(StoneQuestionData::newInstance)
                .orElse(null);
        if (questionData == null) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到图鉴");
        }

        List<String> choiceOptions = new ArrayList<>();
        choiceOptions.add(SafeConverter.toString(questionData.getJsonData().get("right_answer")));
        choiceOptions.add(SafeConverter.toString(questionData.getJsonData().get("wrong_answer")));
        Collections.shuffle(choiceOptions);
        mapMessage.set("choiceOptions", choiceOptions);

        AIUserVideo videoDetail = Optional.ofNullable(task.getVideoId())
                .map(aiUserVideoDao::load)
                .orElse(null);
        if (videoDetail != null) {
            Map<String, Object> res = new HashMap<>();
            res.put("ID", videoDetail.getId());
            videoMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(res)));
        }

        String video = Optional.ofNullable(videoDetail)
                .map(vi -> {
                    if (vi.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                        return vi.getVideo();
                    }
                    if (vi.getStatus() == AIUserVideo.ExamineStatus.Failed) {
                        return "*";
                    }

                    if (StringUtils.isBlank(vi.getVideo())) {
                        return "";
                    }
                    return "#";
                })
                .filter(StringUtils::isNotBlank)
                .orElse("");
        mapMessage.set("userVideo", video);

        String cover = Optional.ofNullable(videoDetail)
                .map(vi -> {
                    if (vi.getStatus() == AIUserVideo.ExamineStatus.Passed) {
                        return task.getCover();
                    }
                    return Optional.ofNullable(task.getUnitId())
                            .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                            .map(ma -> ma.get(task.getUnitId()))
                            .map(StoneUnitData::newInstance)
                            .map(StoneUnitData::getJsonData)
                            .map(StoneUnitData.Unit::getCover_image)
                            .orElse("");
                })
                .filter(StringUtils::isNotBlank)
                .orElse("");
        mapMessage.set("cover", cover);

        User user = userLoaderClient.loadUser(task.getUserId(), UserType.PARENT);
        mapMessage.set("userAvatar", Optional.ofNullable(user).map(User::getProfile).map(UserProfile::getImgUrl).filter(StringUtils::isNotBlank).orElse("http://cdn.17zuoye.com/fs-resource/5c8f6e809831c741caaea393.png"))
                .set("userName", Optional.ofNullable(user).map(User::getProfile).map(UserProfile::getNickName).filter(StringUtils::isNotBlank).orElse(""));

        Map<String, Object> map = chipsContentService.loadDrawingTaskShareQRCfg();
        String qrUrl = SafeConverter.toString(map.get("qrUrl")) + "?app_inviter=" + task.getUserId() + "&refer=330349&channel=parent_app";
        mapMessage.set("qrUrl", qrUrl);

        Set<String> unitList = aiUserUnitResultHistoryDao.loadByUserId(task.getUserId()).stream().filter(e -> !chipsContentService.isTrailUnit(e.getUnitId())).map(AIUserUnitResultHistory::getUnitId).collect(Collectors.toSet());
        int studyNum = unitList != null ? unitList.size() : 0;
        mapMessage.set("studyNum", studyNum);

        Long senNum = Optional.ofNullable(chipsEnglishUserExtSplitDao.load(task.getUserId()))
                .map(ChipsEnglishUserExtSplit::getSentenceLearn)
                .orElse(0L);
        mapMessage.set("sentenceNum", senNum);

        List<Map<String, Object>> joiners = new ArrayList<>();
        Map<Long, ChipsUserDrawingTaskJoin> userJoinMap = chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId()).stream().collect(Collectors.toMap(ChipsUserDrawingTaskJoin::getJoiner, e -> e));
        if (MapUtils.isNotEmpty(userJoinMap)) {
            Map<Long, ChipsWechatUserEntity> userMap = wechatUserPersistence.loads(userJoinMap.keySet());
            for (Long id : userJoinMap.keySet()) {
                ChipsWechatUserEntity userEntity = Optional.ofNullable(userMap)
                        .map(ma -> ma.get(id))
                        .orElse(null);
                if (userEntity == null) {
                    continue;
                }
                Map<String, Object> map1 = new HashMap<>();
                map1.put("avatar", userEntity.getAvatar());
                map1.put("uname", userEntity.getNickName());
                map1.put("user", userEntity.getUserId());
                map1.put("energy", userJoinMap.get(id).getEnergy());
                joiners.add(map1);
            }
        }
        mapMessage.set("joiner", joiners);

        String cardTitle = Optional.ofNullable(task.getUnitId())
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e)))
                .map(ma -> ma.get(task.getUnitId()))
                .map(StoneUnitData::newInstance)
                .map(StoneUnitData::getJsonData)
                .map(StoneUnitData.Unit::getImage_discription)
                .orElse("薯条英语");
        mapMessage.put("cardTitle", cardTitle);
        return mapMessage;
    }

    @Override
    public MapMessage loadDrawingShareInfo(Long drawingTaskId) {
        ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.load(drawingTaskId);
        if (task == null) {
            return MapMessage.errorMessage().set("result", ChipsErrorType.DEFAULT).set("message", "未找到任务");
        }

        User user = Optional.ofNullable(task)
                .map(ChipsUserDrawingTask::getUserId)
                .map(userLoaderClient::loadUser)
                .orElse(null);
        String userImage = Optional.ofNullable(user)
                .map(UserInfoSupport::getUserRoleImage)
                .orElse("");

        String userName = Optional.ofNullable(user)
                .map(User::getProfile)
                .map(UserProfile::getNickName)
                .orElse("");


        String drawingName = Optional.ofNullable(task)
                .map(ChipsUserDrawingTask::getDrawingId)
                .map(drawingId -> {
                    Map<String, StoneData> map = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(drawingId));
                    if (MapUtils.isNotEmpty(map)) {
                        return map.get(drawingId);
                    }
                    return null;
                })
                .map(StoneQuestionData::newInstance)
                .filter(un -> un.getJsonData() != null)
                .map(StoneQuestionData::getJsonData)
                .map(ma -> SafeConverter.toString(ma.get("illust_name")))
                .orElse("");

        MapMessage mapMessage = MapMessage.successMessage();
        Map<String, Object> map = chipsContentService.loadDrawingTaskShareQRCfg();
        mapMessage.putAll(map);
        return mapMessage.set("userAvatar", userImage)
                .set("qrUrl", SafeConverter.toString(map.get("qrUrl")) + "?app_inviter=" + task.getUserId() + "&refer=330348&channel=parent_app")
                .set("userName", userName)
                .set("unitName", drawingName);
    }

    @Override
    public MapMessage loadUserPageRedDot(Long userId, String pageCode) {
        if (userId == null || StringUtils.isBlank(pageCode)) {
            return MapMessage.errorMessage("参数异常");
        }

        ChipsRedDotPage chipsRedDotPage = chipsRedDotPagePersistence.loadByCode(pageCode);
        if (chipsRedDotPage == null) {
            return MapMessage.errorMessage("页面不存在或者删除");
        }

        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("pageCode", pageCode);
        mapMessage.set("pageName", chipsRedDotPage.getName());

        List<ChipsUserRedDotPageRecord> userRecordList = chipsUserRedDotRecordPagePersistence.loadByUser(userId);
        boolean read = Optional.ofNullable(userRecordList)
                .map(list -> list.stream().filter(e -> e.getPage().equals(chipsRedDotPage.getId())).findFirst().orElse(null))
                .map(ChipsUserRedDotPageRecord::getRead)
                .orElse(false);
        mapMessage.set("read", read);
        if (pageCode.equals("course_begin_enroll_info")) {
            mapMessage.set("wechatDomain", WechatConfig.getBaseSiteUrl());
        }

        List<Map<String, Object>> childrenPage = new ArrayList<>();

        List<ChipsRedDotPage> chipsRedDotPages = chipsRedDotPagePersistence.loadByParentPage(chipsRedDotPage.getId());
        if (CollectionUtils.isNotEmpty(chipsRedDotPages)) {
            chipsRedDotPages.sort(Comparator.comparing(ChipsRedDotPage::getRank));
            for (ChipsRedDotPage redDotPage : chipsRedDotPages) {
                Map<String, Object> map = new HashMap<>();
                map.put("pageCode", redDotPage.getCode());
                map.put("pageName", redDotPage.getName());
                boolean re = Optional.ofNullable(userRecordList)
                        .map(list -> list.stream().filter(e -> e.getPage().equals(redDotPage.getId())).findFirst().orElse(null))
                        .map(ChipsUserRedDotPageRecord::getRead)
                        .orElse(false);
                map.put("read", re);
                childrenPage.add(map);
            }
        }

        mapMessage.set("childrenPage", childrenPage);
        return mapMessage;
    }

    public List<List<String>> calUserUnitEngeryByBook(List<Long> userList, String bookId) {
        List<String> unitList = Optional.ofNullable(bookId).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e))).map(e -> e.get(bookId))
                .map(StoneBookData::newInstance).map(StoneBookData::getJsonData).map(e -> e.getChildren())
                .map(l -> l.stream().map(e -> e.getStone_data_id()).collect(Collectors.toList())).orElse(Collections.emptyList());
        String bookName = Optional.ofNullable(bookId).map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(e))).map(e -> e.get(bookId))
                .map(StoneBookData::newInstance).map(e -> e.getCustomName()).orElse("");
        if (CollectionUtils.isEmpty(unitList) || CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }
        List<List<String>> result = new ArrayList<>();
        for (Long userId : userList) {
            result.add(userUnitEngeryForExport(userId, unitList, bookName));
        }
        result.add(0, unitEngeryTitle(unitList));
        return result;
    }

    @Override
    public MapMessage loadUserDrawingInfo(Long userId, String openId) {
        List<ChipsWechatUserEntity> userEntityList = wechatUserPersistence.loadByUserId(userId);
        String avatar = Optional.ofNullable(userEntityList)
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.get(0))
                .map(ChipsWechatUserEntity::getAvatar)
                .orElse("");
        String uname = Optional.ofNullable(userEntityList)
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.get(0))
                .map(ChipsWechatUserEntity::getNickName)
                .orElse("");

        List<ChipsUserCourse> chipsUserCourseList = chipsUserService.loadUserEffectiveCourse(userId);
        int totalEnergy = 0;
        int totalCard = 0;
        int totalFinish = 0;
        if (CollectionUtils.isEmpty(chipsUserCourseList)) {
            return MapMessage.successMessage()
                    .set("totalEnergy", totalEnergy)
                    .set("buy", false)
                    .set("totalCard", totalCard)
                    .set("totalFinish", totalFinish)
                    .set("avatar", avatar)
                    .set("uname", uname)
                    .set("drawingList", Collections.emptyList());
        }

        if (CollectionUtils.isNotEmpty(userEntityList)) {
            for (ChipsWechatUserEntity entity : userEntityList) {
                List<ChipsUserDrawingTaskJoin> joinList = chipsUserDrawingTaskJoinPersistence.loadByJoiner(entity.getId());
                if (CollectionUtils.isEmpty(joinList)) {
                    continue;
                }
                totalEnergy += joinList.stream().mapToInt(ChipsUserDrawingTaskJoin::getEnergy).sum();
            }
        }

        List<ChipsUserDrawingTask> touristTaskList = Optional.ofNullable(wechatUserPersistence.loadByOpenIdAndType(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.getCode()))
                .map(ChipsWechatUserEntity::getUserId)
                .map(chipsUserDrawingTaskPersistence::loadByUser)
                .filter(CollectionUtils::isNotEmpty)
                .orElse(Collections.emptyList());

        List<ChipsDrawingTaskVO> result = new ArrayList<>();
        List<ChipsUserDrawingTask> userTaskList = chipsUserDrawingTaskPersistence.loadByUser(userId);
        Set<String> drawings = userTaskList.stream().map(ChipsUserDrawingTask::getDrawingId).collect(Collectors.toSet());
        Map<String, StoneData> drawingStoneMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(drawings);

        for (ChipsUserDrawingTask task : userTaskList) {
            StoneQuestionData questionData = Optional.ofNullable(drawingStoneMap.get(task.getDrawingId())).map(StoneQuestionData::newInstance).orElse(null);
            if (questionData == null) {
                continue;
            }
            totalCard ++;

            boolean gain = touristTaskList.stream().filter(e -> e.getDrawingId().equals(task.getDrawingId())).findFirst().map(t -> t != null).orElse(false);
            String image = Optional.ofNullable(questionData).map(StoneQuestionData::getJsonData).map(ma -> SafeConverter.toString(ma.get("reward_pic_before_front"))).orElse("");
            if (task.fetchStatus() == ChipsUserDrawingTaskStatus.finished) {
                image = Optional.ofNullable(questionData).map(StoneQuestionData::getJsonData).map(ma -> SafeConverter.toString(ma.get("reward_pic_after_front"))).orElse("");
                totalFinish ++;
                totalEnergy += task.getTotalEnergy();
            } else {
                totalEnergy += chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId()).stream().mapToInt(ChipsUserDrawingTaskJoin::getEnergy).sum();
            }
            ChipsDrawingTaskVO taskVO = new ChipsDrawingTaskVO();
            taskVO.setImage(image);
            taskVO.setGain(gain);
            result.add(taskVO);
        }


        return MapMessage.successMessage()
                .set("totalEnergy", totalEnergy)
                .set("buy", true)
                .set("totalCard", totalCard)
                .set("totalFinish", totalFinish)
                .set("avatar", avatar)
                .set("uname", uname)
                .set("drawingList", result);
    }

    /**
     * 获取用户姓名
     *
     * @param isReal true: 获取真实姓名  false：获取nickName
     */
    private String obtainUserName(Long userId, boolean isReal) {
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return "孩子";
        }
        if (isReal) {
            return user.fetchRealname();
        } else {
            UserProfile userProfile = user.getProfile();
            return userProfile == null ? "孩子" : userProfile.getNickName() != null ? userProfile.getNickName() : "孩子";
        }
    }

    private List<String> unitEngeryTitle(List<String> unitList) {
        if (CollectionUtils.isEmpty(unitList)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        list.add("姓名");
        list.add("用户ID");
        list.add("课程");
        list.add("总能量数");
        for (int i = 0; i < unitList.size(); i++) {
            list.add("Day" + (i + 1));
        }
        return list;
    }

    /**
     * 计算每个用户对应的单元的能量数，和汇总的种能量
     *
     * @param userId
     * @param unitList
     * @param bookName
     * @return
     */
    public List<String> userUnitEngeryForExport(Long userId, List<String> unitList, String bookName) {
        Set<String> unitSet = unitList.stream().collect(Collectors.toSet());
        List<ChipsUserDrawingTask> userTaskList = chipsUserDrawingTaskPersistence.loadByUser(userId);
        List<ChipsUserDrawingTask> taskList = userTaskList.stream().filter(e -> unitSet.contains(e.getUnitId())).collect(Collectors.toList());
        Map<String, Integer> unitEngeryMap = new HashMap<>();
        for (ChipsUserDrawingTask task : taskList) {
            List<ChipsUserDrawingTaskJoin> taskJoinList = chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId());
            int engery = taskJoinList.stream().filter(e -> e.getEnergy() != null).map(e -> e.getEnergy()).reduce((e1, e2) -> (e1 + e2)).orElse(0);
            unitEngeryMap.put(task.getUnitId(), engery);
        }
        List<String> unitEngeryList = new ArrayList<>();
        int totalEngery = 0;
        for (int i = 0; i < unitList.size(); i++) {
            String unitId = unitList.get(i);
            Integer engery = unitEngeryMap.get(unitId);
            if (engery != null && engery != 0) {
                totalEngery += engery;
            }
            unitEngeryList.add(engery == null || engery == 0 ? "--" : engery + "");
        }
        unitEngeryList.add(0, totalEngery + "");
        unitEngeryList.add(0, bookName);
        unitEngeryList.add(0, "" + userId);
        unitEngeryList.add(0, obtainUserName(userId, false));
        return unitEngeryList;
    }

    /**
     *
     * @param userId
     * @param unitList
     */
}
