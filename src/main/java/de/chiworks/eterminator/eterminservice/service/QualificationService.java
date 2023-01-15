package de.chiworks.eterminator.eterminservice.service;

import de.chiworks.eterminator.eterminservice.data.Qualification;
import de.chiworks.eterminator.eterminservice.data.QualificationGroup;
import de.chiworks.eterminator.eterminservice.data.QualificationQueryResult;
import de.chiworks.eterminator.eterminservice.data.QualificationSubgroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificationService {

    private final TerminService terminService;

    public Map<String, QualificationGroup> queryQualifications() {
        QualificationQueryResult result = terminService.getQualifications();
        return result.getW().entrySet().stream().map(QualificationService::toGroup).collect(Collectors.toMap(QualificationGroup::getName, Function.identity()));
    }

    private static QualificationGroup toGroup(Entry<String, Map<String, Map<String, String>>> entry) {
        String groupName = entry.getKey();
        var subgroupMap = entry.getValue();
        QualificationGroup group = new QualificationGroup(groupName);
        subgroupMap.entrySet().stream().map(QualificationService::toSubgroup).forEach(group::addSubgroup);
        return group;
    }

    private static QualificationSubgroup toSubgroup(Entry<String, Map<String, String>> entry) {
        String subgroupName = entry.getKey();
        var qualificationMap = entry.getValue();
        QualificationSubgroup subgroup = new QualificationSubgroup(subgroupName);
        qualificationMap.entrySet().stream().map(QualificationService::toQualification).forEach(subgroup::addQualification);
        return subgroup;
    }

    private static Qualification toQualification(Entry<String, String> entry) {
        String qualificationId = entry.getKey();
        var qualificationName = entry.getValue();
        return new Qualification(qualificationId, qualificationName);
    }
}
