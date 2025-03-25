package com.pingidentity.credentials.wallet.storage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pingidentity.did.sdk.types.Claim;

import java.io.IOException;
import java.lang.reflect.Type;

public class ClaimDeserializer implements JsonDeserializer<Claim>, JsonSerializer<Claim> {
    @Override
    public Claim deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return Claim.fromJson(json.getAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(Claim claim, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(claim.toJson());
    }
}
