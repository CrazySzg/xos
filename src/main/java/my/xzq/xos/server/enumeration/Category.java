package my.xzq.xos.server.enumeration;

public enum Category {

    VIDEO("1"),
    PICTURE("2"),
    DOCUMENT("3"),
    AUDIO("4"),
    DIRECTORY("5");

    String type;
    Category(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
