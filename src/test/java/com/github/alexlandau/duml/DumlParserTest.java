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
    public void testTestCase() throws Exception {
        File jsonFile = new File(dumlFile.getParent(), dumlFile.getName().replace(".duml", ".json"));
        File lostJsonFile = new File(dumlFile.getParent(), dumlFile.getName().replace(".duml", ".lost.json"));
        if (!jsonFile.exists()) {
            throw new IllegalStateException("DUML test case file " + dumlFile.getName() + " does not have a corresponding .json file");
        }

        DumlParseResult parseResult = DumlParser.parse(dumlFile, false);
        DumlNode duml = parseResult.getDuml();
        List<LostNode> lostNodes = parseResult.getLostNodes();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonFile);

        if (!checkEquality(duml, json)) {
            throw new AssertionError("Incorrect parsing result: " + duml);
        }

        if (!lostNodes.isEmpty() || lostJsonFile.exists()) {
            if (!lostJsonFile.exists()) {
                throw new AssertionError("There were unexpected lost nodes");
            }
            JsonNode lostJsons = mapper.readTree(lostJsonFile);
            if (!checkLostEquality(lostNodes, lostJsons)) {
                throw new AssertionError("Incorrect lost nodes result: " + lostNodes);
            }
        }
    }

    private boolean checkEquality(DumlNode duml, JsonNode json) {
        if (duml.isObject()) {
            if (!json.isObject()) {
                return false;
            }

            List<String> orderedKeys = new ArrayList<>(duml.getMap().keySet());
            if (!Iterators.elementsEqual(orderedKeys.iterator(), json.fieldNames())) {
                System.out.println("duml: " + orderedKeys);
                System.out.println("json: " + json.fieldNames());
                return false;
            }
            for (String key : orderedKeys) {
                if (!checkEquality(duml.get(key), json.get(key))) {
                    return false;
                }
            }
        } else if (duml.isStrings()) {
            if (!json.isArray()) {
                return false;
            }
            List<String> dumlStrings = duml.getStrings();
            if (json.size() != dumlStrings.size()) {
                return false;
            }
            for (int i = 0; i < json.size(); i++) {
                if (!json.get(i).isTextual()) {
                    return false;
                }
                if (!dumlStrings.get(i).equals(json.get(i).asText())) {
                    return false;
                }
            }
        } else {
            throw new RuntimeException("Unimplemented case");
        }
        return true;
    }

    private boolean checkLostEquality(List<LostNode> lostNodes, JsonNode lostJsons) {
        if (!lostJsons.isArray()) {
            throw new RuntimeException("Expected the lostJsons file to be a JSON array");
        }
        if (lostNodes.size() != lostJsons.size()) {
            return false;
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
                return false;
            }
            if (!checkEquality(lostDuml.getNode(), lostJson.get("value"))) {
                return false;
            }
        }

        return true;
    }

}
