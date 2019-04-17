package com.voxlearning.utopia.agent.mockexam.integration;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.agent.mockexam.middleschool.service.dto.input.MiddleSchoolExamPaperParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.PageInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 中学内容库试卷客户端
 * @author: kaibo.he
 * @create: 2019-03-18 17:42
 **/
public interface MiddleSchoolExamPaperClient {
    /**
     * 查询成绩
     *
     * @param request 请求
     * @return
     */
    PaperPageResponse queryPage(PaperRequest request);

    /**
     * 查询搜索试卷备选项信息
     * @return
     */
    PaperSearchItemResponse querySearchItems();

    /**
     * 搜索选项相应体
     */
    @Data
    class PaperSearchItemResponse {
        private List<Book> books;
        private List<PaperType> paper_types;
    }

    @Data
    class Book {
        private String id;
        private String name;
    }

    @Data
    class PaperType {
        private Integer id;
        private String name;
    }

    @Data
    class Region {
        private Integer id;
        private String name;
    }

    @Data
    class PaperRequest {
        private String paperId;
        private Integer regionId;
        private String examName;
        private String bookId;
        private String paperTag;
        private String usageMonth;
        private Integer page;

        public static class Builder {
            public static PaperRequest build(MiddleSchoolExamPaperParams params, PageInfo pageInfo) {
                PaperRequest request = new PaperRequest();
                BeanUtils.copyProperties(params, request);
                request.setPage(pageInfo.getPage());
                return request;
            }

            public static Map<Object, Object> build(PaperRequest request) {
                Map<Object, Object> result = new HashMap<>();
                if (StringUtils.isNotBlank(request.getBookId())) {
                    result.put("book_id", request.getBookId());
                }
                if (StringUtils.isNotBlank(request.getExamName())) {
                    result.put("exam_name", request.getExamName());
                }
                if (StringUtils.isNotBlank(request.getPaperId())) {
                    result.put("paper_id", request.getPaperId());
                }
                if (StringUtils.isNotBlank(request.getPaperTag())) {
                    result.put("paper_tag", request.getPaperTag());
                }
                if (StringUtils.isNotBlank(request.getUsageMonth())) {
                    result.put("usage_month", request.getUsageMonth());
                }
                if (Objects.nonNull(request.getRegionId()) && request.getRegionId() > 0) {
                    result.put("region_id", request.getRegionId());
                }
                result.put("page", request.getPage()>0 ? request.getPage():1);
                return result;
            }
        }
    }

    @Data
    class PaperPageResponse {
        private List<PaperInfo> items;
        private Integer page;
        private Integer per_page;
        private boolean success;
        private Integer total;
    }

    @Data
    class PaperInfo {
        private String _id;
        private String created_at;
        private Integer exam_times;
        private Integer minutes;
        private Integer paper_tag;
        private String paper_tag_text;
        private String title;
        private Integer total_num;
        private Integer total_score;
        private List<Book> books;
        private List<Region> regions;
        private String usage_month;
    }
}
