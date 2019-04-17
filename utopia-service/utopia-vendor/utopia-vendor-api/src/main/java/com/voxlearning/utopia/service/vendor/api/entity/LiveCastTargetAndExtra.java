package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jiangpeng
 * @since 2017-10-17 下午4:43
 **/
@Getter
@Setter
public class LiveCastTargetAndExtra implements CacheDimensionDocument {
    private static final long serialVersionUID = -7735814347547537738L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;

    private Target target;

    private Extra extra;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(id)
        };
    }


    public String generateId(){
        if (this.target == null || this.target.type == null || this.target.value == null)
            throw new RuntimeException("type or id no value");

        String s = generateId(this.target.type, this.target.value);
        this.setId(s);
        return s;
    }

    public static String generateId(Target.Type type, Object value){
        return type.name() + "_" + value.toString();
    }



    @Data
    public static class Extra implements Serializable{
        private static final long serialVersionUID = -3627383815747733761L;
        private Integer priority; //必需传
        @JsonProperty("endTimestamp")
        private Date endTime;
    }


    @Data
    public static class Target implements Serializable{
        private static final long serialVersionUID = -8919297371526415971L;
        private Type type;
        private Long value;

        public enum Type{
            sid,
            regionId,
            clazzLevel,
            all
        }
    }
}
