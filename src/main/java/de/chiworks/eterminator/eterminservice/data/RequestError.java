package de.chiworks.eterminator.eterminservice.data;

import java.util.List;

public record RequestError(String referenceId, List<Error> errors, String kv) {
    public record Error(String code, String text) {
    }

}
