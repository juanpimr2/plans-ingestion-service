package com.fever.challenge.plans.adapters.out.provider.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public class ProviderOutput {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "base_plan")
    public List<ProviderBasePlan> basePlans;
}
