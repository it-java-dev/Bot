package org.bot.ua.service.enums;

// contain generation of resources
public enum LinkType {
    GET_FILE("type/file"),
    GET_FILES("type/files");
    private final String link;

    LinkType(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return link;
    }
}
