package com.paydaes.corehr.tenant;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Aspect wil run before @Transactional so that the correct datasource key is set
// before Spring acquires the DB connection for the transaction
// @Transactional default order = Ordered.LOWEST_PRECEDENCE (Integer.MAX_VALUE)
// so we go one step outer to intercept first
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class DataSourceRoutingAspect {

    @Around("@within(com.paydaes.corehr.tenant.annotation.UseCommonDb) || @annotation(com.paydaes.corehr.tenant.annotation.UseCommonDb)")
    public Object switchToCommonDb(ProceedingJoinPoint point) throws Throwable {
        log.debug("Switching to common DB for {}", point.getSignature());
        TenantContext.useCommonDb();
        try {
            return point.proceed();
        } finally {
            TenantContext.useCompanyDb();
            log.debug("Restored to company DB after {}", point.getSignature());
        }
    }
}
