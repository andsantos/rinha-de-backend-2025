package com.andsantos.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.andsantos.model.Pagamento;
import com.andsantos.model.Resumo;
import com.andsantos.model.ResumoDetalhe;

@Configuration
@ImportRuntimeHints(NativeRuntimeHints.Hints.class)
public class NativeRuntimeHints {

    public static class Hints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader cl) {
            hints.reflection().registerTypes(
                    java.util.List.of(
                            TypeReference.of(Resumo.class),
                            TypeReference.of(ResumoDetalhe.class),
                            TypeReference.of(Pagamento.class)),
                    hint -> hint.withMembers());
        }
    }
}
