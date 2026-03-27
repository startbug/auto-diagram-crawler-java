package com.exportbot.crawler.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AuthData {
    private List<Cookie> cookies = new ArrayList<>();
    private String token;
    private String tokenHeader = "Authorization";
    private Map<String, String> headers = new HashMap<>();

    @Data
    public static class Cookie {
        private String name;
        private String value;
        private String domain;
        private String path = "/";
        private Long expires;
        private boolean httpOnly = false;
        private boolean secure = false;
        private String sameSite = "Lax";

    }

}
