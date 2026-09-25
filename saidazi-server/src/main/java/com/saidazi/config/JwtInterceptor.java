package com.saidazi.config;

import com.saidazi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器
 *
 * 设计说明：
 *   本项目当前阶段所有接口采用 userId 传参方式（便于小程序开发联调与 H2 测试），
 *   因此本拦截器「不强制要求登录」--若请求头携带 Authorization: Bearer <token>，
 *   则解析出 userId 放入请求属性 currentUserId，业务可通过 CurrentUserHolder 获取；
 *   若未携带 token，则放行，由各 Controller 的 userId 参数兜底。
 *
 *   后续接入正式登录态时，只需在 WebMvcConfig 中对受保护路径注册本拦截器，
 *   并将 missingTokenReturn401 设为 true 即可启用强制鉴权。
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    public static final String ATTR_CURRENT_USER_ID = "currentUserId";

    @Autowired
    private JwtUtil jwtUtil;

    /** 是否在缺少 token 时直接返回 401（默认 false，保持向后兼容） */
    private boolean missingTokenReturn401 = false;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractToken(authHeader);

        if (token == null) {
            // 当前阶段不强制登录，直接放行
            return !missingTokenReturn401;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("无效的 JWT token: {}", token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute(ATTR_CURRENT_USER_ID, userId);
        return true;
    }

    public void setMissingTokenReturn401(boolean missingTokenReturn401) {
        this.missingTokenReturn401 = missingTokenReturn401;
    }
}