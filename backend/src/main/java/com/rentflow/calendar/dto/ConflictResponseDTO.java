package com.rentflow.calendar.dto;

import java.util.ArrayList;
import java.util.List;

public class ConflictResponseDTO {
    private boolean valid = true;
    private boolean hasConflict = false;
    private List<ConflictDTO> hardConflicts = new ArrayList<>();
    private List<ConflictDTO> warnings = new ArrayList<>();

    public ConflictResponseDTO() {}

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public boolean isHasConflict() { return hasConflict; }
    public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }

    public List<ConflictDTO> getHardConflicts() { return hardConflicts; }
    public void setHardConflicts(List<ConflictDTO> hardConflicts) { this.hardConflicts = hardConflicts; }

    public List<ConflictDTO> getWarnings() { return warnings; }
    public void setWarnings(List<ConflictDTO> warnings) { this.warnings = warnings; }

    public void addHardConflict(ConflictDTO conflict) {
        if (this.hardConflicts == null) this.hardConflicts = new ArrayList<>();
        this.hardConflicts.add(conflict);
        this.valid = false;
        this.hasConflict = true;
    }

    public void addWarning(ConflictDTO warning) {
        if (this.warnings == null) this.warnings = new ArrayList<>();
        this.warnings.add(warning);
        this.hasConflict = true;
    }
}
