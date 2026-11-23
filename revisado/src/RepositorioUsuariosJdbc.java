import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

/** Implementação JDBC com consulta parametrizada e recursos fechados. */
public final class RepositorioUsuariosJdbc implements RepositorioUsuarios {
    private static final String SQL = "SELECT nome FROM usuarios WHERE usuario = ? AND senha = ?";
    private final DataSource dataSource;

    public RepositorioUsuariosJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource é obrigatório");
    }

    @Override
    public String buscarNomePorCredenciais(String usuario, String senha) throws SQLException {
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(SQL)) {
            comando.setString(1, usuario);
            comando.setString(2, senha);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? resultado.getString("nome") : null;
            }
        }
    }
}
