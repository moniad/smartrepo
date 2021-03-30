package pl.edu.agh.smart_repo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.smart_repo.service.SystemStatusProviderService;

import java.util.List;

@RestController
public class SystemStatusProviderController {

    @GetMapping("/status/{componentName}")
    @ResponseBody
    public ResponseEntity<String> get(@PathVariable String componentName) {
        String componentStatusStr = SystemStatusProviderService.getStatusForComponent(componentName);
        return new ResponseEntity<>(componentStatusStr, HttpStatus.OK);
    }
}