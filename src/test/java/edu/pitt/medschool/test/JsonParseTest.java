package edu.pitt.medschool.test;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;

public class JsonParseTest {
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        JsonParser parser = new BasicJsonParser();
        // Map<String, Object> jsonMap = null;
        String json = "[\"aEEG\",[\"Cz-Av17\", \"Pz-Av17\"],[\"Max\", \"Min\"]]";
        System.out.println(json);
        System.out.println();

        List<Object> aList = parser.parseList(json);
        System.out.println(aList);

        json = "{\"type\":\"aEEG\",\"electrodes\":[\"Cz-Av17\", \"Pz-Av17\"],\"columns\":[\"Max\", \"Min\"]}";
        ObjectMapper objectMapper = new ObjectMapper();
        ColumnJSON j = objectMapper.readValue(json, ColumnJSON.class);
        System.out.println(j.getColumns().get(0));
        System.out.println(j.getColumns().get(1));

    }
}
