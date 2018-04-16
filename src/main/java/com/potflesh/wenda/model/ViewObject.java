package com.potflesh.wenda.model;

import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by bazinga on 2017/4/10.
 */

// ViewObject 用于把数据都放在一起
public class ViewObject {
    private Map<String,Object> objectMap = new HashedMap();

    public void set(String key,Object value){

        objectMap.put(key,value);

    }

    public Object get(String key){
        return objectMap.get(key);
    }
}
