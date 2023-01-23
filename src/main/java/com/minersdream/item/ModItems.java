package com.minersdream.item;

import com.minersdream.MinersDream;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MinersDream.MOD_ID);

    public static final RegistryObject<Item> ETERIUM_INGOT = ITEMS.register("eterium_ingot",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MinersDream_TAB)));
    public static final RegistryObject<Item> RAW_ETERIUM = ITEMS.register("raw_eterium",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MinersDream_TAB)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
