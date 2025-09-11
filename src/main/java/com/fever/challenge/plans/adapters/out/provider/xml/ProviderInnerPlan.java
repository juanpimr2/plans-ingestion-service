package com.fever.challenge.plans.adapters.out.provider.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public class ProviderInnerPlan {
    @JacksonXmlProperty(isAttribute = true, localName = "plan_start_date")
    public String planStartDate;

    @JacksonXmlProperty(isAttribute = true, localName = "plan_end_date")
    public String planEndDate;

    @JacksonXmlProperty(isAttribute = true, localName = "plan_id")
    public String planId;

    @JacksonXmlProperty(isAttribute = true, localName = "sell_from")
    public String sellFrom;

    @JacksonXmlProperty(isAttribute = true, localName = "sell_to")
    public String sellTo;

    @JacksonXmlProperty(isAttribute = true, localName = "sold_out")
    public String soldOut;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "zone")
    public List<ProviderZone> zones;
}
