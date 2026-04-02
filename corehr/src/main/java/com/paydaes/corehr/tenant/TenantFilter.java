package com.paydaes.corehr.tenant;

import com.paydaes.corehr.exception.TenantResolutionException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class TenantFilter implements Filter {

    public static final String HEADER_CLIENT_ID = "X-Client-Id";
    public static final String HEADER_COMPANY_ID = "X-Company-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        String clientIdStr = httpRequest.getHeader(HEADER_CLIENT_ID);
        String companyIdStr = httpRequest.getHeader(HEADER_COMPANY_ID);

        try {
            if (clientIdStr == null || companyIdStr == null) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setContentType("application/json");
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write(
                    "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Missing required headers: "
                    + HEADER_CLIENT_ID + ", " + HEADER_COMPANY_ID + "\",\"path\":\"" + path + "\"}"
                );
                return;
            }

            try {
                Long clientId = Long.parseLong(clientIdStr);
                Long companyId = Long.parseLong(companyIdStr);
                TenantContext.setCurrentTenant(clientId, companyId);
                log.debug("Tenant context set — clientId={}, companyId={}", clientId, companyId);
            } catch (NumberFormatException e) {
                throw new TenantResolutionException("Invalid tenant header values — must be numeric IDs");
            }

            chain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}
