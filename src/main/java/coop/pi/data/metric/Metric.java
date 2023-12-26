package coop.pi.data.metric;

import lombok.Data;

@Data
public class Metric {
    private long dt;
    private String coopId;
    private String componentId;
    private String metric;
    private Long value;
}
