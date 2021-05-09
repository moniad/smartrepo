package pl.edu.agh.smart_repo.common.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectoryDto {
    @Getter
    @Setter
    private String path;
}
