package com.voxlearning.washington.mapper.specialteacher.teacherclazz;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 教务老师为老师建班导入数据
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
@EqualsAndHashCode(of = "name")
public class TeacherClazzData {

    private String name;
    private List<ClazzData> clazzList;

    @Setter private Long teacherId;   // 查到老师信息之后回填

    public TeacherClazzData(String teacherName) {
        this.name = teacherName;
        clazzList = new LinkedList<>();
    }

    public void addClazzData(ClazzData data) {
        clazzList.add(data);
    }

    public Set<String> toLocations() {
        return clazzList.stream().map(ClazzData::toLocation).collect(Collectors.toSet());
    }

    public boolean isMultiSubject() {
        return clazzList.stream().map(ClazzData::getSubject).collect(Collectors.toSet()).size() > 1;
    }

    /**
     * 这个需要在确认 isMultiSubject = false 的时候才有意义
     */
    public Subject defaultSubject() {
        return clazzList.stream().map(ClazzData::getSubject).findAny().orElse(null);
    }

    @Getter
    @Setter
    public static class ClazzData {
        private int rowIndex;
        private int colIndex;
        private ClazzLevel grade;
        private Subject subject;
        private String clazzName;

        private Long groupId; // 找到对应groupId之后回填

        public ClazzData(int row, int col) {
            this.rowIndex = row;
            this.colIndex = col;
        }

        /**
         * 获取在Excel单元格中的坐标
         */
        public String toLocation() {
            // 假设不会超过26列
//            return String.valueOf((char) ('A' + colIndex)) + (rowIndex + 1);
            return StringUtils.formatMessage("{}行{}列", (rowIndex + 1), (colIndex + 1));
        }

        public String toTitleLocation() {
            // 假设不会超过26列
            if (grade == null || grade == ClazzLevel.PRIVATE_GRADE) {
//                return String.valueOf((char) ('A' + colIndex)) + 1;
                return StringUtils.formatMessage("{}行{}列", 1, (colIndex + 1));

            }
            if (StringUtils.isBlank(clazzName)) {
//                return String.valueOf((char) ('A' + colIndex)) + 2;
                return StringUtils.formatMessage("{}行{}列", 2, (colIndex + 1));
            }
            return "";
        }

        public String classNameKey() {
            return grade.name() + "_" + clazzName;
        }

        public String className() {
            return grade.getDescription() + clazzName;
        }

    }

}
