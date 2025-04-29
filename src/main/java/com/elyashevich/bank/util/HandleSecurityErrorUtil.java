package com.elyashevich.bank.util;

import com.elyashevich.bank.api.dto.exception.ExceptionBodyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

import java.io.IOException;

@UtilityClass
public class HandleSecurityErrorUtil {

    public HttpServletResponse handleError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var objectMapper = new ObjectMapper();
        var res = objectMapper.writeValueAsString(new ExceptionBodyDto(message));
        response.getWriter().write(res);
        return response;
    }
}
