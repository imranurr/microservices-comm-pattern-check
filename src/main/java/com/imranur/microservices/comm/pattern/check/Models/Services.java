package com.imranur.microservices.comm.pattern.check.Models;

import java.util.List;

public class Services {

    private String build;
    private List<String> links;
    private List<String> depends_on;

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<String> getDepends_on() {
        return depends_on;
    }

    public void setDepends_on(List<String> depends_on) {
        this.depends_on = depends_on;
    }
}
