package sap.ass02.configurationserver.domain;

public interface ConfigurationSharer {

    void addConfiguration(String configurationName, String configurationFile);

    void updateConfiguration(String configurationName, String configurationFile);

}
