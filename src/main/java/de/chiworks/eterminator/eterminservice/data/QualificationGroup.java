package de.chiworks.eterminator.eterminservice.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class QualificationGroup {

    @Getter
    private final String name;

    private final Map<String, QualificationSubgroup> subgroups = new HashMap<>();

    public QualificationSubgroup getSubgroup(String subgroupName) {
        return subgroups.get(subgroupName);
    }

    public QualificationSubgroup addSubgroup(QualificationSubgroup subgroup) {
        subgroup.setGroup(this);
        return subgroups.put(subgroup.getName(), subgroup);
    }

    public int size() {
        return subgroups.size();
    }

    public Set<String> getSubgroupNames() {
        return subgroups.keySet();
    }

    public List<QualificationSubgroup> getSubgroups() {
        return subgroups.values().stream().toList();
    }

    public boolean hasSubgroup(String subGroupName) {
        return subgroups.containsKey(subGroupName);
    }
}
