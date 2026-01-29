package com.tonyguerra.notfisgenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyguerra.notfisgenerator.errors.NotfisException;

public final class NotfisWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final NotfisType type;
    private Map<String, List<NotfisConfigField>> configMap;
    private List<NotfisLine> lines;

    public NotfisWriter(NotfisType type) {
        this.type = type;
        this.configMap = null;
        this.lines = new ArrayList<NotfisLine>();
    }

    private void loadConfigFile() throws NotfisException {
        if (this.configMap != null) {
            return; // cache
        }

        if (type == null) {
            throw new NotfisException("Tipo de notfis não especificado.");
        }

        final String configFilename = "notfis/" + type.getConfigFilename();

        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(configFilename)) {
            if (is == null) {
                throw new NotfisException("Arquivo de configuração não encontrado: " + configFilename);
            }

            final TypeReference<Map<String, List<Map<String, Object>>>> tr = new TypeReference<Map<String, List<Map<String, Object>>>>() {
            };

            final Map<String, List<Map<String, Object>>> raw = MAPPER.readValue(is, tr);

            final Map<String, List<NotfisConfigField>> parsed = new HashMap<String, List<NotfisConfigField>>();

            for (Map.Entry<String, List<Map<String, Object>>> entry : raw.entrySet()) {
                final String identifier = entry.getKey();
                final List<Map<String, Object>> fields = entry.getValue();

                final List<NotfisConfigField> configFields = new ArrayList<NotfisConfigField>();

                if (fields != null) {
                    for (Map<String, Object> f : fields) {
                        final String name = asString(f.get("name"));
                        final String formatStr = asStringOrDefault(f.get("format"), "A");
                        final NotfisFieldType format = "A".equalsIgnoreCase(formatStr)
                                ? NotfisFieldType.ALPHANUMERIC
                                : NotfisFieldType.NUMERIC;

                        final short position = asShort(f.get("position"));
                        final short size = asShort(f.get("size"));
                        final boolean mandatory = asBoolean(f.get("mandatory"));

                        configFields.add(new NotfisConfigField(name, format, position, size, mandatory));
                    }
                }

                parsed.put(identifier, configFields);
            }

            this.configMap = parsed;

        } catch (IOException ex) {
            throw new NotfisException("Erro ao carregar o arquivo de configuração: " + configFilename, ex);
        }
    }

    private void checkAllFields(NotfisPayload payload) throws NotfisException {
        loadConfigFile();

        if (payload == null || payload.getRecords() == null) {
            throw new NotfisException("Payload nulo.");
        }

        final List<NotfisLine> populatedLines = new ArrayList<NotfisLine>();

        for (Map.Entry<String, List<List<NotfisParam>>> entry : payload.getRecords().entrySet()) {
            final String identifier = entry.getKey();
            final List<List<NotfisParam>> payloadLines = entry.getValue();

            final List<NotfisConfigField> configLines = configMap.get(identifier);
            if (configLines == null) {
                throw new NotfisException("Identificador de registro não encontrado na configuração: " + identifier);
            }

            if (payloadLines == null)
                continue;

            for (int i = 0; i < payloadLines.size(); i++) {
                final List<NotfisParam> params = payloadLines.get(i);
                final List<NotfisField> populatedFields = new ArrayList<NotfisField>();

                for (NotfisConfigField cfg : configLines) {
                    final String name = cfg.getName();

                    final NotfisParam match = findParamByName(params, name);

                    if (match != null) {
                        Object value = match.getValue();
                        if (value == null) {
                            throw new NotfisException("Valor nulo encontrado no campo '" + name
                                    + "' no identificador " + identifier);
                        }

                        Object sanitized = sanitizeValue(value, cfg.getFormat());

                        populatedFields.add(new NotfisField(
                                name,
                                cfg.getFormat(),
                                cfg.getPosition(),
                                cfg.getSize(),
                                cfg.isMandatory(),
                                sanitized));
                    } else if (cfg.isMandatory()) {
                        throw new NotfisException("Campo obrigatório '" + name
                                + "' não encontrado no identificador " + identifier);
                    }
                }

                populatedLines.add(new NotfisLine(identifier, populatedFields));
            }
        }

        this.lines.clear();
        this.lines.addAll(populatedLines);
    }

    private static NotfisParam findParamByName(List<NotfisParam> params, String name) {
        if (params == null || name == null)
            return null;
        for (NotfisParam p : params) {
            if (p != null && name.equals(p.getName()))
                return p;
        }
        return null;
    }

    private static Object sanitizeValue(Object value, NotfisFieldType format) {
        if (format == NotfisFieldType.NUMERIC && value instanceof Number) {
            Number n = (Number) value;
            return Math.round(n.doubleValue());
        }

        final String s = value.toString();
        return s.replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public InputStream writeFileToStream(NotfisPayload payload) throws NotfisException {
        checkAllFields(payload);

        this.lines = NotfisLine.orderLines(this.lines);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (final NotfisLine line : this.lines) {
                final List<NotfisField> fields = line.getField();
                if (fields == null || fields.isEmpty()) {
                    continue;
                }

                final int totalLength = maxLineLength(fields);

                final char[] lineChars = new char[totalLength];
                java.util.Arrays.fill(lineChars, ' ');

                for (NotfisField field : fields) {
                    final String v = field.getValue() == null ? "" : field.getValue().toString();

                    final int startPosition = field.getPosition() - 1;
                    final int endPosition = Math.min(startPosition + field.getSize(), totalLength);

                    for (int i = startPosition, j = 0; i < endPosition && j < v.length(); i++, j++) {
                        lineChars[i] = v.charAt(j);
                    }
                }

                outputStream.write(new String(lineChars).getBytes(StandardCharsets.UTF_8));
                outputStream.write('\n');
            }

            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (Exception ex) {
            throw new NotfisException("Erro ao gerar o InputStream.", ex);
        }
    }

    public void setConfigMapForTests(Map<String, List<NotfisConfigField>> cfg) {
        this.configMap = cfg;
    }

    private static int maxLineLength(List<NotfisField> fields) {
        int max = 0;
        for (final var f : fields) {
            final int len = (f.getPosition() - 1) + f.getSize();
            if (len > max)
                max = len;
        }
        return max;
    }

    public InputStream writeFileToStream(String json) throws NotfisException {
        try {
            final var tr = new TypeReference<Map<String, List<List<NotfisParam>>>>() {
            };
            final var records = MAPPER.readValue(json, tr);
            return writeFileToStream(new NotfisPayload(records));
        } catch (Exception ex) {
            throw new NotfisException("JSON inválido: " + ex.getMessage(), ex);
        }
    }

    // ---------- helpers de conversão ----------
    private static String asString(Object v) throws NotfisException {
        if (v == null) {
            throw new NotfisException("Campo 'name' ausente na configuração.");
        }
        return v.toString();
    }

    private static String asStringOrDefault(Object v, String def) {
        return v == null ? def : v.toString();
    }

    private static short asShort(Object v) throws NotfisException {
        if (v == null) {
            throw new NotfisException("Campo numérico ausente na configuração.");
        }

        if (v instanceof Number) {
            return (short) ((Number) v).intValue();
        }

        return Short.parseShort(v.toString());
    }

    private static boolean asBoolean(Object v) {
        if (v == null)
            return false;
        if (v instanceof Boolean)
            return ((Boolean) v).booleanValue();
        return Boolean.parseBoolean(v.toString());
    }
}
