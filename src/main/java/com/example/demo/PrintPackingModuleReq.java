package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrintPackingModuleReq {
    private String dealerRole;

    private String shippingType;

    private String packingModuleId;

    private String packingModuleType;

    private String dealerCode;

    private String countryCode;

    private String language;

    private String shippingDocId="";
}
