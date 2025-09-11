package com.fever.challenge.plans.adapters.out.provider.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ProviderBasePlan {
    @JacksonXmlProperty(isAttribute = true, localName = "base_plan_id")
    public String basePlanId;

    @JacksonXmlProperty(isAttribute = true, localName = "sell_mode")
    public String sellMode;

    @JacksonXmlProperty(isAttribute = true, localName = "title")
    public String title;

    @JacksonXmlProperty(localName = "plan")
    public ProviderInnerPlan plan;
}

