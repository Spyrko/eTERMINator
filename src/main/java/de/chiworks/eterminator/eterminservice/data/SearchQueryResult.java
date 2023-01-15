package de.chiworks.eterminator.eterminservice.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class SearchQueryResult {
    Map<String, Object> praxen;
}
