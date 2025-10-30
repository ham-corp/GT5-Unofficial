package gregtech.common.tileentities.machines.multi.beamcrafting;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.enums.TickTime;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.extensions.ArrayExt;
import gregtech.common.blocks.BlockCasings10;
import gregtech.common.blocks.BlockCasings13;
import gregtech.common.misc.GTStructureChannels;
import gregtech.loaders.postload.recipes.beamcrafter.BeamCrafterMetadata;
import gtnhlanth.api.recipe.LanthanidesRecipeMaps;
import gtnhlanth.common.beamline.BeamInformation;
import gtnhlanth.common.hatch.MTEHatchInputBeamline;
import gtnhlanth.common.register.LanthItemList;
import gtnhlanth.common.tileentity.recipe.beamline.TargetChamberMetadata;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;
import static gregtech.api.enums.HatchElement.InputBus;
import static gregtech.api.enums.HatchElement.InputHatch;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.HatchElement.OutputBus;
import static gregtech.api.enums.HatchElement.OutputHatch;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_MULTI_BREWERY;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_MULTI_BREWERY_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_MULTI_BREWERY_ACTIVE_GLOW;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_MULTI_BREWERY_GLOW;
import static gregtech.api.recipe.RecipeMaps.BEAMCRAFTER_METADATA;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTStructureUtility.chainAllGlasses;
import static gregtech.api.util.GTStructureUtility.ofFrame;
import static gtnhlanth.api.recipe.LanthanidesRecipeMaps.TARGET_CHAMBER_METADATA;

