# Notfis EDI Generator

## Descrição

O **Notfis EDI Generator** é uma aplicação que gera arquivos Notfis EDI em formatos não padronizados para as versões 3.1 e 5.0. O objetivo deste projeto é facilitar a criação e o controle desses arquivos para atender requisitos específicos de integração com sistemas legados ou customizados.

## Funcionalidades

- **Geração de Arquivo Notfis EDI**
  - Suporte para versões 3.1 e 5.0.
  - Estrutura flexível para atender diferentes layouts não padronizados.
  
- **Configuração Dinâmica**
  - Permite a definição de formatos de campos como nome, formato, tamanho, posição e obrigatoriedade através de arquivos de configuração JSON.

- **Validação**
  - Verifica se todos os campos obrigatórios estão presentes e se seguem as especificações configuradas.

## Pré-requisitos
-   Java 11+
-   Maven 3.6.3+

## Arquivos de Configuração
    
Estes arquivos representam os campos necessários para as suas respectivas versões do **NOTFIS**
- **[notfis31.json](src/main/resources/notfis/notfis31.json)**
- **[notfis50.json](src/main/resources/notfis/notfis50.json)**

## Dados de Entrada

Os **Dados de Entrada** tem que vir em um formato específico para que não haja problema na validação e geração do arquivo de notfis.

O Método `writeFile` da classe `NotfisWriter` recebe um `JSONArray` como parametro e o nome do arquivo de saída:

```bash
final var jsonData = new JSONArray("{}");
final var notfisWriter = new NotfisWriter(NotfisType.VERSION31);

final InputStream generatedEdiBytes = notfisWrite.writeFile("notfis.txt");
```

O `JSON` especificado deve vir no seguinte formato:
```bash
{
    "000": [
      [
        
        {
          "name": "IDENTIFICADOR DE REGISTRO",
          "value": "000"
        },

        {
            "name": "IDENTIFICADOR DO REMETENTE",
            "value": "0000000"
        },
        
        {
            "name": "IDENTIFICADOR DE REGISTRO",
            "value": "320"
        }
      ]
      ...
    ]
}
```

### Instalação
Este projeto oferece instalação via `Maven`:
```bash
  <dependency>
    <groupId>io.github.tonyguerra122</groupId>
    <artifactId>notfis-generator</artifactId>
    <version>1.0.7</version>
  </dependency> 
```
