package com.itau.pix_received_publisher.infrastructure.adapters.output.services;

import com.itau.pix_received_publisher.core.application.ports.output.services.EventValidatorPort;
import com.itau.pix_received_publisher.core.domain.models.PixReceivedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PixEventValidator implements EventValidatorPort {

    private static final Pattern ENDTOEND_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{32}$");
    private static final Pattern VALOR_PATTERN = Pattern.compile("^\\d{1,10}\\.\\d{2}$");
    private static final Pattern TXID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,35}$");
    
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{11}$");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^[0-9A-Z]{14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CELULAR_PATTERN = Pattern.compile("^\\+\\d{1,3}\\d{10,15}$");
    private static final Pattern EVP_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final Counter validationFailuresCounter;

    public PixEventValidator(MeterRegistry meterRegistry) {
        this.validationFailuresCounter = Counter.builder("pix.publisher.validation.failures")
                .description("Eventos rejeitados por violação de contrato")
                .register(meterRegistry);
    }

    @Override
    public boolean isValid(PixReceivedEvent event) {
        if (event.endToEndId() == null || !ENDTOEND_ID_PATTERN.matcher(event.endToEndId()).matches()) {
            validationFailuresCounter.increment();
            return false;
        }

        if (event.valor() == null || !VALOR_PATTERN.matcher(event.valor()).matches()) {
            validationFailuresCounter.increment();
            return false;
        }

        if (event.horario() == null) {
            validationFailuresCounter.increment();
            return false;
        }

        if (event.txid() != null && !TXID_PATTERN.matcher(event.txid()).matches()) {
            validationFailuresCounter.increment();
            return false;
        }

        if (event.chave() != null && !isValidChave(event.chave())) {
            validationFailuresCounter.increment();
            return false;
        }

        return true;
    }

    private boolean isValidChave(String chave) {
        if (chave.length() > 77) {
            return false;
        }

        return CPF_PATTERN.matcher(chave).matches() ||
               CNPJ_PATTERN.matcher(chave).matches() ||
               EMAIL_PATTERN.matcher(chave).matches() ||
               CELULAR_PATTERN.matcher(chave).matches() ||
               EVP_PATTERN.matcher(chave).matches();
    }
}
