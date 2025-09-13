package com.lasystems.lagenda.util;

import com.lasystems.lagenda.models.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ConvertAndFormatUtil {

    /**
     * Soma os preços (Double) de uma lista de serviços e retorna formatado como moeda brasileira.
     * Ex: "R$ 125,50"
     */
    public static String calcularValorTotalFormatado(List<Service> services) {
        // Verifica se a lista é nula ou vazia
        if (services == null || services.isEmpty()) {
            return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(0);
        }

        // Soma todos os preços convertendo Double -> BigDecimal
        BigDecimal total = services.stream()
                .map(Service::getPrice)                    // Obtém o Double
                .filter(price -> price != null)            // Evita NPE
                .map(BigDecimal::valueOf)                  // Converte Double -> BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Soma tudo

        // Formata como R$
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return currencyFormat.format(total);
    }
}
