package com.voxlearning.utopia.service.newhomework.api.mapper.report.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Setter
@Getter
public class CrmUnitQuestion implements Serializable {
    private static final long serialVersionUID = 3427295423718822301L;
    private String unitId;
    private String unitName;
    private List<CrmQuestion> qidList = new LinkedList<>();
    private List<String> paperList = new LinkedList<>();
    private List<String> pictureBookList = new LinkedList<>();
    private List<String> videoList = new LinkedList<>();
    private List<String> questionBoxIdList = new LinkedList<>();
    private List<String> dobbingIdsList = new LinkedList<>();
    private String qids;
    private String papers;
    private String pictureBooks;
    private String videos;
    private String questionBoxIds;
    private String dobbingIds;


    @Setter
    @Getter
    public static class CrmQuestion implements Serializable {
        private static final long serialVersionUID = -7781827971573260982L;
        private String qid;
        private String docId;

        public CrmQuestion(String qid) {
            this.qid = qid;
            String[] split = StringUtils.split(qid, "-");
            this.docId = split[0];
        }

    }

    public void handle() {

        if (CollectionUtils.isNotEmpty(paperList)) {
            papers = StringUtils.join(paperList.toArray(), ",");
        }

        if (CollectionUtils.isNotEmpty(pictureBookList)) {
            pictureBooks = StringUtils.join(pictureBookList.toArray(), ",");
        }
        if (CollectionUtils.isNotEmpty(videoList)) {
            videos = StringUtils.join(videoList.toArray(), ",");
        }
        if (CollectionUtils.isNotEmpty(questionBoxIdList)) {
            questionBoxIds = StringUtils.join(questionBoxIdList.toArray(), ",");
        }
        if (CollectionUtils.isNotEmpty(dobbingIdsList)){
            dobbingIds = StringUtils.join(dobbingIdsList.toArray(),",");
        }

    }


}
