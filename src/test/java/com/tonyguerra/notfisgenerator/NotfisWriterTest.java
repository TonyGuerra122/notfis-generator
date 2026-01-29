package com.tonyguerra.notfisgenerator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.tonyguerra.notfisgenerator.errors.NotfisException;

final class NotfisWriterTest {

    @Test
    void writeFileToStream_shouldGenerateFixedWidthLine() throws Exception {
        final var writer = new NotfisWriter(null);
        writer.setConfigMapForTests(cfg000_nameQty());

        final var payload = payload000(
                Arrays.asList(
                        new NotfisParam("name", "ABC"),
                        new NotfisParam("qty", 12)));

        final String out = read(writer.writeFileToStream(payload));

        // name: pos 1 size 10 => "ABC "
        // qty : pos 11 size 3 => "12 "
        assertEquals("ABC       12 \n", out);
    }

    @Test
    void writeFileToStream_shouldThrowWhenMandatoryFieldMissing() throws Exception {
        final var writer = new NotfisWriter(null);
        writer.setConfigMapForTests(cfg000_nameQty());

        final var payload = payload000(
                Arrays.asList(
                        new NotfisParam("name", "ABC")
                // qty faltando
                ));

        final var ex = assertThrows(NotfisException.class, () -> writer.writeFileToStream(payload));
        assertTrue(ex.getMessage().contains("Campo obrigatório"));
        assertTrue(ex.getMessage().contains("qty"));
        assertTrue(ex.getMessage().contains("000"));
    }

    @Test
    void writeFileToStream_shouldThrowIfIdentifierNotInConfig() throws Exception {
        final var writer = new NotfisWriter(null);
        writer.setConfigMapForTests(cfg000_nameQty());

        final Map<String, List<List<NotfisParam>>> records = new HashMap<>();
        records.put("999", Arrays.asList(
                Arrays.asList(new NotfisParam("name", "ABC"), new NotfisParam("qty", 1))));

        final var ex = assertThrows(NotfisException.class,
                () -> writer.writeFileToStream(new NotfisPayload(records)));
        assertTrue(ex.getMessage().contains("Identificador"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void writeFileToStream_shouldRoundNumericValues() throws Exception {
        final var writer = new NotfisWriter(null);

        final Map<String, List<NotfisConfigField>> cfg = new HashMap<>();
        cfg.put("000", Arrays.asList(
                new NotfisConfigField("qty", NotfisFieldType.NUMERIC, (short) 1, (short) 4, true)));
        writer.setConfigMapForTests(cfg);

        final var payload = payload000(
                Arrays.asList(new NotfisParam("qty", 12.7)));

        final String out = read(writer.writeFileToStream(payload));

        // 12.7 -> 13
        assertEquals("13  \n", out);
    }

    @Test
    void writeFileToStream_shouldSanitizeNonAsciiAndSpecialChars() throws Exception {
        final var writer = new NotfisWriter(null);

        final Map<String, List<NotfisConfigField>> cfg = new HashMap<>();
        cfg.put("000", Arrays.asList(
                new NotfisConfigField("name", NotfisFieldType.ALPHANUMERIC, (short) 1, (short) 30, true)));
        writer.setConfigMapForTests(cfg);

        // tem acentos + símbolos
        final String raw = "João!@# da Silva-áéíóú ç";
        final var payload = payload000(
                Arrays.asList(new NotfisParam("name", raw)));

        final String out = read(writer.writeFileToStream(payload));

        // Sanitização do seu código:
        // - remove não-ASCII (ã, áéíóú, ç)
        // - remove tudo que não seja [a-zA-Z0-9\s] (tira !@# e '-')
        // Resultado esperado: "Joo da Silva " ... (pode ter espaços no fim por size 30)
        assertTrue(out.startsWith("Joo da Silva"));
        assertTrue(out.endsWith("\n"));
        assertFalse(out.contains("!"));
        assertFalse(out.contains("@"));
        assertFalse(out.contains("#"));
        assertFalse(out.contains("-"));
    }

    @Test
    void orderLines_shouldPutLowestRegistrationFirst_andIntercalateRepeated() {
        // Constrói linhas: 000 (prioridade), 311x2, 312x2, 400 único
        final List<NotfisLine> lines = new ArrayList<>();
        lines.add(line("312"));
        lines.add(line("311"));
        lines.add(line("400"));
        lines.add(line("311"));
        lines.add(line("312"));
        lines.add(line("000"));

        final var ordered = NotfisLine.orderLines(lines);

        assertEquals(0, ordered.get(0).getRegistration()); // 000 primeiro

        // Checa que os repetidos 311 e 312 aparecem alternados no meio (não 311,311
        // juntos)
        // (não é uma prova formal do algoritmo, mas pega regressão fácil)
        for (int i = 1; i < ordered.size() - 1; i++) {
            final int a = ordered.get(i).getRegistration();
            final int b = ordered.get(i + 1).getRegistration();
            if ((a == 311 && b == 311) || (a == 312 && b == 312)) {
                fail("Repetidos não deveriam ficar colados: " + a + "," + b);
            }
        }
    }

    // ---------------- helpers ----------------

    private static Map<String, List<NotfisConfigField>> cfg000_nameQty() {
        final Map<String, List<NotfisConfigField>> cfg = new HashMap<>();
        cfg.put("000", Arrays.asList(
                new NotfisConfigField("name", NotfisFieldType.ALPHANUMERIC, (short) 1, (short) 10, true),
                new NotfisConfigField("qty", NotfisFieldType.NUMERIC, (short) 11, (short) 3, true)));
        return cfg;
    }

    private static NotfisPayload payload000(List<NotfisParam> params) {
        final Map<String, List<List<NotfisParam>>> records = new HashMap<>();
        records.put("000", Arrays.asList(params));
        return new NotfisPayload(records);
    }

    private static NotfisLine line(String reg) {
        return new NotfisLine(reg, Arrays.asList(
                new NotfisField("dummy", NotfisFieldType.ALPHANUMERIC, (short) 1, (short) 1, false, "X")));
    }

    private static String read(InputStream is) throws Exception {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}
