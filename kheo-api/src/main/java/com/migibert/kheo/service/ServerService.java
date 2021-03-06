package com.migibert.kheo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.migibert.kheo.core.Server;
import com.migibert.kheo.core.ServerEvent;
import com.migibert.kheo.core.ServerState;
import com.migibert.kheo.core.plugin.KheoPlugin;
import com.migibert.kheo.core.plugin.ServerProperty;
import com.migibert.kheo.exception.ServerAlreadyExistException;
import com.migibert.kheo.exception.ServerConnectionException;
import com.migibert.kheo.exception.ServerNotFoundException;
import com.migibert.kheo.managed.ManagedScheduler;
import com.migibert.kheo.util.KheoPluginClassLoader;
import com.migibert.kheo.util.KheoUtils;

public class ServerService {

    private Logger logger = LoggerFactory.getLogger(ServerService.class);
    private ManagedScheduler scheduler;
    private MongoCollection serverCollection;
    private List<KheoPlugin<? extends ServerProperty>> plugins;

    public ServerService(MongoCollection serverCollection, ManagedScheduler scheduler, List<KheoPlugin<? extends ServerProperty>> plugins) {
        this.serverCollection = serverCollection;
        this.scheduler = scheduler;
        this.plugins = plugins;
    }

    public void create(Server server) {
        if (exists(server.host)) {
            throw new ServerAlreadyExistException(server);
        }

        List<String> pluginNames = KheoUtils.getPluginsNames(plugins);
        for (String pluginName : pluginNames) {
            if (!server.discoverySettings.containsKey(pluginName)) {
                server.discoverySettings.put(pluginName, false);
            }
        }

        logger.info("Adding server {}", server.host);
        server.state = ServerState.REGISTERED.name();
        serverCollection.insert(server);

        logger.info("Initializing server {} data with first discovery", server.host);
        scheduler.scheduleDiscovery(server.host);
    }

    public Server read(String host) {
        Thread.currentThread().setContextClassLoader(KheoPluginClassLoader.getInstance());
        return serverCollection.findOne("{host:#}", host).as(Server.class);
    }

    public List<Server> readAll() {
        Thread.currentThread().setContextClassLoader(KheoPluginClassLoader.getInstance());
        return Lists.newArrayList(serverCollection.find().as(Server.class).iterator());
    }

    public void update(Server server) {
        serverCollection.update("{host:#}", server.host).with(server);
    }

    public void delete(String host) {
        if (!exists(host)) {
            throw new ServerNotFoundException(host);
        }
        serverCollection.remove("{host:#}", host);
    }

    public Server discover(Server server) {
        boolean firstDiscovery = ServerState.REGISTERED.name().equals(server.state);
        server.state = ServerState.DISCOVERING.name();
        update(server);

        Server discovered = new Server(server.host, server.user, server.password, server.privateKey);
        discovered.sshPort = server.sshPort;
        discovered.sudo = server.sudo;
        discovered.sshConnectionValidity = false;
        discovered.eventLog = new ArrayList<ServerEvent>(server.eventLog);
        discovered.discoverySettings = server.discoverySettings;

        for (KheoPlugin<? extends ServerProperty> plugin : plugins) {
            if (server.discoverySettings.containsKey(plugin.getName()) && server.discoverySettings.get(plugin.getName())) {
                logger.info("{} discovery has been enabled", plugin.getName());

                try {
                    discovered.serverProperties.addAll(plugin.getSshCommand().executeAndParse(server));
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    discovered.sshConnectionValidity = false;
                    discovered.state = ServerState.ERROR.name();
                    update(discovered);
                    throw new ServerConnectionException(server.host);
                }

                if (firstDiscovery) {
                    logger.info("First discovery for {}, no event generation", server.host);
                } else {
                    Predicate<Object> predicate = Predicates.instanceOf(plugin.getEventGenerator().getPropertyClass());
                    Collection<ServerProperty> serverProperties = Collections2.filter(server.serverProperties, predicate);
                    Collection<ServerProperty> discoveredProperties = Collections2.filter(discovered.serverProperties, predicate);
                    List<ServerEvent> events = plugin.getEventGenerator().generateEvents(new ArrayList<>(serverProperties),
                                                                                         new ArrayList<>(discoveredProperties));
                    discovered.eventLog.addAll(events);
                }
            }
        }

        discovered.sshConnectionValidity = true;
        discovered.state = ServerState.READY.name();
        update(discovered);
        return discovered;

    }

    private boolean exists(String host) {
        return serverCollection.count("{host:#}", host) > 0;
    }

}
