package app.ui.controllers.game.map;

import java.io.IOException;
import java.util.List;

import app.common.Logger;
import app.domain.models.GameMap.Territory;
import app.domain.services.Map.MapService;
import app.domain.services.base.ISubscriber;
import app.ui.views.game.map.MapPanel;

public class MapPanelController implements ISubscriber<Territory> {

    private MapService _mapService;
    private MapPanel _mapPanel;

    public MapPanelController(MapPanel mapPanel, MapService mapService) {
        this._mapPanel = mapPanel;
        this._mapService = mapService;
    }

    public void displayMap() {
        List<Territory> territoryList = _mapService.getTerritoryListFromGraph();
        territoryList.forEach((territory) -> {
            TerritoryComponentController trController;
            try {
                trController = new TerritoryComponentController(territory);
                _mapPanel.drawTerriotry(trController.getTerritoryComponent());
                trController.addSubscriber(this);
            } catch (IOException e) {
                Logger.error(e);
            }
        });
    }

    public void loadMap() {
        _mapService.loadGameMapDataToGraph();
    }

    @Override
    public void update(Territory message) {
        _mapPanel.updateMapInfo(message.getName(), message.getInfantryCount(), message.getChivalryCount(),
                message.getArtilleryCount());
    }
}
