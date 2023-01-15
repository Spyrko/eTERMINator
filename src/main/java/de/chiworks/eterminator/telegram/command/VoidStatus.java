package de.chiworks.eterminator.telegram.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VoidStatus implements CommandStatus {
    public static final VoidStatus VOID_STATUS = new VoidStatus();
}
