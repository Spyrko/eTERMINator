package de.chiworks.eterminator.eterminservice.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class QualificationQueryResult {
    private String version;

    private Map<String, Map<String, Map<String, String>>> w;
}
