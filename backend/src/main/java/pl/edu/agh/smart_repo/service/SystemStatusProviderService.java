package pl.edu.agh.smart_repo.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SystemStatusProviderService {
    public static String getStatusForComponent(String componentName) {
        System.out.println("Getting status for \"" + componentName + "\" ...");
        switch (componentName)
        {
            case "comp1":
                return "Ready for next request.";
            case "comp2":
                return "Busy.";
            case "comp3":
                return "Component is down.";
            default:
                return "Component with name \"" + componentName + "\" not found in system.";
        }
    }
}
