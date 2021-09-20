package CastleWars.data;

import arc.Events;
import arc.struct.ObjectMap;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.type.UnitType;

public class UnitDeathData {

    public static ObjectMap<UnitType, Integer> cost;

    public static void init() {
        cost = new ObjectMap<>();

        // Ground
        cost.put(UnitTypes.dagger, 40);
        cost.put(UnitTypes.mace, 72);
        cost.put(UnitTypes.fortress, 250);
        cost.put(UnitTypes.scepter, 1200);
        cost.put(UnitTypes.reign, 3500);

        // GroundSupport
        cost.put(UnitTypes.nova, 50);
        cost.put(UnitTypes.pulsar, 80);
        cost.put(UnitTypes.quasar, 300);
        cost.put(UnitTypes.vela, 1000);
        cost.put(UnitTypes.corvus, 6000);

        // Naval
        cost.put(UnitTypes.risso, 75);
        cost.put(UnitTypes.minke, 150);
        cost.put(UnitTypes.bryde, 300);
        cost.put(UnitTypes.sei, 2500);
        cost.put(UnitTypes.omura, 6500);

        // Spiders
        cost.put(UnitTypes.crawler, 20);
        cost.put(UnitTypes.atrax, 80);
        cost.put(UnitTypes.spiroct, 150);
        cost.put(UnitTypes.arkyid, 1300);
        cost.put(UnitTypes.toxopid, 5000);

        // Air | xd?
        cost.put(UnitTypes.flare, 25);
        cost.put(UnitTypes.horizon, 50);
        cost.put(UnitTypes.zenith, 250);
        cost.put(UnitTypes.antumbra, 2500);
        cost.put(UnitTypes.eclipse, 5000);

        // Support Air | lol?
        cost.put(UnitTypes.mono, 50);
        cost.put(UnitTypes.poly, 100);
        cost.put(UnitTypes.mega, 300);
        cost.put(UnitTypes.quad, 1500);
        cost.put(UnitTypes.oct, 10000);

        // Core
        cost.put(UnitTypes.alpha, 100);
        cost.put(UnitTypes.beta, 150);
        cost.put(UnitTypes.gamma, 250);

        // Block xd | oh no
        cost.put(UnitTypes.block, 1);

        Events.on(EventType.UnitDestroyEvent.class, event -> {
            if (cost.containsKey(event.unit.type)) {
                for (PlayerData data : PlayerData.datas.values()) {
                    if (event.unit.team != data.player.team() && !event.unit.spawnedByCore) {
                        data.money += get(event.unit.type);
                        Call.label(data.player.con, "[accent]+ [lime]" + get(event.unit.type), 0.5f, event.unit.x, event.unit.y);
                    }
                }
            }
        });
    }

    public static int get(UnitType type) {
        return cost.get(type);
    }
}
