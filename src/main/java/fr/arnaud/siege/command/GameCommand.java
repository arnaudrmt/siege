package fr.arnaud.siege.command;

import fr.arnaud.siege.Siege;
import fr.arnaud.siege.game.state.PreparationState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {

        if(!(sender instanceof Player) || !sender.hasPermission("siege.game")) {
            sender.sendMessage(ChatColor.RED + "You cannot execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if(args.length != 1) {
            player.sendMessage(ChatColor.RED + "Wrong syntax: /siege <start|stop>.");
            return true;
        }

        if(args[0].equalsIgnoreCase("start")) {
            Siege.getInstance().getGameManager().changeState(new PreparationState(Siege.getInstance()));
            player.sendMessage(ChatColor.GREEN + "You have successfully started the game!");
            return true;
        } else if(args[0].equalsIgnoreCase("stop")) {
            Siege.getInstance().getGameManager().shutdown();
            player.sendMessage(ChatColor.RED + "You have shitdown the game.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Wrong syntax: /siege <start|stop>.");
        return true;
    }
}
