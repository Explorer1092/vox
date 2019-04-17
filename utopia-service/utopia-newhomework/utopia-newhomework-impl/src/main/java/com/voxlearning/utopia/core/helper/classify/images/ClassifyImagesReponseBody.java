package com.voxlearning.utopia.core.helper.classify.images;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/10/25
 * \* Time: 12:14 PM
 * \* Description:
 * \
 */
@Getter
@Setter
public class ClassifyImagesReponseBody {

    private Header header;

    private double cost; //cost为服务端耗时，单位秒

    @JSONField(name="request_id")
    private String requestId;

    @JSONField(name="request_time")
    private long requestTime;

    private List<ClassifyImagesItemBody> body;



    @Getter
    @Setter
    public class Header{

        @JSONField(name="err_no")
        private int errorNo; //err_no返回200，msg为success，否则返回对应错误码及错误信息

        @JSONField(name="err_msg")
        private String errorMsg;
    }

    @Getter
    @Setter
    public class  ClassifyImagesItemBody{

        @JSONField(name="data_id")
        private String dataId; //唯一标识该图片

        @JSONField(name="image_url")
        private String imageUrl;

        @JSONField(name="err_no")
        private int errorNo; //仅图片格式出现错误有此信息

        @JSONField(name="err_msg")
        private String errorMsg; //仅图片格式出现错误有此信息

        private List<ImageResult> results;

        @Getter
        @Setter
        public class ImageResult{

            @JSONField(name="err_no")
            private int errorNo; //仅此业务结果出现错误有此信息

            @JSONField(name="err_msg")
            private String errorMsg; //仅此业务结果出现错误有此信息

            private String business;

            private String suggest;

            private String label; //分类标签ID(int类型) ---->  porn: 1正常，2低俗，3色情

            @JSONField(name="label_desc")
            private String labelDesc; //分类标签(string类型) ---->  porn: 1正常，2低俗，3色情

            private double rate; //分类置信度 [0, 1.0]，值越大置信度越高

            private int review; //0不需要人工复审，1需人工复审

            @JSONField(name="task_id")
            private String taskId; //唯一标示该次检测任务
        }
    }
}
