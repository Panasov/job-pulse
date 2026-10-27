package com.jobpulse.processing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramResponse {
    private boolean ok;
    private List<TelegramUpdate> result;

    public boolean isOk() { return ok; }
    public List<TelegramUpdate> getResult() { return result; }
}