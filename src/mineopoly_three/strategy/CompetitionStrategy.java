package mineopoly_three.competition;

import mineopoly_three.game.Economy;
import mineopoly_three.action.TurnAction;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.strategy.MinePlayerStrategy;
import mineopoly_three.strategy.PlayerBoardView;
import mineopoly_three.tiles.TileType;
import mineopoly_three.util.DistanceUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DisgustingStrategyPlus implements MinePlayerStrategy {
    public DisgustingStrategyPlus() {
    }

    private int boardSize;
    private int maxInventorySize;
    private int maxCharge;
    private int winningScore;
    private PlayerBoardView startingBoard;
    private Point startTileLocation;
    private boolean isRedPlayer;
    private Random random;

    private java.util.List<Point> redMarketTileList = new ArrayList<>();
    private java.util.List<Point> blueMarketTileList = new ArrayList<>();
    private java.util.List<Point> rechargeTileList = new ArrayList<>();
    private java.util.List<InventoryItem> inventoryList = new ArrayList<>();
    private int myScore;

    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.boardSize = boardSize;
        this.maxInventorySize = maxInventorySize;
        this.maxCharge = maxCharge;
        this.winningScore = winningScore;
        this.startingBoard = startingBoard;
        this.startTileLocation = startTileLocation;
        this.isRedPlayer = isRedPlayer;
        this.random = random;

        // add points to our list for fixed position
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Point tempPoint = new Point(i,j);
                if (startingBoard.getTileTypeAtLocation(tempPoint) == TileType.RED_MARKET) {
                    redMarketTileList.add(tempPoint);
                } else if (startingBoard.getTileTypeAtLocation(tempPoint) == TileType.BLUE_MARKET) {
                    blueMarketTileList.add(tempPoint);
                } else if (startingBoard.getTileTypeAtLocation(tempPoint) == TileType.RECHARGE) {
                    rechargeTileList.add(tempPoint);
                }
            }
        }

    }

    private TurnAction takeAction(Point myPosition, Point aimedPosition) {
        if (myPosition.equals(aimedPosition)) {
            return null;
        }

        if (myPosition.x > aimedPosition.x) {
            return TurnAction.MOVE_LEFT;
        } else if (myPosition.x < aimedPosition.x) {
            return TurnAction.MOVE_RIGHT;
        } else if (myPosition.y > aimedPosition.y) {
            return TurnAction.MOVE_DOWN;
        }
        return TurnAction.MOVE_UP;
    }

    private Point getNearestPoint(Point curLocation, java.util.List<Point> tileList) {
        int numPoint = tileList.size();
        double shortestDist = Double.POSITIVE_INFINITY;
        Point aimedPoint = null;
        for (int i = 0; i < numPoint; i++) {
            double tempDist = DistanceUtil.getManhattanDistance(curLocation,tileList.get(i));

            // compare distance, take the min distance
            if (tempDist < shortestDist) {
                aimedPoint = tileList.get(i);
                shortestDist = tempDist;
            }
        }
        return aimedPoint;
    }

    private boolean isResourceTile(PlayerBoardView boardView, int i, int j) {
        if (boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_DIAMOND ||
                boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_RUBY ||
                boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_EMERALD) {
            return true;
        }
        return false;
    }

    private int getResourcePrice(PlayerBoardView boardView, Point tempPoint, int diamondPrice,int emeraldPrice, int rubyPrice) {
        if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_DIAMOND) {
            return diamondPrice;
        } else if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_EMERALD) {
            return emeraldPrice;
        } else {
            return rubyPrice;
        }
    }

    private String chooseMarketOrResource(int bound) {
        if (bound == inventoryList.size()) {
            return "market";
        }
        return "resource";
    }

    private Point getExpectedResource(PlayerBoardView boardView, Point curLocation, int diamondPrice, int emeraldPrice, int rubyPrice,int boardSize) {
        Point aimedPoint = null;
        double expValue = 0;
        Map<Point, java.util.List<InventoryItem>> onGroundList = boardView.getItemsOnGround();

        if (onGroundList.get(curLocation) != null && !onGroundList.get(curLocation).isEmpty()) {
            return curLocation;
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (!isResourceTile(boardView,i,j)) {
                    continue;
                }

                Point tempPoint = new Point(i,j);

                int curPrice = getResourcePrice(boardView, tempPoint,diamondPrice,emeraldPrice,rubyPrice);
                int curDist = DistanceUtil.getManhattanDistance(tempPoint,curLocation);

                // calculate the expected value for a expected route
                double curExpValue = 0;
                if (onGroundList.get(tempPoint) != null && !onGroundList.get(tempPoint).isEmpty()) {
                    curExpValue = curPrice/(curDist*2);
                } else {
                    if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_DIAMOND) {
                        curExpValue = curPrice/(curDist*2+3);
                    } else if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_EMERALD) {
                        curExpValue = curPrice/(curDist*2+2);
                    } else {
                        curExpValue = curPrice/(curDist*2+1);
                    }
                }

                // chase the point has the max expected value
                if (curExpValue > expValue) {
                    expValue = curExpValue;
                    aimedPoint = tempPoint;
                }

            }
        }
        return aimedPoint;
    }

    private Point getNearestMarket(PlayerBoardView boardView,Point curLocation, java.util.List<Point> tileList) {
        int numRechargePoint = tileList.size();
        double shortestDist = Double.POSITIVE_INFINITY;
        Point aimedPoint = null;
        for (int i = 0; i < numRechargePoint; i++) {
            double tempDist = DistanceUtil.getManhattanDistance(curLocation,tileList.get(i));

            // compare distance, take the min distance
            if (tempDist < shortestDist && !boardView.getOtherPlayerLocation().equals(tileList.get(i))) {
                aimedPoint = tileList.get(i);
                shortestDist = tempDist;
            }
        }
        return aimedPoint;
    }

    private TurnAction getChargeStrategy(PlayerBoardView boardView,Point curLocation, Point chargeLocation, Point resourceLocation,int currentCharge) {
        TurnAction nextAction =  takeAction(curLocation,chargeLocation);
        if (nextAction == null) {
            if (currentCharge != maxCharge) {
                return null;
            } else {
                if (chooseMarketOrResource(maxInventorySize).equals("market")) {
                    Point nearestMarket = null;
                    if (isRedPlayer) {
                        nearestMarket = getNearestMarket(boardView,curLocation,redMarketTileList);
                    } else {
                        nearestMarket = getNearestMarket(boardView,curLocation,blueMarketTileList);
                    }
                    return takeAction(curLocation,nearestMarket);
                } else {
                    return takeAction(curLocation,resourceLocation);
                }
            }
        } else {
            return nextAction;
        }
    }

    private TurnAction getResourceMarketStrategy(PlayerBoardView boardView, Point curLocation, Point resourceLocation) {
        if (chooseMarketOrResource(maxInventorySize).equals("market")) {
            // go to market
            Point nearestMarket = null;
            if (isRedPlayer) {
                nearestMarket = getNearestMarket(boardView,curLocation,redMarketTileList);
            } else {
                nearestMarket = getNearestMarket(boardView,curLocation,blueMarketTileList);
            }
            return takeAction(curLocation,nearestMarket);
        } else {
            // go to resource
            if (!curLocation.equals(resourceLocation)) {
                return takeAction(curLocation,resourceLocation);
            } else {
                // dig or get item
                Map<Point, java.util.List<InventoryItem>> onGroundList = boardView.getItemsOnGround();
                if (onGroundList.get(curLocation) != null && !onGroundList.get(curLocation).isEmpty()) {
                    return TurnAction.PICK_UP_RESOURCE;
                } else {
                    return TurnAction.MINE;
                }
            }
        }
    }

    private TurnAction goToOtherPlayerMarket(PlayerBoardView boardView,Point curMyLocation, Point curOtherLocation) {
        if (isRedPlayer) {
            Point marketLocation = getNearestMarket(boardView,curOtherLocation,blueMarketTileList);
            return takeAction(curMyLocation,marketLocation);
        } else {
            Point marketLocation = getNearestMarket(boardView,curOtherLocation,redMarketTileList);
            return takeAction(curMyLocation,marketLocation);
        }
    }

    private TurnAction goWhatEverFirst(PlayerBoardView boardView, Point curLocation, Point resourceLocation) {
        if (chooseMarketOrResource(1).equals("market")) {
            // go to market
            Point nearestMarket = null;
            if (isRedPlayer) {
                nearestMarket = getNearestPoint(curLocation,redMarketTileList);
            } else {
                nearestMarket = getNearestPoint(curLocation,blueMarketTileList);
            }
            return takeAction(curLocation,nearestMarket);
        } else {
            // go to resource
            if (!curLocation.equals(resourceLocation)) {
                return takeAction(curLocation,resourceLocation);
            } else {
                // dig or get item
                Map<Point, java.util.List<InventoryItem>> onGroundList = boardView.getItemsOnGround();
                if (onGroundList.get(curLocation) != null && !onGroundList.get(curLocation).isEmpty()) {
                    return TurnAction.PICK_UP_RESOURCE;
                } else {
                    return TurnAction.MINE;
                }
            }
        }
    }

    Point getEasyResource(PlayerBoardView boardView, Point curLocation, int diamondPrice, int emeraldPrice, int rubyPrice,int boardSize) {
        Point aimedPoint = null;
        int shortestPath = 9999;
        Map<Point, List<InventoryItem>> onGroundList = boardView.getItemsOnGround();

        if (onGroundList.get(curLocation) != null && !onGroundList.get(curLocation).isEmpty()) {
            return curLocation;
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (!isResourceTile(boardView,i,j)) {
                    continue;
                }

                Point tempPoint = new Point(i,j);

                int curDist = DistanceUtil.getManhattanDistance(tempPoint,curLocation);
                int totalDist = 9999;
                // calculate the expected value for a expected route
                if (onGroundList.get(tempPoint) != null && !onGroundList.get(tempPoint).isEmpty()) {
                    totalDist = curDist*2;
                } else {
                    if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_DIAMOND) {
                        totalDist = curDist*2+3;
                    } else if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_EMERALD) {
                        totalDist = curDist*2+2;
                    } else {
                        totalDist = curDist*2+1;
                    }
                }

                // chase the point has the max expected value
                if (totalDist < shortestPath) {
                    shortestPath = totalDist;
                    aimedPoint = tempPoint;
                }

            }
        }
        return aimedPoint;
    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        Point curLocation = boardView.getYourLocation();
        Map<ItemType, Integer> resourcePriceList = economy.getCurrentPrices();
        // get three resources price
        int diamondPrice = resourcePriceList.get(ItemType.DIAMOND);
        int emeraldPrice = resourcePriceList.get(ItemType.EMERALD);
        int rubyPrice = resourcePriceList.get(ItemType.RUBY);

        Point chargeLocation = getNearestPoint(curLocation,rechargeTileList);

        Point nearestEnemyMarket = null;
        if (myScore > boardView.getOtherPlayerScore()) {
            if (isRedPlayer) {
                nearestEnemyMarket = getNearestMarket(boardView, boardView.getOtherPlayerLocation(), blueMarketTileList);
            } else {
                nearestEnemyMarket = getNearestMarket(boardView, boardView.getOtherPlayerLocation(), redMarketTileList);
            }
            if (DistanceUtil.getManhattanDistance(nearestEnemyMarket, boardView.getOtherPlayerLocation()) > 2) {
                if (curLocation.equals(nearestEnemyMarket) && currentCharge > 1.0/4*maxCharge) {
                    return TurnAction.MOVE_UP;
                } else if (curLocation.x == nearestEnemyMarket.x && curLocation.y == nearestEnemyMarket.y+1) {
                    return null;
                }
            } else {
                return takeAction(curLocation,nearestEnemyMarket);
            }
        }

        if (myScore == 0) {
            Point easyResourceLocation = getEasyResource(boardView,curLocation,diamondPrice,emeraldPrice,rubyPrice,boardSize);
            return goWhatEverFirst(boardView, curLocation, easyResourceLocation);
        }

        Point resourceLocation = getExpectedResource(boardView,curLocation,diamondPrice,emeraldPrice,rubyPrice,boardSize);
        if (boardView.getOtherPlayerScore() < myScore) {
            return goToOtherPlayerMarket(boardView,curLocation,boardView.getOtherPlayerLocation());
        }

        // if we have less than 1/5*maxCharge or our current location is the same as charge location, we choose to charge until full.
        if (currentCharge <= 1.0/8*maxCharge || curLocation.equals(chargeLocation)) {
            return getChargeStrategy(boardView,curLocation, chargeLocation, resourceLocation,currentCharge);
        }

        // not charge action
        return getResourceMarketStrategy(boardView, curLocation, resourceLocation);
    }

    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        inventoryList.add(itemReceived);
    }

    @Override
    public void onSoldInventory(int totalSellPrice) {
        inventoryList.clear();
        myScore += totalSellPrice;
    }

    @Override
    public String getName() {
        return "Feng Shi Strategy+";
    }

    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {
        this.myScore = 0;
        redMarketTileList.clear();
        blueMarketTileList.clear();
        rechargeTileList.clear();
        inventoryList.clear();
    }
}