package folhapagamento;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class FormatadorMoeda {
    public String formatar(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }
}