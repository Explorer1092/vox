package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareCommentDao;
import com.voxlearning.utopia.service.campaign.impl.dao.TeacherCoursewareDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.campaign.teacher.courseware.evaluation.exchange"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.campaign.teacher.courseware.evaluation.exchange")
        },
        maxPermits = 2
)
@Slf4j
public class TeacherCoursewareEvaluationListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private TeacherCoursewareDao teacherCoursewareDao;

    @Inject
    private TeacherCoursewareCommentDao teacherCoursewareCommentDao;

    @AlpsQueueProducer(queue = "utopia.campaign.teacher.courseware.evaluation.exchange")
    private MessageProducer coursewareProducer;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body == null || !(body instanceof String)) {
            return;
        }
        String json = (String) body;
        Map<String, Object> param = JsonUtils.fromJson(json);
        if (param == null || param.isEmpty()) {
            return;
        }

        String id = SafeConverter.toString(param.get("id"));
        if (StringUtils.isBlank(id)) {
            return;
        }

        // 评论
        String comment_one = SafeConverter.toString(param.get("comment_one"));
        String comment_two = SafeConverter.toString(param.get("comment_two"));
        String comment_three = SafeConverter.toString(param.get("comment_three"));
        String comment_four = SafeConverter.toString(param.get("comment_four"));
        String comment_five = SafeConverter.toString(param.get("comment_five"));
        // 星级
        Integer star = SafeConverter.toInt(param.get("star"));
        // 是否是认证用户
        Boolean authentication = SafeConverter.toBoolean(param.get("authentication"));

        // 并发处理
        try {
            AtomicCallbackBuilderFactory.getInstance().
                    newBuilder().
                    keyPrefix("updateTeacherCourseware").
                    keys(id).
                    callback(()->updateTeacherCourseInfo(id,comment_one,comment_two,
                            comment_three,comment_four,comment_five,star,authentication)).
                    build().
                    execute();
        } catch (CannotAcquireLockException ex){
            try {
                Thread.sleep(300);
            } catch (Exception e){
                logger.error("error",e.getMessage());
            }
            coursewareProducer.produce(message.withPlainTextBody(JsonUtils.toJson(message)));
        }

    }

    public MapMessage updateTeacherCourseInfo(String id, String comment_one, String comment_two,
                                              String comment_three, String comment_four, String comment_five,
                                              Integer star, Boolean authentication){
        try {
            TeacherCourseware teacherCourseware = teacherCoursewareDao.load(id);
            updateCommentLabelInfo(id,comment_one,comment_two,comment_three, comment_four,comment_five,teacherCourseware);
            // 先更新星级信息,再计算评分
            updateStarInfo(id,star,authentication,teacherCourseware);
            updateScoreInfo(id, teacherCourseware);
            return MapMessage.successMessage();
        } catch (Exception e){
            log.error("updateTeacherCourseInfo failed, {}", id, e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 更新评论标签信息
     * @param id
     * @param comment_one
     * @param comment_two
     * @param comment_three
     * @param comment_four
     * @param comment_five
     * @param teacherCourseware
     */
    public void updateCommentLabelInfo(String id,String comment_one,String comment_two,
                                       String comment_three,String comment_four,
                                       String comment_five,TeacherCourseware teacherCourseware){

        List<String> commentList = new ArrayList<>();
        if (StringUtils.isNotEmpty(comment_one)){
            commentList.add(comment_one);
        }
        if (StringUtils.isNotEmpty(comment_two)){
            commentList.add(comment_two);
        }
        if (StringUtils.isNotEmpty(comment_three)){
            commentList.add(comment_three);
        }
        if (StringUtils.isNotEmpty(comment_four)){
            commentList.add(comment_four);
        }
        if (StringUtils.isNotEmpty(comment_five)){
            commentList.add(comment_five);
        }

        if (CollectionUtils.isEmpty(commentList)) {
            return;
        }

        Map<String,Integer> labelInfo = teacherCourseware.getLabelInfo();
        if (MapUtils.isEmpty(labelInfo)) {
            labelInfo = new HashedMap<>();
        }

        for (String comment : commentList) {
            if (labelInfo.containsKey(comment)) {
                labelInfo.put(comment, SafeConverter.toInt(labelInfo.get(comment)) + 1);
            } else {
                labelInfo.put(comment, 1);
            }
        }

        teacherCoursewareDao.updateLabelInfo(id, labelInfo);
    }

    /**
     * 更新星级信息
     * @param id
     * @param star
     * @param authentication
     * @param teacherCourseware
     */
    public void updateStarInfo(String id, Integer star, Boolean authentication, TeacherCourseware teacherCourseware){

        // 根据是否是认证用户获取课件的星级信息
        Map<Integer,Integer> starInfo = authentication ? teacherCourseware.getAuthenticatedStarInfo() : teacherCourseware.getGeneralStarInfo();
        if (MapUtils.isEmpty(starInfo)) {
            starInfo = new HashMap<>();
        }

        if (starInfo.containsKey(star)) {
            starInfo.put(star, SafeConverter.toInt(starInfo.get(star)) + 1);
        } else {
            starInfo.put(star, 1);
        }

        teacherCoursewareDao.updateCourseStarInfo(id, starInfo, authentication);
    }

    /**
     * 更新评分信息
     *
     * @param teacherCourseware
     */
    public void updateScoreInfo(String id, TeacherCourseware teacherCourseware){
        int score = calc(teacherCourseware.getAuthenticatedStarInfo(), teacherCourseware.getGeneralStarInfo());
        teacherCoursewareDao.updateScoreInfo(id, score);
    }

    /**
     *
     * @param as
     * @param uas
     * @return
     */
    private static int calc(Map<Integer,Integer> as, Map<Integer,Integer> uas){
        // 统计非认证用户各星级个数
        double score = 0;
        if (MapUtils.isNotEmpty(uas)){
            score = new Star(toInt(uas.get(1)),
                    toInt(uas.get(2)),
                    toInt(uas.get(3)),
                    toInt(uas.get(4)),
                    toInt(uas.get(5)), 0.3).score();
        }
        // 统计认证用户各星级个数
        if (MapUtils.isNotEmpty(as)) {
            score += new Star(toInt(as.get(1)),
                    toInt(as.get(2)),
                    toInt(as.get(3)),
                    toInt(as.get(4)),
                    toInt(as.get(5)), (score > 0 ? 0.7 : 1)).score();
        }
        return (int)Math.round(score);
    }

    /**
     * 星级评论
     *
     * @return
     */
    @AllArgsConstructor
    public static class Star{
        int c1, c2, c3, c4, c5;//1~5星评论人数
        double factor = 1;//因子

        public double score() {
            int i = c1 + c2 + c3 + c4 + c5;
            if (i == 0) return 0d;
            return (c1 * 20 + c2 * 40 + c3 * 60 + c4 * 80 + c5 * 100) * factor / i;
        }
    }

    public static void main(String[] args) {
        System.out.println(calc(MapUtils.map(5,1), MapUtils.map(1,1,2,4,3,3,4,3,5,1)));
    }
}
