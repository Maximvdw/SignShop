package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class setRedstoneOn implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Boolean foundLever = false;
        for(Block block : ssArgs.getActivatables().get())
            if(block.getType() == Material.getMaterial("LEVER"))
                foundLever = true;
        if(!foundLever) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("lever_missing", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(!setupOperation(ssArgs))
            return false;

        Boolean bReturn = false;
        Block bLever;
        for(int i = 0; i < ssArgs.getActivatables().get().size(); i++) {
            bLever = ssArgs.getActivatables().get().get(i);
            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered())
                    bReturn = true;
            }
        }
        if(!bReturn)
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("already_on", ssArgs.getMessageParts()));
        return bReturn;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        if(!setupOperation(ssArgs))
            return false;
        
        Block bLever = null;

        for(int i = 0; i < ssArgs.getActivatables().get().size(); i++) {
            bLever = ssArgs.getActivatables().get().get(i);
            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered()) {
                    lever.setPowered(true);
                    state.setData(lever);
                    state.update();
                    signshopUtil.generateInteractEvent(bLever, ssArgs.getPlayer().get().getPlayer(), ssArgs.getBlockFace().get());
                }
            }
        }

        return true;
    }
}
