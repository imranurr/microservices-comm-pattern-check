package com.imranur.microservices.comm.pattern.check;

import java.util.Map;

public class DockerServices {
    private String version;
    private Map<String,Services> services;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Services> getServices() {
        return services;
    }

    public void setServices(Map<String, Services> services) {
        this.services = services;
    }
}
