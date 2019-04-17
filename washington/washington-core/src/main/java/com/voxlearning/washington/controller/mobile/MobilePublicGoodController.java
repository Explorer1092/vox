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

package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.reward.api.enums.LikeSourceEnum;
import com.voxlearning.utopia.service.reward.api.mapper.CacheCollectMapper;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.LikeEntriesMapper;
import com.voxlearning.utopia.service.reward.client.PublicGoodLoaderClient;
import com.voxlearning.utopia.service.reward.client.PublicGoodServiceClient;
import com.voxlearning.utopia.service.reward.constant.PublicGoodModel;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.*;
import com.voxlearning.utopia.service.reward.util.TeacherNameWrapper;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.beans.BeanCopier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.alps.calendar.DateUtils.dateToString;
import static com.voxlearning.alps.lang.convert.SafeConverter.toLong;
import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.BooleanUtils.isTrue;
import static java.util.stream.Collectors.*;

/**
 * 公益
 *
 * @author haitian.gan
 * @since 2018/6/11
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/publicGood")
public class MobilePublicGoodController extends AbstractMobileController {

    /** 捐赠记录一页的条数 **/
    private static final int RECORD_PAGE_SIZE = 15;

    @Inject private RaikouSystem raikouSystem;

    @Inject private PublicGoodLoaderClient pbLoaderCli;
    @Inject private PublicGoodServiceClient pbSrvCli;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    /** Copier from RewardActivity to PGActivityMapper **/
    private BeanCopier activityCopier;

    @Override
    public void afterPropertiesSet(){
        activityCopier = BeanCopier.create(RewardActivity.class, PGActivityMapper.class,false);
    }

    /**
     * 公益活动列表
     *
     * @return
     */
    @RequestMapping(value = "/activities.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadRewardActivities() {
        if (currentUser() == null) {
            return MapMessage.errorMessage("请重新登录").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }

        MapMessage resultMsg = MapMessage.successMessage();
        // 获得用户今天参加所有活动的记录
        User user = currentUser();
        Map<Long,List<PublicGoodCollect>> collectMap = pbLoaderCli.loadCollectByUserId(user.getId())
                .stream()
                .collect(Collectors.groupingBy(c -> c.getActivityId()));

        resultMsg.add("unit",user.isTeacher() ? "园丁豆" : "学豆");
        // 筛选出来上线的
        List<PGActivityMapper> mappers = rewardLoaderClient.loadRewardActivities()
                .stream()
                .filter(RewardActivity::getOnline)
                .filter(act -> {
                    // 学生的没限制，家长只给看完成的老项目和所有的新项目
                    if (user.isStudent() || user.isTeacher()) {
                        return true;
                    } else if (user.isParent()) {
                        PublicGoodModel model = PublicGoodModel.parse(act.getModel());
                        return act.isFinished() || model != PublicGoodModel.NONE;
                    }else
                        return false;
                })
                // 按时间倒序，最新的在最前面
                // 这个改成按照排序值来排序
                // 未完成的排在前面，如果状态相同先看排序值，再按创建时间倒序
                .sorted((a1, a2) -> {
                    Integer a2Ow = SafeConverter.toInt(a2.getOrderWeights());
                    Integer a1Ow = SafeConverter.toInt(a1.getOrderWeights());

                    if(Objects.equals(a1.getStatus(),a2.getStatus())){
                        if(Objects.equals(a1Ow,a2Ow)){
                            return a2.getCreateDatetime().compareTo(a1.getCreateDatetime());
                        }else
                            return Integer.compare(a2Ow,a1Ow);
                    }else if(a1.isOnGoing()){
                        return -1;
                    }else{
                        return 1;
                    }
                })
                .map(activity -> {
                    PGActivityMapper mapper = new PGActivityMapper();
                    activityCopier.copy(activity,mapper,null);

                    // 正在进行中的Collect ID，如果没有建设中的教室
                    // 则选取最近完成的教室
                    AtomicReference<String> onGoingId = new AtomicReference<>();
                    String lastFinishId = collectMap.getOrDefault(activity.getId(),Collections.emptyList())
                            .stream()
                            .filter(c -> {
                                if(c.isFinished()){
                                    return true;
                                }else{
                                    onGoingId.set(c.getId());
                                    return false;
                                }
                            })
                            .sorted((c1,c2) -> Long.compare(c2.getFinishTime(),c1.getFinishTime()))
                            .map(c -> c.getId())
                            .findFirst()
                            .orElse(null);

                    if (onGoingId.get() == null) onGoingId.set(lastFinishId);
                    mapper.setCollectId(onGoingId.get());

                    // 如果是家长则显示孩子们的参加信息
                    if(user.isParent()){
                        List<Map<String,Object>> childList = new ArrayList<>();
                        Optional.ofNullable(pbLoaderCli.loadParentChildRef(user.getId()))
                                .map(pc -> pc.getChildMap())
                                .map(childMap -> childMap.keySet())
                                .orElse(Collections.emptySet())
                                .stream()
                                .map(stuId -> raikouSystem.loadUser(stuId))
                                .forEach(u -> childList.add(MapUtils.m("name",u.fetchRealname(),"avatarImg",getUserAvatarImgUrl(u))));

                        mapper.setChildList(childList);
                    }else if(user.isStudent()){
                        // 获得老师参与记录
                        if (activity.isOldModel()) {
                            mapper.setTchJoinList(Collections.emptyList());
                        } else {
                            mapper.setTchJoinList(pbLoaderCli.getTeacherJoinStatus(activity.getId(), user.getId()));
                        }
                    }else if (user.isTeacher()) {
                        mapper.setTargetMoney(mapper.getTargetMoney() / 10);
                        mapper.setRaisedMoney(mapper.getRaisedMoney() / 10);
                    }

                    return mapper;
                })
                .collect(toList());

        resultMsg.add("userId", user.getId());
        resultMsg.add("activities", mappers);
        return resultMsg;
    }

    /**
     * 获取活动详情信息
     *
     * @return
     */
    @RequestMapping(value = "/activity.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage activityDetail() {
        User user = null;
        Long userId = getRequestLong("userId");
        if (Objects.equals(userId, 0L)) {
            user = currentUser();
        } else {
            user = raikouSystem.loadUser(userId);
        }
        if (user == null) {
            return MapMessage.errorMessage("用户不存在!");
        }

        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("活动不存在");
        }

        // 分享页面的标志
        boolean share = getRequestBool("share");

        // 查询图片，做轮播用
        List<String> images = rewardLoaderClient.loadActivityImages(activityId)
                .stream()
                .map(RewardActivityImage::getLocation)
                .collect(toList());

        activity.setImages(images);
        activity.calculateProgress();

        // 兼容旧数据，如果没有完成时间，用创建时间补充上
        if (activity.getFinishTime() == null)
            activity.setFinishTime(new Date());

        MapMessage resultMsg = MapMessage.successMessage();

        if (user.isTeacher()) {
            activity.setTargetMoney(activity.getTargetMoney() / 10);
            activity.setRaisedMoney(activity.getRaisedMoney() / 10);
        }

        resultMsg.add("activityDetail", activity);
        resultMsg.add("activityId",activityId);
        resultMsg.add("userId", user.getId());
        resultMsg.add("unit",user.isTeacher() ? "园丁豆" : "学豆");

        // 分享来源的页面不展示按钮
        if(share){
            resultMsg.add("status","disabled");
            return resultMsg;
        }

        if(user.isParent()){
            // 家长的状态
            String parentStatus = Optional.ofNullable(pbLoaderCli.loadParentChildRef(user.getId()))
                    .filter(PGParentChildRef::hadChildData)
                    .map(pc -> "display")
                    .orElse("disabled");

            resultMsg.add("status", parentStatus);
        }else{
            List<PublicGoodCollect> collectList = pbLoaderCli.loadUserCollectByActId(user.getId(),activityId);
            // 如果是新人或者是完成了上一个项目还没开始下一个的闲置状态，则是进教室选择页
            int finishNum = 0;
            boolean isNewOrIdle = true;
            for(PublicGoodCollect collect : collectList){
                if(collect.getStatus() == PublicGoodCollect.Status.ONGOING){
                    resultMsg.add("collectId",collect.getId());
                    isNewOrIdle = false;
                    break;
                }else if(collect.isFinished())
                    finishNum ++;
            }

            int totalNum = pbLoaderCli.loadStyleByModel(activity.getModel()).size();
            if(finishNum >= totalNum){
                resultMsg.add("status","check");
            }else if(isNewOrIdle){
                resultMsg.add("status","create");
            }else{
                resultMsg.add("status","enter");
            }
        }

        return resultMsg;
    }

    /**
     * 获取我的捐赠记录
     *
     * @return
     */
    @RequestMapping(value = "/my_records.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myHistory() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            // 默认是第一页
            Integer pageNum = getRequestInt("page",1);

            final String dateFormat = "MM-dd";
            Date now = new Date();

            String today = dateToString(now,dateFormat);
            String yesterday = dateToString(DateUtils.addDays(now,-1),dateFormat);

            Function<PGRecordMapper,String> groupByFunc = record -> {
                String timeExpr = dateToString(record.getT(),"MM-dd");
                if(today.equals(timeExpr))
                    return "今天";
                else if(yesterday.equals(timeExpr))
                    return "昨天";
                else
                    return timeExpr;
            };

            Map<Long,RewardActivity> activityMap = rewardLoaderClient.loadRewardActivities()
                    .stream()
                    .collect(Collectors.toMap(k -> k.getId(),v -> v));

            // Transfer function.
            Function<RewardActivityRecord,PGRecordMapper> mapperFunc = getActRecordMapper("donate");
            // 统计捐赠过的活动ID
            Set<Long> activityIdSet = new HashSet<>();
            // 普通捐赠的记录
            List<PGRecordMapper> donateMappers =  rewardLoaderClient.loadActivityUserRecords(user.getId())
                    .stream()
                    .peek(r -> {
                        activityIdSet.add(r.getActivityId());
                        // 如果是老师的话，要换算成园丁豆
                        if(user.isTeacher() && ((TeacherDetail)user).isPrimarySchool()) r.setPrice(r.getPrice() / 10);
                    })
                    .map(mapperFunc)
                    .collect(toList());

            // 活动完成的记录
            List<PGRecordMapper> finishMappers = activityIdSet.stream()
                    .map(activityMap::get)
                    .filter(a -> isTrue(a.isFinished()))
                    .map(act -> {
                        PGRecordMapper mapper = new PGRecordMapper();
                        mapper.setComment(act.getName() + "活动结束!");

                        // 如果没有完成时间则取最后一次的更新时间
                        Date t = Optional.ofNullable(act.getFinishTime()).orElse(act.getUpdateDatetime());
                        mapper.setT(t);
                        mapper.setTime(dateToString(t,"HH:mm"));
                        mapper.setType("finish");

                        return mapper;
                    })
                    .filter(Objects::nonNull)
                    .collect(toList());

            // 记录合在一起混排，按时间f
            donateMappers.addAll(finishMappers);
            Comparator<PGRecordMapper> recordCmpr = (p1,p2) -> p2.getT().compareTo(p1.getT());
            donateMappers.sort(recordCmpr.thenComparing((p1,p2) -> "finish".equals(p1.getType()) ? -1 : 1));

            Pageable pageable = PageableUtils.startFromOne(pageNum,RECORD_PAGE_SIZE);
            Page<PGRecordMapper> page = PageableUtils.listToPage(donateMappers,pageable);

            // 对记录进行分组。先按年分，再按月分~
            final Calendar cal = Calendar.getInstance();
            Function<PGRecordMapper,String> groupByYearFunc = record -> {
                cal.setTime(record.getT());
                return String.valueOf(cal.get(Calendar.YEAR));
            };

            List<Map<String,Object>> result = new ArrayList<>();
            page.getContent().stream().collect(getRecordMapperFunc(groupByYearFunc)).forEach((year, records) -> {
                // 把已经按年分组的数据，再按天分组
                List<Map<String, Object>> dateMap = new ArrayList<>();
                records.stream().collect(getRecordMapperFunc(groupByFunc)).forEach((date, dRecords) -> {
                    dateMap.add(MapUtils.m("date", date, "records", dRecords));
                });

                result.add(MapUtils.m("year", year, "records", dateMap));
            });

            return MapMessage.successMessage()
                    .add("records", result)
                    .add("hasNextPage", page.hasNext())
                    .add("currPageNum", pageNum)
                    .add("unit",user.isTeacher() ? "园丁豆" : "学豆");

        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Get my history records error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private Collector<PGRecordMapper,?,Map<String,List<PGRecordMapper>>> getRecordMapperFunc(Function<PGRecordMapper,String> groupByFunc){
        return Collectors.groupingBy(groupByFunc,LinkedHashMap::new,toList());
    }

    private Function<RewardActivityRecord,PGRecordMapper> getActRecordMapper(String type){
        return r -> {
            PGRecordMapper mapper = new PGRecordMapper();

            // 如果是没有comment内容的老捐赠记录，则补充上临时内容
            mapper.setComment(r.getComment());
            if(StringUtils.isBlank(mapper.getComment())){
                mapper.setComment("捐赠学豆");
            }

            mapper.setT(r.getCreateDatetime());
            mapper.setTime(dateToString(r.getCreateDatetime(),"HH:mm"));
            Integer donateType = r.getType();
            if (donateType != null) {
                if (r.getType().equals(0)) {
                    mapper.setType("donate");
                } else if (r.getType().equals(1)) {
                    mapper.setType("finish");
                }
            } else {
                mapper.setType(type);
            }
            mapper.setPrice(r.getPrice().longValue());
            return mapper;
        };
    }

    /**
     * 获得活动的样式列表
     *
     * @return
     */
    @RequestMapping(value = "/style_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadClassRoomStyles() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            final String schoolName;
            if(user instanceof StudentDetail){
                schoolName = ((StudentDetail)user).getStudentSchoolName();
            }else if(user instanceof TeacherDetail) {
                schoolName = ((TeacherDetail)user).getTeacherSchoolName();
            }else
                schoolName = "";

            Long activityId = getRequestLong("activityId");
            RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
            Validate.isTrue(activity != null,"活动不存在!");

            Map<Long,PublicGoodCollect> collectMap =  pbLoaderCli.loadUserCollectByActId(user.getId(),activityId)
                    .stream()
                    .collect(Collectors.toMap(k -> k.getStyleId(),v -> v));

            // 创建mapper，附上每个教室的状态
            List<PGStyleMapper> styleList = pbLoaderCli.loadStyleByModel(activity.getModel())
                    .stream()
                    .map(s -> {
                        PGStyleMapper mapper = PGStyleMapper.of(s);
                        mapper.setStatus("NEW");

                        // 获得相应样式的教室数据，如果没有，也设置成空值
                        Optional.ofNullable(collectMap.get(s.getId())).ifPresent(c -> {
                            mapper.setCollectId(c.getId());
                            mapper.setStatus(c.getStatus().name());
                        });

                        mapper.setSchoolName(schoolName);
                        return mapper;
                    })
                    .collect(toList());

            return MapMessage.successMessage().add("styles",styleList);
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load style list error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 创建Collect
     *
     * @return
     */
    @RequestMapping(value = "/create_collect.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createCollect() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            Long activityId = getRequestLong("activityId");
            Long styleId = getRequestLong("styleId");

            PublicGoodCollect newCollect = new PublicGoodCollect();
            newCollect.setActivityId(activityId);
            newCollect.setStyleId(styleId);
            newCollect.setStatus(PublicGoodCollect.Status.ONGOING);
            newCollect.setUserId(user.getId());

            return pbSrvCli.upsertCollect(newCollect);
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Create collect error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private Boolean isPrimarySchoolTeacher(User user) {
        Boolean result = false;
        if (user.isTeacher()) {
            TeacherDetail teacherDetail = (TeacherDetail) user;
            if (teacherDetail.isPrimarySchool()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Load collect detail
     *
     * @return
     */
    @RequestMapping(value = "/collect_detail.vpage")
    @ResponseBody
    public MapMessage loadCollectDetail() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            Long userId = getRequestLong("userId");
            User displayUser = raikouSystem.loadUser(userId);
            Validate.isTrue(displayUser != null,"用户不存在!");

            String collectId = getRequestString("id");
            AtomicBoolean hadDonated = new AtomicBoolean(false);
            PublicGoodCollect collect = pbLoaderCli.loadCollectByUserId(userId)
                    .stream()
                    .peek(col -> {if(col.getEnabledEleNum() > 0) hadDonated.set(true);})
                    .filter(c -> Objects.equals(c.getId(),collectId))
                    .findFirst()
                    .orElse(null);
            Validate.isTrue(collect != null, "Collect is not exist!");

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("unit",isPrimarySchoolTeacher(user) ? "园丁豆" : "学豆");
            // 是否捐赠过
            resultMsg.add("hadDonated",hadDonated.get());

            boolean displayLikeView = false;
            // 视图类型
            if(user.isStudent()){
                // 看别人教室的情况
                if(!Objects.equals(user.getId(),userId)){
                    resultMsg.add("view","stuOther");
                    displayLikeView = true;
                }else{
                    resultMsg.add("view","stuSelf");

                    // 老师的留言
                    CommentEntriesMapper commentEntries = pbLoaderCli.getCommentByCollectId(collect.getActivityId(), collectId);
                    if (commentEntries != null) {
                        for (CommentEntriesMapper.Collect item : commentEntries.getCollectList()) {
                            item.setAvatarImg(getUserAvatarImgUrl(item.getAvatarImg()));
                        }
                        for (CommentEntriesMapper.Comment item : commentEntries.getCommentList()) {
                            item.setAvatarImg(getUserAvatarImgUrl(item.getAvatarImg()));
                        }
                    }
                    resultMsg.add("teacherWords", commentEntries.getCommentList()); // 老师留言列表
                    resultMsg.add("teacherCreate", commentEntries.getCollectList().stream().limit(2).collect(toList())); // 是否有老师参与
                    resultMsg.add("underwayTeacherAvatarImgs", Collections.singletonList(getUserAvatarImgUrl(displayUser)));
                }
            }else if(user.isTeacher()){
                // 看别人教室的情况
                if(!Objects.equals(user.getId(),userId)){
                    resultMsg.add("view","tchOther");
                    displayLikeView = true;
                }else{
                    resultMsg.add("view","tchSelf");
                }
            }

            // 显示点赞的视图部分
            if (displayLikeView) {
                List<LikeEntriesMapper> likeEntries = pbLoaderCli.getLikeByCollectId(collect.getActivityId(), collectId);
                for (LikeEntriesMapper likeEntry : likeEntries) {
                    likeEntry.setAvatarImg(getUserAvatarImgUrl(likeEntry.getAvatarImg()));
                }
                resultMsg.add("likeEntries", likeEntries);

                // 判断有没有可查看的证书 FIXME 要改成从Collect数据中判断
                boolean hasCert = pbLoaderCli.loadUserCollectByActId(userId,collect.getActivityId())
                        .stream()
                        .anyMatch(c -> c.isFinished());
                resultMsg.add("hadCert",hasCert);
            }

            PublicGoodUserActivity userActivity = pbLoaderCli.loadUserActivityByUserId(collect.getActivityId(), userId);
            resultMsg.add("needGuide",BooleanUtils.isNotTrue(userActivity.getGuide()));

            // 未引导的时更新引导
            if(BooleanUtils.isNotTrue(userActivity.getGuide())){
                userActivity.setGuide(true);
                pbSrvCli.upsertUserActivity(userActivity);
            }

            resultMsg.add("likeNum", userActivity == null ? 0 : toLong(userActivity.getLikeNum()));
            resultMsg.add("status", collect.getStatus().name());

            UserIntegral integral = null;
            if(user instanceof StudentDetail){
                integral = ((StudentDetail)user).getUserIntegral();
            }else if(user instanceof TeacherDetail){
                integral = ((TeacherDetail)user).getUserIntegral();
            }

            long integralNum =  Optional.ofNullable(integral).map(i -> i.getUsable()).orElse(0L);
            resultMsg.add("integral",integralNum);

            // 捐赠的物件列表
            List<PublicGoodElementType> eleTypes = pbLoaderCli.loadElementTypeByStyleId(collect.getStyleId())
                    .stream()
                    // 老师的价格除10
                    .peek(ele -> { if(isPrimarySchoolTeacher(user)){ ele.setPrice(ele.getPrice() / 10); } })
                    // 按照价格从低到高排序
                    .sorted(Comparator.comparingInt(PublicGoodElementType::getPrice))
                    .collect(toList());
            resultMsg.add("items",eleTypes);

            // 已点亮的物件
            List<String> enableItems = Optional.ofNullable(collect.getElementList())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(e -> e.getCode())
                    .collect(toList());
            resultMsg.add("enableItems", enableItems);

            // 让前端控制显示的字段
            RewardActivity activity = rewardLoaderClient.loadRewardActivity(collect.getActivityId());
            resultMsg.add("model", activity.getModel());
            resultMsg.add("styleId", collect.getStyleId());
            resultMsg.add("feed", pbLoaderCli.getFeedStatus(collect.getActivityId(), displayUser.getId()));

            // 用户信息
            resultMsg.add("userAvatarImg",getUserAvatarImgUrl(displayUser));
            if (displayUser.isTeacher()) {
                resultMsg.add("userName", TeacherNameWrapper.respectfulName(displayUser.fetchRealname()));
            } else {
                resultMsg.add("userName", displayUser.fetchRealname());
            }
            resultMsg.add("userType", displayUser.isTeacher() ? 0 : 1);

            // 是否已经点赞
            Set<Long> likedCollect = pbLoaderCli.loadLikedCollect(collect.getActivityId(), user.getId());
            resultMsg.set("liked", likedCollect.contains(collect.getUserId()));

            return resultMsg;
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load collect detail error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 排行榜
     *
     * @return
     */
    @RequestMapping(value = "/rank.vpage")
    @ResponseBody
    public MapMessage loadRank() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            String range = getRequestString("range");
            Validate.notBlank(range,"参数错误!");
            Validate.isTrue(Arrays.asList("school","nation").contains(range),"range错误!");

            String collectId = getRequestString("collectId");
            PublicGoodCollect collect = pbLoaderCli.loadUserCollectById(user.getId(), collectId);
            Validate.isTrue(collect != null,"Collect is not exist!");

            MapMessage resultMsg;
            if ("school".equals(range)) {
                return pbLoaderCli.loadSchoolRank(user.getId(), collect.getActivityId());
            } else if ("nation".equals(range)) {
                return pbLoaderCli.loadNationRank(user.getId(), collect.getActivityId());
            } else
                return MapMessage.errorMessage("range错误!");
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load rank error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 证书
     *
     * @return
     */
    @RequestMapping(value = "/cert.vpage")
    @ResponseBody
    public MapMessage loadCertList() {
        try{
            Long userId = getRequestLong("userId");
            User displayUser = raikouSystem.loadUser(userId);
            Validate.isTrue(displayUser != null,"参数错误!");

            Long activityId = getRequestLong("activityId");
            RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
            Validate.notNull(activity,"活动不存在!");
            // 获得Style的Map
            Map<Long,PublicGoodStyle> styleMap = pbLoaderCli.loadStyleByModel(activity.getModel())
                    .stream()
                    .collect(Collectors.toMap(s -> s.getId(),v -> v));

            String userName = displayUser.fetchRealname();
            String avatarImg = getUserAvatarImgUrl(displayUser);
            // 证书内容
            String certContent = pbLoaderCli.loadRewardByModel(activity.getModel())
                    .stream()
                    .filter(r -> "CERT".equals(r.getType()))
                    .map(r -> SafeConverter.toString(r.getExtAttrValue("cert")))
                    .findFirst()
                    .orElse("");

            List<Map<String,Object>> mapper = pbLoaderCli.loadUserCollectByActId(userId, activityId)
                    .stream()
                    .filter(c -> c.isFinished())
                    .filter(c -> styleMap.containsKey(c.getStyleId()))
                    .map(c -> {
                        Date finishTime = Optional.ofNullable(c.getFinishTime()).map(Date::new).orElse(new Date());
                        String date = dateToString(finishTime, FORMAT_SQL_DATE);
                        PublicGoodStyle style = styleMap.get(c.getStyleId());

                        return MapUtils.m(
                                "userName", userName,
                                "avatarImg", avatarImg,
                                "date", date,
                                "styleId", c.getStyleId(),
                                "txt", String.format(certContent,date,style.getName())
                        );
                    })
                    .collect(toList());

            return MapMessage.successMessage().add("certList",mapper);
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load cert list error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 家长视角的教室详情页
     *
     * @return
     */
    @RequestMapping(value = "/parent_collect.vpage")
    @ResponseBody
    public MapMessage loadParentCollectDetail() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            Long activityId = getRequestLong("activityId");
            RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
            Validate.isTrue(activity != null, "活动不存在!");

            PGParentChildRef pcRef =  pbLoaderCli.loadParentChildRef(user.getId());
            Validate.isTrue(pcRef != null && pcRef.hadChildData(),"没有孩子的教室数据!");


            Long childId = getRequestLong("childId");
            if(childId == 0L){
                childId = pcRef.getChildMap().keySet().stream().findFirst().orElse(null);
                Validate.isTrue(childId != null ,"孩子信息为空!");
            }

            MapMessage resultMsg = MapMessage.successMessage();
            List<Map<String,Object>> stuList = new ArrayList<>();

            Long selectChildId = childId;
            resultMsg.add("selectChild",selectChildId);

            pcRef.getChildMap().forEach((cId, childRef) -> {
                StudentDetail stu = studentLoaderClient.loadStudentDetail(cId);

                Set<Long> styleList = new HashSet<>();
                List<PublicGoodCollect> collects = pbLoaderCli.loadUserCollectByActId(cId,activityId)
                        .stream()
                        .filter(c -> c.isFinished())
                        .peek(c -> styleList.add(c.getStyleId()))
                        .collect(toList());
                if(collects.size() > 0){
                    stuList.add(MapUtils.m("id",cId, "name",stu.fetchRealname(), "img",getUserAvatarImgUrl(stu)));
                }

                // 选中的小孩子要把教室详情吐出去
                if(!Objects.equals(selectChildId,cId)) {
                    return;
                }

                Map<String, Object> summarize = collects.stream()
                        .reduce((sum, curr) -> {
                            Map<String, PublicGoodCollect.Element> sumEleMap = sum.getElementMap();
                            Map<String, PublicGoodCollect.Element> currEleMap = curr.getElementMap();

                            // 汇总各个elementType的值
                            sumEleMap.forEach((code, ele) -> {
                                PublicGoodCollect.Element existEle = currEleMap.get(code);
                                if (existEle != null)
                                    ele.setCount(toLong(ele.getCount(), 1) + toLong(existEle.getCount(), 1));
                            });

                            return sum;
                        })
                        .map(c -> {
                            // 获得ElementType的code对照
                            Map<String, PublicGoodElementType> codeMap = pbLoaderCli.loadElementTypeByStyleId(c.getStyleId())
                                    .stream()
                                    .collect(toMap(k -> k.getCode(), Function.identity()));

                            List<Map<String, Object>> elementResult = Optional.ofNullable(c.getElementList())
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .map(ele -> {
                                        PublicGoodElementType eleModel = codeMap.get(ele.getCode());
                                        return MapUtils.m(
                                                "count", toLong(ele.getCount(), 1L),
                                                "name", eleModel.getName(),
                                                "quantifier", eleModel.getQuantifier()
                                        );
                                    })
                                    .collect(toList());

                            return MapUtils.m("summary",elementResult);
                        })
                        .orElse(new HashMap<>());

                PublicGoodUserActivity userActivity = pbLoaderCli.loadUserActivityByUserId(activityId, cId);
                if(userActivity != null){
                    summarize.put("totalMoney", userActivity.getMoneyNum());
                    summarize.put("likeCount", userActivity.getLikeNum());
                }

                List<PublicGoodFeed> feedList = pbLoaderCli.loadFeedByUserIdCollectId(activityId, cId, false)
                        .stream()
                        .filter(i -> i.getType() == PublicGoodFeed.Type.COMMENTS)
                        .collect(Collectors.toList());

                List<String> comments = new ArrayList<>();
                for (PublicGoodFeed feed : feedList) {
                    String format = String.format("%s:“%s”", TeacherNameWrapper.respectfulName(feed.getOpName()), feed.getComments());
                    comments.add(format);
                }

                summarize.put("finishNum", collects.size());
                summarize.put("comments", comments);
                summarize.put("styleList", styleList);
                resultMsg.add("summarize", summarize);
            });

            resultMsg.add("stuList",stuList);
            return resultMsg;
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load parent collect error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 我的奖励
     *
     * @return
     */
    @RequestMapping(value = "/my_rewards.vpage")
    @ResponseBody
    public MapMessage loadMyRewards() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            MapMessage mockMsg = MapMessage.successMessage();

            List<RewardActivity> activitys = rewardLoaderClient.loadRewardActivities()
                    .stream()
                    .filter(act -> act.isNewModel())
                    .sorted((r1, r2) -> r2.getCreateDatetime().compareTo(r1.getCreateDatetime()))
                    .collect(toList());
            Validate.isTrue(CollectionUtils.isNotEmpty(activitys), "活动不存在!");

            List<PGRewardMapper> result = new ArrayList<>();
            Date now = new Date();
            for (RewardActivity activity : activitys) {
                Long activityId = activity.getId();
                Map<Long, PublicGoodReward> rewardMap = pbLoaderCli.loadRewardByModel(activity.getModel())
                        .stream()
                        .collect(Collectors.toMap(k -> k.getId(), v -> v));

                List<PGRewardMapper> mappers = pbLoaderCli.loadUserCollectByActId(user.getId(),activityId)
                        .stream()
                        // 只看完成的教室，顺便兼容下错误数据
                        .filter(c -> c.isFinished() && CollectionUtils.isNotEmpty(c.getRewardList()))
                        .flatMap(c -> c.getRewardList().stream())
                        .map(r -> {
                            PublicGoodReward reward = rewardMap.get(r.getId());
                            if(reward == null)
                                return null;

                            // 合并ExtAttr
                            reward.putAllExtAttr(r.getAttr());

                            Object enableVal = r.getAttrVal("enable");
                            if (enableVal != null) {
                                // 判断是否到了开启时间
                                long openTime = SafeConverter.toLong(reward.getExtAttrValue("openTime"));
                                reward.setExtAttrValue("enable", openTime != 0 && now.getTime() >= openTime);
                            }

                            return reward;
                        })
                        .filter(Objects::nonNull)
                        .reduce(new ArrayList<PGRewardMapper>(), (accu, item) -> {

                            PGRewardMapper mapper = new PGRewardMapper();
                            mapper.setActivityId(activityId);

                            mapper.setName(item.fetchPrettyCutName());
                            mapper.setImg(item.getImgUrl());
                            mapper.setNum(1);
                            mapper.setType(item.getType());
                            mapper.setExtAttr(item.getExtAttr());

                            if(CollectionUtils.isEmpty(accu)){
                                accu.add(mapper);
                            }else{
                                boolean doHeap = false;
                                for (PGRewardMapper r : accu) {
                                    if (Objects.equals(r.getType(), item.getType()) && isTrue(item.getCanHeap())) {
                                        r.setNum(r.getNum() + 1);
                                        doHeap = true;
                                        break;
                                    }
                                }

                                if(!doHeap)
                                    accu.add(mapper);
                            }

                            return accu;
                        }, (a, b) -> {
                            a.addAll(b);
                            return a;
                        });
                        result.addAll(mappers);
                }


            mockMsg.add("userId", user.getId());
            mockMsg.put("rewardList", result);
            return mockMsg;
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Load my rewards error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 发放家长奖励
     *
     * @return
     */
    @RequestMapping(value = "/grant_parent_reward.vpage")
    @ResponseBody
    public MapMessage grantParentReward() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            MapMessage mockMsg = MapMessage.successMessage();
            return mockMsg;
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Grant parent reward error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 使用钥匙
     * @return
     */
    @RequestMapping(value = "/use_key.vpage")
    @ResponseBody
    public MapMessage useKey() {
        try{
            User user = currentUser();
            Validate.isTrue(user != null,"请重新登录!");

            Long activityId = getRequestLong("activityId");
            RewardActivity activity = rewardLoaderClient.loadRewardActivity(activityId);
            Validate.isTrue(activity != null,"活动不存在!");

            return pbSrvCli.useKey(user.getId(),activityId);
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }catch (Exception e){
            logger.error("PG:Use key error!",e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    @ResponseBody
    @RequestMapping(value = "donation.vpage", method = RequestMethod.POST)
    public MapMessage donationActivity(@RequestParam(required = false) String collectId,
                                       @RequestParam(required = false) String typeCode) {
        try {
            User user = currentUser();
            Validate.isTrue(user != null, "请重新登录!");

            PublicGoodCollect publicGoodCollect = pbLoaderCli.loadUserCollectById(user.getId(), collectId);
            Validate.isTrue(publicGoodCollect != null, "记录不存在!");

            RewardActivity activity = rewardLoaderClient.loadRewardActivity(publicGoodCollect.getActivityId());
            Validate.isTrue(activity != null, "活动不存在!");

            // 如果已经捐赠完成，则报错返回
            if (Objects.equals(activity.getStatus(), RewardActivity.Status.FINISHED.name())
                    || (activity.getTargetMoney() != 0 && activity.getRaisedMoney() >= activity.getTargetMoney())) {
                return MapMessage.errorMessage("活动已经结束，不能进行捐赠!");
            }

            PublicGoodElementType elementType = pbLoaderCli.loadElementTypeByCode(typeCode);
            Validate.isTrue(elementType != null, "物品种类不存在!");

            Integer price = elementType.getPrice();
            RewardActivityRecord record = new RewardActivityRecord();
            record.setActivityId(activity.getId());
            record.setCollectId(publicGoodCollect.getId());
            // 老师捐的是园丁豆，要处理下
            record.setPrice((double)price);
            record.setUserId(user.getId());
            record.setUserName(user.getProfile().getRealname());
            record.setType(RewardActivityRecord.PLAN);
            record.setComment("捐赠了" + elementType.getName());

            return pbSrvCli.donate(typeCode, record);
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "like.vpage", method = RequestMethod.POST)
    public MapMessage like(@RequestParam(required = false) String collectId,
                           @RequestParam(required = false) String source) {
        try {
            User user = currentUser();
            Validate.notEmpty(collectId, "collectId 不能为空");

            LikeSourceEnum likeSourceEnum = LikeSourceEnum.safeValueOf(source);
            Validate.notNull(likeSourceEnum, "source 不合法");

            if (likeSourceEnum == LikeSourceEnum.A17 && user == null) {
                return MapMessage.errorMessage("请重新登录!");
            }

            Long userId = Long.valueOf(collectId.split("-")[0]);
            PublicGoodCollect publicGoodCollect = pbLoaderCli.loadUserCollectById(userId, collectId);
            Validate.notNull(publicGoodCollect, "未查询到 collect");

            PublicGoodFeed feed = new PublicGoodFeed();
            feed.setUserId(publicGoodCollect.getUserId());
            feed.setType(PublicGoodFeed.Type.LIKE);
            feed.setSourceEnum(likeSourceEnum);
            feed.setActivityId(publicGoodCollect.getActivityId());
            if (user != null) {
                feed.setOpId(user.getId());
                String realname = user.getProfile().getRealname();
                if (user instanceof Teacher) {
                    feed.setOpName(TeacherNameWrapper.respectfulName(realname));
                } else {
                    feed.setOpName(realname);
                }
            }

            return pbSrvCli.addCollectLike(publicGoodCollect, feed);
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "comments.vpage", method = RequestMethod.POST)
    public MapMessage comments(@RequestParam(required = false) String collectId,
                               @RequestParam(required = false) String comments) {
        try {
            User user = currentUser();
            Validate.isTrue(user != null, "请重新登录!");
            Validate.notEmpty(collectId, "collectId 不能为空");
            Validate.notEmpty(comments, "评语不能为空");

            if (!(user instanceof Teacher)) {
                return MapMessage.successMessage("只有老师才可以留言");
            }
            Long userId = Long.valueOf(collectId.split("-")[0]);
            PublicGoodCollect publicGoodCollect = pbLoaderCli.loadUserCollectById(userId, collectId);
            Validate.notNull(publicGoodCollect, "未查询到 collect");

            PublicGoodFeed feed = new PublicGoodFeed();
            feed.setUserId(publicGoodCollect.getUserId());
            feed.setComments(comments);
            feed.setType(PublicGoodFeed.Type.COMMENTS);
            feed.setSourceEnum(LikeSourceEnum.A17);
            feed.setOpId(user.getId());
            feed.setOpName(user.getProfile().getRealname());
            feed.setActivityId(publicGoodCollect.getActivityId());
            MapMessage mapMessage = pbSrvCli.upsertFeed(feed);
            if (mapMessage.isSuccess()) {
                pbSrvCli.addFeedRedis(publicGoodCollect.getActivityId(), publicGoodCollect.getUserId());
            }
            return mapMessage;
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "feed.vpage", method = RequestMethod.GET)
    public MapMessage feed() {
        try {
            User user = currentUser();
            Validate.isTrue(user != null, "请重新登录!");

            String activityId = getRequestString("activityId");
            Validate.notEmpty(activityId, "activityId 不可为空");

            long activityIdLong = SafeConverter.toLong(activityId);
            // 汇总第三方平台数据
            pbSrvCli.collectThirdPartyLike(activityIdLong, user.getId());
            List<PublicGoodFeed> publicGoodFeeds = pbLoaderCli.loadFeedByUserIdCollectId(activityIdLong, user.getId(), true);

            List<FeedRecordMapper> mapperList = convertGroupByDateYear(publicGoodFeeds);

            return MapMessage.successMessage().add("data", mapperList);
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "clazz_info.vpage", method = RequestMethod.GET)
    public MapMessage clazzInfo() {
        try {
            User user = currentUser();
            Validate.isTrue(user != null, "请重新登录!");

            String activityId = getRequestString("activityId");
            Validate.notEmpty(activityId, "activityId 不可为空");

            // 也是醉了 转换头像路径
            MapMessage mapMessage = pbLoaderCli.loadClazzCollect(SafeConverter.toLong(activityId), user.getId());
            if (mapMessage.isSuccess()) {
                @SuppressWarnings(value = {"unchecked"})
                List<SameClazzMapper.ClazzMapper> ck = (List<SameClazzMapper.ClazzMapper>) mapMessage.get("data");
                for (SameClazzMapper.ClazzMapper clazzMapper : ck) {
                    for (CacheCollectMapper cacheCollectMapper : clazzMapper.getCollects()) {
                        cacheCollectMapper.setImgUrl(getUserAvatarImgUrl(cacheCollectMapper.getImgUrl()));
                    }
                }
                mapMessage.put("data", ck);
                return mapMessage;
            }
            return mapMessage;
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    @ResponseBody
    @RequestMapping(value = "share.vpage", method = RequestMethod.GET)
    public MapMessage share(@RequestParam(required = false) String collectId,
                            @RequestParam(required = false) String activityId,
                            @RequestParam(required = false) String userId) {

        try {
            // 学生分享
            if (StringUtils.isEmpty(userId)) {
                return getUserShare(collectId);
            } else {
                //家长分享
                return getParentShare(activityId, userId);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            return MapMessage.errorMessage(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return MapMessage.errorMessage();
    }

    private MapMessage getParentShare(String activityId, String userId) {
        Long userLongId = SafeConverter.toLong(userId);
        Long activityLongId = SafeConverter.toLong(activityId);

        List<PublicGoodCollect> goodCollects = pbLoaderCli.loadCollectByUserId(userLongId).stream().filter(PublicGoodCollect::isFinished).collect(toList());
        List<Long> styleList = goodCollects.stream().map(PublicGoodCollect::getStyleId).collect(Collectors.toList());

        User user = raikouSystem.loadUser(userLongId);

        PublicGoodUserActivity userActivity = pbLoaderCli.loadUserActivityByUserId(activityLongId, userLongId);
        userActivity = Optional.ofNullable(userActivity).orElse(new PublicGoodUserActivity());


        List<PublicGoodFeed> feedList = pbLoaderCli.loadFeedByUserIdCollectId(activityLongId, userLongId, false).stream()
                .filter(i -> i.getType() == PublicGoodFeed.Type.COMMENTS).collect(Collectors.toList());

        List<String> comments = new ArrayList<>();
        for (PublicGoodFeed feed : feedList) {
            String format = String.format("%s:“%s”", TeacherNameWrapper.respectfulName(feed.getOpName()), feed.getComments());
            comments.add(format);
        }

        return MapMessage.successMessage()
                .add("activityId", activityId)
                .add("userId", user.getId())
                .add("userName", user.getProfile().getRealname())
                .add("likeCount", SafeConverter.toLong(userActivity.getLikeNum()))
                .add("moneyCount", SafeConverter.toLong(userActivity.getMoneyNum()))
                .add("collectSize", goodCollects.size())
                .add("certSize", goodCollects.size())
                .add("styleId", styleList)
                .add("comments", comments);
    }

    private MapMessage getUserShare(String collectId) {
        long userId = SafeConverter.toLong(collectId.split("-")[0]);

        PublicGoodCollect collect = pbLoaderCli.loadUserCollectById(userId, collectId);
        Validate.isTrue(collect != null, "记录不存在!");

        PublicGoodStyle style = pbLoaderCli.loadStyleById(collect.getStyleId());
        PublicGoodUserActivity userActivity = pbLoaderCli.loadUserActivityByUserId(collect.getActivityId(), userId);

        List<PublicGoodElementType> publicGoodElementTypes = pbLoaderCli.loadElementTypeByStyleId(collect.getStyleId());
        Map<String, PublicGoodElementType> codeMap = publicGoodElementTypes.stream().collect(toMap(PublicGoodElementType::getCode, Function.identity()));

        User user = raikouSystem.loadUser(userId);
        Long schoolId = null;

        if (user.isStudent()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
            schoolId = studentDetail.getClazz().getSchoolId();
        } else if (user.isTeacher()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(userId);
            schoolId = teacherDetail.getTeacherSchoolId();
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();

        MapMessage resultMessage = MapMessage.successMessage()
                .add("collectId", collect.getId())
                .add("activityId", collect.getActivityId())
                .add("userId", user.getId())
                .add("userName", user.fetchRealname())
                .add("schoolName", school.getCmainName())
                .add("styleId", style == null ? "" : style.getId())
                .add("styleName", style == null ? "" : style.getName())
                .add("likeCount", userActivity == null ? 0 : SafeConverter.toLong(userActivity.getLikeNum()));

        if (!user.isTeacher()) {
            resultMessage.add("moneyCount", userActivity == null ? 0 : SafeConverter.toLong(userActivity.getMoneyNum()));
        }
        return resultMessage;
    }

    /**
     * 把 mongo 中的原始数据按照年和日期分组并转化为前台可用的格式
     */
    private List<FeedRecordMapper> convertGroupByDateYear(List<PublicGoodFeed> publicGoodFeeds) {
        if (publicGoodFeeds == null) {
            return Collections.emptyList();
        }

        Function<PublicGoodFeed, String> groupByYear = feed -> dateToString(feed.getCreateTime(), "yyyy");

        final String dateFormat = "MM-dd";
        Date now = new Date();
        String today = dateToString(now, dateFormat);
        String yesterday = dateToString(DateUtils.addDays(now, -1), dateFormat);

        Function<PublicGoodFeed, String> groupByDay = feed -> {
            String timeString = dateToString(feed.getCreateTime(), "MM-dd");
            if (today.equals(timeString)) {
                return "今天";
            } else if (yesterday.equals(timeString)) {
                return "昨天";
            } else {
                return timeString;
            }
        };

        // 按年分组
        LinkedHashMap<String, List<PublicGoodFeed>> yearAllData = publicGoodFeeds.stream().sorted(feedCreateDesc).collect(groupingBy(groupByYear, LinkedHashMap::new, toList()));

        List<FeedRecordMapper> yearMapperList = new ArrayList<>();

        //年 动态
        for (Map.Entry<String, List<PublicGoodFeed>> stringListEntry : yearAllData.entrySet()) {
            // 专心处理某一年的
            FeedRecordMapper yearMapper = new FeedRecordMapper();

            String yearString = stringListEntry.getKey();
            List<PublicGoodFeed> yearData = stringListEntry.getValue();

            // 按天分组
            LinkedHashMap<String, List<PublicGoodFeed>> dayAllData = yearData.stream().sorted(feedCreateDesc).collect(groupingBy(groupByDay, LinkedHashMap::new, toList()));

            List<FeedRecordMapper.FeedRecord> dayMapperList = new ArrayList<>();

            //天 动态
            for (Map.Entry<String, List<PublicGoodFeed>> entry : dayAllData.entrySet()) {
                FeedRecordMapper.FeedRecord dayMapper = new FeedRecordMapper.FeedRecord();
                String dayString = entry.getKey();
                List<PublicGoodFeed> dayData = entry.getValue();

                List<FeedRecordMapper.FeedRecord.Feed> dayItemMapperList = new ArrayList<>();
                for (PublicGoodFeed item : dayData) {
                    FeedRecordMapper.FeedRecord.Feed feed = new FeedRecordMapper.FeedRecord.Feed();
                    feed.setType(item.getType().name());
                    String timeString = dateToString(item.getCreateTime(), "HH:mm");
                    feed.setTime(timeString);

                    if (item.getType() == PublicGoodFeed.Type.LIKE) {
                        LikeSourceEnum sourceEnum = item.getSourceEnum();
                        if (sourceEnum == LikeSourceEnum.A17) {
                            feed.setContent(item.getOpName() + "为你点了赞");
                        } else {
                            feed.setContent(sourceEnum.getName() + "中有" + item.getCount() + "人为你点了赞");
                        }
                    } else if (item.getType() == PublicGoodFeed.Type.COMMENTS) {
                        feed.setContent(TeacherNameWrapper.respectfulName(item.getOpName()) + "给你留言");
                        feed.setComment(item.getComments());
                    } else {
                        feed.setContent(item.getComments());
                    }
                    dayItemMapperList.add(feed);
                }

                dayMapper.setDate(dayString);
                dayMapper.setFeed(dayItemMapperList);
                dayMapperList.add(dayMapper);
            }

            yearMapper.setYear(yearString);
            yearMapper.setDay(dayMapperList);
            yearMapperList.add(yearMapper);
        }
        return yearMapperList;
    }

    private static Comparator<PublicGoodFeed> feedCreateDesc = (o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime());
}
