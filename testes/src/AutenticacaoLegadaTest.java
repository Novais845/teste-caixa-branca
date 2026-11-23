import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/** Evidências reproduzíveis dos caminhos problemáticos do código legado. */
public final class AutenticacaoLegadaTest {
    private static String sqlCapturada;
    private static int aprovados;

    public static void main(String[] args) throws Exception {
        executar("legado retorna null sem driver", AutenticacaoLegadaTest::deveRetornarNullSemDriver);
        executar("legado engole falha de conexao", AutenticacaoLegadaTest::deveEngolirFalhaDeConexao);
        executar("legado concatena injecao SQL", AutenticacaoLegadaTest::deveConcatenarEntradaHostil);
        System.out.printf("%d testes do legado aprovados.%n", aprovados);
    }

    private static void deveRetornarNullSemDriver() {
        Connection conexao = new AutenticacaoLegada().abrirConexao();
        afirmar(conexao == null, "sem driver MySQL, o legado deveria retornar null");
    }

    private static void deveEngolirFalhaDeConexao() {
        boolean autenticado = new AutenticacaoLegada().autenticar("ana", "segredo");
        afirmar(!autenticado, "a falha deveria ser engolida e retornar false no objeto novo");
    }

    private static void deveConcatenarEntradaHostil() throws Exception {
        Driver driver = new DriverCaptura();
        DriverManager.registerDriver(driver);
        try {
            String carga = "x' OR '1'='1";
            new AutenticacaoLegada().autenticar(carga, "qualquer");
            afirmar(sqlCapturada.contains(carga), "a carga deveria aparecer na SQL construída");
        } finally {
            DriverManager.deregisterDriver(driver);
        }
    }

    private static void executar(String nome, TesteComExcecao teste) throws Exception {
        teste.executar();
        aprovados++;
        System.out.println("PASSOU: " + nome);
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    @FunctionalInterface
    private interface TesteComExcecao { void executar() throws Exception; }

    private static final class DriverCaptura implements Driver {
        @Override public Connection connect(String url, Properties info) {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
                if (m.getName().equals("createStatement")) return statementFalso();
                return valorPadrao(m.getReturnType());
            });
        }
        @Override public boolean acceptsURL(String url) { return url.startsWith("jdbc:mysql:"); }
        @Override public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public Logger getParentLogger() { return Logger.getGlobal(); }

        private Statement statementFalso() {
            return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Statement.class}, (p, m, a) -> {
                if (m.getName().equals("executeQuery")) { sqlCapturada = (String) a[0]; return resultadoVazio(); }
                return valorPadrao(m.getReturnType());
            });
        }

        private ResultSet resultadoVazio() {
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (p, m, a) -> {
                if (m.getName().equals("next")) return false;
                return valorPadrao(m.getReturnType());
            });
        }

        private Object valorPadrao(Class<?> tipo) {
            if (!tipo.isPrimitive()) return null;
            if (tipo == boolean.class) return false;
            if (tipo == char.class) return '\0';
            if (tipo == byte.class || tipo == short.class || tipo == int.class || tipo == long.class) return 0;
            if (tipo == float.class || tipo == double.class) return 0.0;
            return null;
        }
    }
}
