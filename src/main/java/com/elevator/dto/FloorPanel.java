package com.elevator.dto;

import com.elevator.dto.buttons.FloorButton;
import lombok.Data;

import java.util.List;

@Data
public class FloorPanel {

    public FloorPanel(List<FloorButton> floorButtons, PanelType panelType) {
        this.floorButtons = floorButtons;
        this.panelType = panelType;
    }

    private List<FloorButton> floorButtons;
    private boolean work;
    private PanelType panelType;
}
