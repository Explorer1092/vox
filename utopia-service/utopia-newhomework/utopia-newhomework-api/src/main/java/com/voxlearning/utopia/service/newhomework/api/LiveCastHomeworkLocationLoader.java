package com.voxlearning.utopia.service.newhomework.api;


import com.voxlearning.alps.lang.support.LocationLoader;
import com.voxlearning.alps.lang.support.LocationTransformer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;

import java.util.Collection;

public class LiveCastHomeworkLocationLoader extends LocationLoader<LiveCastHomeworkLocationLoader, LiveCastHomework.Location, LiveCastHomework> {
    public LiveCastHomeworkLocationLoader(LocationTransformer<LiveCastHomework.Location, LiveCastHomework> transformer, Collection<LiveCastHomework.Location> locations) {
        super(transformer, locations);
    }
}