public class MTEBeamCrafter extends MTEExtendedPowerMultiBlockBase<gregtech.common.tileentities.machines.multi.beamcrafting.MTEBeamCrafter>
    implements ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "main";

    private static final int CASING_INDEX_CENTRE = 1662; // Shielded Acc.
    private final ArrayList<MTEHatchInputBeamline> mInputBeamline = new ArrayList<>();



    private static final IStructureDefinition<gregtech.common.tileentities.machines.multi.beamcrafting.MTEBeamCrafter> STRUCTURE_DEFINITION = StructureDefinition
        .<gregtech.common.tileentities.machines.multi.beamcrafting.MTEBeamCrafter>builder()
        .addShape(
            STRUCTURE_PIECE_MAIN,
            // spotless:off
            new String[][]{{
                "                 ",
                " BBB         BBB ",
                " BCB         BCB ",
                " BBB         BBB ",
                "                 "
            },{
                " BBB         BBB ",
                "B   B       B   B",
                "B   B       B   B",
                "B   B       B   B",
                " BBB         BBB "
            },{
                " BBB         BBB ",
                "B   B       B   B",
                "B   A       A   B",
                "B   B       B   B",
                " BBB         BBB "
            },{
                " BBB         BBB ",
                "B   B       B   B",
                "B   A       A   B",
                "B   B       B   B",
                " BBB         BBB "
            },{
                " BBB         BBB ",
                "B   BB     BB   B",
                "B   AA     AA   B",
                "B   BB     BB   B",
                " BBB         BBB "
            },{
                "  BBB       BBB  ",
                " B   BB   BB   B ",
                " A   AA   AA   A ",
                " B   BB   BB   B ",
                "  BBB       BBB  "
            },{
                "  BBBB     BBBB  ",
                " B    BBBBB    B ",
                " A    AB~BA    A ",
                " B    BBBBB    B ",
                "  BBBB     BBBB  "
            },{
                "   BBBBBBBBBBB   ",
                "  B           B  ",
                "  A           A  ",
                "  B           B  ",
                "   BBBBBBBBBBB   "
            },{
                "    BBBBBBBBB    ",
                "   B         B   ",
                "   A         A   ",
                "   B         B   ",
                "    BBBBBBBBB    "
            },{
                "      BBBBB      ",
                "    BB     BB    ",
                "    AA     AA    ",
                "    BB     BB    ",
                "      BBBBB      "
            },{
                "                 ",
                "      BBBBB      ",
                "      BBBBB      ",
                "      BBBBB      ",
                "                 "
            }})
        //spotless:on
        .addElement('B', // collider casing
            buildHatchAdder(MTEBeamCrafter.class).atLeast(Energy, ExoticEnergy, Maintenance, InputBus, InputHatch, OutputBus, OutputHatch)
                .casingIndex(((BlockCasings13) GregTechAPI.sBlockCasings13).getTextureIndex(10))
                .dot(1)
                .buildAndChain(GregTechAPI.sBlockCasings13, 10))
        .addElement('A', chainAllGlasses()) // new glass type todo: (?)
        .addElement(
            'C',
            buildHatchAdder(MTEBeamCrafter.class).hatchClass(MTEHatchInputBeamline.class)
                .casingIndex(CASING_INDEX_CENTRE)
                .dot(2)
                .adder(MTEBeamCrafter::addBeamLineInputHatch)
                .build()) // beamline input hatch
        .build();

    private boolean addBeamLineInputHatch(IGregTechTileEntity te, int casingIndex) {
        if (te == null) return false;

        IMetaTileEntity mte = te.getMetaTileEntity();
        if (mte == null) return false;

        if (mte instanceof MTEHatchInputBeamline) {
            return this.mInputBeamline.add((MTEHatchInputBeamline) mte);
        }

        return false;
    }

    public MTEBeamCrafter(final int aID, final String aName, final String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEBeamCrafter(String aName) {
        super(aName);
    }

    @Override
    public IStructureDefinition<gregtech.common.tileentities.machines.multi.beamcrafting.MTEBeamCrafter> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new gregtech.common.tileentities.machines.multi.beamcrafting.MTEBeamCrafter(this.mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
                                 int colorIndex, boolean aActive, boolean redstoneLevel) {
        ITexture[] rTexture;
        if (side == aFacing) {
            if (aActive) {
                rTexture = new ITexture[] {
                    Textures.BlockIcons
                        .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings13, 10)),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_MULTI_BREWERY_ACTIVE)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_MULTI_BREWERY_ACTIVE_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            } else {
                rTexture = new ITexture[] {
                    Textures.BlockIcons
                        .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings13, 10)),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_MULTI_BREWERY)
                        .extFacing()
                        .build(),
                    TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_MULTI_BREWERY_GLOW)
                        .extFacing()
                        .glow()
                        .build() };
            }
        } else {
            rTexture = new ITexture[] { Textures.BlockIcons
                .getCasingTextureForId(GTUtility.getCasingTextureIndex(GregTechAPI.sBlockCasings13, 10)) };
        }
        return rTexture;
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Beam Crafter, Beam Assembler")
            //.addBulkMachineInfo(4, 1.5F, 1F)
            .beginStructureBlock(17, 5, 11, false)
            .addController("Front Center")
            .addCasingInfoMin("Collider Casing", 224, false)
            .addCasingInfoExactly("Any Tiered Glass", 26, false)
            .addInputBus("Any Collider Casing", 1)
            .addOutputBus("Any Collider Casing", 1)
            .addInputHatch("Any Collider Casing", 1)
            .addOutputHatch("Any Collider Casing", 1)
            .addEnergyHatch("Any Collider Casing", 1)
            .addMaintenanceHatch("Any Collider Casing", 1)
            .addSubChannelUsage(GTStructureChannels.BOROGLASS)
            .addTecTechHatchInfo()
            .toolTipFinisher();
        return tt;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 8, 2, 6);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_MAIN, stackSize, 8, 2, 6, elementBudget, env, false, true);
    }


    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 8, 2, 6);
    }


    public int craftProgress = 0;

    @Nullable
    private BeamInformation getInputParticle_A() {
        for (MTEHatchInputBeamline in : this.mInputBeamline) {
            if (in.dataPacket == null) return new BeamInformation(0, 0, 0, 0);
            return in.dataPacket.getContent();
        }
        return null;
    }
    @Nullable
    private BeamInformation getInputParticle_B() {
        int i = 0;
        for (MTEHatchInputBeamline in : this.mInputBeamline) {
            if (i == 1) {
                if (in.dataPacket == null) return new BeamInformation(0, 0, 0, 0);
                return in.dataPacket.getContent();
            }
            i += 1;
        }
        return null;
    }

    private boolean checkIfInputParticleInRecipe(BeamInformation inputParticle_A,BeamInformation inputParticle_B,BeamCrafterMetadata metadata) {

        int particleID_x = metadata.particleID_A;
        int particleID_y = metadata.particleID_B;
        float minEnergy_x = metadata.minEnergy_A;
        float minEnergy_y = metadata.minEnergy_B;

        int inputParticleID_A = inputParticle_A.getParticleId();
        int inputParticleID_B = inputParticle_B.getParticleId();
        float inputEnergy_A = inputParticle_A.getEnergy();
        float inputEnergy_B = inputParticle_B.getEnergy();

        // possibilities: (A = x, B = y); (A = y, B = x)

        return ((inputParticleID_A == particleID_x && inputParticleID_B == particleID_y && inputEnergy_A > minEnergy_x && inputEnergy_B > minEnergy_y)
            || (inputParticleID_A == particleID_y && inputParticleID_B == particleID_x && inputEnergy_A > minEnergy_y && inputEnergy_B > minEnergy_x));

    }

    private GTRecipe lastRecipe;
    @Override
    public @NotNull CheckRecipeResult checkProcessing() {

        // particleARate - the Rate value of beam packet A. for this multi,
        //                think of it as the number of particles in the packet
        // particleBRate - the Rate value of beam packet B.
        // craftProgressA - part of the progress of the current craft.
        //                 if a new craft starts that is a different recipe than the previous, reset to 0
        //                 if the recipe is the same as the previous recipe, keep track of the current craftProgressA
        // craftProgressB - the other part of the progress of the current craft.
        //                 if a new craft starts that is a different recipe than the previous, reset to 0
        //                 if the recipe is the same as the previous recipe, keep track of the current craftProgressB
        // recipeParticleACount - the total number of required particle A for the ongoing recipe
        // recipeParticleBCount - the total number of required particle B for the ongoing recipe
        //
        // run the following every second
        //
        // if there is no ongoing recipe, check the item/fluid inputs for a valid recipe
        //   if not found, do nothing for this processing cycle, and consume a tiny amount of power
        //   if found, start a craft, and consume the recipe's amount of power until it is done
        //
        // add particleRateA to craftProgressA, add particleRateB to craftProgressB
        // every cycle, for every integer number of completed craftProgressA and B,
        //    deliver output. subtract that much progress from craftProgressA and B such that they are both < recipeParticle(A/B)Count
        //    if all cached inputs are *not* consumed, wait for more particle packets (do not fail the craft!)
        //
        // if all cached inputs are consumed delivered, check for the same recipe again. if present, continue the process without
        //    resetting craftProgressA/B. otherwise, reset craftProgressA/B
        //
        // repeat forever
        //
        // how would batch mode work? how would parallels work? are parallels even needed, since the machine has
        // linear scaling of processing speed with particleRate?


        ArrayList<ItemStack> tItems = this.getStoredInputs();
        ItemStack[] inputItems = tItems.toArray(new ItemStack[0]);
        ArrayList<FluidStack> tFluids = this.getStoredFluids();
        FluidStack[] inputFluids = tFluids.toArray(new FluidStack[0]);

        long tVoltageActual = GTValues.VP[(int) this.getInputVoltageTier()];

        GTRecipe tRecipe = RecipeMaps.beamcrafterRecipes.findRecipeQuery()
            .items(inputItems)
            .fluids(inputFluids)
            .voltage(tVoltageActual)
            .filter((GTRecipe recipe) -> {
                BeamCrafterMetadata metadata = recipe.getMetadata(BEAMCRAFTER_METADATA);
                if (metadata == null) return false;

                BeamInformation inputParticle_A = this.getInputParticle_A();
                BeamInformation inputParticle_B = this.getInputParticle_B();

                if ((inputParticle_A != null) || (inputParticle_B != null)) {
                    return checkIfInputParticleInRecipe(inputParticle_A,inputParticle_B,metadata);
                }
                return false;
            })
            .cachedRecipe(this.lastRecipe)
            .find();
        if (tRecipe == null) return CheckRecipeResultRegistry.NO_RECIPE;

        BeamCrafterMetadata metadata = tRecipe.getMetadata(BEAMCRAFTER_METADATA);
        if (metadata == null) return CheckRecipeResultRegistry.NO_RECIPE;

        BeamInformation inputParticle_A = this.getInputParticle_A();
        BeamInformation inputParticle_B = this.getInputParticle_B();
        if (inputParticle_A == null || inputParticle_B == null) return CheckRecipeResultRegistry.NO_RECIPE;

        if (!checkIfInputParticleInRecipe(inputParticle_A,inputParticle_B,metadata)) return CheckRecipeResultRegistry.NO_RECIPE;

        this.mMaxProgresstime = 20;
        if (this.mMaxProgresstime == Integer.MAX_VALUE - 1 && this.mEUt == Integer.MAX_VALUE - 1) return CheckRecipeResultRegistry.NO_RECIPE;

        if (!tRecipe.equals(this.lastRecipe)) this.lastRecipe = tRecipe;

        // todo: subticking

        tRecipe.consumeInput(1, GTValues.emptyFluidStackArray, inputItems);
        ItemStack[] itemOutputArray = ArrayExt.copyItemsIfNonEmpty(tRecipe.mOutputs);
        this.mOutputItems = itemOutputArray;

        this.mEfficiency = (10000 - (this.getIdealStatus() - this.getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        mEUt = (int) -tVoltageActual;
        if (this.mEUt > 0) this.mEUt = (-this.mEUt);

        this.updateSlots();
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.beamcrafterRecipes;
    }
}
