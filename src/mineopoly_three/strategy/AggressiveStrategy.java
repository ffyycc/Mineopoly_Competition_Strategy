package mineopoly_three.strategy;

import mineopoly_three.game.Economy;
import mineopoly_three.action.TurnAction;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.tiles.TileType;
import mineopoly_three.util.DistanceUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// This is my non-competition strategy, please grade this one.
public class AggressiveStrategy implements MinePlayerStrategy {
    /**
     * Called at the start of every round
     *
     * @param boardSize The length and width of the square game board
     * @param maxInventorySize The maximum number of items that your player can carry at one time
     * @param maxCharge The amount of charge your robot starts with (number of tile moves before needing to recharge)
     * @param winningScore The first player to reach this score wins the round
     * @param startingBoard A view of the GameBoard at the start of the game. You can use this to pre-compute fixed
     *                       information, like the locations of market or recharge tiles
     * @param startTileLocation A Point representing your starting location in (x, y) coordinates
     *                              (0, 0) is the bottom left and (boardSize - 1, boardSize - 1) is the top right
     * @param isRedPlayer True if this strategy is the red player, false otherwise
     * @param random A random number generator, if your strategy needs random numbers you should use this.
     */

    // define types
    private int boardSize;
    private int maxInventorySize;
    private int maxCharge;
    private int winningScore;
    private PlayerBoardView startingBoard;
    private Point startTileLocation;
    private boolean isRedPlayer;
    private Random random;

    private List<Point> redMarketTileList = new ArrayList<>();
    private List<Point> blueMarketTileList = new ArrayList<>();
    private List<Point> rechargeTileList = new ArrayList<>();
    private List<InventoryItem> inventoryList = new ArrayList<>();
    private int myScore;


    /**
     * Called at the start of every round
     *
     * @param boardSize The length and width of the square game board
     * @param maxInventorySize The maximum number of items that your player can carry at one time
     * @param maxCharge The amount of charge your robot starts with (number of tile moves before needing to recharge)
     * @param winningScore The first player to reach this score wins the round
     * @param startingBoard A view of the GameBoard at the start of the game. You can use this to pre-compute fixed
     *                       information, like the locations of market or recharge tiles
     * @param startTileLocation A Point representing your starting location in (x, y) coordinates
     *                              (0, 0) is the bottom left and (boardSize - 1, boardSize - 1) is the top right
     * @param isRedPlayer True if this strategy is the red player, false otherwise
     * @param random A random number generator, if your strategy needs random numbers you should use this.
     */
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

    /**
     * a function to give which action to do next
     * @param myPosition my current location
     * @param aimedPosition the location I want to go
     * @return a aimed action
     */
    public TurnAction takeAction(Point myPosition, Point aimedPosition) {
        if (myPosition.equals(aimedPosition)) {
            return null;
        }

        // compare axis and return action
        if (myPosition.x > aimedPosition.x) {
            return TurnAction.MOVE_LEFT;
        } else if (myPosition.x < aimedPosition.x) {
            return TurnAction.MOVE_RIGHT;
        } else if (myPosition.y > aimedPosition.y) {
            return TurnAction.MOVE_DOWN;
        }
        return TurnAction.MOVE_UP;
    }

    /**
     * Helper function to get the closet point from current location
     * @param curLocation current location
     * @param tileList a list showing all recharge points
     * @return closest point to current location
     */
    public Point getNearestPoint(Point curLocation, List<Point> tileList) {
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


    /**
     * a function to determine whether a tile is resource
     * @param boardView A PlayerBoardView object representing all the information about the board
     * @param i current row place
     * @param j current column place
     * @return whether the current tile is a resource tile
     */
    public boolean isResourceTile(PlayerBoardView boardView, int i, int j) {
        if (boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_DIAMOND ||
                boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_RUBY ||
                boardView.getTileTypeAtLocation(i, j) == TileType.RESOURCE_EMERALD) {
            return true;
        }
        return false;
    }

    /**
     * a function to give price of a certain resource
     * @param boardView A PlayerBoardView object representing all the information about the board
     * @param tempPoint current location
     * @param diamondPrice current diamondPrice
     * @param emeraldPrice current emeraldPrice
     * @param rubyPrice current rubyPrice
     * @return aimedResource price
     */
    public int getResourcePrice(PlayerBoardView boardView, Point tempPoint, int diamondPrice,int emeraldPrice, int rubyPrice) {
        if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_DIAMOND) {
            return diamondPrice;
        } else if (boardView.getTileTypeAtLocation(tempPoint) == TileType.RESOURCE_EMERALD) {
            return emeraldPrice;
        } else {
            return rubyPrice;
        }
    }

    /**
     * This function determines whether to go resource or go market
     * @param bound the items in inventory bag when it wants to go market
     * @return a string presents the next location
     */
    public String chooseMarketOrResource(int bound) {
        if (bound == inventoryList.size()) {
            return "market";
        }
        return "resource";
    }

