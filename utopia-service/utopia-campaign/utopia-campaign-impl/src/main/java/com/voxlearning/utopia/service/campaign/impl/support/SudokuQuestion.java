package com.voxlearning.utopia.service.campaign.impl.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SudokuQuestion {
    private String answerDesc;
    private String gameDesc;
    private Integer genre;
    private Integer hardLevel;

    public String getAnswerDesc() {
        return answerDesc;
    }

    public void setAnswerDesc(String answerDesc) {
        this.answerDesc = answerDesc;
    }

    public String getGameDesc() {
        return gameDesc;
    }

    public void setGameDesc(String gameDesc) {
        this.gameDesc = gameDesc;
    }

    public Integer getGenre() {
        return genre;
    }

    public void setGenre(Integer genre) {
        this.genre = genre;
    }

    public Integer getHardLevel() {
        return hardLevel;
    }

    public void setHardLevel(Integer hardLevel) {
        this.hardLevel = hardLevel;
    }
}
