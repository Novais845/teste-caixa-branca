import java.util.Objects;

/** Serviço de autenticação sem estado compartilhado entre requisições. */
public final class AutenticacaoService {
    private final RepositorioUsuarios repositorio;

    public AutenticacaoService(RepositorioUsuarios repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio, "repositorio é obrigatório");
    }

    /**
     * Valida a entrada e delega a consulta parametrizada ao repositório.
     * @throws IllegalArgumentException quando usuário ou senha forem nulos/vazios
     * @throws FalhaAutenticacaoException quando a fonte de dados não puder ser consultada
     */
    public ResultadoAutenticacao autenticar(String usuario, String senha) {
        validarCredencial(usuario, "usuario");
        validarCredencial(senha, "senha");
        try {
            String nome = repositorio.buscarNomePorCredenciais(usuario, senha);
            return nome == null
                    ? ResultadoAutenticacao.falha()
                    : ResultadoAutenticacao.sucesso(nome);
        } catch (Exception e) {
            throw new FalhaAutenticacaoException("Não foi possível consultar as credenciais.", e);
        }
    }

    private static void validarCredencial(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " deve ser informado.");
        }
    }
}
