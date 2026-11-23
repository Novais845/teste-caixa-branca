/** Porta de acesso a usuários. Implementações JDBC devem usar PreparedStatement. */
public interface RepositorioUsuarios {
    /** @return nome do usuário, ou null se as credenciais não existirem. */
    String buscarNomePorCredenciais(String usuario, String senha) throws Exception;
}
