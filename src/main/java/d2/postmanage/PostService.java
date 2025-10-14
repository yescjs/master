package d2.postmanage;

import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post findPostById(Long id) {
        // return postRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글 없음"));
        String key = "post:" + id;
        
        if (redisTemplate.hasKey(key)) {
            String cached = (String) redisTemplate.opsForValue().get(key);
            return new Post(id, cached, "내용 캐시", "작성자", null);
        }
        
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글 없음"));
        redisTemplate.opsForValue().set(key, post.getTitle(), 1, TimeUnit.MINUTES);
        
        return post;
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchPostsByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitleContaining(keyword, pageable);
    }

    public Post updatePost(Long id, Post updatedPost) {
        return postRepository.findById(id).map(post -> {
            post.setTitle(updatedPost.getTitle());
            post.setContent(updatedPost.getContent());
            post.setAuthor(updatedPost.getAuthor());
            return postRepository.save(post);
        }).orElse(null);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
