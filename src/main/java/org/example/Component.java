package org.example;

public interface Component {
    void start();
    void shutdown();
    void pushConfiguration();
    void collectData();
}
