package com.voxlearning.utopia.service.ai.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.TobbitMathBoostService;
import com.voxlearning.utopia.service.ai.api.TobbitMathScoreService;
import com.voxlearning.utopia.service.ai.api.TobbitMathService;
import lombok.Getter;


public class TobbitMathServiceClient {

    @Getter
    @ImportService(interfaceClass = TobbitMathService.class)
    TobbitMathService tobbitMathService;



    @Getter
    @ImportService(interfaceClass = TobbitMathScoreService.class)
    TobbitMathScoreService tobbitMathScoreService;


    @Getter
    @ImportService(interfaceClass = TobbitMathBoostService.class)
    TobbitMathBoostService tobbitMathBoostService;
}
