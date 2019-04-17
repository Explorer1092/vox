package com.voxlearning.utopia.service.vendor.api.entity;

/**
 * @author jiangpeng
 * @since 2017-10-17 下午4:43
 **/

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "livecast_index_remind")
@UtopiaCacheRevision("20180416")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class LiveCastIndexRemind extends LiveCastTargetAndExtra {
    private static final long serialVersionUID = -2668443221693806990L;


    @JsonProperty("content")
    private RemindContent remindContent;

    @JsonProperty("image_content")
    private ImageContent imageContent;

    @Data
    public static class ImageContent implements Serializable{
        private static final long serialVersionUID = 8053878820016966064L;

        private String text;
        @JsonProperty("need_login")
        private Boolean needLogin;

        @JsonProperty("dot_id")
        private String dotId;
        @JsonProperty("content_id")
        private String contentId;

        @JsonProperty("bg_color")
        private BackGroundColor backGroundColor;

        @JsonProperty("left_image_url")
        private String leftImageUrl;

        @JsonProperty("button_image_url")
        private String buttonImageUrl;

        private String url;

        public Boolean safeIsNeedLogin(){
            return SafeConverter.toBoolean(needLogin);
        }
    }

    public enum BackGroundColor{
        BLUE,
        GREEN,
        ORANGE;

        public static BackGroundColor safeParse(String name){
            try {
                return valueOf(name);
            }catch (Exception e){
                return BLUE;
            }
        }
    }



    @Data
    public static class RemindContent implements Serializable{
        private static final long serialVersionUID = 7024343457942682339L;
        @JsonProperty("icon_type")
        private IconType iconType = IconType.common;
        private String text;
        private String url;
        @JsonProperty("need_login")
        private Boolean needLogin;
        @JsonProperty("dot_id")
        private String dotId;
        @JsonProperty("content_id")
        private String contentId;

        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("image_dot_id")
        private String imageDotId;
        @JsonProperty("image_content_id")
        private String imageContentId;

        public boolean safeIsNeedLogin(){
            return SafeConverter.toBoolean(needLogin);
        }


        public enum IconType{
            play,
            common
        }
    }




}
