package com.example.demo.interceptor;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication Middleware - Sends a SOAP XML call to user-soap-service
 * to validate the JWT token before allowing access to REST endpoints.
 */
@Component
public class TokenValidationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TokenValidationInterceptor.class);

    @Value("${soap.service.url}")
    private String soapServiceUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Token ilgeegdeeegui (No Bearer token provided)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authorization header with Bearer token required\"}");
            return false;
        }

        String token = authHeader.substring(7);
        log.info("[Middleware] Token oldsn, SOAP service ruu shalgah huselt ilgeej baina...");
        log.info("[Middleware] Token: {}...{}", token.substring(0, Math.min(20, token.length())),
                token.substring(Math.max(0, token.length() - 10)));

        try {
            boolean valid = callSoapValidateToken(token);
            if (!valid) {
                log.warn("[Middleware] Token HUCHIINGUI - 401 butsaalaa");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return false;
            }
            log.info("[Middleware] Token BATALGAAJSAN - huselt zovshoorchloooo");
            return true;
        } catch (Exception e) {
            log.error("SOAP service-ruu holbogdoj chadsangui: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication service unavailable\"}");
            return false;
        }
    }

    private boolean callSoapValidateToken(String token) throws Exception {
        String soapEnvelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:auth="http://example.com/auth">
                    <soapenv:Body>
                        <auth:validateTokenRequest>
                            <auth:token>%s</auth:token>
                        </auth:validateTokenRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(token);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(soapServiceUrl))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                .build();

        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() != 200) {
            log.error("SOAP response status: {}", httpResponse.statusCode());
            return false;
        }

        return parseSoapValidationResponse(httpResponse.body());
    }

    private boolean parseSoapValidationResponse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList validNodes = doc.getElementsByTagNameNS("http://example.com/auth", "valid");
        if (validNodes.getLength() > 0) {
            return Boolean.parseBoolean(validNodes.item(0).getTextContent());
        }
        return false;
    }
}
