package br.com.gilliardcarvalho.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.gilliardcarvalho.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

    var servletPath = request.getServletPath();

    if (servletPath.startsWith("/tasks")) {

        var authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Basic ")) {
            var base64Credentials = authorization.substring("Basic ".length());
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded); // Ex: "usuario:senha"

            String[] values = credentials.split(":", 2);
            if (values.length != 2) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciais inválidas");
                return;
            }

            String username = values[0];
            String password = values[1];

            var userOptional = this.userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário sem autorização");
                return;
            }

            var user = userOptional.get();

            var result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

            if (result.verified) {
                request.setAttribute("idUser", user.getId());
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Senha incorreta");
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header ausente ou inválido");
        }

    } else {
        filterChain.doFilter(request, response);
    }
}


}