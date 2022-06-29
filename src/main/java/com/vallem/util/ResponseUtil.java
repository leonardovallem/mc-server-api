package com.vallem.util;

import io.activej.http.HttpResponse;
import io.activej.promise.Promise;

public class ResponseUtil {
    public static Promise<HttpResponse> success() {
        return responseOf(200);
    }
    public static Promise<HttpResponse> success(String message) {
        return responseOf(200, message);
    }

    public static Promise<HttpResponse> badRequest() {
        return responseOf(400, "Bad request");
    }
    public static Promise<HttpResponse> badRequest(String message) {
        return responseOf(400, message);
    }

    public static Promise<HttpResponse> unauthorized() {
        return responseOf(401, "Unauthorized");
    }
    public static Promise<HttpResponse> unauthorized(String message) {
        return responseOf(401, message);
    }

    public static Promise<HttpResponse> responseOf(int code) {
        return Promise.of(HttpResponse.ofCode(code));
    }
    public static Promise<HttpResponse> responseOf(int code, String message) {
        return Promise.of(HttpResponse.ofCode(code).withPlainText(message));
    }
}
