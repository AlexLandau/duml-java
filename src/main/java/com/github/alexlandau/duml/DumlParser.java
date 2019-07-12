package com.github.alexlandau.duml;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DumlParser {
    // TODO: Actually support trimValues; add tests for this
    private final boolean trimValues;
    private DumlParser(boolean trimValues) {
        this.trimValues = trimValues;
    }

    public static DumlParseResult parse(String dumlText, boolean trimValues) {
        try {
            return new DumlParser(trimValues).run(new StringReader(dumlText));
        } catch (IOException e) {
            // This shouldn't happen
            e.printStackTrace();
            throw new RuntimeException("Error parsing DUML string " + dumlText, e);
        }
    }

    public static DumlParseResult parse(File dumlFile, boolean trimValues) throws IOException {
        return new DumlParser(trimValues).run(new BufferedReader(new FileReader(dumlFile)));
    }

    private static final int NEWLINE_N = '\n';
    private static final int NEWLINE_R = '\r';
    private static final int EOF = -1;
    private static final int SPACE = ' ';
    private static final int TAB = '\t';

    // State of the thing being built
    private final DumlNode rootNode = DumlNode.emptyObject();
    private final List<LostNode> lostNodes = new ArrayList<>();

    private DumlParseResult run(Reader reader) throws IOException {
        perLineLoop : while (true) {
            StringBuilder keyBuilder = new StringBuilder();
            final String key;

            reader.mark(1);
            int firstChar = reader.read();
            if (firstChar == '#') {
                // Handle comment; ignore the rest of the line
                int curChar = firstChar;
                while (curChar != '\n' && curChar != '\r' && curChar != -1) {
                    curChar = reader.read();
                }
                continue perLineLoop;
            } else {
                reader.reset();
            }

            readKeyCharLoop : while (true) {
                int curChar = reader.read();
                if (curChar == SPACE || curChar == TAB) {
                    key = keyBuilder.toString();
                    break readKeyCharLoop;
                } else if (curChar == NEWLINE_R || curChar == NEWLINE_N || curChar == EOF) {
                    key = keyBuilder.toString();
                    if (!key.isEmpty()) {
                        processKeyAndValue(key, "");
                    }
                    if (curChar == EOF) {
                        return new DumlParseResult(rootNode, lostNodes);
                    }
                    continue perLineLoop;
                } else {
                    keyBuilder.appendCodePoint(curChar);
                }
            }

            StringBuilder valueBuilder = new StringBuilder();
            readValueLoop : while (true) {
                int curChar = reader.read();

                if (curChar == NEWLINE_N || curChar == NEWLINE_R || curChar == EOF) {
                    String value = valueBuilder.toString();
                    processKeyAndValue(key, value);
                    if (curChar == EOF) {
                        return new DumlParseResult(rootNode, lostNodes);
                    }
                    continue perLineLoop;
                } else {
                    valueBuilder.appendCodePoint(curChar);
                }
            }
        }
    }

    private void processKeyAndValue(String key, String value) {
        String[] keyParts = key.split("\\.");

        DumlNode keyNode = rootNode;
        // For each key part that isn't the last...
        for (int i = 0; i < keyParts.length - 1; i++) {
            String keyPart = keyParts[i];
            // TODO: Get back to this part
            DumlNode curValue = keyNode.get(keyPart);
            if (curValue == null) {
                DumlNode.DumlObjectNode newNode = DumlNode.emptyObject();
                keyNode.getMap().put(keyPart, newNode);
                keyNode = newNode;
            } else if (curValue.isObject()) {
                keyNode = curValue;
            } else if (curValue.isStrings()) {
                lostNodes.add(new LostNode(Arrays.asList(keyParts).subList(0, i + 1), curValue));

                DumlNode.DumlObjectNode newNode = DumlNode.emptyObject();
                keyNode.getMap().put(keyPart, newNode);
                keyNode = newNode;
            }
        }

        // For the last part of the key, assume the current keyNode is an object
        String lastKeyPart = keyParts[keyParts.length - 1];
        DumlNode curNodeAtKey = keyNode.get(lastKeyPart);
        if (curNodeAtKey == null) {
            DumlNode newNode = DumlNode.emptyStrings();
            keyNode.getMap().put(lastKeyPart, newNode);
            curNodeAtKey = newNode;
        } else if (curNodeAtKey.isObject()) {
            lostNodes.add(new LostNode(Arrays.asList(keyParts), curNodeAtKey));

            DumlNode newNode = DumlNode.emptyStrings();
            keyNode.getMap().put(lastKeyPart, newNode);
            curNodeAtKey = newNode;
        } else {
            // This case is fine
        }

        curNodeAtKey.getStrings().add(value);
    }
}
