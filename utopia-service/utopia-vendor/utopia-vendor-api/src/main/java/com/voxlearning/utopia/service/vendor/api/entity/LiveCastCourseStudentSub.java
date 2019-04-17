package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:01
 **/
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "livecast_course_student_sub")
@UtopiaCacheRevision("20180920")
public class LiveCastCourseStudentSub implements CacheDimensionDocument {
    private static final long serialVersionUID = 6445345518373522012L;
    /**
     * studentId_courseId
     */
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private String courseId;

    private Long studentId;

    private Long subscribeNum;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey(id)};
    }

    public static String generateId(Long studentId, String courseId) {
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(studentId);
        return studentId + "_" + courseId;
    }

    public String generateId(){
        Objects.requireNonNull(courseId);
        Objects.requireNonNull(studentId);
        String id = generateId(studentId, courseId);
        this.id = id;
        return id;
    }
}
