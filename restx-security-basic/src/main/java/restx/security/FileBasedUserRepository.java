package restx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.common.Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A UserRepository implementation using 2 files to load data.
 *
 * <p>
 *     One file is used to store users, in json format as a Map&lt;String, U&gt; read with jackson.
 *
 *     Example:
 *     <pre>
 *         [
 *             {
 *                 "name": "admin",
 *                 "principalRoles": ["restx-admin"]
 *             }
 *         ]
 *     </pre>
 * </p>
 * <p>
 *     Another file is used to store user credentials.
 *     Storing credentials separately is a good practice, which allow to have different security policies on the
 *     two files, and avoid to have credentials part of the user class.
 *
 *     Example:
 *     <pre>
 *         {
 *             "admin": "tuyvuicxvcx78vdsfuisd"
 *         }
 *     </pre>
 * </p>
 */
public class FileBasedUserRepository<U extends RestxPrincipal> implements UserRepository<U> {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedUserRepository.class);

    private final Class<U> userClass;
    private final U defaultAdmin;
    private final CachedData<String> credentials;
    private final CachedData<U> users;

    public FileBasedUserRepository(Class<U> userClass, ObjectMapper mapper, U defaultAdmin,
                                   Path usersPath, Path credentialsPath, boolean reloadOnChange) {
        this.userClass = userClass;
        this.defaultAdmin = defaultAdmin;
        this.credentials = new CachedData<>(credentialsPath, "credentials",
                mapper, Types.newParameterizedType(Map.class, String.class, String.class), reloadOnChange);
        this.users = new CachedData<U>(usersPath, "users",
                mapper, Types.newParameterizedType(List.class, userClass), reloadOnChange) {
            @Override
            protected Map<String, U> toMap(Object o) {
                List<U> users = (List<U>) o;
                Map<String, U> usersMap = new LinkedHashMap();
                for (U user : users) {
                    usersMap.put(user.getName(), user);
                }
                return usersMap;
            }
        };
    }

    @Override
    public Optional<U> findUserByName(String name) {
        return users.get(name);
    }

    @Override
    public Optional<String> findCredentialByUserName(String userName) {
        return credentials.get(userName);
    }

    @Override
    public boolean isAdminDefined() {
        for (U u : users.data().values()) {
            if (u.getPrincipalRoles().contains("restx-admin")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public U defaultAdmin() {
        return defaultAdmin;
    }

    private static class CachedData<T> {
        private final Path dataPath;
        private final String name;
        private final ObjectReader reader;
        private final boolean reloadOnChange;
        private long dataFileTimestamp;
        private Map<String, T> data;

        public CachedData(Path path, String name, ObjectMapper mapper, ParameterizedType valueType, boolean reloadOnChange) {
            this.dataPath = path;
            this.name = name;
            this.reader = mapper.reader().withType(valueType);
            this.reloadOnChange = reloadOnChange;
        }

        public Optional<T> get(String name) {
            return Optional.fromNullable(data().get(name));
        }

        private synchronized Map<String, T> data() {
            if (data == null || reloadOnChange) {
                if (!dataPath.toFile().exists()) {
                    logger.warn(name + " file " + dataPath.toAbsolutePath() + " not found");
                    if (data == null) {
                        data = new HashMap<>();
                    }
                } else {
                    if (dataFileTimestamp >= dataPath.toFile().lastModified()) {
                        logger.debug(name + " are up to date");
                    } else {
                        logger.debug("loading " + name + " from " + dataPath.toAbsolutePath());
                        try {
                            long lastModified = dataPath.toFile().lastModified();
                            data = toMap(reader.readValue(dataPath.toFile()));
                            dataFileTimestamp = lastModified;
                        } catch (IOException e) {
                            logger.warn("error while loading " + name + " file " + dataPath + ": " + e.getMessage(), e);
                            data = new HashMap<>();
                        }
                    }
                }
            }
            return data;
        }

        protected Map<String, T> toMap(Object o) {
            return (Map<String, T>) o;
        }

    }
}
