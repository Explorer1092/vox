package com.voxlearning.utopia.service.crm.api.entities.crm;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.crm.AppPushMsgConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于保存位于工作流中的AppPush消息
 * Created by yuechen.wang on 2017/03/21.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "app_push_workflow")
@UtopiaCacheRevision("170418")
public class AppPushWfMessage implements CacheDimensionDocument {
    private static final long serialVersionUID = -3810622607443054611L;
    private static final String SEP = ",";

    @DocumentId
    private String id;
    private Long recordId;           // 对应 工作流记录的 ID
    private String status;           // 工作流状态
    private Date sendTime;           // 发送时间
    private String sendStatus;       // 发送状态 waiting/success/failed

    private String userType;         // 原来的sendApp字段含义畸形，当userType用的，现在正过来
    private String sendApp;          // 要发送的APP端
    private Integer pushType;        // 发送方式
    private Boolean onlyPush;        // 发送Push
    private Boolean onlyMsg;         // 发送系统消息
    private Boolean isTop;           // 是否置顶
    private String topEndTimeStr;    // 置顶截止时间
    private String notifyContent;    // JPush 内容
    private String title;            // 消息标题
    private String content;          // 消息概要
    private String fileName;         // 图片地址
    private String link;             // 内容地址
    private Integer durationTime;    // 推送时长

    // 学生端专享字段
    private Integer msgExtType;      // 消息扩展类型

    // 家长端专享字段
    private String messageTag;
    private Boolean share;            // 是否可分享
    private String shareContent;      // 分享文案
    private String shareUrl;          // 分享地址
    private Integer idType;           // 按学生ID发送

    // 扩展字段
    private String ktwelve;           // 学段，j->小学，m->中学, i->学前
    private String clazzLevel;        // 年级，逗号连接， 1~9
    private String subject;           // 学科，逗号连接，CHINESE,ENGLISH
    private String authStat;          // 认证状态，逗号连接，WAITING,SUCCESS
    private Boolean paymentBlackList; // 只发送付费黑名单用户
    private Boolean noneBlackList;    // 不包含黑名单

    // 投放策略
    private List<Long> targetUser;      // 投放用户
    private String fileUrl;             // 用户ID文件
    private List<Integer> targetRegion; // 投放地区
    private List<Long> targetSchool;    // 投放学校
    private List<List<Map<String, Object>>> targetTagGroups;      // 投放的指定标签  [[{tagId:3091511,tagName:自学产品层面}],[{tagId:3091513,tagName:增值产品层面},{tagId:3091512,tagName:作业产品层面}]]
    private Set<Long> groupIds;        // 投放的群组ID
    //环信消息需要的字段
    private String easeMobFileName;     //发到环信群文件的文件名
    private String easeMobFileUuid;     //发到环信群文件的uuid

    @DocumentFieldIgnore private boolean isFast = false; // 快速推送

    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    public boolean canSendPush() {
        return Boolean.TRUE.equals(onlyPush);
    }

    public boolean canSendMsg() {
        return Boolean.TRUE.equals(onlyMsg);
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }

    /**
     * 判断投放用户的类型
     */
    public UserType fetchTargetUserType() {
        if (userType == null) return null;
        switch (userType) {
            case "teacher":
                return UserType.TEACHER;
            case "parent":
                if (idType == 2)
                    return UserType.PARENT;
                if (idType == 1)
                    return UserType.STUDENT;
                return null;
            case "student":
                return UserType.STUDENT;
            default:
                return null;
        }
    }

    public UserType fetchUserType() {
        if (userType == null) return null;
        switch (userType) {
            case "teacher":
                return UserType.TEACHER;
            case "parent":
                return UserType.PARENT;
            case "student":
                return UserType.STUDENT;
            default:
                return null;
        }
    }

    /**
     * 获取指定截止之间的时间戳
     */
    public Long fetchTopEndTime() {
        return StringUtils.isNotBlank(topEndTimeStr) ? DateUtils.stringToDate(topEndTimeStr, AppPushMsgConstants.dateFormat).getTime() : 0L;
    }

    /**
     * 获取学科（老师属性）
     */
    public Subject fetchSubject() {
        return parseSubjects().stream().findFirst().orElse(null);
    }

    public List<Subject> parseSubjects() {
        if (StringUtils.isBlank(subject)) {
            return Collections.emptyList();
        }
        return Arrays.stream(subject.split(SEP)).map(Subject::ofWithUnknown)
                .filter(sub -> Subject.UNKNOWN != sub)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取认证状态（老师属性）
     */
    public AuthenticationState fetchAuthenticationState() {
        return parseAuthenticationStates().stream().findFirst().orElse(null); // 认证状态
    }

    public List<AuthenticationState> parseAuthenticationStates() {
        if (StringUtils.isBlank(authStat)) {
            return Collections.emptyList();
        }
        return Arrays.stream(authStat.split(SEP))
                .map(auth -> AuthenticationState.safeParse(SafeConverter.toInt(auth), null))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取年级
     */
    public ClazzLevel fetchClazzLevel() {
        return parseClazzLevels().stream().findFirst().orElse(null);
    }


    public List<ClazzLevel> parseClazzLevels() {
        return Arrays.stream(clazzLevel.split(SEP))
                .map(SafeConverter::toInt)
                .map(ClazzLevel::parse)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断是否是黑名单地区（学生/家长属性）
     */
    public boolean inPaymentBlackList() {
        return Boolean.TRUE.equals(paymentBlackList);
    }

    /**
     * 判断是否是非黑名单地区（学生/家长属性）
     */
    public boolean inNoneBlackList() {
        return Boolean.TRUE.equals(noneBlackList);
    }

    /**
     * 判断扩展条件是否只有一个多选
     */
    public boolean checkOnlyOneMultiChoice() {
        UserType userType = fetchUserType();
        if (userType == null) {
            return false;
        }
        List<Integer> selectedCount = new ArrayList<>();
        selectedCount.add(parseClazzLevels().size()); // 年级
        selectedCount.add(CollectionUtils.isEmpty(targetRegion) ? 0 : targetRegion.size()); // 地区
        selectedCount.add(CollectionUtils.isEmpty(targetSchool) ? 0 : targetSchool.size()); // 学校
        if (UserType.TEACHER == userType) {
            selectedCount.add(parseSubjects().size()); // 学科
            selectedCount.add(parseAuthenticationStates().size());  // 认证状态
        }
        return selectedCount.stream().filter(cnt -> cnt > 1).count() <= 1;
    }

    public Long fetchSendTime() {
        if (sendTime == null || sendTime.before(new Date())) {
            return 0L;
        }
        Long originEpochMilli = sendTime.getTime();
        Long SEND_TIME_DISCRETE_BASED_SECONDS = 5 * 60L;

        // copy from JpushTimingMessageSendTimeCalculator.sendTimeCeil
        if (Long.MAX_VALUE < originEpochMilli || originEpochMilli <= 0) {
            return 0L;
        }

        Long originEpochSecond = originEpochMilli / 1000;

        if (originEpochSecond % SEND_TIME_DISCRETE_BASED_SECONDS != 0) {
            originEpochSecond = originEpochSecond - originEpochSecond % SEND_TIME_DISCRETE_BASED_SECONDS + SEND_TIME_DISCRETE_BASED_SECONDS;
        }

        return originEpochSecond * 1000;
    }

}
