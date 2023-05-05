package app.domain.services;

import app.domain.models.ArmyUnit.ArmyUnitType;
import app.domain.models.Card.BaseCard;
import app.domain.models.Card.CardType;
import app.domain.models.Card.TerritoryCard;
import app.domain.models.GameMap.Territory;
import app.domain.models.Player.Player;
import app.domain.models.ArmyUnit.Army;
import app.domain.services.Map.MapReadService;
import app.domain.services.Map.MapService;

import java.util.ArrayList;
import java.util.Random;

public class PlayerService {

    private static ArrayList<Player> players = new ArrayList<>();
    private static MapService mapService;
    private static MapReadService mapReadService;
    private static int playerCount;
    private static final int UPPER_BOUND = 6;

    public static void createPlayer(ArrayList<String> names) {
        for (int i = 0; i < names.size(); i++) {
            Player newPlayer = new Player(names.get(i), i + 1);
            players.add(newPlayer);
            System.out.println(
                    "New Player is created successfully with id: " + newPlayer.getId() + " and name: "
                            + newPlayer.getUsername());

        }

        playerCount = names.size();
    }

    public static Player getPlayer(int playerId) {
        for (Player p : players) {
            if (p.getId().equals(playerId)) {
                return p;
            }
        }
        return null;
    }

    public boolean attack(int attackingPlayerId, int attackedPlayerId, int attackerTerritoryId, int attackedTerritoryId) {

        Territory attackerTerritory = getPlayer(attackingPlayerId).getTerritory(attackerTerritoryId);
        Territory attackedTerritory = getPlayer(attackedPlayerId).getTerritory(attackedTerritoryId);

        int attackerDiceRoll = rollDice();
        int attackedDiceRoll = rollDice();

        if (attackerDiceRoll > attackedDiceRoll) {
            dealArmyAttackerWin(attackedTerritory);
        } else {
            dealArmyDefenderWin(attackerTerritory);
        }

        if (attackedTerritory.getTerritoryArmy().getTotalArmyAmount() <= 0) {

            if (attackerTerritory.getTerritoryArmy().getArmyAmount(ArmyUnitType.Infantry) > 0) {
                attackerTerritory.getTerritoryArmy().transferArmyUnits(attackedTerritory.getTerritoryArmy(),
                        ArmyUnitType.Infantry, 1);
            } else if (attackerTerritory.getTerritoryArmy().getArmyAmount(ArmyUnitType.Chivalry) > 0) {
                attackerTerritory.getTerritoryArmy().transferArmyUnits(attackedTerritory.getTerritoryArmy(),
                        ArmyUnitType.Chivalry, 1);
            } else if (attackedTerritory.getTerritoryArmy().getArmyAmount(ArmyUnitType.Artillery) > 0) {
                attackerTerritory.getTerritoryArmy().transferArmyUnits(attackedTerritory.getTerritoryArmy(),
                        ArmyUnitType.Artillery, 1);
            }

            getPlayer(attackingPlayerId).addTerritory(getPlayer(attackedPlayerId).removeTerritory(attackerTerritoryId));
            return true;
        }

        return false;
    }

    public static int rollDice() {
        Random rand = new Random();
        return rand.nextInt(UPPER_BOUND);
    }

