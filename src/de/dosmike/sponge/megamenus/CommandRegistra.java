package de.dosmike.sponge.megamenus;

import de.dosmike.sponge.megamenus.api.IMenu;
import de.dosmike.sponge.megamenus.api.MenuRender;
import de.dosmike.sponge.megamenus.api.elements.*;
import de.dosmike.sponge.megamenus.api.elements.concepts.IElement;
import de.dosmike.sponge.megamenus.api.util.Tickable;
import de.dosmike.sponge.megamenus.impl.BaseMenuImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Collections;

public class CommandRegistra {

    static BaseMenuImpl menu = (BaseMenuImpl)MegaMenus.createMenu();
    static MSlot outSlot = new MSlot(ItemStack.empty());
    static {
        menu.setTitle(Text.of("This is a test"));
        menu.setBackgroundProvider(BackgroundProvider.BACKGROUND_GRAYPANE);
        menu.add(MSpinner.builder()
                .addValue(IIcon.of(ItemStack.builder()
                        .itemType(ItemTypes.DYE)
                        .add(Keys.DYE_COLOR, DyeColors.RED)
                        .build()))
                .addValue(IIcon.of(ItemStack.builder()
                        .itemType(ItemTypes.DYE)
                        .add(Keys.DYE_COLOR, DyeColors.ORANGE)
                        .build()))
                .addValue(IIcon.of(ItemStack.builder()
                        .itemType(ItemTypes.DYE)
                        .add(Keys.DYE_COLOR, DyeColors.YELLOW)
                        .build()))
                .addValue(IIcon.of(ItemStack.builder()
                        .itemType(ItemTypes.DYE)
                        .add(Keys.DYE_COLOR, DyeColors.GREEN)
                        .build()))
                .setName(Text.of("Pick a Color"))
                .setLore(Arrays.asList(
                        Text.of("Red"),
                        Text.of("Orange"),
                        Text.of("Yellow"),
                        Text.of("Green")
                ))
                .setPosition(new SlotPos(0, 0))
                .build());
        menu.add(2, MCheckbox.builder()
                .setName(Text.of("Vanish"))
                .setLore(Arrays.asList(
                        Text.of("If you're in vanish other"),
                        Text.of("players won't see you")
                        ))
                .setPosition(new SlotPos(4,1))
                .setOnChangeListener((o,n,e,v)->{
                    v.offer(Keys.VANISH, n==1);
                    if (n==1)
                        v.sendMessage(Text.of("You're now in vanish"));
                    else
                        v.sendMessage(Text.of("You've left vanish"));
                })
                .setValue(0)
                .build());

        MSlot inSlot = new MSlot(ItemStack.empty());
        inSlot.hookThinkTick(new Tickable() {
            int time=0;
            @Override
            public boolean tick(int ms) {
                if (inSlot.getItemStack().isPresent() && outSlot.getItemStack().map(ItemStack::getQuantity).orElse(0) < 64) {
                    time += ms;
                    if (time > 1000) {
                        time -= 1000;
                        ItemStack stack = inSlot.getItemStack().get();
                        stack.setQuantity(stack.getQuantity() - 1);
                        if (!outSlot.getItemStack().isPresent())
                            outSlot.setItemStack(ItemTypes.COAL);
                        else {
                            stack = outSlot.getItemStack().get();
                            stack.setQuantity(stack.getQuantity() + 1);
                        }
                        return true;
                    }
                } else {
                    time = 0;
                }
                return false;
            }
        });
        inSlot.setPosition(new SlotPos(3,1));
        inSlot.setAccess(IElement.GUI_ACCESS_PUT);
        outSlot.setPosition(new SlotPos(5,1));
        outSlot.setAccess(IElement.GUI_ACCESS_TAKE);
        menu.add(3, inSlot);
        menu.add(3, outSlot);
        menu.add(3, MIcon.builder()
                .setIcon(IIcon.of(ItemStack.builder().itemType(ItemTypes.COAL).build()))
                .setName(Text.of("What's this?"))
                .setLore(Collections.singletonList(Text.of("Converts any item into coal")))
                .setPosition(new SlotPos(4,0))
                .build());
    }

    public static void registerCommands() {
        //region /mm
        Sponge.getCommandManager().register(MegaMenus.getInstance(), CommandSpec.builder()
                .permission("megamenus.test")
                .description(Text.of("Just a test command"))
                .arguments(
                        GenericArguments.player(Text.of("player"))
                ).executor((src,args)->{

                    Player player = args.<Player>getOne("player").get();

                    menu.createBoundGuiRenderer(3).open(player);

                    return CommandResult.success();
                }).build(), "mm");
        //endregion
        //region /mm2
        Sponge.getCommandManager().register(MegaMenus.getInstance(), CommandSpec.builder()
                .permission("megamenus.test")
                .description(Text.of("Just a test command"))
                .arguments(
                        GenericArguments.player(Text.of("player"))
                ).executor((src,args)->{

                    Player player = args.<Player>getOne("player").get();

                    player.sendBookView(BookView.builder().build());

                    return CommandResult.success();
                }).build(), "mm2");
        //endregion
    }

}
