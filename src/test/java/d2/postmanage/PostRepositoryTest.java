package d2.postmanage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        postRepository.save(new Post(null, "Spring Boot Guide", "Content about Spring Boot", "Author1", LocalDateTime.now()));
        postRepository.save(new Post(null, "Java Programming", "Content about Java", "Author2", LocalDateTime.now()));
        postRepository.save(new Post(null, "Spring Data JPA", "Content about Spring Data JPA", "Author3", LocalDateTime.now()));
    }

    // @Test
    void testFindByTitleContaining() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findByTitleContaining("Spring", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).contains("Spring");
        assertThat(result.getContent().get(1).getTitle()).contains("Spring");
    }
}