package com.resourcebooking.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlotGenerateResponse {

    private int generated;
    private int skipped;
}