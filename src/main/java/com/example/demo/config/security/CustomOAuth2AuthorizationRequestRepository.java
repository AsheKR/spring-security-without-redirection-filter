package com.example.demo.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final char PATH_DELIMITER = '/';
    private final InMemoryClientRegistrationRepository clientRegistrationRepository;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        ClientRegistration clientRegistration = resolveRegistration(request);
        if (clientRegistration == null) {
            return null;
        }

        String redirectUriAction = getAction(request, "login");
        String redirectUriStr = expandRedirectUri(request, clientRegistration, redirectUriAction);

        return OAuth2AuthorizationRequest.authorizationCode()
                .attributes((attrs) -> attrs.put(OAuth2ParameterNames.REGISTRATION_ID, clientRegistration.getRegistrationId()))
                .clientId(clientRegistration.getClientId())
                .redirectUri(redirectUriStr)
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .state(request.getParameter("state"))
                .scopes(clientRegistration.getScopes())
                .build();
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request) {
        return loadAuthorizationRequest(request);
    }

    private ClientRegistration resolveRegistration(HttpServletRequest request) {
        return StreamSupport.stream(clientRegistrationRepository.spliterator(), false)
                .filter(registration -> {
                    return Optional
                            .ofNullable(UriComponentsBuilder
                                    .fromHttpUrl(registration.getRedirectUri())
                                    .build().getPath())
                            .orElse("")
                            .equals(request.getRequestURI());
                })
                .findFirst()
                .orElse(null);
    }

    private String getAction(HttpServletRequest request, String defaultAction) {
        String action = request.getParameter("action");
        if (action == null) {
            return defaultAction;
        }
        return action;
    }

    private static String expandRedirectUri(HttpServletRequest request, ClientRegistration clientRegistration,
                                            String action) {
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("registrationId", clientRegistration.getRegistrationId());
        // @formatter:off
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .build();
        // @formatter:on
        String scheme = uriComponents.getScheme();
        uriVariables.put("baseScheme", (scheme != null) ? scheme : "");
        String host = uriComponents.getHost();
        uriVariables.put("baseHost", (host != null) ? host : "");
        // following logic is based on HierarchicalUriComponents#toUriString()
        int port = uriComponents.getPort();
        uriVariables.put("basePort", (port == -1) ? "" : ":" + port);
        String path = uriComponents.getPath();
        if (StringUtils.hasLength(path)) {
            if (path.charAt(0) != PATH_DELIMITER) {
                path = PATH_DELIMITER + path;
            }
        }
        uriVariables.put("basePath", (path != null) ? path : "");
        uriVariables.put("baseUrl", uriComponents.toUriString());
        uriVariables.put("action", (action != null) ? action : "");
        return UriComponentsBuilder.fromUriString(clientRegistration.getRedirectUri()).buildAndExpand(uriVariables)
                .toUriString();
    }
}
