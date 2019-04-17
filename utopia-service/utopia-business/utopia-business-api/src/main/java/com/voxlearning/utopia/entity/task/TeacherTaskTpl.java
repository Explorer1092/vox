package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 老师任务模板(字典)
 *
 * 已经从表迁移到配置文件eacherTask
 *
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_task_tpl")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("201809020")
public class TeacherTaskTpl implements CacheDimensionDocument {

    private static final long serialVersionUID = 4999312244316017679L;

    /**
     * 小学学科的模板
     */
    @Getter
    private static final List<Tpl> primaryTpl = new ArrayList<>();

    /**
     * 非小学学科的模板
     */
    @Getter
    private static final List<Tpl> notPrimaryTpl = new ArrayList<>();

    static {
        primaryTpl.add(Tpl.PRIMARY_ROOKIE);
        primaryTpl.add(Tpl.PRIMARY_AWAKE);
        primaryTpl.add(Tpl.PRIMARY_INVITATION);
        primaryTpl.add(Tpl.PRIMARY_THREE_HOMEWORK);
        primaryTpl.add(Tpl.PRIMARY_USER_SIGN_IN);
        primaryTpl.add(Tpl.PRIMARY_ASSIGN_CHECK_HOMEWORK);
        primaryTpl.add(Tpl.PRIMARY_COMMENT_AND_AWARD_HOMEWORK);
        primaryTpl.add(Tpl.PRIMARY_TOPIC_ERROR);
        primaryTpl.add(Tpl.PRIMARY_RECEIVE_AWARD);
        primaryTpl.add(Tpl.PRIMARY_SHARE_ARTICLE);
        primaryTpl.add(Tpl.PRIMARY_USER_INFO_FULL);
        primaryTpl.add(Tpl.PRIMARY_ROOKIE_ENGLISH_CHINESE);

        notPrimaryTpl.add(Tpl.JUNIOR_ROOKIE);
        notPrimaryTpl.add(Tpl.JUNIOR_INVITATION);
    }

    @DocumentId private Long id;
    private String name;
    private String instruction;         // 任务说明
    private String buttonName;          // 去完成说明
    private String type;                // 新手、限时还是常规则；TimeLimit：限时任务、Rookie：新手任务、Unknown：未知任务
    private Integer sort;               // 排序
    private Boolean loop;               // 是否为循环任务
    private String cycleUnit;           // 循环周期的单位、D:按天循环 W:按周循环 M：按月循环，O:其他
    private Integer cycle;              // 周期
    private Boolean autoReceive;        // 是否为自动领取
    private String putOnExpr;           // 判断是否为投放对象的表达式
    private Integer activeTime;         // 有效时间，单位天
    private List<SubTask> subTaskList;  // 子任务列表
    private List<Reward> rewards;       // 所有子任务完成后，可以发钱
    private Map<String,Object> extAttr; // 以防万一，额外添加的属性

    /**
     * 任务的模板枚举
     * @author zhouwei
     */
    @Getter
    public enum Tpl{
        PRIMARY_ROOKIE(1L),                     //小学数学新手任务
        PRIMARY_AWAKE(2L),                      //小学全科唤醒任务
        PRIMARY_INVITATION(3L),                 //小学全科邀请新老师任务
        PRIMARY_THREE_HOMEWORK(4L),             //小学数学三次作业
        JUNIOR_ROOKIE(5L),                      //初中英语新手任务
        JUNIOR_INVITATION(6L),                  //初中全科邀请新老师任务
        PRIMARY_USER_SIGN_IN(7L),               //小学全科老师签到
        PRIMARY_ASSIGN_CHECK_HOMEWORK(8L),      //小学全科布置检查作业的常规任务
        PRIMARY_COMMENT_AND_AWARD_HOMEWORK(9L), //小学全科评论奖励学生
        PRIMARY_TOPIC_ERROR(10L),               //小学全科题目报错
        PRIMARY_RECEIVE_AWARD(11L),             //小学全科提学生接受奖励
        PRIMARY_SHARE_ARTICLE(12L),             //小学全科分享文章
        PRIMARY_USER_INFO_FULL(13L),            //小学全科完善个人信息
        PRIMARY_ROOKIE_ENGLISH_CHINESE(14L),    //小学语文、英语新手任务
        PRIMARY_SHARE_REPORT(15L),              //小学全科分享练习报告
        FINAL_BASIC_REVIEW(16L),                //期末复习-基础必过
        FINAL_TERM_REVIEW(17L),                 //期末复习-重点复习
        WINTER_WORK(18L),                       //寒假作业
        INVITATION_2019(19L),                   //2019年年初寒假期间邀请老师

