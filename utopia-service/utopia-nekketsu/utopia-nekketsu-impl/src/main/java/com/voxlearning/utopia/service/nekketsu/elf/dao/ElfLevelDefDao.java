/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.elf.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfLevelDef;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
public class ElfLevelDefDao extends StaticMongoDao<ElfLevelDef,String> {

    private Map<String,ElfLevelDef> idMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        reload();
    }

    public void reload(){
        idMap.clear();
        BsonDocument sort = new BsonDocument();
        sort.put("order", new BsonInt32(1));
        for(ElfLevelDef ppi : __find_OTF(new BsonDocument(), null, null, null, sort, ReadPreference.primary())){
            idMap.put(ppi.getId(), ppi);
        }
    }

    public ElfLevelDef findByLevelId(String levelId){
        return idMap.get(levelId);
    }

    public Collection<ElfLevelDef> listAll(){
        return idMap.values();
    }

    public List<ElfLevelDef> replaceAll(Collection<ElfLevelDef> LevelDefs){
        deletes(__find_OTF().stream().map(e -> e.getId()).collect(Collectors.toList()));
        inserts(LevelDefs);
        reload();
        return (List)LevelDefs;
    }

    @Override
    protected void calculateCacheDimensions(ElfLevelDef source, Collection<String> dimensions) {

    }
}
