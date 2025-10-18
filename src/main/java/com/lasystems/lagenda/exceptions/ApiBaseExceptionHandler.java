package com.lasystems.lagenda.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiBaseExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    private ApiBaseEntityException.ApiBaseEntityExceptionBuilder createProblemBuilder(HttpStatus status,
                                                                                      String msg, String detail) {

        return ApiBaseEntityException.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .title(msg)
                .detail(detail);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex);

        if (rootCause instanceof InvalidFormatException) {
            return handleInvalidFormat((InvalidFormatException) rootCause, headers, HttpStatus.valueOf(status.value()), request);
        } else if (rootCause instanceof PropertyBindingException) {
            return handlePropertyBinding((PropertyBindingException) rootCause, headers, HttpStatus.valueOf(status.value()), request);
        }

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.valueOf(status.value()), getMessageResource("title.body.invalid"),
                getMessageResource("detail.body.invalid"))
                .userMessage(getMessageResource("userMessage.body.invalid"))
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    private ResponseEntity<Object> handlePropertyBinding(PropertyBindingException ex,
                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {

        // Criei o método joinPath para reaproveitar em todos os métodos que precisam
        // concatenar os nomes das propriedades (separando por ".")
        String path = joinPath(ex.getPath());
        String title = getMessageResource("title.body.invalid");
        String detail = String.format(getMessageResource("detail.body.propertyNotFound"), path);
        String userMessage = getMessageResource("userMessage.body.invalid");

        ApiBaseEntityException body = createProblemBuilder(status, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex,
                                                       HttpHeaders headers, HttpStatus status, WebRequest request) {

        String path = joinPath(ex.getPath());
        String title = getMessageResource("title.body.invalid");
        String detail = String.format(getMessageResource("detail.body.invalidFormat"),
                path, ex.getValue(), ex.getTargetType().getSimpleName());
        String userMessage = getMessageResource("userMessage.body.invalid");

        ApiBaseEntityException body = createProblemBuilder(status, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, headers, status, request);
    }

//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<?> handlerDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
//        String title = getMessageResource("title.dataIntegrityViolation");
//        String detail = getMessageResource("detail.dataIntegrityViolation");
//        String userMessage = getMessageResource("userMessage.dataIntegrityViolation");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.CONFLICT, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
//    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handlerEntityNotFound(EntityNotFoundException ex, WebRequest request) {

        String title = getMessageResource("title.entityNotFound");
        String detail = getMessageResource("detail.entityNotFound");
        String userMessage = getMessageResource("userMessage.entityNotFound");

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.NOT_FOUND, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UUIDIllegalArgumentException.class)
    public ResponseEntity<?> handlerUUIDIllegalArgumentException(UUIDIllegalArgumentException ex, WebRequest request) {

        String title = getMessageResource("title.uuid.invalid");
        String detail = getMessageResource("detail.uuid.invalid");
        String userMessage = getMessageResource("userMessage.uuid.invalid");

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.BAD_REQUEST, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<?> handlerSlotNotAvailableException(SlotNotAvailableException ex, WebRequest request) {

        String title = getMessageResource("title.slotNotAvailable");
        String detail = ex.getMessage(); //getMessageResource("title.slotNotAvailable");
        String userMessage = ex.getMessage();//getMessageResource("title.slotNotAvailable");

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.BAD_REQUEST, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(EnableSubscriptionException.class)
    public ResponseEntity<?> handlerEnableSubscriptionException(EnableSubscriptionException ex, WebRequest request) {

        String title = getMessageResource("title.subscription");
        String detail = ex.getMessage();
        String userMessage = ex.getMessage();

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.BAD_REQUEST, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ClientConflictException.class)
    public ResponseEntity<?> handlerClientConflictException(ClientConflictException ex, WebRequest request) {

        String title = getMessageResource("title.clientConfict");
        String detail = ex.getMessage();
        String userMessage = ex.getMessage();

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.BAD_REQUEST, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

//    @ExceptionHandler(DadosDuplicadosException.class)
//    public ResponseEntity<?> handlerDadosDuplicados(DadosDuplicadosException ex, WebRequest request) {
//
//        String title = getMessageResource("title.dadosDuplicados");
//        String detail = getMessageResource("detail.dadosDuplicados");
//        String userMessage = getMessageResource("userMessage.dadosDuplicados");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.CONFLICT, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
//    }

//    @ExceptionHandler(BadCredentialsApiException.class)
//    public ResponseEntity<?> handlerBadCredentialsApiException(BadCredentialsApiException ex, WebRequest request) {
//
//        String title = getMessageResource("title.badCredentials");
//        String detail = getMessageResource("detail.badCredentials");
//        String userMessage = getMessageResource("userMessage.badCredentials");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.FORBIDDEN, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
//    }

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<?> handlerAccessDeniedException(AccessDeniedException ex, WebRequest request) {
//
//        String title = getMessageResource("title.accessDenied");
//        String detail = getMessageResource("detail.accessDenied");
//        String userMessage = getMessageResource("userMessage.accessDenied");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.FORBIDDEN, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
//    }

//    @ExceptionHandler(ContaDesabilitadaException.class)
//    public ResponseEntity<?> handlerContaBloqueadaException(ContaDesabilitadaException ex, WebRequest request) {
//
//        String title = getMessageResource("title.accountBlocked");
//        String detail = getMessageResource("detail.accountBlocked");
//        String userMessage = getMessageResource("userMessage.accountBlocked");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.FORBIDDEN, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
//    }

//    @ExceptionHandler(EntityInUseException.class)
//    public ResponseEntity<?> handlerEntityInUse(EntityInUseException ex, WebRequest request) {
//
//        String title = getMessageResource("title.entityInUse");
//        String detail = getMessageResource("detail.entityInUse");
//        String userMessage = getMessageResource("userMessage.entityInUse");
//
//        ApiBaseEntityException body = createProblemBuilder(HttpStatus.CONFLICT, title, detail)
//                .userMessage(userMessage)
//                .build();
//
//        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.CONFLICT, request);
//    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        if (body == null) {
            body = ApiBaseEntityException.builder()
                    .timestamp(LocalDateTime.now())
                    .title(HttpStatus.valueOf(status.value()).getReasonPhrase())
                    .status(status.value())
                    .userMessage(getMessageResource("error.generic"))
                    .build();
        } else if (body instanceof String) {
            body = ApiBaseEntityException.builder()
                    .timestamp(LocalDateTime.now())
                    .title((String) body)
                    .status(status.value())
                    .userMessage(getMessageResource("error.generic"))
                    .build();
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    private String joinPath(List<JsonMappingException.Reference> references) {
        return references.stream()
                .map(JsonMappingException.Reference::getFieldName)
                .collect(Collectors.joining("."));
    }

    // 1. MethodArgumentTypeMismatchException é um subtipo de TypeMismatchException

    // 2. ResponseEntityExceptionHandler já trata TypeMismatchException de forma mais abrangente

    // 3. Então, especializamos o método handleTypeMismatch e verificamos se a exception
    //	    é uma instância de MethodArgumentTypeMismatchException

    // 4. Se for, chamamos um método especialista em tratar esse tipo de exception

    // 5. Poderíamos fazer tudo dentro de handleTypeMismatch, mas preferi separar em outro método



    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {



        if (ex instanceof MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatch(
                    (MethodArgumentTypeMismatchException) ex, headers, HttpStatus.valueOf(status.value()), request);
        }

        return super.handleTypeMismatch(ex, headers, HttpStatus.valueOf(status.value()), request);
    }

    private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {

        String title = getMessageResource("title.param.invalid");
        String detail = String.format(getMessageResource("detail.param.invalid"),
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        String userMessage = getMessageResource("userMessage.param.invalid");

        ApiBaseEntityException body = createProblemBuilder(status, title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String title = getMessageResource("title.resource.notFound");
        String detail = String.format(getMessageResource("detail.resource.notFound"),
                ex.getRequestURL());
        String userMessage = getMessageResource("userMessage.resource.notFound");

        ApiBaseEntityException body = createProblemBuilder(HttpStatus.valueOf(status.value()), title, detail)
                .userMessage(userMessage)
                .build();

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String title = getMessageResource("error.unexpected");
        String detail = getMessageResource("error.generic");

        ApiBaseEntityException body = createProblemBuilder(status, title, detail)
                .userMessage(getMessageResource("error.generic"))
                .build();

        // Importante colocar o printStackTrace (pelo menos por enquanto, que não estamos
        // fazendo logging) para mostrar a stacktrace no console
        // Se não fizer isso, você não vai ver a stacktrace de exceptions que seriam importantes
        // para você durante, especialmente na fase de desenvolvimento
        ex.printStackTrace();

        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return handleValidationInternal(ex, headers, HttpStatus.valueOf(status.value()), request, ex.getBindingResult());
    }

//    @Override
//    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status,
//                                                                          WebRequest request) {
//        return handleValidationInternal(ex, headers, HttpStatus.valueOf(status.value()), request, ex.getBody().);
//    }

//    @Override
    protected ResponseEntity<Object> handleServletRequestBindException(BindException ex, HttpHeaders headers, HttpStatusCode status,
                                                                       WebRequest request) {
        // TODO Auto-generated method stub
        return handleValidationInternal(ex, headers, HttpStatus.valueOf(status.value()), request, ex.getBindingResult());
    }

    private ResponseEntity<Object> handleValidationInternal(Exception ex, HttpHeaders headers,
                                                            HttpStatus status, WebRequest request, BindingResult bindResult) {
        String title = getMessageResource("title.data.invalid");
        String detail = getMessageResource("detail.data.invalid");
        String userMessage = getMessageResource("userMessage.data.invalid");

        List<ApiBaseEntityException.Field> fields = bindResult.getFieldErrors().stream()
                .map(fieldError -> {
                    String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
                    return ApiBaseEntityException.Field.builder()
                            .name(fieldError.getField())
                            .userMessage(message)
                            .build();
                })
                .collect(Collectors.toList());

        ApiBaseEntityException body = createProblemBuilder(status, title, detail)
                .userMessage(userMessage)
                .fields(fields)
                .build();

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private String getMessageResource(String messageKey) {
        return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    }
}
