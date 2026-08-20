package com.clothingstore.backend.security.filter;

import com.clothingstore.backend.security.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — chạy một lần cho mỗi HTTP request.
 * <p>
 * Luồng xử lý:
 * 1. Đọc header: Authorization: Bearer <token>
 * 2. Lấy JWT từ header
 * 3. Extract email từ JWT
 * 4. Load UserDetails từ DB
 * 5. Kiểm tra token hợp lệ
 * 6. Set Authentication vào SecurityContext
 * 7. Tiếp tục filter chain
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Lấy Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Nếu không có header hoặc không bắt đầu bằng "Bearer " → bỏ qua
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Tách JWT khỏi "Bearer "
        final String jwt = authHeader.substring(7);

        try {
            // Lấy email từ JWT
            final String userEmail = jwtService.extractUsername(jwt);

            // Chỉ xử lý nếu email tồn tại và chưa authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user từ database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Kiểm tra token hợp lệ (username khớp + chưa hết hạn)
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Tạo authentication object và set vào SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            // Token hết hạn — tiếp tục xử lý (SecurityContext rỗng → 401)
        } catch (JwtException e) {
            // Token không hợp lệ — bỏ qua
        }

        filterChain.doFilter(request, response);
    }
}
