package com.brownnpc

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ConsumableComponent
import net.minecraft.component.type.FoodComponent
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.Item.Settings
import net.minecraft.item.consume.ApplyEffectsConsumeEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.minecraft.item.ItemGroups
import org.slf4j.LoggerFactory
import kotlin.random.Random



object NightVisionRevamp : ModInitializer {
    private val logger = LoggerFactory.getLogger("night-vision-revamp")
    override fun onInitialize() {
    logger.info("Registering NightVisionRevamp...")
    // Make coated glow berry item that is crafted with 1 glow ink sac and 1 glow berries
    // crafting recipe is not defined in this file
    val id= Identifier.of("night-vision-revamp", "coated_glow_berries")
    val key = RegistryKey.of(RegistryKeys.ITEM, id);
    val settings = Item.Settings()
        .registryKey(key).
        food(
            FoodComponent.Builder()
            .alwaysEdible()
            .build()
        )
    val COATED_GLOW_BERRY = Registry.register( // register custom item
    Registries.ITEM,
    key,
    Item(settings)
    )
    // the custom item is a food
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register { group ->
        group.add(COATED_GLOW_BERRY)
    }

    // it gives nausea for 7 seconds, makes you glow for 20, and gives you 2 mins of nightvision
    DefaultItemComponentEvents.MODIFY.register( { ctx ->
        ctx.modify(COATED_GLOW_BERRY) { builder ->

            builder.add(
                DataComponentTypes.CONSUMABLE,
                ConsumableComponent.builder()
                    .consumeEffect(
                        ApplyEffectsConsumeEffect(
                            listOf(
                                StatusEffectInstance(StatusEffects.NAUSEA, 20 * 5), // 6 seconds (squid ink is gross)
                                StatusEffectInstance(StatusEffects.GLOWING, 20 * 8), // 8 seconds (squid ink makes you glow)
                                StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 100) // 100 seconds
                            )
                        )
                    )
                    .build()
            )
            // gives the same nutrition as a regular glow berry
            builder.add(DataComponentTypes.FOOD, 
                FoodComponent.Builder()
                    .nutrition(2)
                    .saturationModifier(0.6f)
                    .alwaysEdible()
                    .build()
            )
        }
        // Modify vanilla glow berries to give 15 seconds of night vision
        ctx.modify(Items.GLOW_BERRIES) { builder ->
            builder.add(
                DataComponentTypes.CONSUMABLE,
                ConsumableComponent.builder()
                    .consumeEffect(
                        ApplyEffectsConsumeEffect(
                            StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 15)
                        )
                    )
                    .build()
            )
            val food = builder.getOrDefault(DataComponentTypes.FOOD, FoodComponent.Builder().build())

            builder.add(
                DataComponentTypes.FOOD,
                FoodComponent.Builder()
                    .nutrition(food.nutrition())
                    .saturationModifier(food.saturation())
                    .alwaysEdible()
                    .build()
            )
        }
        // sea pickles give 5 seconds of night vision
        ctx.modify(Items.SEA_PICKLE) { builder ->
            builder.add(
                DataComponentTypes.CONSUMABLE,
                ConsumableComponent.builder()
                    .consumeEffect(
                        ApplyEffectsConsumeEffect(
                            listOf(
                                StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 5), // 5 seconds
                            )
                        )
                    )
                    .build()
            )
            builder.add(DataComponentTypes.FOOD, 
                FoodComponent.Builder()
                    .nutrition(1)
                    .alwaysEdible()
                    .build()
            )
        }

    })
    }
}

