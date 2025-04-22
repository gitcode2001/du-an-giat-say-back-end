package com.example.duangiatsay.component;




import com.example.duangiatsay.service.implement.AccountService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@WebFilter("/*")
public class JwtFilter extends OncePerRequestFilter {
    private final AccountService accountService;
    @Value("${jwt.secret}")
    private String secretKey;

    public JwtFilter(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            System.out.println("Token nhận được: " + token);
            try {
                String username = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
                if (username != null) {
                    String role = accountService.getRoleIdByUsername(username);
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                System.out.println("Token không hợp lệ hoặc hết hạn.");
                System.out.println("🚫 Lỗi xác thực JWT: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
