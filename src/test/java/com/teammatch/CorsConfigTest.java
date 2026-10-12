package com.teammatch;

import com.teammatch.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

public class CorsConfigTest {

    @Test
    public void testVercelOriginAllowed() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "https://teammatch-frontend-ten.vercel.app");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "Authorization, Content-Type");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertNotNull(config, "CORS configuration should be present for /api/auth/login");
        
        String checkedOrigin = config.checkOrigin("https://teammatch-frontend-ten.vercel.app");
        assertEquals("https://teammatch-frontend-ten.vercel.app", checkedOrigin, "Vercel origin must be allowed");
        
        assertTrue(config.getAllowCredentials(), "Allow credentials should be true");
        assertTrue(config.getAllowedMethods().contains("OPTIONS"), "OPTIONS should be allowed");
        assertTrue(config.getAllowedMethods().contains("POST"), "POST should be allowed");
    }

    @Test
    public void testLocalOriginsAllowed() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/opportunities");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "GET");

        CorsConfiguration config = source.getCorsConfiguration(request);
        assertNotNull(config);
        assertEquals("http://localhost:3000", config.checkOrigin("http://localhost:3000"));
        assertEquals("http://127.0.0.1:3000", config.checkOrigin("http://127.0.0.1:3000"));
    }

    @Test
    public void testDisallowedOriginRejected() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        CorsConfiguration config = source.getCorsConfiguration(request);
        assertNotNull(config);

        String checkedOrigin = config.checkOrigin("https://unauthorized-domain.com");
        assertNull(checkedOrigin, "Unauthorized domain must not be allowed");
    }
}
