package com.clothingstore.backend.config;

import com.clothingstore.backend.security.filter.JwtAuthFilter;
import com.clothingstore.backend.security.handler.CustomAccessDeniedHandler;
import com.clothingstore.backend.security.handler.CustomAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình Spring Security Filter Chain & CORS:
 * - Disable CSRF (REST API không cần CSRF)
 * - Stateless session (dùng JWT thay vì session)
 * - CORS cho phép frontend gọi API
 * - Phân quyền endpoint
 * - Thêm JwtAuthFilter vào filter chain
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Cho phép dùng @PreAuthorize trong controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Cấu hình Security Filter Chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF — REST API dùng JWT
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Cấu hình CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Không dùng session — stateless (JWT tự mang thông tin)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Phân quyền endpoint
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints: tất cả đều public
                .requestMatchers("/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/logout").permitAll()

                // Xem sản phẩm, danh mục, thương hiệu: public
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/banners/**").permitAll()

                // Admin: chỉ ADMIN
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Còn lại: phải đăng nhập
                .anyRequest().authenticated()
            )

            // 5. Authentication Provider
            .authenticationProvider(authenticationProvider)

            // 6. Xử lý lỗi xác thực
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)        // 401
                .accessDeniedHandler(accessDeniedHandler)         // 403
            )

            // 7. Thêm JWT filter — chạy trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CORS cho phép frontend gọi API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Chỉ cho phép origin của frontend
        config.setAllowedOrigins(List.of(allowedOrigins));

        // Các HTTP method được phép
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Các header được phép gửi lên
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Cho phép gửi credentials (cookie, authorization header)
        config.setAllowCredentials(true);

        // Cache preflight request trong 1 giờ
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
