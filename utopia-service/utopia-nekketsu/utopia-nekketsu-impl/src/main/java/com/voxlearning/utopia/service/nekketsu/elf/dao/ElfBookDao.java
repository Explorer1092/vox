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
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfBookDef;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
public class ElfBookDao extends StaticMongoDao<ElfBookDef,String> {

    @Override
    protected void calculateCacheDimensions(ElfBookDef source, Collection<String> dimensions) {

    }

    private Map<String,ElfBookDef> idMap = new LinkedHashMap<>();

    private Map<String,List<ElfBookDef>> levelMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        reload();
    }

    public void reload(){
        idMap.clear();
        levelMap.clear();
        BsonDocument sort = new BsonDocument();
        sort.put("indexOfLevel", new BsonInt32(1));
        BsonDocument bsonDocument = new BsonDocument();
        for(ElfBookDef ppi : __find_OTF(bsonDocument, null, null, null, sort, ReadPreference.primary())){
            idMap.put(ppi.getBookId(),ppi);
            List<ElfBookDef> elfBookDefs = levelMap.get(ppi.getLevelId());
            if(null == elfBookDefs){
                elfBookDefs = new ArrayList<>();
                levelMap.put(ppi.getLevelId(),elfBookDefs);
            }
            elfBookDefs.add(ppi);
        }
    }

    public ElfBookDef findByBookId(String bookId){
        return idMap.get(bookId);
    }

    public List<ElfBookDef> findByLevelId(String levelId){
        return levelMap.get(levelId);
    }
    public List<ElfBookDef> replaceAll(Collection<ElfBookDef> ElfBookDefs) {
        deletes(__find_OTF().stream().map(e -> e.getBookId()).collect(Collectors.toList()));
        inserts(ElfBookDefs);
        reload();
        return (List)ElfBookDefs;
    }
}
