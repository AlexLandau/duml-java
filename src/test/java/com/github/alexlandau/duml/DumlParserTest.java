package com.github.alexlandau.duml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class DumlParserTest {
    @Test
    public void testCase1() {
        DumlNode object = DumlParser.parse("", false);
        assertEquals(0, object.getMap().size());
    }
    @Test
    public void testCase2() {
        DumlNode object = DumlParser.parse("\n", false);
        assertEquals(0, object.getMap().size());
    }
    @Test
    public void testCase3() {
        DumlNode object = DumlParser.parse("foo bar", false);
        assertEquals(1, object.getMap().size());
        assertEquals(ImmutableMap.of("foo", DumlNode.strings(ImmutableList.of("bar"))), object.getMap());
    }
}
