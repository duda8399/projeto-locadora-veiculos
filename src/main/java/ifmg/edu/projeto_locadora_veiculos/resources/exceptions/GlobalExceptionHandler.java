package ifmg.edu.projeto_locadora_veiculos.resources.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> body = Map.of(
                "error", ex.getStatusCode().toString(),
                "message", ex.getReason()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}

