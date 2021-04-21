package pl.edu.agh.smart_repo.common.json;

import java.util.HashMap;
import java.util.Map;

public class EscapeCharMapper {
    private final Map<String, String> mapper;

    public EscapeCharMapper(){
        mapper = new HashMap<>();

        mapper.put("\n", " ");
        mapper.put("\t", " ");
        mapper.put("\r", " ");
        mapper.put("\b", " ");
        mapper.put("\f", " ");
        mapper.put("\\\\", "\\\\\\\\");
    }

    public String mapAll(String string){
        for (Map.Entry<String, String> entry: mapper.entrySet())
            string = string.replaceAll(entry.getKey(), entry.getValue());
        return string;
    }
}
