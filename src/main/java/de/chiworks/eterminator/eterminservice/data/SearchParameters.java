package de.chiworks.eterminator.eterminservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class SearchParameters {
    @NonNull
    private String token;
    @NonNull
    private String zipCode;
    @NonNull
    private String radius;
    @NonNull
    private QualificationSubgroup qualificationSubgroup;
}
