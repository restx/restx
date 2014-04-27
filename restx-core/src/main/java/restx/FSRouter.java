package restx;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import restx.http.HTTP;
import restx.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Date: 28/11/13
 * Time: 21:46
 */
public class FSRouter {
    public static FSRouter mount(String fsPath) {
        return new FSRouter(fsPath);
    }

    private String fsPath;
    private boolean readonly;
    private boolean allowDirectoryListing;

    private FSRouter(String fsPath) {
        this.fsPath = fsPath;
    }

    public RestxRouter on(String onPath) {
        if (!onPath.endsWith("/")) {
            onPath = onPath + "/";
        }
        final Path root = Paths.get(fsPath);
        RestxRouter.Builder builder = RestxRouter.builder().name("FS:" + fsPath)
                .addRoute(new StdRoute("FS:GET:" + fsPath, new StdRestxRequestMatcher("GET", onPath + "{path:.*}")) {
                    @Override
                    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
                            throws IOException {
                        String path = match.getPathParam("path");

                        File file = root.resolve(path).toFile();
                        if (!file.exists()) {
                            notFound(match, resp);
                            return;
                        }

                        if (file.isFile()) {
                            resp.setStatus(HttpStatus.OK);
                            resp.setContentType(HTTP.getContentTypeFromExtension(file.getName()).or("application/binary"));
                            Files.asByteSource(file).copyTo(resp.getOutputStream());
                        } else if (file.isDirectory() && allowDirectoryListing) {
                            resp.setStatus(HttpStatus.OK);
                            resp.setContentType("application/json");
                            PrintWriter writer = resp.getWriter();
                            writer.println("[");
                            Path dir = file.toPath();
                            for (Iterator<Path> iterator = java.nio.file.Files.newDirectoryStream(dir).iterator(); iterator.hasNext(); ) {
                                Path s = iterator.next();
                                boolean isDirectory = s.toFile().isDirectory();
                                writer.println("\"" + dir.relativize(s) + (isDirectory ? "/" : "") + "\""
                                        + (iterator.hasNext() ? "," : "") +"");
                            }
                            writer.println("]");
                        } else {
                            throw new WebException(HttpStatus.UNAUTHORIZED);
                        }
                    }

                    @Override
                    public String toString() {
                        return getMatcher().toString() + " => FS:" + root.toFile().getAbsolutePath();
                    }
                });

        if (!readonly) {
            builder.addRoute(new StdRoute("FS:PUT:" + fsPath, new StdRestxRequestMatcher("PUT", onPath + "{path:.*}")) {
                @Override
                public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
                        throws IOException {
                    String path = match.getPathParam("path");

                    File file = root.resolve(path).toFile();
                    
                    if (file.exists() && !file.isFile()) {
                        throw new WebException(HttpStatus.UNAUTHORIZED);
                    }

                    if (path.endsWith("/")) {
                        if (!file.mkdirs()) {
                            throw new WebException(HttpStatus.UNAUTHORIZED);
                        } else {
                            resp.setStatus(HttpStatus.CREATED);
                            return;
                        }
                    }
                    
                    if (!file.exists()) {
                        // ensure parent exist
                        if (!file.getParentFile().exists()) {
                            if (!file.getParentFile().mkdirs()) {
                                throw new WebException(HttpStatus.UNAUTHORIZED);
                            }
                        }
                        Files.asByteSink(file).writeFrom(req.getContentStream());
                        resp.setStatus(HttpStatus.CREATED);
                    } else {
                        Files.asByteSink(file).writeFrom(req.getContentStream());
                        resp.setStatus(HttpStatus.ACCEPTED);
                    }
                }

                @Override
                public String toString() {
                    return getMatcher().toString() + " => FS:" + root.toFile().getAbsolutePath();
                }
            });
            builder.addRoute(new StdRoute("FS:DELETE:" + fsPath, new StdRestxRequestMatcher("DELETE", onPath + "{path:.*}")) {
                @Override
                public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx)
                        throws IOException {
                    String path = match.getPathParam("path");

                    File file = root.resolve(path).toFile();
                    
                    if (!file.exists()) {
                        // nothing to do, send NO_CONTENT to be idempotent
                        resp.setStatus(HttpStatus.NO_CONTENT);
                        return;
                    }
                    
                    if (file.isDirectory()) {
                        if (file.list().length > 0) {
                            throw new WebException(HttpStatus.UNAUTHORIZED, "can't delete non empty directory");
                        }
                    }

                    java.nio.file.Files.delete(file.toPath());
                    resp.setStatus(HttpStatus.NO_CONTENT);
                }

                @Override
                public String toString() {
                    return getMatcher().toString() + " => FS:" + root.toFile().getAbsolutePath();
                }
            });
        }
        
        return builder
                .build();
    }

    public FSRouter readonly() {
        readonly = true;
        return this;
    }

    public FSRouter allowDirectoryListing() {
        allowDirectoryListing = true;
        return this;
    }

}
