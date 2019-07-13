package com.github.alexlandau.duml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@RunWith(Parameterized.class)
public class DumlParserTest {
    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data() {
        File testCasesFolder = new File("testCases");
        if (!testCasesFolder.isDirectory()) {
            throw new IllegalStateException("This test is being run with the wrong working directory (" + new File(".").getAbsolutePath() + "); it must be run from the duml-java folder");
        }
        return Arrays.stream(testCasesFolder.listFiles(file -> file.getName().endsWith(".duml")))
                .map(file -> new Object[] { file })
                .collect(Collectors.toList());
    }

    private final File dumlFile;

    public DumlParserTest(File dumlFile) {
        this.dumlFile = dumlFile;
    }

    @Test
    public void testWithoutTrimming() throws Exception {
        testTestCase(false);
    }

    @Test
    public void testWithTrimming() throws Exception {
        testTestCase(true);
    }

    public void testTestCase(boolean trimValues) throws Exception {
        File jsonFile = new File(dumlFile.getParent(), dumlFile.getName().replace(".duml", ".json"));
        if (trimValues) {
            File trimmedJsonFile = new File(dumlFile.getParent(), dumlFile.getName().replace(".duml", ".trimmed.json"));
            if (trimmedJsonFile.exists()) {
                jsonFile = trimmedJsonFile;
            }
        }
        File lostJsonFile = new File(dumlFile.getParent(), dumlFile.getName().replace(".duml", ".lost.json"));
        if (!jsonFile.exists()) {
            throw new IllegalStateException("DUML test case file " + dumlFile.getName() + " does not have a corresponding .json file");
        }

        DumlParseResult parseResult = DumlParser.parse(dumlFile, trimValues);
        DumlNode duml = parseResult.getDuml();
        List<LostNode> lostNodes = parseResult.getLostNodes();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonFile);

        try {
            checkEquality(duml, json);
        } catch (Throwable t) {
            throw new AssertionError("Incorrect parsing result comparing to JSON file " + jsonFile.getPath(), t);
        }

        if (!lostNodes.isEmpty() || lostJsonFile.exists()) {
            if (!lostJsonFile.exists()) {
                throw new AssertionError("There were unexpected lost nodes");
            }
            JsonNode lostJsons = mapper.readTree(lostJsonFile);

            try {
                checkLostEquality(lostNodes, lostJsons);
            } catch (Throwable t) {
                throw new AssertionError("Incorrect lost nodes result comparing to JSON file " + lostJsonFile.getPath(), t);
            }
        }
    }

    private void checkEquality(DumlNode duml, JsonNode json) {
        if (duml.isObject()) {
            if (!json.isObject()) {
                throw new AssertionError("Expected JSON to be an object, but was " + json.toString());
            }

            List<String> orderedKeys = new ArrayList<>(duml.getMap().keySet());
            if (!Iterators.elementsEqual(orderedKeys.iterator(), json.fieldNames())) {
                throw new AssertionError("Expected keys to be equal, but DUML keys were: " + orderedKeys + " and JSON keys were: " + Iterators.toString(json.fieldNames()));
            }
            for (String key : orderedKeys) {
                try {
                    checkEquality(duml.get(key), json.get(key));
                } catch (Throwable t) {
                    throw new AssertionError("Error checking key " + key, t);
                }
            }
        } else if (duml.isStrings()) {
            if (!json.isArray()) {
                throw new AssertionError("Expected JSON to be an array, but was " + json.toString());
            }
            List<String> dumlStrings = duml.getStrings();
            if (json.size() != dumlStrings.size()) {
                throw new AssertionError("Expected DUML and JSON strings to be the same, but DUML was " + dumlStrings + " and JSON was " + json.toString());
            }
            for (int i = 0; i < json.size(); i++) {
                if (!json.get(i).isTextual()) {
                    throw new AssertionError("Expected JSON to be textual but was " + json.toString());
                }
                if (!dumlStrings.get(i).equals(json.get(i).asText())) {
                    throw new AssertionError("Expected DUML and JSON strings to be the same, but DUML was " + dumlStrings.get(i) + " and JSON was " + json.get(i).asText());
                }
            }
        } else {
            throw new RuntimeException("Unimplemented case");
        }
    }

    private void checkLostEquality(List<LostNode> lostNodes, JsonNode lostJsons) {
        if (!lostJsons.isArray()) {
            throw new AssertionError("Expected the lostJsons file to be a JSON array");
        }
        if (lostNodes.size() != lostJsons.size()) {
            throw new AssertionError("Expected the number of lost nodes to be the same, but DUML had " + lostNodes + " and JSON had " + lostJsons);
        }
        for (int i = 0; i < lostNodes.size(); i++) {
            LostNode lostDuml = lostNodes.get(i);
            JsonNode lostJson = lostJsons.get(i);
            if (!lostJson.isObject()) {
                throw new RuntimeException("Expected the lostJson array element to be an object");
            }
            List<String> jsonLocation = StreamSupport.stream(lostJson.get("location").spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.toList());
            if (!lostDuml.getLocation().equals(jsonLocation)) {
                throw new AssertionError("Difference in lost nodes locations: DUML had " + lostDuml.getLocation() + " but JSON had " + jsonLocation);
            }
            checkEquality(lostDuml.getNode(), lostJson.get("value"));
        }
    }

}
