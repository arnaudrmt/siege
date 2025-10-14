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
            return false;
        }

        if(args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Wrong Syntax: /siege <start/stop>.");
            return false;
        }

        if(args[0].equalsIgnoreCase("start")) {
            Siege.getInstance().getGameManager().changeState(new PreparationState(Siege.getInstance()));
        } else if(args[0].equalsIgnoreCase("stop")) {
            Siege.getInstance().getGameManager().shutdown();
        } else {
            sender.sendMessage(ChatColor.RED + "Wrong Syntax: /siege <start/stop>.");
            return false;
        }

        return false;
    }
}
