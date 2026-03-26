package com.exportbot.crawler.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthData {
    private List<Cookie> cookies = new ArrayList<>();
    private String token;
    private String tokenHeader = "Authorization";
    private Map<String, String> headers = new HashMap<>();

    public static class Cookie {
        private String name;
        private String value;
        private String domain;
        private String path = "/";
        private Long expires;
        private boolean httpOnly = false;
        private boolean secure = false;
        private String sameSite = "Lax";

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public Long getExpires() { return expires; }
        public void setExpires(Long expires) { this.expires = expires; }
        public boolean isHttpOnly() { return httpOnly; }
        public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }
        public boolean isSecure() { return secure; }
        public void setSecure(boolean secure) { this.secure = secure; }
        public String getSameSite() { return sameSite; }
        public void setSameSite(String sameSite) { this.sameSite = sameSite; }
    }

    public List<Cookie> getCookies() { return cookies; }
    public void setCookies(List<Cookie> cookies) { this.cookies = cookies; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTokenHeader() { return tokenHeader; }
    public void setTokenHeader(String tokenHeader) { this.tokenHeader = tokenHeader; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}
