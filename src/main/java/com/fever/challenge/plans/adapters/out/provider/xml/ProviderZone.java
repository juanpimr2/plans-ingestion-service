package com.fever.challenge.plans.adapters.out.provider.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderZone {
    @JacksonXmlProperty(isAttribute = true, localName = "zone_id")
    public String zoneId;

    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JacksonXmlProperty(isAttribute = true)
    public String price;

    @JacksonXmlProperty(isAttribute = true)
    public String capacity;

    @JacksonXmlProperty(isAttribute = true)
    public String numbered;
}
