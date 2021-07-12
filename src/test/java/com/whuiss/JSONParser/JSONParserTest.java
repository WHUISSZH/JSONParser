package com.whuiss.JSONParser;

import com.whuiss.JSONParser.model.JsonArray;
import com.whuiss.JSONParser.model.JsonObject;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class JSONParserTest {

    @Test
    public void fromJSON() throws Exception {
        String path = this.getClass().getResource("/music.json").getFile();
        System.out.println(path);
        path = path.substring(1);
        System.out.println(path);
        String json = new String(Files.readAllBytes(Paths.get(path)), "utf-8");
//       String json = getJSON();

        JSONParser jsonParser = new JSONParser();
        JsonObject jsonObject = (JsonObject) jsonParser.fromJSON(json);
        System.out.println(jsonObject);

        JsonObject playlist = jsonObject.getJsonObject("playlist");
        assertEquals(52, playlist.get("commentCount"));
        assertEquals(19208468137575293L, playlist.get("coverImgId"));
        assertEquals("2017年八月最热新歌TOP50", playlist.get("name"));
        assertFalse((Boolean) playlist.get("highQuality"));

        JsonArray trackIds = playlist.getJsonArray("trackIds");
        assertEquals(50, trackIds.size());
        JsonObject trackId = trackIds.getJsonObject(7);
        assertEquals(499274374, trackId.get("id"));
        assertEquals(14, trackId.get("v"));

        JsonArray tracks = playlist.getJsonArray("tracks");
        JsonObject track3 = tracks.getJsonObject(3);
        assertEquals("带你去旅行", track3.get("name"));
        assertEquals(4, track3.get("v"));
        JsonObject track17 = tracks.getJsonObject(17);
        assertEquals("EVERYDAY", track17.get("name"));
        assertEquals(null, track17.get("a"));
        assertEquals(5619229, track17.get("mv"));
    }

    @org.junit.Test
    public void fromJSON1() throws Exception {
        String json = "{\"a\": 1, \"b\": \"b\", \"c\": {\"a\": 1, \"b\": null, \"d\": [0.1, \"a\", 1,2, 123, 1.23e+10, true, false, null]}}";
        JSONParser jsonParser = new JSONParser();
        JsonObject jsonObject = (JsonObject) jsonParser.fromJSON(json);
        System.out.println(jsonObject);

        assertEquals(1, jsonObject.get("a"));
        assertEquals("b", jsonObject.get("b"));

        JsonObject c = jsonObject.getJsonObject("c");
        assertEquals(null, c.get("b"));

        JsonArray d = c.getJsonArray("d");
        assertEquals(0.1, d.get(0));
        assertEquals("a", d.get(1));
        assertEquals(123, d.get(4));
        assertEquals(1.23e+10, d.get(5));
        assertTrue((Boolean) d.get(6));
        assertFalse((Boolean) d.get(7));
        assertEquals(null, d.get(8));
    }

    @org.junit.Test
    public void fromJSON2() throws Exception {
        String json = "[[1,2,3,\"\u4e2d\"]]";
        JSONParser jsonParser = new JSONParser();
        JsonArray jsonArray = (JsonArray) jsonParser.fromJSON(json);
        System.out.println(jsonArray);
    }

    @Test
    public void beautifyJSON() throws Exception {
        String json = "{\"name\": \"狄仁杰\", \"type\": \"射手\", \"ability\":[\"六令追凶\",\"逃脱\",\"王朝密令\"],\"history\":{\"DOB\":630,\"DOD\":700,\"position\":\"宰相\",\"dynasty\":\"唐朝\"}}";
        System.out.println("原 JSON 字符串：");
        System.out.println(json);
        System.out.println("\n");
        System.out.println("美化后的 JSON 字符串：");
        JSONParser jsonParser = new JSONParser();
        JsonObject drj = (JsonObject) jsonParser.fromJSON(json);
        System.out.println(drj);
    }
}