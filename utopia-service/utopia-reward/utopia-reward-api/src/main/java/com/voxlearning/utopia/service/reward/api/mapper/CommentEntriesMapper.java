package com.voxlearning.utopia.service.reward.api.mapper;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CommentEntriesMapper implements java.io.Serializable {
    private static final long serialVersionUID = 979850759327966662L;

    private List<Collect> collectList;
    private List<Comment> commentList;

    @Setter
    @Getter
    public static class Comment implements java.io.Serializable {
        private static final long serialVersionUID = 979850759327966662L;

        private String avatarImg;
        private String word;

    }

    @Setter
    @Getter
    @EqualsAndHashCode(of = "teacherName")
    public static class Collect implements java.io.Serializable {
        private static final long serialVersionUID = 979850759327966662L;

        private String avatarImg;
        private String teacherName;

    }


}
