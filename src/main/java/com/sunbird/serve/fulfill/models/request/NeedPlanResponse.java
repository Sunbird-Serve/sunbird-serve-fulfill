package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.Need.NeedPlan;
import com.sunbird.serve.fulfill.models.Need.Occurrence;
import com.sunbird.serve.fulfill.models.Need.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeedPlanResponse {
    private NeedPlan plan;
    private Occurrence occurrence;
    private List<TimeSlot> timeSlots;
}
