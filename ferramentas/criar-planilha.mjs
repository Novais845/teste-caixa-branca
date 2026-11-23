import fs from "node:fs/promises";
import { SpreadsheetFile, Workbook } from "@oai/artifact-tool";

const outputDir = "outputs/caixa-branca-20261123";
await fs.mkdir(outputDir, { recursive: true });
const workbook = Workbook.create();
const sheet = workbook.worksheets.add("CAIXA BRANCO (ESTÁTICO)");
sheet.showGridLines = false;

sheet.getRange("A1:G1").merge();
sheet.getRange("A1").values = [["PLANO DE TESTE — ANÁLISE ESTÁTICA | CENÁRIO 1: AUTENTICAÇÃO JDBC"]];
sheet.getRange("A3:G3").values = [["Localização", "Descrição da ocorrência", "Categoria", "Severidade", "Justificativa", "Risco / consequência", "Recomendação"]];
sheet.getRange("A4:G11").values = [
  ["abrirConexao(), linha 13", "URL JDBC contém usuário root e senha em texto claro.", "Segurança / gestão de segredos", "Crítica", "Credencial privilegiada exposta no código e em eventual repositório público.", "Acesso não autorizado ao banco e vazamento de segredo.", "Mover a configuração para ambiente/gerenciador de segredos e aplicar privilégio mínimo."],
  ["autenticar(), linhas 22–23", "Consulta SQL é montada por concatenação de usuario e senha.", "Segurança / injeção SQL", "Crítica", "Dados controlados pelo usuário alteram o comando SQL.", "Bypass de autenticação, leitura ou alteração indevida de dados.", "Usar PreparedStatement e parâmetros posicionais."],
  ["abrirConexao(), linhas 15–17", "catch (Exception) vazio ignora falha de conexão.", "Tratamento de exceções", "Alta", "A falha técnica fica invisível ao chamador e devolve null.", "Diagnóstico impossível e falha posterior por NullPointerException.", "Capturar SQLException, preservar a causa e sinalizar erro técnico."],
  ["autenticar(), linhas 25–33", "Connection, Statement e ResultSet não são fechados; catch vazio.", "Recursos externos / exceções", "Alta", "Recursos JDBC podem permanecer abertos e o erro é mascarado.", "Esgotamento de conexões e resultados incorretos sem diagnóstico.", "Usar try-with-resources e exceção explícita."],
  ["autenticar(), linha 20", "abrirConexao pode retornar null e a chamada seguinte lança NPE capturado silenciosamente.", "Fluxo / retorno", "Alta", "O método não diferencia indisponibilidade de credencial inválida.", "Usuário recebe resposta enganosa; operação falha de modo opaco.", "Não retornar null silenciosamente; propagar exceção de infraestrutura."],
  ["Atributos, linhas 7–8 e retorno linha 34", "autenticado e ultimoUsuario persistem entre chamadas.", "Estado / concorrência", "Alta", "Após sucesso, tentativa inválida pode reutilizar true e nome anterior.", "Autenticação indevida e vazamento de identidade em reuso/concor­rência.", "Retornar resultado imutável por chamada, sem estado de sessão no serviço."],
  ["autenticar(), linhas 20–34", "Não valida null, vazio ou espaços e concentra regra, JDBC e estado.", "Validação / responsabilidade", "Média", "Entradas ruins chegam à camada de dados e a classe tem múltiplas responsabilidades.", "Erros tardios, manutenção e teste difíceis.", "Validar no limite e separar serviço de repositório."],
  ["Classe e métodos", "Documentação mínima e dependência estática de DriverManager.", "Legibilidade / testabilidade", "Média", "Contrato, falhas e dependências não estão claros nem substituíveis.", "Maior custo de manutenção e testes dependentes de ambiente.", "Adicionar JavaDoc essencial e injetar DataSource/repositório."]
];

sheet.getRange("A1:G1").format = { fill: "#FFFFFF", font: { name: "Arial", size: 14, bold: true, color: "#1F2937" }, horizontalAlignment: "left", verticalAlignment: "center" };
sheet.getRange("A3:G3").format = { fill: "#1F4E78", font: { name: "Arial", size: 10, bold: true, color: "#FFFFFF" }, horizontalAlignment: "center", verticalAlignment: "center", wrapText: true };
sheet.getRange("A4:G11").format = { font: { name: "Arial", size: 10, color: "#1F2937" }, verticalAlignment: "top", wrapText: true };
sheet.getRange("A3:G11").format.borders = { preset: "all", style: "thin", color: "#D9E2F3" };
sheet.getRange("D4:D11").conditionalFormats.add("containsText", { text: "Crítica", format: { fill: "#FDE2E1", font: { bold: true, color: "#B91C1C" } } });
sheet.getRange("D4:D11").conditionalFormats.add("containsText", { text: "Alta", format: { fill: "#FFF2CC", font: { bold: true, color: "#9A6700" } } });
sheet.getRange("A:A").format.columnWidth = 22;
sheet.getRange("B:B").format.columnWidth = 37;
sheet.getRange("C:C").format.columnWidth = 26;
sheet.getRange("D:D").format.columnWidth = 13;
sheet.getRange("E:E").format.columnWidth = 35;
sheet.getRange("F:F").format.columnWidth = 37;
sheet.getRange("G:G").format.columnWidth = 42;
sheet.getRange("A1:G1").format.rowHeight = 26;
sheet.getRange("A3:G3").format.rowHeight = 32;
sheet.getRange("A4:G11").format.rowHeight = 65;
sheet.freezePanes.freezeRows(3);
workbook.recalculate();
const check = await workbook.inspect({ kind: "table", range: "CAIXA BRANCO (ESTÁTICO)!A1:G11", include: "values,formulas", tableMaxRows: 12, tableMaxCols: 7 });
console.log(check.ndjson);
const errors = await workbook.inspect({ kind: "match", searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A", options: { useRegex: true, maxResults: 50 }, summary: "formula error scan" });
console.log(errors.ndjson);
const preview = await workbook.render({ sheetName: "CAIXA BRANCO (ESTÁTICO)", range: "A1:G11", scale: 1, format: "png" });
await fs.writeFile(`${outputDir}/planilha-preview.png`, new Uint8Array(await preview.arrayBuffer()));
const output = await SpreadsheetFile.exportXlsx(workbook);
await output.save("PLANO_DE_TESTE.xlsx");
