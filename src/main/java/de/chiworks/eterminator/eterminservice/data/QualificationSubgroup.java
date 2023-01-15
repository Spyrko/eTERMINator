package de.chiworks.eterminator.eterminservice.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class QualificationSubgroup {
    @Getter
    private final String name;

    @Getter
    @Setter
    private QualificationGroup group;

    private final Map<String, Qualification> qualifications = new HashMap<>();

    public Qualification getQualification(String qualificationId) {
        return qualifications.get(qualificationId);
    }

    public Collection<Qualification> getQualifications() {
        return qualifications.values();
    }

    public Qualification addQualification(Qualification qualification) {
        qualification.setSubgroup(this);
        return qualifications.put(qualification.getId(), qualification);
    }
}
