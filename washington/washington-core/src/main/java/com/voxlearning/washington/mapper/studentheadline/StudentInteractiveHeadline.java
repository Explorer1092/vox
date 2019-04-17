package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.utopia.entity.comment.UserRecordEcho;
import com.voxlearning.utopia.entity.like.RecordLikeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 可以点赞和评论的新鲜事
 *
 * @author yuechen.wang
 * @since 10/11/2017
 */
@Getter
@Setter
public abstract class StudentInteractiveHeadline extends StudentHeadlineMapper {
    private static final long serialVersionUID = 5845988271244968837L;

    private Long userId;          // 获得学生
    private String userName;      // 学生姓名
    private String avatar;        // 学生头像
    private String headWear;      // 学生头饰

    private Boolean encourageBtn = Boolean.TRUE;  // 鼓励按钮状态
    private Boolean commentBtn = Boolean.TRUE;    // 评论按钮状态
    private Boolean deleteBtn = Boolean.FALSE;    // 删除按钮状态

    /**
     * 鼓励人列表
     * 学生id :  id
     * 学生姓名 : name
     * 学生头像 :  headIcon
     */
    private List<Map<String, Object>> encouragerList = new LinkedList<>();

    /**
     * 评论列表
     * 学生id : id
     * 学生姓名 : name
     * 评论内容 : comment
     * 评论时间 : createTime
     */
    private List<Map<String, Object>> commentList = new LinkedList<>();

    public void resetBtnStatusWithEcho(UserRecordEcho echo, Long currentUserId) {
        if (echo == null) {
            return;
        }

        commentList = echo.commentInfoList();
        commentBtn = !echo.alreadyComment(currentUserId);
    }

    public void resetBtnStatusWithLike(RecordLikeInfo likeInfo, Long currentUserId) {
        if (likeInfo == null) {
            return;
        }

        encourageBtn = !likeInfo.hasLiked(currentUserId);

        List<Map<String, Object>> encourager = new ArrayList<>();
        for (String likerName : likeInfo.getLikerNames()) {
            Map<String, Object> item = new HashMap<>();
            item.put("userName", likerName);
            encourager.add(item);
        }

        encouragerList = encourager;
    }

}
