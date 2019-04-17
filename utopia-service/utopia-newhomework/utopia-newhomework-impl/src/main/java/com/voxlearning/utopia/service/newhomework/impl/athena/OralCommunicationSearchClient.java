package com.voxlearning.utopia.service.newhomework.impl.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.athena.api.search.loader.OralCommSearchLoader;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.athena.OralCommunicationSearchClient")
public class OralCommunicationSearchClient {

    @Getter
    @ImportService(interfaceClass = OralCommSearchLoader.class)
    private OralCommSearchLoader oralCommSearchLoader;
}
