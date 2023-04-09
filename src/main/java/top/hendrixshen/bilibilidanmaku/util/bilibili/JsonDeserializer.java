package top.hendrixshen.bilibilidanmaku.util.bilibili;

import com.google.gson.*;
import me.jvav.blivevote.BLiveVote;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

public class JsonDeserializer implements com.google.gson.JsonDeserializer<String> {
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject data = json.getAsJsonObject();
            String cmd = data.get("cmd").getAsString();
            if (cmd.contains("DANMU_MSG")) {
                JsonArray info = data.getAsJsonArray("info");
                JsonArray user = info.get(2).getAsJsonArray();
                BLiveVote.handleDanmaku(user.get(1).getAsString(), info.get(1).getAsString(), StringUtils.isNotBlank(user.get(7).getAsString()));
            }
        }
        return null;
    }
}
