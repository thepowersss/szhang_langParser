public class Closure {
    Parse parse;
    Environment env;
    String[] params;

    Closure(Parse parse, Environment env, String[] params) {
        this.parse = parse;
        this.env = env;
        this.params = params;
    }
}
