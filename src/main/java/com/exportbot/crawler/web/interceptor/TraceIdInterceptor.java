package com.exportbot.crawler.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Trace ID 拦截器
 * 为每个请求生成唯一的 trace ID，用于日志追踪
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先从请求头中获取 trace ID（用于链路传递）
        String traceId = request.getHeader(TRACE_ID_HEADER);
        
        // 如果没有则生成新的
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 放入 MDC
        MDC.put(TRACE_ID_KEY, traceId);
        
        // 设置到响应头，方便客户端获取
        response.setHeader(TRACE_ID_HEADER, traceId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 MDC
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 生成 trace ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