        ROOKIE_2019(20L),                       //2019年上学期新手任务
        LIMIT_TIME_2019(21L),                   //2019年上学期限时任务

        ASSIGN_HOMEWORK_2019(22L),              //2019年上学期布置作业
        CHECK_HOMEWORK_2019(23L),               //2019年上学期检查作业
        COMMENT_STUDENT_2019(24L),              //2019年上学期点评奖励学生
        SHARE_HOMEWORK_REPORT_2019(25L),        //2019年上学期分享作业报告

        INVITATION_FIRST_2019(26L),             //2019年上学期邀请老师
        WEEK_CHECK_HOMEWORK_2019(27L),          // 新的每周检查3次作业

        ;

        private long tplId;

        Tpl(long tplId){
            this.tplId = tplId;
        }

        public static TeacherTaskTpl.Tpl getTplById(Long id) {
            if (null == id) {
                return null;
            }
            for (Tpl tpl : TeacherTaskTpl.Tpl.values()) {
                if (tpl.getTplId() == id) {
                    return tpl;
                }
            }
            return null;
        }
    }

    public enum RewardUnit {
        exp,
        integral,
        cash
    }


    /**
     * 产生任务完成与更新任务进度的事件源
     * @author zhouwei
     */
    @Getter
    public enum TplEvaluatorEvent {
        DEFAULT,                            //默认的，不需要任何事件触发的任务类型
        UPDATE_TASK_INFO,                   //需要更新相关任务信息的事件，并不是触发任务进度的事件

        ASSIGN_HOMEWORK,                    //布置作业
        CHECK_HOMEWORK,                     //检查作业
        CHECK_HOMEWORK_V2,                  //检查作业v2
        CREATE_CLAZZ,                       //建立班级
        REPORT_HOMEWORK,                    //分享作业
        COMMENT_HOMEWORK,                   //点评学生
        COMMENT_AND_AWARD_HOMEWORK,         //点评或者奖励学生事件
        USER_SIGN_IN,                       //签到
        USER_AWAKE,                         //唤醒
        USER_INVITATION,                    //邀请新老师
        SHARE_ARTICLE,                      //分享文章
        USER_INFO_CHANGED,                  //用户信息变更
        ;
    }

    /**
     * 获取tpl的模板的任务类型
     * @return
     */
    public Tpl getTpl() {
        for (Tpl tpl : Tpl.values()) {
            if (Objects.equals(tpl.getTplId(), this.getId())) {
                return tpl;
            }
        }
        return null;
    }

    @Getter
    public enum Type{
        Top("置顶任务", 1),
        Rookie("新人福利", 2),
        DAY("常规任务", 3),
        Special("特别任务", 4),
        TimeLimit("限时福利", 5),
        @Deprecated
        Routine("常规任务", 6),
        Unknown("未知任务", 7),
        Deprecated("老任务", 8),
        ;

        private String desc;
        private int sort;

        Type(String desc, int sort){
            this.desc = desc;
            this.sort = sort;
        }

        public static Type of(String type){
            try{
                return valueOf(type);
            }catch (Exception e){
                return Unknown;
            }
        }
    }

    /*** 子任务 ***/
    @Getter
    @Setter
    public static class SubTask implements Serializable{
        private static final long serialVersionUID = 8233080353086309376L;
        private Long id;                            // 子任务的ID，【注意】请不要随意修改这个ID，该ID会被不少地方关联
        private String desc;                        // 描述
        private String calType;                     // 计算类型，标明任务变量记算的方式，HOMEWORK与NORMAL
        private String expression;                  // 表达式，子任务完成的条件
        private Long prevCondId;                    // 前置条件子任务ID
        private Boolean showProgress;               // 是否展示进度
        private Progress progress;                  // 进度信息
        private Map<String,Object> skip;            // 跳转的信息，给前端提供的
        private List<Reward> rewards;               // 奖励列表，任务的奖励信息，可能会有多种奖励（经验值、园丁都、现金）
        private Map<String,Object> extAttr;         // 以防万一，额外添加的属性

        @DocumentFieldIgnore
        private SubTask prevCondTask;   // 前置条件

        @DocumentFieldIgnore
        private int orderWeight;        // 排序的权重值，根据前置条件的个数决定。越少越靠前

