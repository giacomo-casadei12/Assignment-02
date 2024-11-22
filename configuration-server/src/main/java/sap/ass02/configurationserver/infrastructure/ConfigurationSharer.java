package sap.ass02.configurationserver.infrastructure;

public interface ConfigurationSharer {

    void addConfiguration(String configurationName, String configurationFile);

    void updateConfiguration(String configurationName, String configurationFile);

}
