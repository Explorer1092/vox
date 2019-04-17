package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import com.voxlearning.utopia.service.ai.data.ChipsEnglishClassInfo;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ChipsContentDailyClassContext extends AbstractAIContext<ChipsContentDailyClassContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private User user;
    private String unitId;
    private String bookId;

    // middle
    private Set<String> userBoughtBooks = new HashSet<>();
    private ChipsEnglishProductTimetable timetable;
    private AIUserLessonBookRef bookRef = new AIUserLessonBookRef();
    private StoneUnitData unit = new StoneUnitData();
    private StoneBookData book = new StoneBookData();
    private int rank = 1;
    private Date beginDate = new Date();
    private Date endDate = new Date();

    // out
    private String className;
    private String mapUrl;
    private String summaryUrl;
    private AIBookStatus status;
    private ChipsEnglishClassInfo classInfo;
    private Boolean checkIn;
    private Integer studyNumber;
    private Long sentenceNumber;
    private String voiceRadio = "1.0";
    private Map<String, Object> extMap = new HashMap<>();

    public ChipsContentDailyClassContext(User user, String unitId, String bookId) {
        this.setUnitId(unitId);
        this.setUser(user);
        this.setBookId(bookId);
    }
}
