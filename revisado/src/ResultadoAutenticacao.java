/** Resultado imutável de uma tentativa de autenticação. */
public record ResultadoAutenticacao(boolean autenticado, String nomeUsuario) {
    public static ResultadoAutenticacao sucesso(String nomeUsuario) {
        return new ResultadoAutenticacao(true, nomeUsuario);
    }

    public static ResultadoAutenticacao falha() {
        return new ResultadoAutenticacao(false, null);
    }
}
