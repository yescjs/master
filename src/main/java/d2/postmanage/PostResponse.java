package d2.postmanage;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {
    
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
