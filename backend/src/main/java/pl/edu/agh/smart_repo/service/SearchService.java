package pl.edu.agh.smart_repo.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SearchService {
    public static List<String> searchDocuments(String phrase) {
        System.out.println("Searching for: " + phrase + " . . .");
        return Collections.singletonList("przepisyChodakowska.pdf");
    }
}
