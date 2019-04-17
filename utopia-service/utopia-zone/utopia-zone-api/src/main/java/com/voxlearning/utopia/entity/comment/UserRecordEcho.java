package com.voxlearning.utopia.entity.comment;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.aerospike.DocumentNamespace;
import com.voxlearning.alps.annotation.dao.aerospike.DocumentSet;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户动态相关记录
 * Created by Yuechen.Wang on 17/11/06.
 */
@Getter
@Setter
@DocumentConnection(configName = "utopiaex")
@DocumentNamespace(namespace = "vox")
@DocumentSet(setName = "UserRecordEcho")
@EqualsAndHashCode(of = "id")
public class UserRecordEcho implements Serializable {

    private static final long serialVersionUID = 7260394331860956469L;

    public static final int COMMENT_LIMIT = 3;

    @DocumentId private String id;                 // ComplexID(recordId_recordType)
    @DocumentField("CL") private List<UserRecordSnapshot> commentList;        // 评论列表
    // @DocumentField("EL") private List<UserRecordSnapshot> encourageList;      // 鼓励列表
    @DocumentRevision private Integer revision;
    @DocumentExpiration private Date expiration;

    public static UserRecordEcho createByClazzJournal(ClazzJournal clazzJournal) {
        if (clazzJournal == null || clazzJournal.getJournalType() == null) {
            return null;
        }
        UserRecordEcho info = new UserRecordEcho();
        String recordId = clazzJournal.getId() + "_CLAZZ_JOURNAL";
        info.setId(recordId);
        info.setCommentList(new ArrayList<>());

        ClazzJournalType journalType = clazzJournal.getJournalType();
        info.setExpiration(DateUtils.calculateDateDay(new Date(), journalType.getDuration()));

        return info;
    }

    public static UserRecordEcho createByClazzRecord(String recordId, UserLikeType likeType) {
        UserRecordEcho info = new UserRecordEcho();
        info.setId(StringUtils.join(recordId, "_", likeType.name()));
        info.setCommentList(new ArrayList<>());
        info.setExpiration(DateUtils.calculateDateDay(new Date(), likeType.getDuration()));
        return info;
    }

    public void comment(UserRecordSnapshot snapshot) {
        if (alreadyComment(snapshot.getUserId())) {
            return;
        }

        if (commentList == null) {
            commentList = new LinkedList<>();
        }

        CollectionUtils.addNonNullElement(commentList, snapshot);

        // 控制下大小，暂时只允许最大30条吧
        if (commentList.size() > 30) {
            commentList.remove(0);
        }
    }

    public void recallComment(Long userId, String comment) {
        if (userId == null || StringUtils.isBlank(comment)) {
            return;
        }
        if (commentList == null || commentList.isEmpty()) {
            return;
        }
        commentList.removeIf(c -> Objects.equals(c.getUserId(), userId) && StringUtils.equals(c.getComment(), comment));
    }

    public boolean alreadyComment(Long userId) {
        return commentList != null && commentList.stream().filter(c -> Objects.equals(c.getUserId(), userId)).count() >= 3;
    }

    public List<Map<String, Object>> commentInfoList() {
        if (commentList == null || commentList.isEmpty()) {
            return Collections.emptyList();
        }
        return commentList.stream().sorted().map(UserRecordSnapshot::snapshot).collect(Collectors.toList());
    }

}