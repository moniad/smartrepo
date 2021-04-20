package pl.edu.agh.smart_repo.common.json;

import java.util.HashMap;
import java.util.Map;

public class EscapeCharMapper {
    private Map<String, String> mapper;

    public EscapeCharMapper(){
        mapper = new HashMap<>();

        mapper.put("\\\\", "\\\\\\\\");
        mapper.put("\n", " ");
        mapper.put("\t", " ");
        mapper.put("\r", " ");
        mapper.put("\b", " ");
        mapper.put("\f", " ");
    }

    public String mapChar(String string, String ch){
        return string.replaceAll(ch, mapper.get(ch));
    }

    public String mapAll(String string){
        for (Map.Entry<String, String> entry: mapper.entrySet())
            string = string.replaceAll(entry.getKey(), entry.getValue());
        return string;
    }
}
