package com.github.alexlandau.duml;

import org.junit.Test;

import static org.junit.Assert.*;

public class DumlNodeTest {
    @Test
    public void testGet() {
        DumlNode root = DumlParser.parse("a.b.c d", false).getDuml();
        assertTrue(root instanceof DumlNode.DumlObjectNode);
        DumlNode a = root.get("a");
        assertNotNull(a);
        assertTrue(a instanceof DumlNode.DumlObjectNode);
        DumlNode b = a.get("b");
        assertNotNull(b);
        assertTrue(b instanceof DumlNode.DumlObjectNode);
        DumlNode c = b.get("c");
        assertNotNull(c);
        assertTrue(c instanceof DumlNode.DumlStringsNode);

        // Dot-based getter
        assertSame(a, root.get("a"));
        assertSame(b, root.get("a.b"));
        assertSame(c, root.get("a.b.c"));
        assertSame(b, a.get("b"));
        assertSame(c, a.get("b.c"));
        assertSame(c, b.get("c"));

        // Multi-string getter
        assertSame(b, root.get("a", "b"));
        assertSame(c, root.get("a", "b", "c"));
        assertSame(c, root.get("a.b", "c"));
        assertSame(c, root.get("a", "b.c"));

        assertNull(root.get("ab"));
        assertNull(root.get("b"));
        assertNull(c.get(""));
        assertNull(c.get("d"));
    }
}
