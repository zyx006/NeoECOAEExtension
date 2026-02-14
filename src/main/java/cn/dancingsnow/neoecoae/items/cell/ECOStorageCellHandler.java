package cn.dancingsnow.neoecoae.items.cell;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Cell handler for ECO storage cells to enable compatibility with ME IO Port
 */
public class ECOStorageCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof ECOStorageCellItem;
    }

    @Nullable
    @Override
    public ECOStorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider container) {
        if (is.getItem() instanceof ECOStorageCellItem) {
            return new ECOStorageCell(is, container);
        }
        return null;
    }
}

