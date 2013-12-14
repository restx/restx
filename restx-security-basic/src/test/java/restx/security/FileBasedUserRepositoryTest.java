package restx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 14/12/13
 * Time: 17:06
 */
public class FileBasedUserRepositoryTest {
    private Path usersPath;
    private Path credentialsPath;
    private StdUser admin = new StdUser("admin", ImmutableSet.<String>of("restx-admin"));
    private ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule());

    @Before
    public void setup() throws IOException {
        usersPath = File.createTempFile("users", ".json").toPath();
        credentialsPath = File.createTempFile("credentials", ".json").toPath();
    }

    @After
    public void teardown() {
        org.assertj.core.util.Files.delete(usersPath.toFile());
        org.assertj.core.util.Files.delete(credentialsPath.toFile());
    }

    @Test
    public void should_load_users() throws Exception {
        FileBasedUserRepository<StdUser> repo = newRepo(false);

        Files.copy(getClass().getResourceAsStream("users.json"), usersPath, StandardCopyOption.REPLACE_EXISTING);

        assertThat(repo.findUserByName("admin").isPresent()).isFalse();

        checkUser(repo.findUserByName("john"), "john", "role1");
    }

    @Test
    public void should_load_credentials() throws Exception {
        FileBasedUserRepository<StdUser> repo = newRepo(false);

        Files.copy(getClass().getResourceAsStream("credentials.json"), credentialsPath, StandardCopyOption.REPLACE_EXISTING);

        assertThat(repo.findCredentialByUserName("admin").isPresent()).isFalse();
        assertThat(repo.findCredentialByUserName("john")).isEqualTo(Optional.of("johnpwd"));
    }

    @Test
    public void should_reload_credentials() throws Exception {
        FileBasedUserRepository<StdUser> repo = newRepo(true);

        Files.copy(getClass().getResourceAsStream("credentials.json"), credentialsPath, StandardCopyOption.REPLACE_EXISTING);

        assertThat(repo.findCredentialByUserName("john")).isEqualTo(Optional.of("johnpwd"));

        Thread.sleep(1000); // sleep enough to make sure timestamp changes

        Files.copy(getClass().getResourceAsStream("credentials2.json"), credentialsPath, StandardCopyOption.REPLACE_EXISTING);
        assertThat(repo.findCredentialByUserName("john")).isEqualTo(Optional.of("johnpwd2"));
    }

    @Test
    public void should_reload_users() throws Exception {
        FileBasedUserRepository<StdUser> repo = newRepo(true);
        Files.copy(getClass().getResourceAsStream("users.json"), usersPath, StandardCopyOption.REPLACE_EXISTING);

        checkUser(repo.findUserByName("john"), "john", "role1");
        assertThat(repo.findUserByName("jane").isPresent()).isFalse();

        Thread.sleep(1000); // sleep enough to make sure timestamp changes
        Files.copy(getClass().getResourceAsStream("users2.json"), usersPath, StandardCopyOption.REPLACE_EXISTING);

        checkUser(repo.findUserByName("john"), "john", "role2");
        checkUser(repo.findUserByName("jane"), "jane", "role1", "role2");
    }


    @Test
    public void should_not_reload_users() throws Exception {
        FileBasedUserRepository<StdUser> repo = newRepo(false);
        Files.copy(getClass().getResourceAsStream("users.json"), usersPath, StandardCopyOption.REPLACE_EXISTING);

        checkUser(repo.findUserByName("john"), "john", "role1");
        assertThat(repo.findUserByName("jane").isPresent()).isFalse();

        Thread.sleep(1000); // sleep enough to make sure timestamp changes
        Files.copy(getClass().getResourceAsStream("users2.json"), usersPath, StandardCopyOption.REPLACE_EXISTING);

        checkUser(repo.findUserByName("john"), "john", "role1");
        assertThat(repo.findUserByName("jane").isPresent()).isFalse();
    }


    private void checkUser(Optional<StdUser> u, String name, String... roles) {
        assertThat(u.isPresent()).isTrue();
        assertThat(u.get().getName()).isEqualTo(name);
        assertThat(u.get().getPrincipalRoles()).isEqualTo(ImmutableSet.<String>copyOf(roles));
    }


    private FileBasedUserRepository<StdUser> newRepo(boolean reloadOnChange) {
        return new FileBasedUserRepository(
                    StdUser.class, mapper, admin,
                    usersPath, credentialsPath, reloadOnChange);
    }
}
