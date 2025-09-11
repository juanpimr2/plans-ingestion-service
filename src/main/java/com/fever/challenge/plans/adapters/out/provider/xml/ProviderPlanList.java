package com.fever.challenge.plans.adapters.out.provider.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "planList")
public class ProviderPlanList {
    @JacksonXmlProperty(localName = "output")
    public ProviderOutput output;
}
