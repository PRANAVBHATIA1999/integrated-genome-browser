package org.lorainelab.igb.ucsc.rest.api.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BigWigTypeData {
    int start;
    int end;
    float value;
}
