package com.tiger.tgcloud.security;

import com.tiger.tgcloud.security.core.properties.OAuth2ClientProperties;
import com.tiger.tgcloud.security.core.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @description:
 * @author: tiger
 * @date: 2018/8/1 22:18
 * @version: V1.0
 * @modified by:
 */
@Slf4j
@Component("restClientDetailsService")
public class RestClientDetailsServiceImpl implements ClientDetailsService {

    private ClientDetailsService clientDetailsService;

    @Autowired
    private SecurityProperties securityProperties;

    /**
     * Init.
     * Oauth支持的5类 grant_type 及说明
     * authorization_code 授权码模式(即先登录获取code,再获取token)
     * password 密码模式(将用户名,密码传过去,直接获取token)
     * client_credentials 客户端模式(无用户,用户向客户端注册,然后客户端以自己的名义向’服务端’获取资源)
     * implicit 简化模式(在redirect_uri 的Hash传递token; Auth客户端运行在浏览器中,如JS,Flash)
     * refresh_token 刷新access_token
     */
    @PostConstruct
    public void init() {
        InMemoryClientDetailsServiceBuilder builder = new InMemoryClientDetailsServiceBuilder();
        if (ArrayUtils.isNotEmpty(securityProperties.getOauth2().getClients())) {
            for (OAuth2ClientProperties client : securityProperties.getOauth2().getClients()) {
                builder.withClient(client.getClientId())
                        .secret(client.getClientSecret())
                        .authorizedGrantTypes("refresh_token", "password", "client_credentials")
                        .accessTokenValiditySeconds(client.getAccessTokenValidateSeconds())
                        .refreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds())
                        .scopes(client.getScope());
            }
        }
        try {
            clientDetailsService = builder.build();
        } catch (Exception e) {
            log.error("init={}", e.getMessage(), e);
        }
    }

    /**
     * Load client by client id client details.
     *
     * @param clientId the client id
     * @return the client details
     * @throws ClientRegistrationException the client registration exception
     */
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return clientDetailsService.loadClientByClientId(clientId);
    }
}