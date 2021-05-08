package pl.edu.agh.smart_repo.common.request;

import lombok.*;
import pl.edu.agh.smart_repo.services.translation.Language;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
public class SearchRequest {
    private String phrase;
    private List<Language> languagesToSearchIn;
}