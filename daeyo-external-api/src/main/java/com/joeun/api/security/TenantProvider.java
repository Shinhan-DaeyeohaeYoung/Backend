package com.joeun.api.security;

import org.springframework.stereotype.Component;

@Component
public class TenantProvider {
    public Long universityId()   { return 1L; }
    public Long organizationId() { return 2L; }
}
