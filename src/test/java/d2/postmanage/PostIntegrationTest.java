package d2.postmanage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class PostIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    // @Test
    void endToEnd_CRUD() {
        // Create
        Post toCreate = new Post();
        toCreate.setTitle("Integration Test Title");
        toCreate.setContent("Integration Test Content");
        Post created = postService.createPost(toCreate);
        assertNotNull(created.getId());

        // Read
        Post fetched = postService.findPostById(created.getId());
        assertEquals("Integration Test Title", fetched.getTitle());

        // Update
        fetched.setContent("Updated Content");
        Post updated = postService.updatePost(fetched.getId(), fetched);
        assertEquals("Updated Content", updated.getContent());

        // Delete
        postService.deletePost(updated.getId());
        assertThrows(RuntimeException.class, () -> postService.findPostById(updated.getId()));
    }
}
