package pl.edu.agh.smart_repo.common.request;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectoryRequest {
    @Getter
    @Setter
    private String path;
}
