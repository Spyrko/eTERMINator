package de.chiworks.eterminator.eterminservice.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class Qualification {

    private final String id;

    private final String name;

    private QualificationSubgroup subgroup;

    public QualificationGroup getGroup() {
        return subgroup.getGroup();
    }
}
