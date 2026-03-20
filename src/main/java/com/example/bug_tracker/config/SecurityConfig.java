package com.example.bug_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
// // UserDetails は認証済みユーザー情報の型
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
// // InMemoryUserDetailsManager はDB未導入段階で使える最小ユーザー管理
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// // PasswordEncoder は平文パスワードをそのまま扱わないために使う
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // // URL単位の認可ルールを決める
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // // 今回はまず formLogin を使う
                .formLogin(Customizer.withDefaults())

                // // URLごとのアクセス制御
                .authorizeHttpRequests(auth -> auth
                        // // 起動確認用。READMEの導線を壊さないため公開
                        .requestMatchers("/health").permitAll()

                        // // 今は最小で API 全体をログイン必須にする
                        // // Day3 で DELETE のみ ADMIN 制限へ細分化しやすい形
                        .requestMatchers("/api/bugs/**").authenticated()

                        // // それ以外も一旦許可せず、ログイン前提に寄せる
                        .anyRequest().authenticated());

        return http.build();
    }

    // // 今はDBユーザー未実装なので、仮のメモリ内ユーザーで進める
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("userpass"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("adminpass"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    // // パスワードをハッシュ化する
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}