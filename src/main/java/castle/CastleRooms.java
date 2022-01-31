package castle;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import castle.components.Bundle;
import castle.components.CastleIcons;
import castle.components.PlayerData;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CastleRooms {

    public static final Seq<Room> rooms = new Seq<>();

    public static final int size = 8;

    public static Tile shardedSpawn, blueSpawn;

    public enum UnitRoomType {
        attack, defend
    }

    public static class Room {
        public int x;
        public int y;
        public int centrex;
        public int centrey;
        public int endx;
        public int endy;

        public int cost;
        public int size;

        public String label;
        public boolean showLabel;

        public Room(int x, int y, int cost, int size) {
            this.x = x;
            this.y = y;
            this.centrex = x + size / 2;
            this.centrey = y + size / 2;
            this.endx = x + size;
            this.endy = y + size;

            this.cost = cost;
            this.size = size;

            this.label = "";
            this.showLabel = true;
        }

        public void update() {}

        public void buy(PlayerData data) {
            data.money -= cost;
        }

        public boolean canBuy(PlayerData data) {
            return data.money >= cost;
        }

        public void spawn(Tiles tiles) {
            for (int x = 0; x <= size; x++) {
                for (int y = 0; y <= size; y++) {
                    Floor floor = (x == 0 || y == 0 || x == size || y == size ? Blocks.metalFloor5 : Blocks.metalFloor).asFloor();
                    Tile tile = tiles.getn(this.x + x, this.y + y);
                    if (tile != null) {
                        Time.runTask(60f, () -> tile.setFloorNet(floor));
                    }
                }
            }
        }
    }

    public static class BlockRoom extends Room {
        public Block block;
        public Team team;

        public boolean bought;

        public BlockRoom(Block block, Team team, int x, int y, int cost) {
            super(x, y, cost, block.size);
            this.block = block;
            this.team = team;

            this.bought = false;

            this.label = CastleIcons.get(block) + " :[white] " + cost;
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);

            world.tile(centrex, centrey).setNet(block, team, 0);
            if (block instanceof ItemTurret turret) {
                world.tile(x, centrey).setNet(Blocks.itemSource, team, 0);
                world.build(x, centrey).configure(turret.ammoTypes.keys().toSeq().first());
            } else if (block instanceof LaserTurret) {
                world.tile(x, centrey).setNet(Blocks.liquidSource, team, 0);
                world.build(x, centrey).configure(Liquids.cryofluid);
            }

            bought = true;
            showLabel = false;
            Groups.player.each(p -> Call.label(p.con, Bundle.format("events.buy", Bundle.findLocale(p), data.player.coloredName()), 4f, centrex * tilesize, centrey * tilesize));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && !bought && world.build(centrex, centrey) == null;
        }

        @Override
        public void update() {
            if (bought && world.build(centrex, centrey) == null) {
                bought = false;
                showLabel = true;
            }
        }
    }

    public static class UnitRoom extends Room {
        public UnitType unitType;
        public UnitRoomType roomType;

        public int income;

        public UnitRoom(UnitType unitType, UnitRoomType roomType, int income, int x, int y, int cost) {
            super(x, y, cost, 3);
            this.unitType = unitType;
            this.roomType = roomType;
            this.income = income;

            StringBuilder str = new StringBuilder();

            str.append(" ".repeat(Math.max(0, (String.valueOf(income).length() + String.valueOf(cost).length() + 2) / 2))).append(CastleIcons.get(unitType));

            if (roomType == UnitRoomType.attack) str.append(" [accent]").append(Iconc.modeAttack);
            else str.append(" [scarlet]").append(Iconc.defense);

            str.append("\n[gray]").append(cost).append("\n[white]").append(Iconc.blockPlastaniumCompressor).append(" : ");

            this.label = str.append(income < 0 ? "[crimson]" : income > 0 ? "[lime]+" : "[gray]").append(income).toString();
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            data.income += income;

            if (roomType == UnitRoomType.attack) {
                unitType.spawn(data.player.team(), (data.player.team() == Team.sharded ? blueSpawn.drawx() : shardedSpawn.drawx()) + Mathf.random(-40, 40), (data.player.team() == Team.sharded ? blueSpawn.drawy() : shardedSpawn.drawy()) + Mathf.random(-40, 40));
            } else if (data.player.team().core() != null) {
                unitType.spawn(data.player.team(), data.player.team().core().x + 30, data.player.team().core().y + Mathf.random(-40, 40));
            }
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && (income > 0 || data.income - income >= 0);
        }
    }

    public static class ItemRoom extends Room {
        public ItemStack stack;

        public ItemRoom(ItemStack stack, int x, int y, int cost) {
            super(x, y, cost, 3);
            this.stack = stack;

            this.label = "[white]" + stack.amount + "x" + CastleIcons.get(stack.item) + " [white]: [gray]" + cost;
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            Call.transferItemTo(Nulls.unit, stack.item, stack.amount, centrex * tilesize, centrey * tilesize, data.player.team().core());
            if (stack.item == Items.plastanium) {
                Call.transferItemTo(Nulls.unit, Items.metaglass, Mathf.ceil(stack.amount * 0.4f), centrex * tilesize, centrey * tilesize, data.player.team().core());
            }
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && data.player.team().core() != null;
        }
    }
    
    public static class EffectRoom extends Room {
        public StatusEffect effect;
        public Interval interval;

        public EffectRoom(StatusEffect effect, int x, int y, int cost) {
            super(x, y, cost, 3);
            this.effect = effect;
            this.interval = new Interval(2);
        }

        @Override
        public void buy(PlayerData data) {
            super.buy(data);
            Groups.unit.each(unit -> unit.team == data.player.team(), unit -> unit.apply(effect, Float.POSITIVE_INFINITY));
        }

        @Override
        public boolean canBuy(PlayerData data) {
            return super.canBuy(data) && interval.get(data.player.team() == Team.sharded ? 0 : 1, 300f);
        }
    }
}
