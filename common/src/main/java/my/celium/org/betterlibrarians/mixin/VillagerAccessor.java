package my.celium.org.betterlibrarians.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads the villager's private restock counters for the restock timer (server side). */
@Mixin(Villager.class)
public interface VillagerAccessor {
    @Accessor("lastRestockGameTime")
    long betterlibrarians$lastRestock();

    @Accessor("numberOfRestocksToday")
    int betterlibrarians$restocksToday();

    @Accessor("lastRestockCheckDay")
    long betterlibrarians$lastCheckDay();
}
