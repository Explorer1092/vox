package com.voxlearning.washington.controller.mobile.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.service.conversation.api.HomeworkThankLoader;
import com.voxlearning.utopia.service.reward.util.CacheKeyUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.FlowerExchangeHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.constant.LikeType;
import com.voxlearning.washington.mapper.LikeAccumulateMapper;
import com.voxlearning.washington.mapper.LikeExchangeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by jiangpeng on 16/9/9.
 */
@Controller
@RequestMapping(value = "/teacherMobile/flower")
@Slf4j
public class MobileTeacherLikeController extends AbstractMobileTeacherController implements InitializingBean {

    @ImportService(interfaceClass = HomeworkThankLoader.class)
    private HomeworkThankLoader homeworkThankLoader;
    private IRedisCommands redisCommands;


    /**
     * 我的点赞页面
     *
     * @return
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index() {
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;

        Boolean multipleSubject = teacher.getSubjects().size() > 1;

        List<String> subjectList = new ArrayList<>();
        if (multipleSubject) {
            List<Subject> subjects = teacher.getSubjects();
            for (Subject subject : subjects) {
                subjectList.add(subject.name());
            }
        }

        Subject currentSubject = currentSubject() == null ? teacher.getSubject() : currentSubject();
        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), currentSubject);
        if (relTeacherId == null || relTeacherId == 0L)
            return MapMessage.errorMessage("当前教师无" + currentSubject.getValue() + "学科");

        List<Clazz> clazzList = getTeacherClazzListForFlower(teacher, currentSubject);

        long likeCount = 0;
        for (Clazz clazz : clazzList) {
            List<Flower> flowerList = flowerServiceClient.getFlowerService()
                    .loadClazzFlowers(clazz.getId()).getUninterruptibly();
            if (CollectionUtils.isEmpty(flowerList)) {
                continue;
            }
            likeCount += flowerList.stream()
                    .filter(flower -> relTeacherId.equals(flower.getReceiverId())
                            && MonthRange.current().contains(flower.getCreateDatetime()))
                    .count();
        }

        boolean hasNewThanks = redisCommands.sync().getRedisStringCommands().getbit("teacher_like", relTeacherId) == 1L;

        return MapMessage.successMessage()
                .add("main_subject", currentSubject.name())
                .add("subject_list", subjectList)
                .add("like_count", likeCount)
                .add("like_text", LikeType.of(likeCount).getText())
                .add("like_title", LikeType.of(likeCount).getTitle())
                .add("teacherName", teacher.getProfile().getRealname())
                .add("cityName", currentTeacherDetail().getCityName())
                .add("has_new_thank", hasNewThanks);
    }

    /**
     * 家长感谢语详情页
     *
     * @return
     */
    @RequestMapping(value = "parent_thanks.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentThanks() {
        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        Subject currentSubject = currentSubject();
        if (currentSubject == null)
            return MapMessage.errorMessage("学科是必填参数");

        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), currentSubject);
        if (relTeacherId == null || relTeacherId == 0L)
            return MapMessage.errorMessage("当前教师无" + currentSubject.getValue() + "学科");
        //红点消失
        redisCommands.async().getRedisStringAsyncCommands().setbit("teacher_like", relTeacherId, 0);

        return homeworkThankLoader.loadHomeworkThanks(relTeacherId)
                .add("cityName", currentTeacherDetail().getCityName());
    }

    /**
     * 点赞获取详情页
     *
     * @return
     */
    @RequestMapping(value = "like_get_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage likeGetDetail() {

        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        Subject currentSubject = currentSubject();
        if (currentSubject == null)
            return MapMessage.errorMessage("学科是必填参数");

        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), currentSubject);
        if (relTeacherId == null || relTeacherId == 0L)
            return MapMessage.errorMessage("当前教师无" + currentSubject.getValue() + "学科");


        List<Clazz> clazzList = getTeacherClazzListForFlower(teacher, currentSubject);
        if (CollectionUtils.isEmpty(clazzList)) {
            return MapMessage.errorMessage("教师无对应的班级");
        }

        List<LikeAccumulateMapper> resultList = caculateLikeAccumulate(relTeacherId, clazzList);

        if (CollectionUtils.isEmpty(resultList)) {
            return MapMessage.errorMessage("暂无记录");
        }

        return MapMessage.successMessage().add("data", resultList)
                .add("cityName", currentTeacherDetail().getCityName());
    }

    /**
     * 点赞使用详情页
     *
     * @return
     */
    @RequestMapping(value = "like_exchange_history.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage likeExchangeHistory() {

        Teacher teacher = currentTeacher();
        if (teacher == null)
            return noLoginResult;
        Subject currentSubject = currentSubject();
        if (currentSubject == null)
            return MapMessage.errorMessage("学科是必填参数");

        Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), currentSubject);
        if (relTeacherId == null || relTeacherId == 0L)
            return MapMessage.errorMessage("当前教师无" + currentSubject.getValue() + "学科");


        List<Clazz> clazzList = getTeacherClazzListForFlower(teacher, currentSubject);
        if (CollectionUtils.isEmpty(clazzList)) {
            return MapMessage.errorMessage("教师无对应的班级");
        }
        List<Long> clazzIdList = clazzList.stream().map(Clazz::getId).collect(toList());

        // 获取本月当前老师 当前班级 已经兑换的记录
        List<FlowerExchangeHistory> histories = flowerServiceClient.getFlowerService()
                .loadTeacherFlowerExchangeHistories(relTeacherId)
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(histories)) {
            return MapMessage.errorMessage("暂无记录");
        }
        histories = histories.stream()
                .filter(h -> clazzIdList.contains(h.getClazzId()))
                .sorted(new Comparator<FlowerExchangeHistory>() {
                    @Override
                    public int compare(FlowerExchangeHistory o1, FlowerExchangeHistory o2) {
                        return o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                    }
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(histories)) {
            return MapMessage.errorMessage("暂无记录");
        }

        List<LikeExchangeMapper> resultList = new ArrayList<>();
        for (FlowerExchangeHistory history : histories) {
            Clazz cla = clazzList.stream()
                    .filter(clazz -> clazz.getId().equals(history.getClazzId()))
                    .findFirst().orElse(null);
            LikeExchangeMapper likeExchangeMapper = new LikeExchangeMapper();
            likeExchangeMapper.setCreate(history.getCreateDatetime());
            likeExchangeMapper.setNum(10L);
            likeExchangeMapper.setClazzName(cla.formalizeClazzName());
            resultList.add(likeExchangeMapper);
        }

        return MapMessage.successMessage().add("data", resultList).add("cityName", currentTeacherDetail().getCityName());
    }

    private List<LikeAccumulateMapper> caculateLikeAccumulate(Long relTeacherId, List<Clazz> clazzList) {
        List<LikeAccumulateMapper> resultList = new ArrayList<>();

        for (Clazz clazz : clazzList) {
            List<Flower> flowerList = flowerServiceClient.getFlowerService()
                    .loadClazzFlowers(clazz.getId()).getUninterruptibly();
            if (CollectionUtils.isEmpty(flowerList)) {
                continue;
            }
            List<Flower> flowers = flowerList.stream()
                    .filter(flower -> relTeacherId.equals(flower.getReceiverId())).collect(toList());
            if (CollectionUtils.isEmpty(flowers)) {
                continue;
            }

            List<Flower> medalFlowers = flowers.stream()
                    .filter(flower -> flower.getSourceType().equals(FlowerSourceType.MORAL_MEDAL.name()))
                    .collect(toList());
            Map<String, List<Flower>> homeworkCommentFlowers = flowers.stream()
                    .filter(flower -> flower.getSourceType().equals(FlowerSourceType.HOMEWORK_COMMENT.name()))
                    .sorted(new Comparator<Flower>() {
                        @Override
                        public int compare(Flower o1, Flower o2) {
                            return o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
                        }
                    })
                    .collect(Collectors.groupingBy(m -> m.getHomeworkId(), toList()));
            Map<String, List<Flower>> homeworkFlowers = flowers.stream()
                    .filter(flower -> flower.getSourceType().equals(FlowerSourceType.HOMEWORK.name()))
                    .sorted(new Comparator<Flower>() {
                        @Override
                        public int compare(Flower o1, Flower o2) {
                            return o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
                        }
                    })
                    .collect(Collectors.groupingBy(m -> m.getHomeworkId(), toList()));

            if (CollectionUtils.isNotEmpty(medalFlowers)) {
                for (Flower medalFlower : medalFlowers) {
                    LikeAccumulateMapper likeAccumulateMapper = new LikeAccumulateMapper();
                    likeAccumulateMapper.setFlowerSourceType(FlowerSourceType.MORAL_MEDAL.name());
                    likeAccumulateMapper.setClazzName(clazz.formalizeClazzName());
                    likeAccumulateMapper.setNum(1L);
                    likeAccumulateMapper.setCreate(medalFlower.getCreateDatetime());
                    resultList.add(likeAccumulateMapper);
                }
            }

            if (CollectionUtils.isNotEmpty(homeworkCommentFlowers.keySet())) {
                for (String homeworkId : homeworkCommentFlowers.keySet()) {
                    List<Flower> list = homeworkCommentFlowers.get(homeworkId);
                    LikeAccumulateMapper likeAccumulateMapper = new LikeAccumulateMapper();
                    likeAccumulateMapper.setFlowerSourceType(FlowerSourceType.HOMEWORK_COMMENT.name());
                    likeAccumulateMapper.setClazzName(clazz.formalizeClazzName());
                    likeAccumulateMapper.setNum(SafeConverter.toLong(list.size()));
                    likeAccumulateMapper.setCreate(list.get(0).getCreateDatetime());
                    resultList.add(likeAccumulateMapper);
                }
            }

            if (homeworkFlowers == null || CollectionUtils.isEmpty(homeworkFlowers.keySet())) {
                continue;
            }
            for (String homeworkId : homeworkFlowers.keySet()) {
                List<Flower> list = homeworkFlowers.get(homeworkId);
                LikeAccumulateMapper likeAccumulateMapper = new LikeAccumulateMapper();
                likeAccumulateMapper.setFlowerSourceType(FlowerSourceType.HOMEWORK.name());
                likeAccumulateMapper.setClazzName(clazz.formalizeClazzName());
                likeAccumulateMapper.setNum(SafeConverter.toLong(list.size()));
                likeAccumulateMapper.setCreate(list.get(0).getCreateDatetime());
                resultList.add(likeAccumulateMapper);
            }

        }

        List<LikeAccumulateMapper> result = resultList.stream().sorted(new Comparator<LikeAccumulateMapper>() {
            @Override
            public int compare(LikeAccumulateMapper o1, LikeAccumulateMapper o2) {
                return o2.getCreate().compareTo(o1.getCreate());
            }
        }).collect(toList());

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        redisCommands = builder.getRedisCommands("user-easemob");
    }
}
