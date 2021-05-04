import java.util.HashMap;

public class Environment {
    HashMap<String,Integer> variables;
    Environment prevEnv;

    Environment() {
        variables = new HashMap<>();
        prevEnv = null;
    }

    Environment(HashMap<String,Integer> variables, Environment prevEnv) {
        this.variables = variables;
        this.prevEnv = prevEnv;
    }
}
