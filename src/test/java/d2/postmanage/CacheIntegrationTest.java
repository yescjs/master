package d2.postmanage;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class CacheIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        redis.start();
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.save(new Post(null, "Spring Boot Guide", "Content about Spring Boot", "Author1", LocalDateTime.now()));
        postRepository.save(new Post(null, "Java Programming", "Content about Java", "Author2", LocalDateTime.now()));
        postRepository.save(new Post(null, "Spring Data JPA", "Content about Spring Data JPA", "Author3", LocalDateTime.now()));
    }

    // @Test
    void cacheTest() {
        postService.findPostById(1L);
        postService.findPostById(1L);
    }
}
