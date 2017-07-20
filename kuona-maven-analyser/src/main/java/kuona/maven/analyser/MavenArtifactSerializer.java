package kuona.maven.analyser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class MavenArtifactSerializer implements JsonSerializer<MavenArtifact> {
    public JsonElement serialize(MavenArtifact artifact, Type typeOfSrc, JsonSerializationContext context) {

        return new JsonPrimitive(artifact.toString());
    }
}

