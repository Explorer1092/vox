package com.voxlearning.utopia.service.ai.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OcrImageDto implements Serializable {

    private static final long serialVersionUID = 5596609859929367221L;
    public String img_url;
    public String img_height;
    public String img_width;

    public List<OcrForms> forms;
    public String qid;
    public boolean persist;

    public String origin_json;

    public boolean blocked;

    @Data
    public static class OcrForms implements Serializable {
        private static final long serialVersionUID = -5596609859929367221L;
        public String text;
        public int judge;
        public OcrPosition position;
        public List<OrcCoordinate> coordinate;
    }

    @Data
    public static class OcrPosition implements Serializable {
        private static final long serialVersionUID = -5596609859929367221L;
        public int x, y, h, w;
    }
    @Data
    public static class OrcCoordinate implements Serializable{
        private static final long serialVersionUID = -5596609859929367221L;
        public int x, y;

        public OrcCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public OrcCoordinate(){
            super();
        }
    }


}
