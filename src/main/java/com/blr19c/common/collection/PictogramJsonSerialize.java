package com.blr19c.common.collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 使PictogramMap在json序列化时转为普通map
 *
 * @author blr
 */
public class PictogramJsonSerialize extends JsonSerializer<PictogramMap> {
    @Override
    public void serialize(PictogramMap value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.getMap());
    }
}