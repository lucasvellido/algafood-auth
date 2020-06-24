package com.algaworks.algafoodauth.secutity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("algafood-web")
                .secret(passwordEncoder.encode("321"))
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("write", "read")
                .accessTokenValiditySeconds(6 * 60 * 60) // 6 horas
                .refreshTokenValiditySeconds(60 * 24 * 60 * 60) // 60 dias
            .and()
                .withClient("foodanalytics")
                .secret(passwordEncoder.encode("food321"))
                .authorizedGrantTypes("authorization_code")
                .scopes("write", "read")
                .redirectUris("http://aplicacao-cliente")
            .and()
                .withClient("webadmin")
                .authorizedGrantTypes("implicit")
                .scopes("write", "read")
                .redirectUris("http://aplicacao-cliente")
            .and()
                .withClient("faturamento") // esse cara é para um outro servidor acessar nossa API
                .secret(passwordEncoder.encode("321321"))
                .authorizedGrantTypes("client_credentials")
                .scopes("read")
                .accessTokenValiditySeconds(6 * 60 * 60) // 6 horas
            .and()
                .withClient("check-token")
                .secret(passwordEncoder.encode("check321"));
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http.httpBasic()
//                .and()
//                .authorizeRequests()
////                    .antMatchers("/restaurantes/**").permitAll()
//                .anyRequest().authenticated()
//
//                /*Para não guardar cookies (sesion)*/
//                .and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//
//                .and()
//                .csrf()
//                .disable();
//    }


    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("isAuthenticated()");


//                .tokenKeyAccess("permitAll()"); /*server para gerar a chava publica*/
//        security.checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        /*Isso serve para adicionar as infos que configuramos na classe JwtCustomClaimsTokenEnhancer no token*/
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(new JwtCustomClaimsTokenEnhancer(), jwtAccessTokenConverter()));
        endpoints
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailService)
                .reuseRefreshTokens(false) // cada vez gera um novo refresh token
                .accessTokenConverter(jwtAccessTokenConverter())
                .approvalStore(approvalStore(endpoints.getTokenStore())) //add aprovação granular
                .tokenEnhancer(tokenEnhancerChain);
//            .tokenStore(redisTokenStore())

    }

    /*TODO Pkce para authorization server*/
//    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
//
//    }

    private ApprovalStore approvalStore(TokenStore tokenStore) {
        TokenApprovalStore tokenApprovalStore = new TokenApprovalStore();
        tokenApprovalStore.setTokenStore(tokenStore);
        return tokenApprovalStore;
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        /*Isso é para chave simetrica*/
//        jwtAccessTokenConverter.setSigningKey("qweqweqweqdadasdsadasdasdewqeqwedaasdasdaweqwe");

        /*Chave assimetrica*/
        ClassPathResource jksResource = new ClassPathResource("/keystores/algafood.jks");
        String keyStorePass = "321321";
        String keyPairAlias = "algafood";

        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(jksResource, keyStorePass.toCharArray());

        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(keyPairAlias);

        jwtAccessTokenConverter.setKeyPair(keyPair);

        return jwtAccessTokenConverter;
    }

    /*Para armazenar os token no Redis*/
//    private TokenStore redisTokenStore() {
//        return new RedisTokenStore(redisConnectionFactory);
//    }
}
