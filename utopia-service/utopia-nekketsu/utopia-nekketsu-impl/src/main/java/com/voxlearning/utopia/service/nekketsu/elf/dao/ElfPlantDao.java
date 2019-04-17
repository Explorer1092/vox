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

import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfPlantDef;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sadi.Wan on 2015/2/27.
 */
@Named
public class ElfPlantDao extends StaticMongoDao<ElfPlantDef,String> {


    private Map<String,ElfPlantDef> idMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        reload();
    }

    public void reload(){
        idMap.clear();
        for(ElfPlantDef ppi : __find_OTF()){
            idMap.put(ppi.getPlantId(),ppi);
        }
    }

    public ElfPlantDef findByPlantId(String plantId){
        return idMap.get(plantId);
    }

    public Collection<ElfPlantDef> loadAll(){
        return idMap.values();
    }
    public List<ElfPlantDef> replaceAll(Collection<ElfPlantDef> plantDefs){
        deletes(__find_OTF().stream().map(e -> e.getPlantId()).collect(Collectors.toList()));
        inserts(plantDefs);
        reload();
        return (List<ElfPlantDef>) plantDefs;
    }

    public Map<String,ElfPlantDef> findByStar(int star){
        Map<String,ElfPlantDef> rtn = new HashMap<>();
        for(Map.Entry<String,ElfPlantDef> entry : idMap.entrySet()){
            if(entry.getValue().getStar() == star){
                rtn.put(entry.getKey(),entry.getValue());
            }
        }
        return rtn;
    }

    @Override
    protected void calculateCacheDimensions(ElfPlantDef source, Collection<String> dimensions) {

    }
}
