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
        if (!jsonFile.exists()) {
            throw new IllegalStateException("DUML test case file " + dumlFile.getName() + " does not have a corresponding .json file");
        }

        DumlNode duml = DumlParser.parse(dumlFile, false);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonFile);

        if (!checkEquality(duml, json)) {
            throw new AssertionError("Unexpected parsing result: " + duml);
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
}
