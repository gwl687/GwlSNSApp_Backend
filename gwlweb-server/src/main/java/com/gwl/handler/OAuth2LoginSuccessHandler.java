package com.gwl.handler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.gwl.mapper.UserMapper;
import com.gwl.pojo.entity.User;
import com.gwl.properties.BaseProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    UserMapper userMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    BaseProperties baseProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) authentication;
        String provider = auth.getAuthorizedClientRegistrationId();
        OAuth2User oAuth2User = auth.getPrincipal();
        Map<String, Object> attr = oAuth2User.getAttributes();

        switch (provider) {
            case "google" -> {
                String sub = (String) attr.get("sub");
                String emailaddress = (String) attr.get("email");
                // トーケンを生成する
                log.info("google login,emailaddress: {}", emailaddress);
                // もし今回は初めてのグーグルログイン、データベースに入れる
                if (userMapper.getByUserEmail(emailaddress) == null) {
                    log.info("first time google login,insert into mysql");
                    String userName = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                    User user = User.builder()
                            .emailaddress(emailaddress)
                            .username(userName)
                            .status(1)
                            .build();
                    userMapper.insert(user);
                }
                Long userId = userMapper.getByUserEmail(emailaddress).getId();
                // グーグルログインが必要なコードを生成する
                String code = UUID.randomUUID().toString().replace("-", "");
                String redisKey = "oauth:code:" + code;
                stringRedisTemplate.opsForValue().set(
                        redisKey,
                        userId.toString(),
                        60,
                        TimeUnit.SECONDS);
                String redirectUrl = baseProperties.getUrl() + "?code=" + code;
                getRedirectStrategy().sendRedirect(request, null, redirectUrl);
            }
            case "line" -> {
            }
        }
    }
}
