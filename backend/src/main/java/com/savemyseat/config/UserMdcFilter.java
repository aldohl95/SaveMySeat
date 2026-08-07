package com.savemyseat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class UserMdcFilter extends HttpFilter{
    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {


        try {

            Authentication auth =
                    SecurityContextHolder.getContext()
                            .getAuthentication();


            if(auth != null && auth.isAuthenticated()) {

                MDC.put(
                        "userId",
                        auth.getName()
                );

            }


            chain.doFilter(request,response);


        } finally {

            MDC.clear();

        }
    }
}
