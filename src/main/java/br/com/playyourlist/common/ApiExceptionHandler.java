package br.com.playyourlist.common;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    private ProblemDetail problema(HttpStatusCode status, String detalhe, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> recurso(ResponseStatusException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatusCode()).body(problema(ex.getStatusCode(), ex.getReason(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = problema(HttpStatus.BAD_REQUEST, "Verifique os campos informados.", request);
        Map<String, List<String>> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                erros.computeIfAbsent(error.getField(), field -> new ArrayList<>()).add(error.getDefaultMessage()));
        problem.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> formato(Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "JSON ou parâmetro inválido.", request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problema(HttpStatus.CONFLICT,
                "A operação conflita com um registro existente ou com os vínculos do recurso.", request));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetail> integracao(FeignException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.status()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case -1 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
        String mensagem = switch (status) {
            case NOT_FOUND -> "Música ou playlist não encontrada.";
            case CONFLICT -> "A música já pertence à playlist ou há um conflito nos vínculos.";
            case BAD_REQUEST -> "Dados inválidos para a operação.";
            default -> "Não foi possível acessar o serviço solicitado.";
        };
        return ResponseEntity.status(status).body(problema(status, mensagem, request));
    }
}
