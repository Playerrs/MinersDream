package com.minersdream.block.entity.custom;

import com.minersdream.block.ModBlocks;
import com.minersdream.block.custom.MinerMk1Motor;
import com.minersdream.block.custom.NodesHandler;
import com.minersdream.block.entity.ModBlockEntities;
import com.minersdream.block.screen.MinerMK1.MinerMK1Menu;
import com.minersdream.util.ITags;
import com.minersdream.util.SendMessage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
@Mod.EventBusSubscriber
public class MinerMK1BlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 1;

    //Alguma coisa relacionada ao tick do bloco, sla
    public MinerMK1BlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.MINER_MK1_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch (pIndex) {
                    case 0:
                        return MinerMK1BlockEntity.this.progress;
                    case 1:
                        return MinerMK1BlockEntity.this.maxProgress;
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0:
                        MinerMK1BlockEntity.this.progress = pValue;
                        break;
                    case 1:
                        MinerMK1BlockEntity.this.maxProgress = pValue;
                        break;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    //Nome do bloco que aparece na GUI
    @Override
    public Component getDisplayName() {
        return new TextComponent("Miner T1");
    }

    //Cria literalmente o inventario do bloco
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MinerMK1Menu(pContainerId, pPlayerInventory, this, this.data);
    }

    //Alguma coisa sobre compatibilidade
    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }
    //On load '-'
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    //sla, Existe!!!
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    //Grava tanto os itens do inventário do bloco quanto o progresso
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("miner_mk1.progress", progress);
        super.saveAdditional(tag);
    }

    //Carrega os itens do inventario do bloco (quando carrega a chunk)
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    //private static final Logger LOGGER = LogUtils.getLogger();

    //Dropa o conteudo do bloco caso ele seje quebrado
    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    //Verifica se o bloco na posição de saida é uma esteira
    public static boolean verifyConveiorTags(ItemStack item) {
        return item.is(ITags.Items.CONVEYOR_BELT);
    }

    //Verifica se o bloco na frente do separador tem inventario
    public static final boolean hasInventory(LevelAccessor world, BlockPos pPos, ItemStack item) {
        pPos = new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ());
        BlockEntity tileEntity = world.getBlockEntity(pPos);
        if (tileEntity != null && tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()){
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent();
                return true;
        }
        return false;
    }


    public static BlockPos getChestPos(BlockState pState, BlockPos pPos){
        if(pState.getValue(FACING) == Direction.NORTH){
            return new BlockPos(pPos.getX()+3, pPos.getY()-1, pPos.getZ());
        }
        else if(pState.getValue(FACING) == Direction.EAST){
            return new BlockPos(pPos.getX(), pPos.getY()-1, pPos.getZ()+3);

        }
        else if(pState.getValue(FACING) == Direction.SOUTH){
            return new BlockPos(pPos.getX()-3, pPos.getY()-1, pPos.getZ());

        }
        else if(pState.getValue(FACING) == Direction.WEST){
            return new BlockPos(pPos.getX(), pPos.getY()-1, pPos.getZ()-3);

        }
        return null;
    }

    //O paranaue necessario de verificação e setagem de itens
    public static void execute(LevelAccessor world, double x, double y, double z, MinerMK1BlockEntity entity, Block resource, BlockPos pPos, BlockState pState) {
        //Pega o Proprio bloco como entidade
        BlockEntity _ent = world.getBlockEntity(new BlockPos(x, y, z));
        if (_ent != null) {
            //Base 1 itens por segundo em uma miner mk1 en um nodo impuro sem upgrades
            // * pureza do nodo
            // + Overclock %
            //Id do slot de saida
            //TODO >> jogar no Bau
            final int _slotid = 0;
            //Verifica se tem e quantos upgrades tem no bloco
            hasUpgrades(entity);
            //Pega a quantidade de itens que tem no slot de saida
            final int slotCount = entity.itemHandler.getStackInSlot(_slotid).getCount();
            //Pede para outra classe verificar se o bloco a ser minerado é valido e qual o iten que vai ser dropado
            final ItemStack _setstack = new ItemStack(NodesHandler.getParallelItem(resource));
            //final ItemStack _setstack = NodesHandler.tagToItem("forge:ingots/copper");
            BlockEntity chest = world.getBlockEntity(new BlockPos(getChestPos(pState, pPos)));
            //Se requisição de classe acima retornar valido
            //E se o bloco me questão (x y z) estiver energisado por redstone ->>
            if (_setstack != null && world instanceof Level _lvl_isPow && _lvl_isPow.hasNeighborSignal(new BlockPos(x, y, z))) {
                //Todo fazer encher o slot da mineradora quando tiver cheio o bau de saida
                //Tem um inventário na frente da Mineradora?
                if(hasInventory(world,getChestPos(pState, pPos), _setstack)){
                    SendMessage.send(world, "Tem um inventario: ");
                    _setstack.setCount((slotCount + 1));
                        //Faz algo
                        chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        if (capability instanceof IItemHandlerModifiable) {
                            for(int i = 0; i < capability.getSlots(); i++){
                                if(capability.getStackInSlot(i).getCount() < capability.getStackInSlot(i).getMaxStackSize() || capability.getStackInSlot(i) == _setstack) {
                                    //Isso seta o item
                                    capability.insertItem(i, _setstack, false);
                                    _ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capabilityMiner -> {
                                        if (capabilityMiner instanceof IItemHandlerModifiable) {
                                            ((IItemHandlerModifiable) capabilityMiner).setStackInSlot(_slotid, Items.AIR.getDefaultInstance());
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    });
                }
                //Tem uma esteira?
                else if(verifyConveiorTags(world.getBlockState(new BlockPos(getChestPos(pState, pPos))).getBlock().asItem().getDefaultInstance())){
                    SendMessage.send(world, "Tem uma Esteira: ");
                    if (world instanceof Level _level && !_level.isClientSide()) {
                        //Define a posição e o iten que sera jogado no mundo
                        ItemEntity entityToSpawn = new ItemEntity(_level, getChestPos(pState, pPos).getX()+0.5, getChestPos(pState, pPos).getY()+1.8, getChestPos(pState, pPos).getZ()+0.5, new ItemStack(_setstack.getItem()));
                        //seta um delay para poder pegar o iten
                        //entityToSpawn.setPickUpDelay(1);
                        //Não sei, mas ta ai
                        _level.addFreshEntity(entityToSpawn);
                        //Faz barulhinho, mesmo problema acima /|\
                        world.levelEvent(2001, new BlockPos(x, y - 2, z), Block.getId(resource.defaultBlockState()));
                }
                }
                //Se o slot estiver vazio OU com menos items que a capacidade e for o mesmo iten que vai ser minerado
                else if(entity.itemHandler.getStackInSlot(_slotid).getCount() == 0|| entity.itemHandler.getStackInSlot(_slotid).getCount() < entity.itemHandler.getSlotLimit(_slotid)&& entity.itemHandler.getStackInSlot(_slotid).getItem() == _setstack.getItem()) {
                    SendMessage.send(world, "Tem espaço no slot: ");
                    //Adiciona a quantidade existente no slot de saida + 1
                    //Todo >> deve dar pra melhorar esse setCount
                    _setstack.setCount(slotCount + 1);
                    //Faz barulhindo de quebrar bloco
                    //Todo >> resource.defaultBlockState() teoricamente deveria pegar o material do bloco para fazer o son correspondente
                    world.levelEvent(2001, new BlockPos(x, y - 2, z), Block.getId(resource.defaultBlockState()));
                    //Todo >> não sei se isso serve para algo nesse caso
                    _ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
                        if (capability instanceof IItemHandlerModifiable)
                            ((IItemHandlerModifiable) capability).setStackInSlot(_slotid, _setstack);

                    });
                    //Caso o slat de saida do bloco esteja cheio
                }
                }
            }
        //Por fim reseta o progresso do bloco para ele começar a contagem de ticks nessesarios para gerar outro iten
        entity.resetProgress();
    }

    //Verifica se o bloco na posição "Mineravel" tem a Tag de RESOURCE_NODES que tem em todos os nodos de recurso
    public static boolean verifyTags(ItemStack item) {
        return item.is(ITags.Items.RESOURCE_NODES);
    }
    //Tick Manager
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, MinerMK1BlockEntity pBlockEntity) {
        //Passa o bloco na posição "Mineravel" para a variavel resource
        Block resource = pLevel.getBlockState(new BlockPos(pPos.getX(), pPos.getY() - 2, pPos.getZ())).getBlock();
        //Verifica se tem a tag RESOURCE_NODES
        if (verifyTags(new ItemStack(resource, 1)) && pLevel.hasNeighborSignal(new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ()))) {
            //Adiciona 1 a progress (por tick)
            pBlockEntity.progress++;
            //SendMessage.send(pLevel, "Progresso: " + pBlockEntity.progress);
            //Não sei
            setChanged(pLevel, pPos, pState);
            //Verifica se o Progresso atingiu seu maximo
            if (pBlockEntity.progress >= pBlockEntity.maxProgress) {
                //Chama a função que vai gerar o iten no slot de saida
                execute(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), pBlockEntity, resource, pPos, pState);
            } else {
                //Reseta o progresso do bloco
                //pBlockEntity.progress = pBlockEntity.progress;
                //Não sei denovo
                setChanged(pLevel, pPos, pState);
            }
        }
    }

    //Verificar a se e qual a quantidade de Overclocks tem nos slots de overclock
    private static void hasUpgrades(MinerMK1BlockEntity entity) {
        //Reseta a quantidade para não escalonar
        //int upgrades = 0;
        //Reseta o MaxProgress do bloco
        entity.maxProgress = 180;
        //Verifica todos os slots de 1 a 3
        for (int i = 1; i <= 3; i++) {
            //Se tiver Overclock no slot, adiciona 1 a variavel
            if (entity.itemHandler.getStackInSlot(i).getItem() == ModBlocks.OVERCLOCK.get().asItem()) {
                entity.maxProgress -= entity.maxProgress * 0.5;
                //upgrades++;
            }
        }
        //Seta a velocidade que o bloco vai trabalhar (Em ticks)
        //
        //entity.maxProgress = entity.maxProgress - upgrades * 33;
    }

    private void resetProgress() {
        this.progress = 0;
    }
    //Dispara o Demolish quando o bloco é quebrado
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        SendMessage.send(event.getWorld(),"Event? ");
        demolish(event.getWorld(), event.getState(), event.getPos());
    }
    //Verifica se os blocos tem a tag "MINER_MK1"
    public static boolean verifyMinerTags(ItemStack item) {
        return item.is(ITags.Items.MINER_MK1);
    }
    //Desmontar
    public void demolish(LevelAccessor pLevel, BlockState pState, BlockPos pPos){
        SendMessage.send(pLevel,"Break? " + pState.getValue(MinerMk1Motor.FACING));
        int x = pPos.getX();
        int y = pPos.getY();
        int z = pPos.getZ();
        switch (pState.getValue(MinerMk1Motor.FACING)){
            case WEST:
            case SOUTH:
            case EAST:
            default:
                for(int i = x+2; i < x-1; i--){
                    for(int j = y+3; j > y-1; j--){
                        if(verifyMinerTags(new ItemStack((ItemLike) pLevel.getBlockState(new BlockPos(i,j,z))))) {
                            pLevel.setBlock(new BlockPos(i,j,z), Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
        }
    }
}
