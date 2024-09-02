package io.github.tonyguerra122.notfisgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.tonyguerra122.utils.JSONUtils;

public final class NotfisWriter {

    private final NotfisType type;
    private JSONObject configFile;
    private final List<List<NotfisField>> lines;

    public NotfisWriter(NotfisType type) {
        this.type = type;
        configFile = null;
        lines = new ArrayList<>();
    }

    private void loadConfigFile() throws NotfisException {
        if (type == null) {
            throw new NotfisException("Tipo de notfis não especificado.");
        }

        final String configFilename = "notfis/" + type.getConfigFilename();

        try (final var is = getClass().getClassLoader().getResourceAsStream(configFilename)) {

            if (is == null) {
                throw new NotfisException("Arquivo de configuração não encontrado: " + configFilename);
            }

            final String content = new String(is.readAllBytes());
            this.configFile = new JSONObject(content);
        } catch (IOException ex) {
            throw new NotfisException("Erro ao ler o arquivo de configuração: " + ex.getMessage());
        }
    }

    private void checkAllFields(JSONObject json) throws NotfisException {
        loadConfigFile();

        final Map<String, List<JSONObject>> configLinesMap = new HashMap<>();
        for (String key : configFile.keySet()) {
            configLinesMap.put(key, JSONUtils.jsonArrayToListJsonObject(configFile.getJSONArray(key)));
        }

        final List<List<NotfisField>> populatedLines = new ArrayList<>();

        for (String identifier : json.keySet()) {
            final JSONArray jsonLines = json.getJSONArray(identifier);
            final List<JSONObject> configLines = configLinesMap.get(identifier);

            if (configLines == null) {
                throw new NotfisException("Identificador de registro não encontrado na configuração: " + identifier);
            }

            final Map<String, Boolean> fieldMandatory = new HashMap<>();
            final List<NotfisField> populatedFields = new ArrayList<>();

            for (JSONObject configObj : configLines) {
                final String name = configObj.optString("name");
                final NotfisFieldType format = configObj.optString("format", "A").equals("A")
                        ? NotfisFieldType.ALPHANUMERIC
                        : NotfisFieldType.NUMERIC;
                final short position = (short) configObj.optInt("position");
                final short size = (short) configObj.optInt("size");
                final boolean mandatory = configObj.optBoolean("mandatory");

                JSONObject matchingParam = null;
                for (int j = 0; j < jsonLines.length(); j++) {
                    final JSONObject param = jsonLines.getJSONObject(j);
                    if (name.equals(param.optString("name"))) {
                        matchingParam = param;
                        break;
                    }
                }

                if (matchingParam != null) {
                    final Object value = matchingParam.opt("value");
                    if (value == null) {
                        throw new NotfisException(
                                "Valor nulo encontrado no campo '" + name + "' no identificador " + identifier);
                    }
                    final NotfisField field = new NotfisField(name, format, position, size, mandatory, value);
                    populatedFields.add(field);
                } else if (mandatory) {
                    throw new NotfisException(
                            "Campo obrigatório '" + name + "' não encontrado no identificador " + identifier);
                }

                fieldMandatory.put(name, mandatory);
            }

            final String errorMessage = fieldMandatory.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .filter(fields -> {
                        for (int j = 0; j < jsonLines.length(); j++) {
                            JSONObject param = jsonLines.getJSONObject(j);
                            if (fields.getKey().equals(param.optString("name"))) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(", "));

            if (!errorMessage.isEmpty()) {
                throw new NotfisException("Os seguintes campos obrigatórios estão ausentes no identificador "
                        + identifier + ": " + errorMessage);
            }

            populatedLines.add(populatedFields);
        }

        lines.clear();
        lines.addAll(populatedLines);
    }

    public File writeFile(JSONObject json, String filename) throws NotfisException {
        checkAllFields(json);
        final File outputFile = new File(filename);

        try (var writer = new java.io.FileWriter(outputFile)) {
            for (final List<NotfisField> line : lines) { // Linha 137
                final int totalLength = line.stream()
                        .mapToInt(field -> field.getPosition() + field.getSize())
                        .max()
                        .orElse(0);

                final char[] lineChars = new char[totalLength];
                java.util.Arrays.fill(lineChars, ' ');

                for (NotfisField field : line) {
                    String value = field.getValue().toString();

                    // Trunca o valor se exceder o tamanho definido
                    if (value.length() > field.getSize()) {
                        value = value.substring(0, field.getSize());
                    }

                    final int startPosition = field.getPosition() - 1;
                    final int endPosition = Math.min(startPosition + field.getSize(), totalLength);

                    for (int i = startPosition, j = 0; i < endPosition && j < value.length(); i++, j++) {
                        lineChars[i] = value.charAt(j);
                    }
                }

                writer.write(new String(lineChars));
                writer.write("\n");
            }
        } catch (Exception ex) {
            throw new NotfisException("Erro ao escrever o arquivo: " + filename);
        }

        return outputFile;
    }

    public File writeFile(String json, String filename) throws NotfisException {
        return writeFile(new JSONObject(json), filename);
    }
}
