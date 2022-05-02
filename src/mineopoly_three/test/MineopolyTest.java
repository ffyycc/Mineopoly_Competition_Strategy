package mineopoly_three.test;

import mineopoly_three.strategy.AggressiveStrategy;
import mineopoly_three.strategy.MinePlayerStrategy;
import mineopoly_three.strategy.PlayerBoardView;
import org.junit.Before;
import org.junit.Test;

import mineopoly_three.game.Economy;
import mineopoly_three.action.TurnAction;
import mineopoly_three.item.InventoryItem;
import mineopoly_three.item.ItemType;
import mineopoly_three.tiles.TileType;
import mineopoly_three.util.DistanceUtil;
import mineopoly_three.strategy.AggressiveStrategy;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MineopolyTest {
    private TileType[][] testMap;
    private PlayerBoardView startingBoard;
    private AggressiveStrategy curStrategy;
    private int boardSize = 4;
    private int maxCharge = 80;
    private int winningScore = 20;
    private boolean isRedPlayer = true;
    private Random random;
    private Economy economy;
    private ItemType[] resourceTypes;

    @Before
    public void setUp() {
        // This is run before every test
        testMap = new TileType[][] {
                {TileType.RESOURCE_DIAMOND,TileType.RESOURCE_EMERALD,TileType.RESOURCE_RUBY,TileType.EMPTY},
                {TileType.RED_MARKET,TileType.RECHARGE,TileType.EMPTY,TileType.EMPTY},
                {TileType.EMPTY,TileType.EMPTY, TileType.RECHARGE,TileType.BLUE_MARKET},
                {TileType.RESOURCE_DIAMOND,TileType.EMPTY,TileType.RESOURCE_RUBY,TileType.RESOURCE_EMERALD},
        };

        Point myLocation = new Point (0,2);
        Point otherLocation = new Point(3,1);
        startingBoard = new PlayerBoardView(testMap,null,myLocation,otherLocation,0);
        resourceTypes = new ItemType[] {ItemType.DIAMOND,ItemType.EMERALD,ItemType.RUBY,ItemType.AUTOMINER};
        economy = new Economy(resourceTypes);
        curStrategy = new AggressiveStrategy();
    }

    // function takeAction() test
    @Test
    public void takeActionLeft() {
        assertEquals(TurnAction.MOVE_LEFT,curStrategy.takeAction(new Point(1,2),new Point(0,2)));
    }

    @Test
    public void testTakeActionRight() {
        assertEquals(TurnAction.MOVE_RIGHT,curStrategy.takeAction(new Point(1,2),new Point(2,2)));
    }

    @Test
    public void testTakeActionUp() {
        assertEquals(TurnAction.MOVE_UP,curStrategy.takeAction(new Point(2,2),new Point(2,3)));
    }

    @Test
    public void testTakeActionDown() {
        assertEquals(TurnAction.MOVE_DOWN,curStrategy.takeAction(new Point(2,2),new Point(2,1)));
    }

    @Test
    public void testTakeActionNull() {
        assertEquals(null,curStrategy.takeAction(new Point(2,2),new Point(2,2)));
    }

    // function getNearestPoint() test
    @Test
    public void testGetNearestPointYaxis() {
        List<Point> testPointsList =  new ArrayList<>();
        testPointsList.add(new Point(1,1));
        testPointsList.add(new Point(0,1));

        assertEquals(new Point(0,1),curStrategy.getNearestPoint(new Point (0,0),testPointsList));
    }

    @Test
    public void testGetNearestPointXaxis() {
        List<Point> testPointsList =  new ArrayList<>();
        testPointsList.add(new Point(1,1));
        testPointsList.add(new Point(1,0));

        assertEquals(new Point(1,0),curStrategy.getNearestPoint(new Point (0,0),testPointsList));
    }

    // test whether a tile is resource
    @Test
    public void testIsResourceTileTrueDiamond() {
        assertEquals(true,curStrategy.isResourceTile(startingBoard,0,0));
    }

    @Test
    public void testIsResourceTileTrueEmerald() {
        assertEquals(true,curStrategy.isResourceTile(startingBoard,3,0));
    }

    @Test
    public void testIsResourceTileTrueRuby() {
        assertEquals(true,curStrategy.isResourceTile(startingBoard,2,0));
    }

    @Test
    public void testIsResourceTileFalseEmpty() {
        assertEquals(false,curStrategy.isResourceTile(startingBoard,0,1));
    }

    @Test
    public void testIsResourceTileFalseRecharge() {
        assertEquals(false,curStrategy.isResourceTile(startingBoard,2,1));
    }

    // test different types of resources
    @Test
    public void testGetResourcePriceDiamond() {
        assertEquals(300,curStrategy.getResourcePrice(startingBoard,new Point(0,0),300,200,100));
    }

    @Test
    public void testGetResourcePriceEmerald() {
        assertEquals(200,curStrategy.getResourcePrice(startingBoard,new Point(1,3),300,200,100));
    }

    @Test
    public void testGetResourcePriceRuby() {
        assertEquals(200,curStrategy.getResourcePrice(startingBoard,new Point(1,3),300,200,100));
    }

    // test market or resource choice valid
    @Test
    public void testChooseMarketOrResource() {
        assertEquals("market",curStrategy.chooseMarketOrResource(0));
    }

    @Test
    public void testChooseMarketResource() {
        assertEquals("resource",curStrategy.chooseMarketOrResource(1));
    }

}
