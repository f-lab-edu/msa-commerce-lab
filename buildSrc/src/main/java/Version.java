public enum Version {
    SPRING_BOOT("3.5.4"),
    DEPENDENCY_MANAGEMENT("1.1.7"),
    SONARQUBE("6.2.0.5505"),
    FLYWAY("10.21.0"),
    QUERYDSL_PLUGIN("1.0.10"),
    JAVA_VERSION("21");

    private final String version;

    Version(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionAsInt() {
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Version '" + version + "' cannot be converted to integer", e);
        }
    }

    @Override
    public String toString() {
        return version;
    }
}
