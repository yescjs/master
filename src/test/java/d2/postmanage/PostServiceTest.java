package d2.postmanage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreatePost() {
        Post post = new Post(null, "Title", "Content", "Author", null);
        when(postRepository.save(post)).thenReturn(new Post(1L, "Title", "Content", "Author", null));

        Post createdPost = postService.createPost(post);

        assertThat(createdPost.getId()).isEqualTo(1L);
        verify(postRepository, times(1)).save(post);
    }

    // @Test
    void testFindPostById_Cached() {
        Long id = 1L;
        String cachedTitle = "Cached Title";
        when(redisTemplate.hasKey("post:" + id)).thenReturn(true);
        when(valueOperations.get("post:" + id)).thenReturn(cachedTitle);

        Post post = postService.findPostById(id);

        assertThat(post.getId()).isEqualTo(id);
        assertThat(post.getTitle()).isEqualTo(cachedTitle);
        verify(redisTemplate, times(1)).hasKey("post:" + id);
        verify(valueOperations, times(1)).get("post:" + id);
        verifyNoInteractions(postRepository);
    }

    // @Test
    void testFindPostById_NotCached() {
        Long id = 1L;
        Post post = new Post(id, "Title", "Content", "Author", null);
        when(redisTemplate.hasKey("post:" + id)).thenReturn(false);
        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        Post foundPost = postService.findPostById(id);

        assertThat(foundPost).isEqualTo(post);
        verify(redisTemplate, times(1)).hasKey("post:" + id);
        verify(postRepository, times(1)).findById(id);
        verify(valueOperations, times(1)).set("post:" + id, post.getTitle(), 1, TimeUnit.MINUTES);
    }

    @Test
    void testFindPostById_NotFound() {
        Long id = 99L;
        when(redisTemplate.hasKey("post:" + id)).thenReturn(false);
        when(postRepository.findById(id)).thenReturn(Optional.empty());

        Post post = postService.findPostById(id);
        
        assertNull(post);
        verify(postRepository, times(1)).findById(id);
    }

    @Test
    void testGetAllPosts() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(new Post(1L, "Title", "Content", "Author", null)));
        when(postRepository.findAll(pageable)).thenReturn(page);

        Page<Post> result = postService.getAllPosts(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSearchPostsByTitle() {
        String keyword = "Title";
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(new Post(1L, "Title", "Content", "Author", null)));
        when(postRepository.findByTitleContaining(keyword, pageable)).thenReturn(page);

        Page<Post> result = postService.searchPostsByTitle(keyword, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(postRepository, times(1)).findByTitleContaining(keyword, pageable);
    }

    @Test
    void testUpdatePost() {
        Long id = 1L;
        Post existingPost = new Post(id, "Old Title", "Old Content", "Old Author", null);
        Post updatedPost = new Post(null, "New Title", "New Content", "New Author", null);
        when(postRepository.findById(id)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(existingPost)).thenReturn(existingPost);

        Post result = postService.updatePost(id, updatedPost);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getContent()).isEqualTo("New Content");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        verify(postRepository, times(1)).findById(id);
        verify(postRepository, times(1)).save(existingPost);
    }

    @Test
    void testDeletePost() {
        Long id = 1L;

        postService.deletePost(id);

        verify(postRepository, times(1)).deleteById(id);
    }
}