package com.minicore.core.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Gắn mã truy vết cho mỗi request: lấy từ header X-Correlation-Id nếu phía gọi
 * (sau này là api-gateway) đã gửi, nếu không thì tự sinh. Mã này nằm trong log
 * và trong trường traceId của response.
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, traceId);
        response.setHeader(HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    public static String currentTraceId() {
        return MDC.get(MDC_KEY);
    }
}
