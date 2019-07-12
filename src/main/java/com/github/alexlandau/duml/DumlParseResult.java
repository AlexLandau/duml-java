package com.github.alexlandau.duml;

import java.util.List;
import java.util.Objects;

public class DumlParseResult {
    private final DumlNode duml;
    private final List<LostNode> lostNodes;

    public DumlParseResult(DumlNode duml, List<LostNode> lostNodes) {
        this.duml = duml;
        this.lostNodes = lostNodes;
    }

    public DumlNode getDuml() {
        return duml;
    }

    public List<LostNode> getLostNodes() {
        return lostNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DumlParseResult that = (DumlParseResult) o;
        return Objects.equals(duml, that.duml) &&
                Objects.equals(lostNodes, that.lostNodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duml, lostNodes);
    }

    @Override
    public String toString() {
        if (lostNodes.isEmpty()) {
            return duml.toString();
        }
        return duml.toString() + " with lost nodes: " + lostNodes.toString();
    }
}
