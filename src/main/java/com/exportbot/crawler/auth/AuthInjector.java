package com.exportbot.crawler.auth;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.microsoft.playwright.Route;

@Component
public class AuthInjector {

    private static final Logger logger = LoggerFactory.getLogger(AuthInjector.class);

    public void injectAuth(BrowserContext context, Page page, AuthData auth, String targetUrl) {
        // Inject cookies
        if (!auth.getCookies().isEmpty()) {
            injectCookies(context, auth.getCookies(), targetUrl);
        }

        // Inject token and custom headers via request interception
        if (auth.getToken() != null || !auth.getHeaders().isEmpty()) {
            injectHeaders(page, auth);
        }
    }

    private void injectCookies(BrowserContext context, List<AuthData.Cookie> cookies, String targetUrl) {
        List<Cookie> playwrightCookies = new ArrayList<>();

        for (AuthData.Cookie cookie : cookies) {
            String domain = cookie.getDomain();
            if (domain == null || domain.isEmpty()) {
                domain = extractDomain(targetUrl);
            }

            Cookie pwCookie = new Cookie(cookie.getName(), cookie.getValue());
            pwCookie.setDomain(domain);
            pwCookie.setPath(cookie.getPath());
            if (cookie.getExpires() != null) {
                pwCookie.setExpires(cookie.getExpires());
            }
            pwCookie.setHttpOnly(cookie.isHttpOnly());
            pwCookie.setSecure(cookie.isSecure());
            
            // Convert SameSite string to enum
            SameSiteAttribute sameSite = parseSameSite(cookie.getSameSite());
            if (sameSite != null) {
                pwCookie.setSameSite(sameSite);
            }

            playwrightCookies.add(pwCookie);
        }

        context.addCookies(playwrightCookies);
        logger.info("Injected {} cookies", playwrightCookies.size());
    }

    private SameSiteAttribute parseSameSite(String sameSite) {
        if (sameSite == null) return null;
        return switch (sameSite.toLowerCase()) {
            case "strict" -> SameSiteAttribute.STRICT;
            case "lax" -> SameSiteAttribute.LAX;
            case "none" -> SameSiteAttribute.NONE;
            default -> SameSiteAttribute.LAX;
        };
    }

    private void injectHeaders(Page page, AuthData auth) {
        page.route("**/*", route -> {
            var headers = new java.util.HashMap<>(route.request().headers());

            // Inject token header
            if (auth.getToken() != null && !auth.getToken().isEmpty()) {
                headers.put(auth.getTokenHeader(), auth.getToken());
                logger.debug("Injected token header: {}", auth.getTokenHeader());
            }

            // Inject custom headers
            auth.getHeaders().forEach((key, value) -> {
                headers.put(key, value);
                logger.debug("Injected custom header: {}", key);
            });

            route.resume(new Route.ResumeOptions().setHeaders(headers));
        });

        logger.info("Set up header injection");
    }

    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return ".example.com";
        }
        try {
            URL parsed = new URL(url);
            String host = parsed.getHost();
            // Return domain with leading dot for subdomain cookies
            return host.startsWith("www.") ? host.substring(3) : "." + host;
        } catch (MalformedURLException e) {
            logger.warn("Failed to parse URL: {}", url);
            return ".example.com";
        }
    }
}
