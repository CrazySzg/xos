package my.xzq.xos.server.enumeration;

public enum Category {

    VIDEO("1"),
    PICTURE("2"),
    FILE("3"),
    DOCUMENT("4"),
    AUDIO("5"),
    DIRECTORY("6");

    String type;
    Category(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
