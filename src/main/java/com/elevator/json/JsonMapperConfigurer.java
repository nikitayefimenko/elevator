package com.elevator.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapperConfigurer {

    private static ObjectMapper mapper;

    public static ObjectMapper getObjectMapper(){
        if(mapper == null){
            mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        }

        return mapper;
    }

}
