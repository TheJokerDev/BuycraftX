package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.velocity.BuycraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.io.IOException;

public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    public SecretSubcommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final CommandSource sender, final String[] args) {
        if (!sender.equals(plugin.getServer().getConsoleCommandSource())) {
            sender.sendMessage(Component.text(plugin.getI18n().get("secret_console_only")).color(TextColor.color(255, 0, 0)));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(Component.text(plugin.getI18n().get("secret_need_key")).color(TextColor.color(255, 0, 0)));
            return;
        }

        plugin.getPlatform().executeAsync(() -> {
            BuyCraftAPI client = BuyCraftAPI.create(args[0], plugin.getHttpClient());
            try {
                plugin.updateInformation(client);
            } catch (IOException e) {
                plugin.getLogger().error("Unable to verify secret", e);
                sender.sendMessage(Component.text(plugin.getI18n().get("secret_does_not_work")).color(TextColor.color(255, 0, 0)));
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getConfiguration().setServerKey(args[0]);
            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                sender.sendMessage(Component.text(plugin.getI18n().get("secret_cant_be_saved")).color(TextColor.color(255, 0, 0)));
            }

            sender.sendMessage(Component.text(plugin.getI18n().get("secret_success",
                    information.getServer().getName(), information.getAccount().getName())).color(TextColor.color(0, 255, 0)));
            plugin.getPlatform().executeAsync(plugin.getDuePlayerFetcher());
        });
    }

    @Override
    public String getDescription() {
        return "Sets the secret key to use for this server.";
    }
}
