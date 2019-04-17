package com.voxlearning.washington.mapper.studytogether;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author jiangpeng
 * @since 2019-01-04 8:10 PM
 **/
@NoArgsConstructor
@Data
public class CardViewMapper implements Serializable {
    private static final long serialVersionUID = -2269558186615439640L;


    /**
     * icon : https://www.wlwlwlww.jpg
     * title : 21天学16首必背古诗三
     * labels : ["标签1","标签2"]
     * sub_title : {"text":{"color":"#DE393K","content":"今日课程"},"star":{"total":3,"count":1}}
     * bottom : {"text":{"color":"#DE393K","content":"今日课程"},"progress":{"rate":0.8,"rate_text":"8/10"}}
     * jump : {"type":"NATIVE","link":"xxxxxxxxxxxxx","extra":{"url":"*****","name":"****","orientation":"*****","browser":"****","fullScreen":true,"hideTitle":true}}
     */

    private String icon;
    private String title;
    private String type; // add , card
    @JsonProperty("sub_title")
    private SubTitle subTitle;
    private Bottom bottom;
    private Jump jump;
    private List<String> labels;

    @JsonIgnore
    private StudyLesson studyLesson;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SubTitle implements Serializable {
        private static final long serialVersionUID = -1876641702652954553L;
        /**
         * text : {"color":"#DE393K","content":"今日课程"}
         * star : {"total":3,"count":1}
         */

        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private Text text;
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private Star star;


    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Star implements Serializable {
        private static final long serialVersionUID = -3856508532405868025L;
        /**
         * total : 3
         * count : 1
         */

        private int total;
        private int count;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Bottom implements Serializable{
        private static final long serialVersionUID = 5665795133733567853L;
        /**
         * text : {"color":"#DE393K","content":"今日课程"}
         * progress : {"rate":0.8,"rate_text":"8/10"}
         */
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private Text text;
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private Progress progress;

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class Progress implements Serializable {
            private static final long serialVersionUID = 3614355219700642686L;
            /**
             * rate : 0.8
             * rate_text : 8/10
             */

            private String rate;
            @JsonProperty("rate_text")
            private String rateText;
        }
    }

    @NoArgsConstructor
    @Data
    public static class Jump implements Serializable {
        private static final long serialVersionUID = 8975751497833546523L;
        /**
         * type : NATIVE
         * link : xxxxxxxxxxxxx
         * extra : {"url":"*****","name":"****","orientation":"*****","browser":"****","fullScreen":true,"hideTitle":true}
         */

        private JumpType type;
        private String link;
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        private Extra extra;

        @NoArgsConstructor
        @Data
        public static class Extra {
            /**
             * url : *****
             * name : ****
             * orientation : *****
             * browser : ****
             * fullScreen : true
             * hideTitle : true
             */

            private String url;
            private String name;
            private String orientation;
            private String browser;
            private boolean fullScreen;
            private boolean hideTitle;
            private String useNewCore;
        }

        public enum JumpType{
            H5,
            NATIVE
        }
    }



    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Text implements Serializable {
        private static final long serialVersionUID = 1769551070667041588L;
        /**
         * color : #DE393K
         * content : 今日课程
         */

        private String color;
        private String content;

        public final static Text finishRate = new Text("#9C9C9C", "完成率");
    }
}
