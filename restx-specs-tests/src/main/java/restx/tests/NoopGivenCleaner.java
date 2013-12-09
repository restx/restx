package restx.tests;

/**
 * Date: 9/12/13
 * Time: 21:31
 */
public class NoopGivenCleaner implements GivenCleaner {
    public static final GivenCleaner INSTANCE = new NoopGivenCleaner();

    private NoopGivenCleaner() {}

    @Override
    public void cleanUp() {
    }
}
