package d2.postmanage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void testCreatePost() throws Exception {
        Post post = new Post();
        post.setTitle("Test Title");
        post.setContent("Test Content");
        when(postService.createPost(any(Post.class))).thenReturn(post);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Title\",\"content\":\"Test Content\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Title"))
            .andExpect(jsonPath("$.content").value("Test Content"));

        verify(postService, times(1)).createPost(any(Post.class));
    }

    @Test
    void testGetAllPosts() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> posts = new PageImpl<>(Collections.emptyList());
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(posts);

        mockMvc.perform(get("/posts")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        verify(postService, times(1)).getAllPosts(any(Pageable.class));
    }

    @Test
    void testGetPostById() throws Exception {
        Long id = 1L;
        Post post = new Post();
        post.setId(id);
        when(postService.findPostById(id)).thenReturn(post);

        mockMvc.perform(get("/posts/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));

        verify(postService, times(1)).findPostById(id);
    }

    @Test
    void testGetPostByTitle() throws Exception {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> posts = new PageImpl<>(Collections.emptyList());
        when(postService.searchPostsByTitle(eq(keyword), any(Pageable.class))).thenReturn(posts);

        mockMvc.perform(get("/posts/search")
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        verify(postService, times(1)).searchPostsByTitle(eq(keyword), any(Pageable.class));
    }

    @Test
    void testUpdatePost() throws Exception {
        Long id = 1L;
        Post updatedPost = new Post();
        updatedPost.setTitle("Updated Title");
        updatedPost.setContent("Updated Content");
        when(postService.updatePost(eq(id), any(Post.class))).thenReturn(updatedPost);

        mockMvc.perform(put("/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\",\"content\":\"Updated Content\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(postService, times(1)).updatePost(eq(id), any(Post.class));
    }

    @Test
    void testDeletePost() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/posts/{id}", id))
            .andExpect(status().isOk());

        verify(postService, times(1)).deletePost(id);
    }
}