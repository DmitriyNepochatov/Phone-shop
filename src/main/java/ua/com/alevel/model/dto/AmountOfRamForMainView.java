package ua.com.alevel.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class AmountOfRamForMainView {
    private int amountOfRam;
    private boolean enabled;
}