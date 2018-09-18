package edu.pitt.medschool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import org.junit.Test;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;

import java.util.List;

import static org.junit.Assert.*;

public class JsonParseTest {

    @Test
    public void test1() throws Exception {
        JsonParser parser = new BasicJsonParser();

        String json = "[\"aEEG\",[\"Cz-Av17\", \"Pz-Av17\"],[\"Max\", \"Min\"]]";

        List<Object> aList = parser.parseList(json);
        assertEquals(aList.size(), 3);
        assertEquals(((List<Object>) (aList.get(2))).size(), 2);

        json = "{\"type\":\"aEEG\",\"electrodes\":[\"Cz-Av17\", \"Pz-Av17\"],\"columns\":[\"Max\", \"Min\"]}";

        ObjectMapper objectMapper = new ObjectMapper();
        ColumnJSON j = objectMapper.readValue(json, ColumnJSON.class);

        assertEquals(j.getColumns().get(0), "Max");
        assertEquals(j.getColumns().get(1), "Min");

        j.setType("heheeh");
        assertEquals(j.getType(), "heheeh");
    }
}
