package com.algaworks.algafoodauth.secutity;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;

public class JwtCustomClaimsTokenEnhancer implements TokenEnhancer {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {

        /*Esse if serve para não quebrar qnd temos outro tipo de autenticação (Ex: grant_type = client_credentials)*/
        if (oAuth2Authentication.getPrincipal() instanceof AuthUser) {
            /*Isso serve para adicionar as infos no token*/
            AuthUser authUser = (AuthUser) oAuth2Authentication.getPrincipal();

            HashMap<String, Object> info = new HashMap<>();
            info.put("nome_completo", authUser.getFullName());
            info.put("usuario_id", authUser.getUserId());

            /*Fizemos o cast para ter o setAdditionalInformation*/
            DefaultOAuth2AccessToken defaultOAuth2AccessToken = (DefaultOAuth2AccessToken) oAuth2AccessToken;
            defaultOAuth2AccessToken.setAdditionalInformation(info);
        }

        return oAuth2AccessToken;
    }
}
