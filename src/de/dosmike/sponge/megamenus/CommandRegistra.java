package de.dosmike.sponge.megamenus;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

final public class CommandRegistra {

    public static void registerCommands() {
        //region /megamenus
        Sponge.getCommandManager().register(MegaMenus.getInstance(), CommandSpec.builder()
                .permission("megamenus.command")
                .description(Text.of("MegaMenus management command"))
                .child(CommandSpec.builder()
                    .description(Text.of("Pardon a glitcher and allow access to menus again"))
                    .permission("megamenus.command.pardon")
                    .arguments(GenericArguments.player(Text.of("player")))
                    .executor(((src, args) -> {
                        Player target = args.<Player>getOne("player").get();
                        if (AntiGlitch.pardonGlitcher(target)) {
                            src.sendMessage(Text.of("You've pardoned ", target.getName()));
                            if (src instanceof Player)
                                MegaMenus.getLogger().error(String.format("%s(%s) pardoned %s(%s) and allowed menu access again",
                                        src.getName(),
                                        ((Player)src).getUniqueId().toString(),
                                        target.getName(),
                                        target.getUniqueId().toString()));
                        } else {
                            src.sendMessage(Text.of(target.getName(), " was not marked as glitcher by the system"));
                        }
                        return CommandResult.success();
                    }))
                    .build(), "pardon")
                .child(CommandSpec.builder()
                    .description(Text.of("Reloads the configuration"))
                    .permission("megamenus.command.reload")
                    .arguments(GenericArguments.none())
                    .executor(((src, args) -> {
                        try {
                            MegaMenus.getInstance().loadConfig();
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new CommandException(Text.of(TextColors.RED, e.getMessage()));
                        }
                        src.sendMessage(Text.of(TextColors.GREEN, "MegaMenus reloaded"));
                        return CommandResult.success();
                    }))
                    .build(), "reload")
                .executor((src,args)->{
                    throw new CommandException(Text.of("Missing sub-command (pardon, reload)"));
                }).build(), "megamenus", "mm");
        //endregion

        //region DELETEME
        Sponge.getCommandManager().register(MegaMenus.getInstance(), CommandSpec.builder()
                .executor((src, args)->{
                    if (!(src instanceof Player))
                        throw new CommandException(Text.of("You shall not pass"));
                    ItemStack glitch = ItemStack.builder()
                            .fromContainer(ItemStack.of(ItemTypes.STONE).toContainer()
                                    .set(AntiGlitch.inject, true)
                            ).build();
                    ((Player) src).getInventory().offer(glitch);
                    return CommandResult.success();
                }).build(),
                "glitch");
        //endregion
    }

}
