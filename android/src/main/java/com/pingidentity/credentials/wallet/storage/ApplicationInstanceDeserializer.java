package com.pingidentity.credentials.wallet.storage;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pingidentity.did.sdk.client.service.model.ApplicationInstance;

import java.io.IOException;
import java.lang.reflect.Type;

public class ApplicationInstanceDeserializer implements JsonDeserializer<ApplicationInstance>, JsonSerializer<ApplicationInstance> {
    @Override
    public ApplicationInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ApplicationInstance.fromJson(json.getAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(ApplicationInstance applicationInstance, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive((applicationInstance.toJson(true)));
    }
}
