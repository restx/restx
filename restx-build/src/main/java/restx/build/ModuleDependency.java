package restx.build;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 3:19 PM
 */
public class ModuleDependency {
    private final GAV gav;

    public ModuleDependency(GAV gav) {
        this.gav = gav;
    }

    public GAV getGav() {
        return gav;
    }
}
