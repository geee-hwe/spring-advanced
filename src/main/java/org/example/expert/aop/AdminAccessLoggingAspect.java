package org.example.expert.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.expert.config.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AdminAccessLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccessLoggingAspect.class);
    private final HttpServletRequest request;
    private final JwtUtil jwtUtil;

    public AdminAccessLoggingAspect(HttpServletRequest request, JwtUtil jwtUtil) {
        this.request = request;
        this.jwtUtil = jwtUtil;
    }

    // CommentAdminController의 deleteComment() 메소드에 대한 접근 로그
    @Before("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    public void logAfterDeleteComment(JoinPoint joinPoint) {
        logAdminAccess(joinPoint);
    }

    // UserAdminController의 changeUserRole() 메소드에 대한 접근 로그
    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void logAfterChangeUserRole(JoinPoint joinPoint) {
        logAdminAccess(joinPoint);
    }

    private void logAdminAccess(JoinPoint joinPoint) {
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.substringToken(authHeader);

        String userId = extractUserId(token);
        String requestUrl = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        logger.info("Admin Access Log - User ID: {}, Request Time: {}, Request URL: {}, Method: {}",
                userId, requestTime, requestUrl, joinPoint.getSignature().getName());
    }

    private String extractUserId(String token) {
        Claims claims = jwtUtil.extractClaims(token);
        return claims.getSubject();
    }
}


