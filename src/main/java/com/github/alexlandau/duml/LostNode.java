package com.github.alexlandau.duml;

import java.util.List;
import java.util.Objects;

public class LostNode {
    private final List<String> location;
    private final DumlNode node;

    public LostNode(List<String> location, DumlNode node) {
        this.location = location;
        this.node = node;
    }

    public List<String> getLocation() {
        return location;
    }

    public DumlNode getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LostNode lostNode = (LostNode) o;
        return Objects.equals(location, lostNode.location) &&
                Objects.equals(node, lostNode.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, node);
    }

    @Override
    public String toString() {
        return location + ": " + node;
    }
}
