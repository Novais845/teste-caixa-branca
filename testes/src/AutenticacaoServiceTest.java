import java.util.HashMap;
import java.util.Map;

/** Testes repetíveis sem banco de dados ou dependências externas. */
public final class AutenticacaoServiceTest {
    private static int aprovados;

    public static void main(String[] args) {
        executar("credenciais validas", AutenticacaoServiceTest::deveAutenticarCredenciaisValidas);
        executar("credenciais invalidas", AutenticacaoServiceTest::deveRecusarCredenciaisInvalidas);
        executar("usuario vazio", AutenticacaoServiceTest::deveRejeitarUsuarioVazio);
        executar("senha nula", AutenticacaoServiceTest::deveRejeitarSenhaNula);
        executar("falha no repositorio", AutenticacaoServiceTest::deveExporFalhaTecnica);
        System.out.printf("%d testes aprovados.%n", aprovados);
    }

    private static void deveAutenticarCredenciaisValidas() {
        AutenticacaoService servico = new AutenticacaoService(repositorioComAna());
        ResultadoAutenticacao resultado = servico.autenticar("ana", "segredo");
        afirmar(resultado.autenticado(), "autenticação deveria ser verdadeira");
        afirmar("Ana Silva".equals(resultado.nomeUsuario()), "nome retornado incorreto");
    }

    private static void deveRecusarCredenciaisInvalidas() {
        AutenticacaoService servico = new AutenticacaoService(repositorioComAna());
        ResultadoAutenticacao resultado = servico.autenticar("ana", "errada");
        afirmar(!resultado.autenticado(), "autenticação deveria ser falsa");
        afirmar(resultado.nomeUsuario() == null, "falha não deve expor nome");
    }

    private static void deveRejeitarUsuarioVazio() {
        esperar(IllegalArgumentException.class, () -> new AutenticacaoService(repositorioComAna()).autenticar(" ", "x"));
    }

    private static void deveRejeitarSenhaNula() {
        esperar(IllegalArgumentException.class, () -> new AutenticacaoService(repositorioComAna()).autenticar("ana", null));
    }

    private static void deveExporFalhaTecnica() {
        RepositorioUsuarios indisponivel = (usuario, senha) -> { throw new Exception("banco indisponível"); };
        esperar(FalhaAutenticacaoException.class, () -> new AutenticacaoService(indisponivel).autenticar("ana", "segredo"));
    }

    private static RepositorioUsuarios repositorioComAna() {
        Map<String, String> usuarios = new HashMap<>();
        usuarios.put("ana\u0000segredo", "Ana Silva");
        return (usuario, senha) -> usuarios.get(usuario + "\u0000" + senha);
    }

    private static void executar(String nome, Runnable teste) {
        teste.run();
        aprovados++;
        System.out.println("PASSOU: " + nome);
    }

    private static void esperar(Class<? extends Throwable> tipo, Runnable acao) {
        try { acao.run(); } catch (Throwable erro) {
            afirmar(tipo.isInstance(erro), "exceção esperada: " + tipo.getSimpleName());
            return;
        }
        throw new AssertionError("nenhuma exceção foi lançada");
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }
}
