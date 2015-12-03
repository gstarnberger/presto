package com.wrmsr.presto.config;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public final class PluginsConfigNode
    extends StringListConfigNode
{
    @JsonCreator
    public PluginsConfigNode(List<String> items)
    {
        super(items);
    }
}
