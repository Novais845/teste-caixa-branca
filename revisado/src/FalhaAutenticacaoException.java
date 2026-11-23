/** Indica indisponibilidade ou erro técnico na fonte de usuários. */
public final class FalhaAutenticacaoException extends RuntimeException {
    public FalhaAutenticacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