    /**
     * This function gives us an optimistic resource we want to fetch next
     * @param boardView A PlayerBoardView object representing all the information about the board
     * @param curLocation player current location
     * @param diamondPrice current diamond price
     * @param emeraldPrice current emerald price
     * @param rubyPrice current ruby Price
     * @param boardSize boardSize of the baord
     * @return aimedPoint Resource location you want to go next, if all resources are collected, it just return null
     */
    private Point getExpectedResource(PlayerBoardView boardView, Point curLocation, int diamondPrice, int emeraldPrice, int rubyPrice,int boardSize) {
        Point aimedPoint = null;
        double expValue = 0;
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

    /**
     * strategy if our robot needs to go to charge
     * @param curLocation robot's current location
     * @param chargeLocation robot's location to go to charge
     * @param resourceLocation aimed resource location
     * @param currentCharge current charge value
     * @return next action
     */
    private TurnAction getChargeStrategy(Point curLocation, Point chargeLocation, Point resourceLocation,int currentCharge) {
        TurnAction nextAction =  takeAction(curLocation,chargeLocation);
        if (nextAction == null) {
            if (currentCharge != maxCharge) {
                return null;
            } else {
                if (chooseMarketOrResource(maxInventorySize).equals("market")) {
                    Point nearestMarket = null;
                    if (isRedPlayer) {
                        nearestMarket = getNearestPoint(curLocation,redMarketTileList);
                    } else {
                        nearestMarket = getNearestPoint(curLocation,blueMarketTileList);
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

    /**
     * strategy if our robot needs to go to collect resource or go to market
     * @param boardView A PlayerBoardView object representing all the information about the board
     * @param curLocation current robot location
     * @param resourceLocation aimed resource location
     * @return next action
     */
    private TurnAction getResourceMarketStrategy(PlayerBoardView boardView, Point curLocation, Point resourceLocation) {
        if (chooseMarketOrResource(maxInventorySize).equals("market")) {
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
                Map<Point, List<InventoryItem>> onGroundList = boardView.getItemsOnGround();
                if (onGroundList.get(curLocation) != null && !onGroundList.get(curLocation).isEmpty()) {
                    return TurnAction.PICK_UP_RESOURCE;
                } else {
                    return TurnAction.MINE;
                }
            }
        }
    }

    /**
     * The main part of your strategy, this method returns what action your player should do on this turn
     *
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                   that your strategy is allowed to access
     * @param economy The GameEngine's economy object which holds current prices for resources
     * @param currentCharge The amount of charge your robot has (number of tile moves before needing to recharge)
     * @param isRedTurn For use when two players attempt to move to the same spot on the same turn
     *                   If true: The red player will move to the spot, and the blue player will do nothing
     *                   If false: The blue player will move to the spot, and the red player will do nothing
     * @return The TurnAction enum for the action that this strategy wants to perform on this game turn
     */
    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        Point curLocation = boardView.getYourLocation();
        Map<ItemType, Integer> resourcePriceList = economy.getCurrentPrices();

        // get three resources price
        int diamondPrice = resourcePriceList.get(ItemType.DIAMOND);
        int emeraldPrice = resourcePriceList.get(ItemType.EMERALD);
        int rubyPrice = resourcePriceList.get(ItemType.RUBY);

        Point chargeLocation = getNearestPoint(curLocation,rechargeTileList);
        Point resourceLocation = getExpectedResource(boardView,curLocation,diamondPrice,emeraldPrice,rubyPrice,boardSize);

        // if we have less than 1/5*maxCharge or our current location is the same as charge location, we choose to charge until full.
        if (currentCharge <= 1.0/8*maxCharge || curLocation.equals(chargeLocation)) {
            return getChargeStrategy(curLocation, chargeLocation, resourceLocation,currentCharge);
        }

        // not charge action
        return getResourceMarketStrategy(boardView, curLocation, resourceLocation);
    }

    /**
     * Called when the player receives an item from performing a TurnAction that gives an item.
     * At the moment this is only from using PICK_UP on top of a mined resource
     *
     * @param itemReceived The item received from the player's TurnAction on their last turn
     */
    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        inventoryList.add(itemReceived);
    }

    /**
     * Called when the player steps on a market tile with items to sell. Tells your strategy how much all
     *  of the items sold for.
     *
     * @param totalSellPrice The combined sell price for all items in your strategy's inventory
     */
    @Override
    public void onSoldInventory(int totalSellPrice) {
        inventoryList.clear();
        myScore += totalSellPrice;
    }

    /**
     * Gets the name of this strategy. The amount of characters that can actually be displayed on a screen varies,
     *  although by default at screen size 750 it's about 16-20 characters depending on character size
     *
     * @return The name of your strategy for use in the competition and rendering the scoreboard on the GUI
     */
    @Override
    public String getName() {
        return "Aggressive Strategy";
    }

    /**
     * Called at the end of every round to let players reset, and tell them how they did if the strategy does not
     *  track that for itself
     *
     * @param pointsScored The total number of points this strategy scored
     * @param opponentPointsScored The total number of points the opponent's strategy scored
     */
    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {
        // reset properties at the end of game
        this.myScore = 0;
        redMarketTileList.clear();
        blueMarketTileList.clear();
        rechargeTileList.clear();
        inventoryList.clear();
    }

}
