package gwl.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import gwl.context.BaseContext;
import gwl.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
         log.info("进入拦截器");
        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }
        String token = null;
        // 从 cookie 中取 token
        // 1️⃣ 先从请求头取 Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉 "Bearer "
        }
        // 校验 token（这里只是判断是否存在，你可以接 JWT 解析库）
        log.info("拦截器拿到token: {}",token);
        if (token != null && !token.isEmpty()) {
            BaseContext.setCurrentId(JwtUtil.parseToken(token));
            return true; // token 存在，放行
        }
        return true;
    }
}
