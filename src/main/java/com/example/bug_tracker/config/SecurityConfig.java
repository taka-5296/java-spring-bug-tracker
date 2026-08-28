package com.example.bug_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    // URL単位の認可ルールを決める
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.formLogin(Customizer.withDefaults())
                // URLごとのアクセス制御
                .authorizeHttpRequests(auth -> auth
                        // ("/health")公開
                        .requestMatchers("/health").permitAll()
                        // DELETEは"ADMIN"のみ
                        .requestMatchers(HttpMethod.DELETE, "/api/bugs/**").hasRole("ADMIN")
                        // ("api/bugs/**") 以下は認証必須
                        .requestMatchers("/api/bugs/**").hasAnyRole("USER", "ADMIN")
                        // それ以外も一旦許可せず、ログイン前提に寄せる
                        .anyRequest().authenticated());

        return http.build();
    }

    // パスワードをハッシュ化する
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
