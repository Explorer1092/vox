package com.voxlearning.washington.mapper.specialteacher.base;

import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class KlxStudentClazz {
    private Clazz clazz;
    private String klxId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof KlxStudentClazz) {
            KlxStudentClazz klxStudentClazz = (KlxStudentClazz) obj;
            return Objects.equals(klxStudentClazz.klxId , klxId ) && Objects.equals(klxStudentClazz.clazz.getClassName(), clazz.getClassName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
