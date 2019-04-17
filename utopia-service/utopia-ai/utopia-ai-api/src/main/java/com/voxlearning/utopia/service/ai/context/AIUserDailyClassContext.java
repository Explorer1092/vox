package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.data.AIClassInfo;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2018/3/29
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AIUserDailyClassContext extends AbstractAIContext<AIUserDailyClassContext> {

    private static final long serialVersionUID = -8746327867170805154L;

    // in
    private User user;
    private String unitId;

    // middle
    private String bookId = "";
    private NewBookCatalog unit = new NewBookCatalog();
    private Map<String, Object> orderAttributes = new HashMap<>();
    private int rank = 1;
    private Date beginDate = new Date();
    private Date endDate = new Date();

    // out
    private String className;
    private AIClassInfo classInfo;
    private AIBookStatus status;
    private Map<String, Object> extMap = new HashMap<>();

}
