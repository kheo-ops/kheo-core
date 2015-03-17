package com.migibert.kheo.core;

import java.util.List;

import com.migibert.kheo.core.plugin.KheoPlugin;

public class PluginDTO {

    public String name;
    public List<String> propertiesNames;
    public String version;

    public PluginDTO(KheoPlugin<?> plugin) {
        name = plugin.getName();
        propertiesNames = plugin.getPropertiesNames();
        version = plugin.getVersion();
    }
}
