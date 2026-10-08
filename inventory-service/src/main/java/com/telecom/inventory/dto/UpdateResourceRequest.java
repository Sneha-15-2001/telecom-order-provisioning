package com.telecom.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Editable resource fields (details only; status changes via lifecycle APIs)")
public record UpdateResourceRequest(@Size(max = 1000) String details) {}
