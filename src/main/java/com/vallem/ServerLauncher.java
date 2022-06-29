package com.vallem;

import io.activej.config.Config;
import io.activej.config.ConfigModule;
import io.activej.config.converter.ConfigConverters;
import io.activej.eventloop.Eventloop;
import io.activej.eventloop.inspector.EventloopInspector;
import io.activej.eventloop.inspector.ThrottlingController;
import io.activej.http.AsyncHttpServer;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import io.activej.inject.binding.OptionalDependency;
import io.activej.inject.module.AbstractModule;
import io.activej.inject.module.Module;
import io.activej.inject.module.Modules;
import io.activej.launcher.Launcher;
import io.activej.launchers.initializers.Initializers;
import io.activej.service.ServiceGraphModule;

import java.net.InetSocketAddress;

import static com.vallem.util.ServerUtil.getPort;

public abstract class ServerLauncher extends Launcher {
    public static final String PROPERTIES_FILE = "http-server.properties";
    @Inject
    AsyncHttpServer httpServer;

    public ServerLauncher() {}

    @Provides
    Eventloop eventloop(Config config, OptionalDependency<ThrottlingController> throttlingController) {
        return (Eventloop.create()
                .withInitializer(Initializers.ofEventloop(config.getChild("eventloop"))))
                .withInitializer((eventloop) -> {
                    eventloop.withInspector(throttlingController.orElse(null));
                });
    }

    @Provides
    AsyncHttpServer server(Eventloop eventloop, AsyncServlet rootServlet, Config config) {
        return AsyncHttpServer.create(eventloop, rootServlet).withInitializer(Initializers.ofHttpServer(config.getChild("http")));
    }

    @Provides
    Config config() {
        return Config.create().with("http.listenAddresses", Config.ofValue(ConfigConverters.ofInetSocketAddress(), new InetSocketAddress(getPort()))).overrideWith(Config.ofClassPathProperties("http-server.properties", true)).overrideWith(Config.ofSystemProperties("config"));
    }

    protected final Module getModule() {
        return Modules.combine(ServiceGraphModule.create(), ConfigModule.create().withEffectiveConfigLogger(), this.getBusinessLogicModule());
    }

    protected Module getBusinessLogicModule() {
        return Module.empty();
    }

    protected void run() throws Exception {
        this.logger.info("HTTP Server is now available at {}", String.join(", ", this.httpServer.getHttpAddresses()));
        this.awaitShutdown();
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new ServerLauncher() {
            protected Module getBusinessLogicModule() {
                return new AbstractModule() {
                    @Provides
                    public AsyncServlet servlet(Config config) {
                        String message = config.get("message", "Hello, world!");
                        return (request) -> HttpResponse.ok200().withPlainText(message);
                    }
                };
            }
        };
        launcher.launch(args);
    }
}
