package com.voxlearning.utopia.service.parent.homework.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 用户进度
 *
 * @author Wenlong Meng
 * @since Feb 19, 2019
 */
@Getter
@Setter
public class UserProgress implements Serializable {

    private String bookId; // 教材

    private String unitId; // 单元

    private String sectionId; // 课时

    private String course; // 课时

    private Date createTime;//创建时间
    private Map<String, Object> extInfo;//扩展信息

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProgress that = (UserProgress) o;
        return Objects.equals(bookId, that.bookId) &&
                Objects.equals(unitId, that.unitId) &&
                Objects.equals(sectionId, that.sectionId) &&
                Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {

        return Objects.hash(bookId, unitId, sectionId, course);
    }
}
