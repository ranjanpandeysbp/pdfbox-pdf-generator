package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintPackingModuleResp {
    private String tagNo;
    private String trackingUnit;
    private String ec;
    private String aproxNetWeight;
    private String quantity;
    private String desc;
    private String language;
    private String subTotalWeight;
    private String grandTotalWeight;
    private String totalNetWeightApprox;
    private String strModuleRollNumber;
    private String packingModule;
    private String shippingDocId="";
}
