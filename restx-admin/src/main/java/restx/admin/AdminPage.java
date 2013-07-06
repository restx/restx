package restx.admin;

/**
 * User: xavierhanin
 * Date: 4/7/13
 * Time: 1:54 PM
 */
public class AdminPage {
    public static final String RESTX_ADMIN_ROLE = "restx-admin";

    private final String path;
    private final String title;

    /**
     * Constructs a new AdminPage
     * @param path the absolute restx path of the admin page (usually something like /@/ui/xxxx/).
     * @param title the title of the page, as it will appear in admin menu.
     */
    public AdminPage(String path, String title) {
        this.path = path;
        this.title = title;
    }

    @Override
    public String toString() {
        return "AdminPage{" +
                "path='" + path + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Returns a new AdminPage instance which path is a full URI for the page, based on the provided baseUri.
     *
     * @param baseUri the uri to use as base in the path.
     * @return a new admin page with path being a full URI.
     */
    public AdminPage rootOn(String baseUri) {
        return new AdminPage(baseUri + path, title);
    }
}