        public int getOrderWeight(){
            if(prevCondId == null)
                return 1;
            else
                return orderWeight;
        }

        public List<Reward> getRewards(){
            if(CollectionUtils.isEmpty(rewards))
                this.rewards = new ArrayList<>();

            return this.rewards;
        }
    }

    /** 奖励 **/
    @Getter
    @Setter
    public static class Progress implements Serializable {
        private static final long serialVersionUID = 8859571398164749224L;
        private String targetExpr;          // 目标值的计算公式，注意：tagert与tagertExpr取其一即可，某些场景，目标是动态的
        private String proExpr;             // 计算进度的表达式，计算进度的表达式（ 如：proExpr/targer ）
        private String quantifier;          // 任务的量词
        private List<Reward> rewards;       // 奖励列表，任务的奖励信息，可能会有多种奖励（经验值、园丁都、现金）
        private Map<String,Object> extAttr; // 以防万一，额外添加的属性
    }

    /** 总任务与子任务奖励 **/
    @Getter
    @Setter
    public static class Reward implements Serializable {
        private static final long serialVersionUID = -7214442689896543097L;
        private Long id;                    // ID，【注意】请不要随意修改这个ID，该ID会被不少地方关联
        private String unit;                // 单位，integral：园丁豆、exp：经验、cash：现金
        private String expression;          // 表达式，用来计算奖励。
        private Map<String,Object> extAttr; // 以防万一，额外添加的属性
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ALL")
        };
    }

    public void getSubTasksForEach(Consumer<SubTask> consumer){
        if(CollectionUtils.isEmpty(subTaskList))
            return;

        Map<Long, SubTask> subTaskMap = subTaskList.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
        // 根据前置任务，调整顺序
        for(SubTask st : subTaskList){
            SubTask tail = st;
            int orderWeight = 0;
            while (tail.prevCondTask == null && tail.prevCondId != null) {
                st.prevCondTask = subTaskMap.get(st.prevCondId);
                tail = st.prevCondTask;
                orderWeight ++;
            }

            orderWeight += tail.getOrderWeight();
            st.setOrderWeight(orderWeight);
        }

        subTaskList.sort(Comparator.comparingInt(s -> s.orderWeight));
        subTaskList.forEach(consumer);
    }

    public int getSubTaskNum(){
        return Optional.ofNullable(subTaskList).map(stl -> stl.size()).orElse(0);
    }

    public SubTask getSubTaskById(Long id){
        return Optional.ofNullable(subTaskList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(st -> Objects.equals(st.getId(),id))
                .findFirst()
                .orElse(null);
    }

    public IntegralType getTypeByTpl() {
        return getByTplId(this.getId());
    }

    public static IntegralType getByTplId(Long tplId) {
        switch (tplId.intValue()) {
            case 1: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_ROOKIE;
            case 2: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_AWAKE;
            case 3: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_INVITATION;
            case 4: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_THREE_HOMEWORK;
            case 5: return IntegralType.TEACHER_GROWTH_REWARD_TASK_JUNIOR_ROOKIE;
            case 6: return IntegralType.TEACHER_GROWTH_REWARD_TASK_JUNIOR_INVITATION;
            case 7: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_USER_SIGN_IN;
            case 8: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_ASSIGN_CHECK_HOMEWORK;
            case 9: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_COMMENT_AND_AWARD_HOMEWORK;
            case 10: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_TOPIC_ERROR;
            case 11: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_RECEIVE_AWARD;
            case 12: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_SHARE_ARTICLE;
            case 13: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_USER_INFO_FULL;
            case 14: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_ROOKIE_ENGLISH_CHINESE;
            case 15: return IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_SHARE_REPORT;
            case 16: return IntegralType.TEACHER_GROWTH_REWARD_TASK_FINAL_BASIC_REVIEW;
            case 17: return IntegralType.TEACHER_GROWTH_REWARD_TASK_FINAL_TERM_REVIEW;
            case 18: return IntegralType.TEACHER_GROWTH_REWARD_TASK_WINTER_WORK;
            case 22: return IntegralType.TEACHER_DAY_TASK_ASSIGN_CHECK;
            case 23: return IntegralType.TEACHER_DAY_TASK_ASSIGN_CHECK;
            case 24: return IntegralType.TEACHER_DAY_TASK_COMMENT_SHARE;
            case 25: return IntegralType.TEACHER_DAY_TASK_COMMENT_SHARE;
            default: return IntegralType.TEACHER_GROWTH_REWARD;
        }
    }
}
