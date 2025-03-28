package com.example.timesheet.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private ProjectDTO project;
    private List<PurchaseOrderDTO> purchaseOrders;
    private List<ResourceCostDTO> resources;

    public void setResources(List<ResourceDTO> resourceDTOs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
