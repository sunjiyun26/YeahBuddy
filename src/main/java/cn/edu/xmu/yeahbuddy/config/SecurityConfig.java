package cn.edu.xmu.yeahbuddy.config;

import cn.edu.xmu.yeahbuddy.service.AdministratorService;
import cn.edu.xmu.yeahbuddy.service.TeamService;
import cn.edu.xmu.yeahbuddy.service.TutorService;
import cn.edu.xmu.yeahbuddy.service.YbPasswordEncodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableWebSecurity
@EnableTransactionManagement
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Order(3)
    @Configuration
    public static class AdministratorSecurityConfig extends WebSecurityConfigurerAdapter {

        private final AdministratorService administratorService;

        private Environment environment;

        @Autowired
        public AdministratorSecurityConfig(AdministratorService administratorService, Environment environment) {
            this.administratorService = administratorService;
            this.environment = environment;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                        .antMatchers("/webjars/**")
                            .permitAll();

            String activeDb = environment.getProperty("spring.datasource.driver-class-name");

            if(activeDb != null && activeDb.contains("org.h2")) {
                http
                        .authorizeRequests()
                            .antMatchers("/h2-console/**")
                                .permitAll();

                http
                        .headers()
                            .frameOptions()
                                .sameOrigin();
            }

            http
                    .authorizeRequests()
                        .anyRequest()
                            .authenticated().and()
                    // log in
                    .formLogin()
                        .loginPage("/login")
                            .permitAll()
                        .failureUrl("/login?error=1")
                        .defaultSuccessUrl("/admin").and()
                    // logout
                    .logout()
                        .logoutUrl("/**/logout")
                            .permitAll()
                        .logoutSuccessUrl("/login")
                            .deleteCookies("JSESSIONID").and()
                    .csrf()
                        .disable();
        }

        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(administratorService).passwordEncoder(new YbPasswordEncodeService());
        }
    }

    @Order(1)
    @Configuration
    public static class TeamSecurityConfig extends WebSecurityConfigurerAdapter {

        private final TeamService teamService;

        @Autowired
        public TeamSecurityConfig(TeamService teamService) {
            this.teamService = teamService;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/team/**")
                        .authorizeRequests()
                            .anyRequest()
                            .authenticated().and()
                    // log in
                        .formLogin()
                            .loginPage("/team/login")
                                .permitAll()
                            .failureUrl("/team/login?error=1")
                            .defaultSuccessUrl("/team").and()
                    // logout
                        .logout()
                            .logoutUrl("/team/**/logout")
                                .permitAll()
                            .logoutSuccessUrl("/team/login")
                                .deleteCookies("JSESSIONID").and()
                        .csrf()
                            .disable();
        }

        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(teamService).passwordEncoder(new YbPasswordEncodeService());
        }
    }

    @Order(2)
    @Configuration
    public static class TutorSecurityConfig extends WebSecurityConfigurerAdapter {

        private final TutorService tutorService;

        private final AuthTokenAuthenticationProvider authTokenAuthenticationProvider;

        @Autowired
        public TutorSecurityConfig(TutorService tutorService, AuthTokenAuthenticationProvider authTokenAuthenticationProvider) {
            this.tutorService = tutorService;
            this.authTokenAuthenticationProvider = authTokenAuthenticationProvider;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .addFilterBefore(
                        new AuthTokenFilter(
                                "/tutor/token",
                                "/tutor",
                                authenticationManager()),
                        UsernamePasswordAuthenticationFilter.class);

            http
                    .antMatcher("/tutor/**")
                        .authorizeRequests()
                            .anyRequest()
                            .authenticated().and()
                    // log in
                    .formLogin()
                        .loginPage("/tutor/login")
                            .permitAll()
                        .failureUrl("/tutor/login?error=1")
                        .defaultSuccessUrl("/tutor").and()
                    // logout
                    .logout()
                        .logoutUrl("/tutor/**/logout")
                            .permitAll()
                        .logoutSuccessUrl("/tutor/login")
                            .deleteCookies("JSESSIONID").and()
                    .csrf()
                        .disable();
        }

        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(preAuthenticatedAuthenticationProvider()).authenticationProvider(authTokenAuthenticationProvider).userDetailsService(tutorService).passwordEncoder(new YbPasswordEncodeService());
        }

        @Bean
        public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
            PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
            preAuthenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(tutorService);
            return preAuthenticatedAuthenticationProvider;
        }
    }
}