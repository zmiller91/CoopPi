package coop.pi.data.config;

import lombok.Data;

@Data
public class IotState {
    private CoopConfig desired;
    private CoopConfig reported;
}
