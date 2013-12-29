package org.wargamer2010.signshop.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;
import org.bukkit.material.SimpleAttachableMaterialData;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryHolder;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.blocks.BookFactory;
import org.wargamer2010.signshop.blocks.IBookItem;
import org.wargamer2010.signshop.blocks.IItemTags;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.blocks.SignShopItemMeta;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;

public class itemUtil {
    private static HashMap<Integer, String> discs;

    private itemUtil() {

    }

    public static void initDiscs() {
        // This is pretty ugly but I really couldn't find another way in
        // bukkit's source to get this via a native function
        // Source: http://www.minecraftwiki.net/wiki/Data_values
        discs = new HashMap<Integer, String>();
        discs.put(2256, "13 Disc");
        discs.put(2257, "Cat Disc");
        discs.put(2258, "Blocks Disc");
        discs.put(2259, "Chirp Disc");
        discs.put(2260, "Far Disc");
        discs.put(2261, "Mall Disc");
        discs.put(2262, "Mellohi Disc");
        discs.put(2263, "Stal Disc");
        discs.put(2264, "Strad Disc");
        discs.put(2265, "Ward Disc");
        discs.put(2266, "11 Disc");
        discs.put(2267, "Wait Disc");
    }

    public static ItemStack[] getSingleAmount(ItemStack[] isItems) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        IItemTags tags = BookFactory.getItemTags();
        for(ItemStack item: isItems) {
            ItemStack isBackup = getSingleAmountOfStack(item);
            if(!items.contains(isBackup))
                items.add(isBackup);
        }
        ItemStack[] isBackupToTake = new ItemStack[items.size()];
        int i = 0;
        for(ItemStack entry : items) {
            isBackupToTake[i] = entry;
            i++;
        }
        return isBackupToTake;
    }

    public static ItemStack[] getAllItemStacksForContainables(List<Block> containables) {
        List<ItemStack> tempItems = new LinkedList<ItemStack>();

        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0) {
                        tempItems.add(item);
                    }
                }
            }
        }

        return tempItems.toArray(new ItemStack[tempItems.size()]);
    }

    public static boolean stockOKForContainables(List<Block> containables, ItemStack[] items, boolean bTakeOrGive) {
        return (getFirstStockOKForContainables(containables, items, bTakeOrGive) != null);
    }

    public static InventoryHolder getFirstStockOKForContainables(List<Block> containables, ItemStack[] items, boolean bTakeOrGive) {
        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                if(isStockOK(Holder.getInventory(), items, bTakeOrGive))
                    return Holder;
            }
        }
        return null;
    }

    public static Boolean singeAmountStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, boolean bTakeOrGive) {
        return isStockOK(iiInventory, getSingleAmount(isItemsToTake), bTakeOrGive);
    }

    public static Boolean isStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, boolean bTakeOrGive) {
        try {
            ItemStack[] isChestItems = iiInventory.getContents();
            ItemStack[] isBackup = getBackupItemStack(isChestItems);
            ItemStack[] isBackupToTake = getBackupItemStack(isItemsToTake);
            HashMap<Integer, ItemStack> leftOver;
            if(bTakeOrGive)
                leftOver = iiInventory.removeItem(isBackupToTake);
            else
                leftOver = iiInventory.addItem(isBackupToTake);
            Boolean bStockOK = true;
            if(!leftOver.isEmpty())
                bStockOK = false;
            iiInventory.setContents(isBackup);
            return bStockOK;
        } catch(NullPointerException ex) {
            // Chest is not available, contents are NULL. So let's assume the Stock is not OK
            return false;
        }
    }

    public static String binaryToRoman(int binary) {
        final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
        final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                               40,   10,    9,   5,   4,    1};
        if (binary <= 0 || binary >= 4000) {
            return "";
        }
        String roman = "";
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }

    private static String lookupDisc(int id) {
        if(discs.containsKey(id))
            return discs.get(id);
        else
            return "";
    }

    public static boolean isDisc(int id) {
        return (discs.containsKey(id) || id > 2267);
    }

    public static String formatData(MaterialData data) {
        short s = 0;
        return formatData(data, s);
    }

    public static String formatData(MaterialData data, short durability) {
        String sData;
        // Lookup spout custom material
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Spout")) {
            sData = spoutUtil.getName(data, durability);
            if(sData != null)
                return sData;
        }

        // For some reason running tostring on data when it's from an attachable material
        // will cause a NullPointerException, thus if we're dealing with an attachable, go the easy way :)
        if(data instanceof SimpleAttachableMaterialData)
            return stringFormat(data.getItemType().name());

        if(!(sData = lookupDisc(data.getItemTypeId())).isEmpty())
            return sData;
        else
            sData = data.toString().toLowerCase();

        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");

        StringBuffer sb = new StringBuffer(sData.length());
        p = Pattern.compile("(^|\\W)([a-z])");
        m = p.matcher(sData);
        while(m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

    private static String stringFormat(String sMaterial){
        sMaterial = sMaterial.replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(sMaterial.toLowerCase());
        StringBuffer sb = new StringBuffer(sMaterial.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

    private static ItemStack getSingleAmountOfStack(ItemStack item) {
        if(item == null)
            return null;
        IItemTags tags = BookFactory.getItemTags();
        ItemStack isBackup = tags.getCraftItemstack(
            item.getType(),
            1,
            item.getDurability()
        );
        safelyAddEnchantments(isBackup, item.getEnchantments());
        if(item.getData() != null){
            isBackup.setData(item.getData());
        }
        return tags.copyTags(item, isBackup);
    }

    public static String itemStackToString(ItemStack[] isStacks) {
        HashMap<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
        HashMap<ItemStack, Map<Enchantment,Integer>> enchantments = new HashMap<ItemStack, Map<Enchantment,Integer>>();
        String sItems = "";
        Boolean first = true;
        Integer tempAmount;
        IItemTags tags = BookFactory.getItemTags();
        for(ItemStack item: isStacks) {
            if(item == null)
                continue;
            ItemStack isBackup = getSingleAmountOfStack(item);

            if(item.getEnchantments().size() > 0)
                enchantments.put(isBackup, item.getEnchantments());
            if(items.containsKey(isBackup)) {
                tempAmount = (items.get(isBackup) + item.getAmount());
                items.put(isBackup, tempAmount);
            } else
                items.put(isBackup, item.getAmount());
        }
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if(first) first = false;
            else sItems += ", ";
            String newItemMeta = SignShopItemMeta.getName(entry.getKey());
            String count = (SignShopItemMeta.getTextColor() + entry.getValue().toString() + " ");
            if(newItemMeta.isEmpty())
                sItems += (count + formatData(entry.getKey().getData(), entry.getKey().getDurability()));
            else
                sItems += (count + newItemMeta);
            if(itemUtil.isWriteableBook(entry.getKey())) {
                IBookItem book = BookFactory.getBookItem(entry.getKey());
                if(book != null && (book.getAuthor() != null || book.getTitle() != null))
                    sItems += (" (" + (book.getTitle() == null ? "Unknown" : book.getTitle())  + " by " + (book.getAuthor() == null ? "Unknown" : book.getAuthor()) + ")");
            }
            sItems += ChatColor.WHITE;
        }

        return sItems;
    }

    public static String enchantmentsToMessageFormat(Map<Enchantment,Integer> enchantments) {
        String enchantmentMessage = "";
        Boolean eFirst = true;

        enchantmentMessage += "(";
        for(Map.Entry<Enchantment,Integer> eEntry : enchantments.entrySet()) {
            if(eFirst) eFirst = false;
            else enchantmentMessage += ", ";
            enchantmentMessage += (stringFormat(eEntry.getKey().getName()) + " " + binaryToRoman(eEntry.getValue()));
        }
        enchantmentMessage += ")";
        return enchantmentMessage;
    }

    public static void setSignStatus(Block sign, ChatColor color) {
        if(clickedSign(sign)) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();
            if(ChatColor.stripColor(sLines[0]).length() < 14) {
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
                signblock.update();
            }
        }
    }

    public static Boolean safelyAddEnchantments(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {
        if(enchantments.isEmpty())
            return true;

        try {
            isEnchantMe.addEnchantments(enchantments);
        } catch(IllegalArgumentException ex) {
            if(SignShopConfig.getAllowUnsafeEnchantments()) {
                try {
                    isEnchantMe.addUnsafeEnchantments(enchantments);
                } catch(IllegalArgumentException exfinal) {
                    return false;
                }
            } else
                return false;
        }
        return true;
    }

    public static HashMap<ItemStack, Integer> StackToMap(ItemStack[] isStacks) {
        ItemStack[] isBackup = getBackupItemStack(isStacks);
        HashMap<ItemStack, Integer> mReturn = new HashMap<ItemStack, Integer>();
        int tempAmount;
        for(int i = 0; i < isBackup.length; i++) {
            if(isBackup[i] == null) continue;
            tempAmount = isBackup[i].getAmount();
            isBackup[i].setAmount(1);
            if(mReturn.containsKey(isBackup[i])) {
                tempAmount += mReturn.get(isBackup[i]);
                mReturn.remove(isBackup[i]);
                mReturn.put(isBackup[i], tempAmount);
            } else
                mReturn.put(isBackup[i], tempAmount);
        }
        return mReturn;
    }

    public static ItemStack[] getBackupItemStack(ItemStack[] isOriginal) {
        if(isOriginal == null)
            return null;
        ItemStack[] isBackup = new ItemStack[isOriginal.length];
        for(int i = 0; i < isOriginal.length; i++){
            if(isOriginal[i] != null) {
                isBackup[i] = getBackupSingleItemStack(isOriginal[i]);
            }
        }
        return isBackup;
    }

    public static ItemStack getBackupSingleItemStack(ItemStack isOriginal) {
        // Calls to Bukkit's inventory modifiers like "removeItem" and such used to modify the incoming stack
        // Hence, it was decided to backup all stacks before attempting to do anything with it to prevent the shop's or player's inventory
        // from being modified. The bugs have been solved at some point and especially from 1.4.5 onward. Which is why we version-check now
        // and take a shortcut if we can. Clone always does a shallow copy here.
        if(versionUtil.getBukkitVersionType() == SSBukkitVersion.Post145)
            return isOriginal.clone();

        IItemTags tags = BookFactory.getItemTags();
        ItemStack isBackup = tags.getCraftItemstack(
            isOriginal.getType(),
            isOriginal.getAmount(),
            isOriginal.getDurability()
        );
        itemUtil.safelyAddEnchantments(isBackup, isOriginal.getEnchantments());
        isBackup = tags.copyTags(isOriginal, isBackup);


        if(isOriginal.getData() != null) {
            isBackup.setData(isOriginal.getData());
        }

        return isBackup;
    }

    public static ItemStack[] filterStacks(ItemStack[] all, ItemStack[] filterby) {
        ItemStack[] filtered = new ItemStack[all.length];
        List<ItemStack> tempFiltered = new LinkedList<ItemStack>();
        HashMap<ItemStack, Integer> mFilter = StackToMap(filterby);
        for(ItemStack stack : all) {
            ItemStack temp = getBackupSingleItemStack(stack);
            temp.setAmount(1);
            if(mFilter.containsKey(temp)) {
                tempFiltered.add(stack);
            }
        }

        return tempFiltered.toArray(filtered);
    }

    public static HashMap<ItemStack[], Float> variableAmount(Inventory iiFrom, ItemStack[] isItemsToTake) {
        ItemStack[] isBackup = getBackupItemStack(isItemsToTake);
        HashMap<ItemStack[], Float> returnMap = new HashMap<ItemStack[], Float>();
        returnMap.put(isItemsToTake, 1.0f);
        Boolean fromOK = itemUtil.isStockOK(iiFrom, isBackup, true);
        IItemTags tags = BookFactory.getItemTags();
        if(fromOK) {
            returnMap.put(isItemsToTake, 1.0f);
            return returnMap;
        } else if(!SignShopConfig.getAllowVariableAmounts() && !fromOK) {
            returnMap.put(isItemsToTake, 0.0f);
            return returnMap;
        }
        returnMap.put(isItemsToTake, 0.0f);
        float iCount = 0;
        float tempCount;
        int i = 0;
        HashMap<ItemStack, Integer> mItemsToTake = StackToMap(isBackup);
        HashMap<ItemStack, Integer> mInventory = StackToMap(iiFrom.getContents());
        ItemStack[] isActual = new ItemStack[mItemsToTake.size()];
        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {
            if(iCount == 0 && mInventory.containsKey(entry.getKey()))
                iCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());
            else if(iCount != 0 && mInventory.containsKey(entry.getKey())) {
                tempCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());
                if(tempCount != iCount)
                    return returnMap;
            } else
                return returnMap;

            isActual[i] = itemUtil.getBackupSingleItemStack(entry.getKey());
            isActual[i].setAmount(mInventory.get(entry.getKey()));

            i++;
        }
        returnMap.clear();
        returnMap.put(isActual, iCount);
        return returnMap;
    }

    public static void updateStockStatusPerChest(Block bHolder, Block bIgnore) {
        List<Block> signs = Storage.get().getSignsFromHolder(bHolder);
        if(signs != null) {
            for (Block temp : signs) {
                if(temp == bIgnore)
                    continue;
                if(!clickedSign(temp))
                    continue;
                Seller seller = Storage.get().getSeller(temp.getLocation());
                updateStockStatusPerShop(seller);
            }
        }
    }

    public static void updateStockStatusPerShop(Seller pSeller) {
        if(pSeller != null) {
            Block pSign = pSeller.getSign();
            if(pSign == null || !(pSign.getState() instanceof Sign))
                return;
            String[] sLines = ((Sign) pSign.getState()).getLines();
            if(SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0])).isEmpty())
                return;
            List<String> operation = SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if(SignShopOperations == null)
                return;
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), pSeller.getItems(), pSeller.getContainables(), pSeller.getActivatables(),
                                                                null, null, pSign, signshopUtil.getOperation(sLines[0]), null, SignShopArgumentsType.Check);
            if(pSeller.getMisc() != null)
                ssArgs.miscSettings = pSeller.getMisc();
            Boolean reqOK = true;
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                reqOK = ssOperation.getOperation().checkRequirements(ssArgs, false);
                if(!reqOK) {
                    itemUtil.setSignStatus(pSign, ChatColor.DARK_RED);
                    break;
                }
            }
            if(reqOK)
                itemUtil.setSignStatus(pSign, ChatColor.DARK_BLUE);
        }
    }

    public static void updateStockStatus(Block bSign, ChatColor ccColor) {
        Seller seTemp = Storage.get().getSeller(bSign.getLocation());
        if(seTemp != null) {
            List<Block> iChests = seTemp.getContainables();
            for(Block bHolder : iChests)
                updateStockStatusPerChest(bHolder, bSign);
        }
        setSignStatus(bSign, ccColor);
    }

    public static Boolean clickedSign(Block bBlock) {
        return (bBlock.getType() == Material.getMaterial("SIGN") || bBlock.getType() == Material.getMaterial("WALL_SIGN") || bBlock.getType() == Material.getMaterial("SIGN_POST"));
    }

    public static Boolean clickedDoor(Block bBlock) {
        return (bBlock.getType() == Material.getMaterial("WOODEN_DOOR") || bBlock.getType() == Material.getMaterial("IRON_DOOR") || bBlock.getType() == Material.getMaterial("IRON_DOOR_BLOCK"));
    }

    private static boolean isTopHalf(byte data) {
        return ((data & 0x8) == 0x8);
    }

    public static Block getOtherDoorPart(Block bBlock) {
        if(!clickedDoor(bBlock))
            return null;
        Block up = bBlock.getWorld().getBlockAt(bBlock.getX(), bBlock.getY()+1, bBlock.getZ());
        Block down = bBlock.getWorld().getBlockAt(bBlock.getX(), bBlock.getY()-1, bBlock.getZ());

        Block otherpart = isTopHalf(bBlock.getData()) ? down : up;
        if(clickedDoor(otherpart))
            return otherpart;
        return null;
    }

    public static ItemStack[] convertStringtoItemStacks(List<String> sItems) {
        IItemTags tags = BookFactory.getItemTags();
        ItemStack isItems[] = new ItemStack[sItems.size()];
        for(int i = 0; i < sItems.size(); i++) {
            try {
                String[] sItemprops = sItems.get(i).split(Storage.getItemSeperator());
                if(sItemprops.length < 4)
                    continue;
                isItems[i] = tags.getCraftItemstack(
                        Material.getMaterial(Integer.parseInt(sItemprops[1])),
                        Integer.parseInt(sItemprops[0]),
                        Short.parseShort(sItemprops[2])
                );
                isItems[i].getData().setData(new Byte(sItemprops[3]));
                if(sItemprops.length > 4)
                    safelyAddEnchantments(isItems[i], signshopUtil.convertStringToEnchantments(sItemprops[4]));
                if(sItemprops.length > 5) {
                    try {
                        isItems[i] = SignShopBooks.addBooksProps(isItems[i], Integer.parseInt(sItemprops[5]));
                    } catch(NumberFormatException ex) {

                    }
                }
                if(sItemprops.length > 6) {
                    try {
                        SignShopItemMeta.setMetaForID(isItems[i], Integer.parseInt(sItemprops[6]));
                    } catch(NumberFormatException ex) {

                    }
                }
            } catch(Exception ex) {
                continue;
            }
        }
        return isItems;
    }

    public static boolean isWriteableBook(ItemStack item) {
        if(item == null) return false;
        return (item.getType() == Material.getMaterial("WRITTEN_BOOK") || item.getType() == Material.getMaterial("BOOK_AND_QUILL"));
    }

    public static String[] convertItemStacksToString(ItemStack[] isItems) {
        List<String> sItems = new ArrayList<String>();
        if(isItems == null)
            return new String[1];

        ItemStack isCurrent;
        for(int i = 0; i < isItems.length; i++) {
            if(isItems[i] != null) {
                isCurrent = isItems[i];
                String ID = "";
                if(itemUtil.isWriteableBook(isCurrent))
                    ID = SignShopBooks.getBookID(isCurrent).toString();
                String metaID = SignShopItemMeta.getMetaID(isCurrent).toString();
                if(metaID.equals("-1"))
                    metaID = "";
                sItems.add((isCurrent.getAmount() + Storage.getItemSeperator()
                        + isCurrent.getTypeId() + Storage.getItemSeperator()
                        + isCurrent.getDurability() + Storage.getItemSeperator()
                        + isCurrent.getData().getData() + Storage.getItemSeperator()
                        + signshopUtil.convertEnchantmentsToString(isCurrent.getEnchantments()) + Storage.getItemSeperator()
                        + ID + Storage.getItemSeperator()
                        + metaID));
            }

        }
        String[] items = new String[sItems.size()];
        sItems.toArray(items);
        return items;
    }

    public static boolean itemstackEqual(ItemStack a, ItemStack b, boolean ignoredur) {
        if(a.getType() != b.getType())
            return false;
        if(!ignoredur && a.getData().getData() != b.getData().getData())
            return false;
        if(!ignoredur && a.getDurability() != b.getDurability())
            return false;
        if(a.getEnchantments() != b.getEnchantments())
            return false;
        if(!SignShopItemMeta.isLegacy() && !SignShopItemMeta.getMetaAsMap(a.getItemMeta()).equals(SignShopItemMeta.getMetaAsMap(b.getItemMeta())))
            return false;
        if(a.getMaxStackSize() != b.getMaxStackSize())
            return false;
        return true;
    }

    public static boolean loadChunkByBlock(Block block, int radius) {
        boolean OK = true;
        int chunksize = 12;
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    OK = (OK ? loadChunkByBlock(
                            block.getWorld().getBlockAt(
                                block.getX()+(x*chunksize),
                                block.getY()+(y*chunksize),
                                block.getZ()+(z*chunksize)))
                            : true);
                }
            }
        }
        return OK;
    }

    public static boolean loadChunkByBlock(Block block) {
        if(block == null)
            return false;
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded())
            return block.getChunk().load();
        return true; // Chunk was already loaded
    }
}
