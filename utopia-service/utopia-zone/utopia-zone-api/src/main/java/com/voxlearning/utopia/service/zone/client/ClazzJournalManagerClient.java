package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.zone.api.ClazzJournalManager;
import lombok.Getter;

public class ClazzJournalManagerClient {

    @Getter
    @ImportService(interfaceClass = ClazzJournalManager.class)
    private ClazzJournalManager clazzJournalManager;
}
