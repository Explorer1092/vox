package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanguohong
 * @since 2016/11/17
 */
public class TeacherNewHomeworkCommentLibraryManager extends PojoCacheObject<TeacherNewHomeworkCommentLibraryManager.BuildCacheKey, String> {
    public TeacherNewHomeworkCommentLibraryManager(UtopiaCache cache) {
        super(cache);
    }

    public List<String> find(Long teacherId) {
        if (null == teacherId) return new ArrayList<>();
        String value = load(new BuildCacheKey(teacherId));
        List<String> comments = JsonUtils.fromJsonToList(value, String.class);
        return comments != null ? comments : new ArrayList<>();
    }

    public void addComment(Long teacherId, String comment) {
        if (null == teacherId || StringUtils.isBlank(comment)) return;
        List<String> comments = find(teacherId);
        if (!comments.contains(comment)) {
            comments.add(0, comment);
            if (comments.size() > 10) {
                comments = comments.subList(0, 10);
            }
            cache.set(cacheKey(new BuildCacheKey(teacherId)), 0, JsonUtils.toJson(comments));
        }
    }

    public boolean removeComment(Long teacherId, String comment) {
        if (null == teacherId || StringUtils.isBlank(comment)) return false;
        List<String> comments = find(teacherId);
        if (comments.contains(comment)) {
            comments.remove(comment);
            cache.set(cacheKey(new BuildCacheKey(teacherId)), 0, JsonUtils.toJson(comments));
            return true;
        } else {
            return false;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"teacherId"})
    class BuildCacheKey {
        public Long teacherId;

        @Override
        public String toString() {
            return "tid=" + teacherId;
        }
    }


}
