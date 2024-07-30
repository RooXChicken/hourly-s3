package com.rooxchicken.hourly.Tasks;

import com.rooxchicken.hourly.Hourly;

public class TemplateTask extends Task
{
    public TemplateTask(Hourly _plugin)
    {
        super(_plugin);
        tickThreshold = 4;
    }

    @Override
    public void run()
    {
        
    }
}
