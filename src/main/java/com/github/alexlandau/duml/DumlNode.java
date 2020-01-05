package com.github.alexlandau.duml;

import java.util.*;

//@NotThreadSafe
public abstract class DumlNode {
    private DumlNode() {
        // Only instantiable as DumlObjectNode or DumlStringsNode
    }

    public static DumlObjectNode emptyObject() {
        return new DumlObjectNode(new LinkedHashMap<>());
    }
    public static DumlStringsNode emptyStrings() {
        return new DumlStringsNode(new ArrayList<>());
    }
    public static DumlStringsNode strings(List<String> strings) {
        if (strings == null) {
            throw new NullPointerException();
        }
        return new DumlStringsNode(strings);
    }

    public abstract boolean isObject();
    public abstract boolean isStrings();

    //@Nullable
    public abstract DumlNode get(String key, String... moreKeys);
    public abstract List<String> getStrings();
    public List<String> getStrings(String key, String... moreKeys) {
        DumlNode nodeAtKey = get(key, moreKeys);
        if (nodeAtKey == null) {
            return Collections.emptyList();
        }
        return nodeAtKey.getStrings();
    }
    //@Nullable
    public abstract String getLastString();
    //@Nullable
    public String getLastString(String key, String... moreKeys) {
        DumlNode nodeAtKey = get(key, moreKeys);
        if (nodeAtKey == null) {
            return null;
        }
        return nodeAtKey.getLastString();
    }

    public abstract Map<String, DumlNode> getMap();

    public static final class DumlObjectNode extends DumlNode {
        private final Map<String, DumlNode> contents;

        private DumlObjectNode(Map<String, DumlNode> contents) {
            this.contents = contents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DumlObjectNode that = (DumlObjectNode) o;
            return contents.equals(that.contents);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contents);
        }

        @Override
        public String toString() {
            return contents.toString();
        }

        @Override
        public boolean isObject() {
            return true;
        }

        @Override
        public boolean isStrings() {
            return false;
        }

        @Override
        public DumlNode get(String key, String... moreKeys) {
            DumlNode result = getInternal(key);
            if (result == null) {
                return result;
            }
            for (String additionalKey : moreKeys) {
                if (!(result instanceof DumlObjectNode)) {
                    return null;
                }
                result = ((DumlObjectNode) result).getInternal(additionalKey);
                if (result == null) {
                    return null;
                }
            }
            return result;
        }

        //@Nullable
        private DumlNode getInternal(String key) {
            String[] keyComponents = key.split("\\.");
            DumlNode curNode = this;
            for (String keyComponent : keyComponents) {
                if (!(curNode instanceof DumlObjectNode)) {
                    return null;
                }
                DumlNode containedNode = ((DumlObjectNode) curNode).contents.get(keyComponent);
                if (containedNode == null) {
                    return null;
                }
                curNode = containedNode;
            }
            return curNode;
        }

        @Override
        public List<String> getStrings() {
            return Collections.emptyList();
        }

        @Override
        public String getLastString() {
            return null;
        }

        @Override
        public Map<String, DumlNode> getMap() {
            return contents;
        }
    }

    public static final class DumlStringsNode extends DumlNode {
        private final List<String> strings;

        private DumlStringsNode(List<String> strings) {
            this.strings = strings;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DumlStringsNode that = (DumlStringsNode) o;
            return strings.equals(that.strings);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strings);
        }

        @Override
        public String toString() {
            return strings.toString();
        }

        @Override
        public boolean isObject() {
            return false;
        }

        @Override
        public boolean isStrings() {
            return true;
        }

        @Override
        public DumlNode get(String key, String... moreKeys) {
            return null;
        }

        @Override
        public List<String> getStrings() {
            return strings;
        }

        @Override
        public String getLastString() {
            if (strings.isEmpty()) {
                return null;
            } else {
                return strings.get(strings.size() - 1);
            }
        }

        @Override
        public Map<String, DumlNode> getMap() {
            return Collections.emptyMap();
        }
    }
}
