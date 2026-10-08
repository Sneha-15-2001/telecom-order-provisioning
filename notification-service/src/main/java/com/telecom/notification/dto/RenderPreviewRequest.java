package com.telecom.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Preview of a rendered template (no state change)")
public record RenderPreviewRequest(
    @Schema(example = "ORDER_CREATED") String templateCode,
    Map<String, String> variables) {}
