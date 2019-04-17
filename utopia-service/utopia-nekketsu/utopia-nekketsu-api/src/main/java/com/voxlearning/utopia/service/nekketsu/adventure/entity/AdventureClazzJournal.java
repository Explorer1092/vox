package com.voxlearning.utopia.service.nekketsu.adventure.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 奇幻探险班级分享
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/10/8 14:06
 */
public class AdventureClazzJournal {

    @Getter @Setter private Type type;//"CROWN":皇冠;   "EXCHANGE":PK武器兑换;   "BEYOND"：班级超越

    @Getter @Setter private String content;

    @Getter @Setter private String img;

    @Getter @Setter private List<BeyondClassmate> classmates;

    public static enum Type {
        CROWN,
        EXCHANGE,
        BEYOND;
    }

}
