package com.vallem;

import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.vallem.model.Token;
import com.vallem.service.AuthService;
import com.vallem.service.InstanceService;
import com.vallem.util.JSON;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpHeaders;
import io.activej.http.HttpMethod;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;

import static com.vallem.service.InstanceService.getInstance;
import static com.vallem.util.ResponseUtil.badRequest;
import static com.vallem.util.ResponseUtil.responseOf;
import static com.vallem.util.ResponseUtil.success;
import static com.vallem.util.ResponseUtil.unauthorized;

public class WebApp extends ServerLauncher {
    @Provides
    AsyncServlet servlet() {
        return RoutingServlet.create()
                .map("/", request -> request.loadBody()
                        .then(() -> {
                            if (!AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                                return unauthorized();
                            }

                            var instance = getInstance();
                            if (instance == null) return badRequest("EC2 Instance was not found");
                            return success(JSON.stringify(instance));
                        }))
                .map("/state", request -> request.loadBody()
                        .then(() -> {
                            if (!AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                                return unauthorized();
                            }

                            final var state = InstanceService.getEC2InstanceState();
                            return state == null
                                    ? badRequest("EC2 instance state could not be retrieved")
                                    : success(JSON.stringify(state));
                        }))
                .map(HttpMethod.POST, "/stop", request -> {
                    if (!AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                        return unauthorized();
                    }

                    try {
                        InstanceService.stopEC2Instance();
                    } catch (AmazonEC2Exception e) {
                        e.printStackTrace();
                        return badRequest(e.getMessage());
                    } catch (Exception e) {
                        return responseOf(500, e.getMessage());
                    }

                    return success("EC2 instance being stopped");
                })
                .map(HttpMethod.POST, "/start", request -> {
                    if (!AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                        return unauthorized();
                    }

                    try {
                        InstanceService.startEC2Instance();
                    } catch (AmazonEC2Exception e) {
                        e.printStackTrace();
                        return badRequest(e.getMessage());
                    } catch (Exception e) {
                        return responseOf(500, e.getMessage());
                    }

                    return success("EC2 instance being started");
                })
                .map(HttpMethod.POST, "/restart", request -> {
                    if (!AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))) {
                        return unauthorized();
                    }

                    try {
                        InstanceService.restartEC2Instance();
                    } catch (AmazonEC2Exception e) {
                        e.printStackTrace();
                        return badRequest(e.getMessage());
                    } catch (Exception e) {
                        return responseOf(500, e.getMessage());
                    }

                    return success("EC2 instance being restarted");
                })
                .map(HttpMethod.POST, "/token", request -> request.loadBody()
                        .then(() -> {
                            var secretId = request.getPostParameter("SECRET_ID");
                            if (secretId == null) return badRequest("Missing identification");

                            try {
                                var token = AuthService.generateToken(secretId);
                                if (token == null) return unauthorized("Identification not allowed");

                                return success(JSON.stringify(new Token(token)));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return responseOf(500, e.getMessage());
                            }
                        })
                )
                .map("/token", request -> request.loadBody()
                        .then(() -> AuthService.isAuthorized(request.getHeader(HttpHeaders.AUTHORIZATION))
                                ? success("Token is valid")
                                : unauthorized("Token is invalid"))
                );
    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new WebApp();
        launcher.launch(args);
    }
}
