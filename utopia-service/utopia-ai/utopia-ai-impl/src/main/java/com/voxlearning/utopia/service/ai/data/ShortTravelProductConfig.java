package com.voxlearning.utopia.service.ai.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ShortTravelProductConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private String productId;
    private List<String> calendar;
    private Integer endDay;
    private Date sellOutEndDate;
    private String description;
    private List<String> contentCards;
    private String video;
    private String videoImage;
    private String bookId;
    private List<String> adImages;
    private List<String> cardImages;
    private List<String> adImages2;
    private Integer rank;
    private Boolean saleOut;
    private Integer surplus;
    private String trialBookId; //试用的教材id
    private String trialUnitId; //试用的unit id
}
