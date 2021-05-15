package pl.edu.agh.smart_repo.common.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.smart_repo.services.translation.Language;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
public class SearchRequest {
    private String phrase;
    @JsonAlias("languages")
    private List<Language> languagesToSearchIn;
}