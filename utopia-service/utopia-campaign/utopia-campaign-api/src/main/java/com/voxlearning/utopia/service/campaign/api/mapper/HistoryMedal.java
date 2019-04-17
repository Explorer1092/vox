package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HistoryMedal implements Serializable {
    private static final long serialVersionUID = 7638565219743123978L;

    private String date;

    private List<Medal> list;

    @Data
    public static class Medal implements Serializable {
        private static final long serialVersionUID = 7295285139935697178L;
        private Integer medalId;

        private String name;

        private String icon;

        private String num;

        private String desc;
    }


}
