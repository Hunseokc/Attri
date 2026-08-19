package projectSpring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Exception caught in the filter is saved in the request attribute
        String exception = (String) request.getAttribute("exception");
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        
        if ("ExpiredJwtException".equals(exception)) {
            body.put("error", "Unauthorized");
            body.put("message", "토큰이 만료되었습니다.");
        } else if ("InvalidJwtException".equals(exception)) {
            body.put("error", "Unauthorized");
            body.put("message", "유효하지 않은 토큰입니다.");
        } else {
            body.put("error", "Unauthorized");
            body.put("message", "인증이 필요한 요청입니다.");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
