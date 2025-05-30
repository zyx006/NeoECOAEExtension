package cn.dancingsnow.neoecoae.blocks.entity.crafting;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import cn.dancingsnow.neoecoae.gui.widget.AEPatternViewSlotWidget;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

public class ECOCraftingPatternBusBlockEntity extends AbstractCraftingBlockEntity<ECOCraftingPatternBusBlockEntity>
    implements IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity, IManaged, InternalInventoryHost, IUIHolder.Block {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ECOCraftingPatternBusBlockEntity.class);
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    public static final int ROW_SIZE = 9;
    public static final int COL_SIZE = 7;

    @Persisted
    @DescSynced
    private final AppEngInternalInventory inventory;

    public final IItemHandlerModifiable itemHandler;

    class AEEncodedPatternFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return PatternDetailsHelper.decodePattern(stack, level) instanceof IMolecularAssemblerSupportedPattern;
        }
    }

    public ECOCraftingPatternBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.inventory = new AppEngInternalInventory(this, ROW_SIZE * COL_SIZE);
        this.inventory.setFilter(new AEEncodedPatternFilter());
        this.itemHandler = (IItemHandlerModifiable) inventory.toItemHandler();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.saveChanges();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return syncStorage;
    }

    @Override
    public void onChanged() {
        setChanged();
        markForUpdate();
    }

    @Override
    public IManagedStorage getRootStorage() {
        return syncStorage;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        IntStream.range(0, ROW_SIZE * COL_SIZE)
            .mapToObj(inventory::getStackInSlot)
            .filter(s -> !s.isEmpty())
            .forEach(drops::add);
    }

    private void onPatternChange(int index) {
        // TODO: sync
    }

    private WidgetGroup createUI() {
        WidgetGroup root = new WidgetGroup();
        root.setSize(178, 238);
        root.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);

        TextTextureWidget text = new TextTextureWidget(8, 8, 160,9);
        text.setText(Component.translatable("block.neoecoae.crafting_pattern_bus"));
        text.textureStyle(t -> t.setType(TextTexture.TextType.LEFT_ROLL));
        root.addWidget(text);

        WidgetGroup items = new WidgetGroup();
        for (int x = 0; x < COL_SIZE; x++) {
            for (int y = 0; y < ROW_SIZE; y++) {
                AEPatternViewSlotWidget slot = aePatternViewSlotWidget(x, y);
                items.addWidget(slot);
            }
        }

        PlayerInventoryWidget playerInventoryWidget = new PlayerInventoryWidget();
        playerInventoryWidget.setSelfPosition(3, 146);
        root.addWidget(playerInventoryWidget);

        root.addWidget(items);
        return root;
    }

    private @NotNull AEPatternViewSlotWidget aePatternViewSlotWidget(int x, int y) {
        int index = ROW_SIZE * x + y;
        AEPatternViewSlotWidget slot = new AEPatternViewSlotWidget(
            itemHandler,
            index,
            8 + y * 18,
            20 + x * 18
        );
        slot.initTemplate();
        slot.setItemHook(stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem<?> pattern) {
                ItemStack out =  pattern.getOutput(stack);
                if (!out.isEmpty()) {
                    return out;
                }
            }
            return stack;
        });
        slot.setChangeListener(() -> onPatternChange(index));
        return slot;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(createUI(), this, entityPlayer);
    }
}