    public void dealArmyAttackerWin(Territory loserTerritory) {

        Army loserArmy = loserTerritory.getTerritoryArmy();

        if (loserArmy.getArmyAmount(ArmyUnitType.Artillery) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Artillery, 1);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Chivalry) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Chivalry, 1);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Infantry) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Infantry, 1);
        }
    }

    public void dealArmyDefenderWin(Territory loserTerritory) {

        Army loserArmy = loserTerritory.getTerritoryArmy();

        if (loserArmy.getArmyAmount(ArmyUnitType.Artillery) > 1) {
            loserArmy.getArmyUnits(ArmyUnitType.Artillery, 2);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Artillery) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Artillery, 1);
            loserArmy.getArmyUnits(ArmyUnitType.Chivalry, 1);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Chivalry) > 1) {
            loserArmy.getArmyUnits(ArmyUnitType.Chivalry, 2);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Chivalry) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Chivalry, 1);
            loserArmy.getArmyUnits(ArmyUnitType.Infantry, 1);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Infantry) > 1) {
            loserArmy.getArmyUnits(ArmyUnitType.Infantry, 2);
        } else if (loserArmy.getArmyAmount(ArmyUnitType.Infantry) > 0) {
            loserArmy.getArmyUnits(ArmyUnitType.Infantry, 1);
        }
    }

    public boolean tradeTerritoryCards(String continentName, int playerId){
        if (checkIfTerritoryCardsTradable(continentName, playerId)){
            Player tradingPlayer = getPlayer(playerId);
            Player opponentPlayer;
            ArrayList<Territory> continentTerritories = (ArrayList<Territory>) mapService.getTerritoriesOfContinent(continentName);
            TerritoryCard tc;

            for (Territory t: continentTerritories){
                if (t.getOwnerId() != playerId){
                    opponentPlayer = getPlayer(t.getOwnerId());
                    opponentPlayer.removeTerritory(t.getTerritoryId());
                    tradingPlayer.addTerritory(t);
                    t.setOwnerId(tradingPlayer.getId());
                }
                for (BaseCard c: tradingPlayer.getPlayerDeck().getCardContainer().get(CardType.Territory)){
                    tc = (TerritoryCard) c;
                    if (t.getTerritoryId() == tc.getTerritoryId()){
                        tradingPlayer.getPlayerDeck().drawTerritoryCard(tc.getTerritoryId());
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkIfTerritoryCardsTradable(String continentName, int playerId){
        ArrayList<Territory> continentTerritoryList = (ArrayList<Territory>) mapService.getTerritoriesOfContinent(continentName);
        ArrayList<Territory> playerTerritoryCardTerritoryList = getTerritoriesFromTerritoryCards(playerId);

        return playerTerritoryCardTerritoryList.containsAll(continentTerritoryList);
    }

    private ArrayList<Territory> getTerritoriesFromTerritoryCards(int playerId){
        Player p = getPlayer(playerId);
        ArrayList<Territory> territoryCardTerritories = null;

        for (BaseCard t: p.getPlayerDeck().getCardContainer().get(CardType.Territory)){
            TerritoryCard tt = (TerritoryCard) t;
            territoryCardTerritories.add(mapService.findTerritory(tt.getTerritoryId()));
        }

        return territoryCardTerritories;
    }

    public boolean tradeArmyCards(int infantryCardAmount, int cavalryCardAmount, int artilleryCardAmount, int playerId,
            int territoryId) {

        Player tradingPlayer = getPlayer(playerId);

        if (!this.checkIfArmyCardTradable(infantryCardAmount, cavalryCardAmount, artilleryCardAmount, playerId)) {
            if (infantryCardAmount == 3 && cavalryCardAmount == 0 && artilleryCardAmount == 0) {
                tradingPlayer.getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Chivalry, 1);
            } else if (infantryCardAmount == 2 && cavalryCardAmount == 1 && artilleryCardAmount == 0) {
                getPlayer(playerId).getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Chivalry, 2);
            } else if (infantryCardAmount == 2 && cavalryCardAmount == 0 && artilleryCardAmount == 1) {
                getPlayer(playerId).getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Artillery,
                        2);
            } else if (infantryCardAmount == 1 && cavalryCardAmount == 2 && artilleryCardAmount == 0) {
                getPlayer(playerId).getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Chivalry, 1);
                getPlayer(playerId).getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Artillery,
                        1);
            } else if (infantryCardAmount == 0 && cavalryCardAmount == 2 && artilleryCardAmount == 1) {
                getPlayer(playerId).getTerritory(territoryId).getTerritoryArmy().addArmyUnits(ArmyUnitType.Artillery,
                        3);
            }

            for (int i = 0; i < infantryCardAmount; i++){
                tradingPlayer.getPlayerDeck().drawCard(CardType.Infantry);
            }
            for (int i = 0; i < cavalryCardAmount; i++){
                tradingPlayer.getPlayerDeck().drawCard(CardType.Cavalry);
            }
            for (int i = 0; i < artilleryCardAmount; i++){
                tradingPlayer.getPlayerDeck().drawCard(CardType.Artillery);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean checkIfArmyCardTradable(int infantryCardAmount, int cavalryCardAmount, int artilleryCardAmount,
            int playerId) {

        Player tradingPlayer = getPlayer(playerId);

        return tradingPlayer.getPlayerDeck().findCardAmount(CardType.Infantry) >= infantryCardAmount &&
                tradingPlayer.getPlayerDeck().findCardAmount(CardType.Cavalry) >= cavalryCardAmount &&
                tradingPlayer.getPlayerDeck().findCardAmount(CardType.Artillery) >= artilleryCardAmount;
    }
}
