package com.elastech.helpdelas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration //marca a classe como configuração para o spring
public class SecurityConfig {

    @Autowired
    private CustomDetailsService customDetailsService;

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean // essa anotação indica que o método vai retornar um bean que o spring vai gerenciar
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests( authorize -> authorize //para poder configurar como as requisições são autorizadas
                        .requestMatchers( "/", "/assets/**", "/css/**", "/js/**", "/portfolio/**").permitAll()
                        .requestMatchers( "/salvar-usuario", "/login", "/solicitar-nova-senha", "resetar-senha").permitAll()
                        .requestMatchers("/dashboard-tecnico","/dashboard-tecnico/meus-chamados", "/tecnico/chamado/{ticketId}", "/tecnico/editar-chamado/{ticketId}","/mostrar-tecnico").hasAuthority("TECH")
                        .requestMatchers("/criar-chamado", "/dashboard-usuario", "/usuario/editar-chamado/{ticketId}", "/usuario/chamado/{ticketId}", "/mostrar-usuario", "/editar-usuario").hasAuthority("USER")
                        .requestMatchers("/todos-usuarios", "/mostrar-usuario/{id}", "/desativar-usuario/{id}", "/ativar-usuario/{id}","/cadastrar-tecnico", "/dashboard-admin" ).hasAuthority("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler).permitAll())
                .logout(form -> form.invalidateHttpSession(true).clearAuthentication(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout").permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/acesso-negado"))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build(); // aqui retorna o objeto HttpSecurityConfigurado, para criar a cadeia de filtros de segurança
    }

    // este método é usado para retornar uma instância da classe e podermos usá-la para criptografar as senhas
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* O método configure basicamente informa ao Spring Security como lidar com a autenticação do usuário
    Ele especifica o customDetailsService responsável por buscar informações do usuário
    Define o BCryptPasswordEncoder usado para criptografia e comparação de senhas  */
    public void configure (AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

}
