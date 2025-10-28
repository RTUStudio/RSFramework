package kr.rtustudio.framework.bukkit.plugin.command.framework;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

public class ItemCommand extends RSCommand<RSFramework> {

    public ItemCommand(RSFramework plugin) {
        super(plugin, "item", PermissionDefault.OP);
    }

    @Override
    protected Result execute(RSCommandData data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        if (data.length(1)) {
            EntityEquipment equipment = player.getEquipment();
            if (equipment != null) {
                ItemStack itemStack = equipment.getItemInMainHand();
                String id = CustomItems.to(itemStack);
                chat().announce(message().get(player(), "command.item").replace("{id}", id));
                return Result.SUCCESS;
            } else return Result.NOT_FOUND_ITEM;
        } else {
            String id = data.args(1);
            ItemStack itemStack = CustomItems.from(id);
            if (itemStack == null) return Result.NOT_FOUND_ITEM;
            player().getInventory().addItem(itemStack);
            return Result.SUCCESS;
        }
    }
}
