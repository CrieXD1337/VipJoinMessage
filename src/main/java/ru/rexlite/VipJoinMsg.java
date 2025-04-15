package ru.rexlite;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.Map;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import ru.nukkit.multipass.Multipass;

public class VipJoinMsg extends PluginBase implements Listener {
    private String provider;

    @Override
    public void onEnable() {
        this.getLogger().info("§bVip§3JoinMsg §b| Plugin enabled!");
        this.getLogger().info("§bPlugin from:§3 https://cloudburstmcorg/resources/vipjoinmessages.1066");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getDataFolder().mkdirs();
        this.saveResource("config.yml");

        // Загрузка провайдера из конфига
        Config cfg = new Config(new File(this.getDataFolder(), "config.yml"), 2);
        this.provider = cfg.getString("provider", "LuckPerms"); // По умолчанию LuckPerms
        this.getLogger().info("§bUsing provider: §3" + this.provider);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Config cfg = new Config(new File(this.getDataFolder(), "config.yml"), 2);
        Player player = event.getPlayer();
        String nick = player.getName();

        // Получение группы игрока в зависимости от провайдера
        String group = getGroup(player);

        if (group != null) {
            // Получаем сообщение для группы из конфига
            Map<String, Object> groups = cfg.getSection("groups").getAllMap();
            String message = (String) groups.get(group);

            if (message != null) {
                // Заменяем {player} на имя игрока и отправляем сообщение
                message = message.replace("{player}", nick);
                this.getServer().broadcastMessage(TextFormat.colorize(message));
            }
        }
    }

    private String getGroup(Player player) {
        switch (provider.toLowerCase()) {
            case "luckperms":
                return getLuckPermsGroup(player);
            case "multipass":
                return Multipass.getGroup(player.getName());
            default:
                this.getLogger().warning("§cUnknown provider: §f" + provider);
                return null;
        }
    }

    private String getLuckPermsGroup(Player player) {
        try {
            var luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                this.getLogger().warning("§cLuckPerms user not found for player: §f" + player.getName());
                return null;
            }

            return user.getPrimaryGroup();
        } catch (Exception e) {
            this.getLogger().warning("§cError while fetching LuckPerms group: §f" + e.getMessage());
            return null;
        }
    }
}
