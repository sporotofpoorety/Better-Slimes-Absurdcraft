package com.mic.betterslimes.entity.slimes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mic.betterslimes.util.BetterSlimesConfigMobs;
import com.mic.betterslimes.util.LootTables;
import com.mic.betterslimes.util.Reference;
import com.mic.betterslimes.entity.EntityBetterSlime;
import com.mic.betterslimes.entity.ISpecialSlime;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.sporotofpoorety.eternitymode.core.EternityModeSoundEvents;
import org.sporotofpoorety.eternitymode.entity.EntityEarthPiece;
import org.sporotofpoorety.eternitymode.entity.EntityExplosiveShockwave;
import org.sporotofpoorety.eternitymode.entity.EntityMeteorBlock;
import org.sporotofpoorety.eternitymode.entity.EntityMeteorBlockHoming;
import org.sporotofpoorety.eternitymode.entity.EntityThrownBlock;
import org.sporotofpoorety.eternitymode.entity.ai.EntityAIRelentlessTargetPlayers;
import org.sporotofpoorety.eternitymode.entity.ai.EntityAIStun;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotBouncing;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotLinearSplits;
import org.sporotofpoorety.eternitymode.interfacemixins.IMixinEntityLiving;
import org.sporotofpoorety.eternitymode.util.AbsurdcraftMathUtils;
import org.sporotofpoorety.eternitymode.util.EntityUtil;
import org.sporotofpoorety.eternitymode.util.ExplosionUtil;
import org.sporotofpoorety.eternitymode.util.MiscUtil;
import org.sporotofpoorety.eternitymode.util.ProjectileUtil;
import org.sporotofpoorety.eternitymode.util.QueuedActionAtPos;

import org.sporotofpoorety.srpabsurdcraft.entity.EntityOrbVoidCustom;




// Renamed from King Slime to Quazar
public class Quazar extends EntityBetterSlime implements ISpecialSlime 
{

//These are unrelated to the boss itself
    private final BossInfoServer bossInfo 
        = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS));




//Interface with mixin
    public IMixinEntityLiving livingEntityMixin;




//Values config
    public int quazarSize;

    public int leapWarning;
    public int leapCooldown;
    public int leapLandingRadius;
    public float leapLandingDamage;
    public float leapLaunchMultiplier;

    public int specialWarning;
    public int specialCooldown;
    public int specialSequenceCooldown;
    public int specialSequenceMax;
    public int meteorLandingRadius;
    public float meteorLandingDamage;
    public float meteorLaunchMultiplier;




//Attack state
    public String behaviorState;


//  ArrayList<Integer> currentAttacksProjectile = new ArrayList<>();
//  ArrayList<Integer> currentAttacksMovement = new ArrayList<>();
    int currentAttackProjectile;
    int currentAttackMovement;
    public int leapCooldownCurrent; 
    private boolean isPerformingLeap;   


    public int specialCooldownCurrent;
    public double teleportX, teleportY, teleportZ;


    private boolean wasOnGroundPreviousTick;
    private boolean isPerformingSpecial;
    protected boolean shouldExplodeOnLanding;
    private int landingExplosionWait;
    public int specialSequenceAt;




    public Quazar(World worldIn) 
    {
        super(worldIn);
        this.moveHelper = new EntityMoveHelper(this); 
//      this.ignoreFrustumCheck = true;

        this.setAttackModifier(1);
        this.setHealthModifier(1);
        this.setSlimeSize(this.quazarSize * 2, true);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.07D);
        this.isImmuneToFire = true;




//Interface with mixin
        this.livingEntityMixin = (IMixinEntityLiving) this;




//Values config
        this.quazarSize = BetterSlimesConfigMobs.quazarSize;

        this.leapCooldown = BetterSlimesConfigMobs.leapCooldown;
        this.leapWarning = BetterSlimesConfigMobs.leapWarning;
        this.leapLandingRadius = BetterSlimesConfigMobs.leapLandingRadius;
        this.leapLandingDamage = BetterSlimesConfigMobs.leapLandingDamage;
        this.leapLaunchMultiplier = BetterSlimesConfigMobs.leapLaunchMultiplier;

        this.specialWarning = BetterSlimesConfigMobs.specialWarning;
        this.specialCooldown = BetterSlimesConfigMobs.specialCooldown;
        this.specialSequenceCooldown = BetterSlimesConfigMobs.specialSequenceCooldown;
        this.specialSequenceMax = BetterSlimesConfigMobs.specialSequenceMax;
        this.meteorLandingRadius = BetterSlimesConfigMobs.meteorLandingRadius;
        this.meteorLandingDamage = BetterSlimesConfigMobs.meteorLandingDamage;
        this.meteorLaunchMultiplier = BetterSlimesConfigMobs.meteorLaunchMultiplier;




//Attack state
        this.behaviorState = "default";

        this.currentAttackProjectile = rand.nextInt(3);
        this.currentAttackMovement = rand.nextInt(2);
        this.leapCooldownCurrent = leapCooldown;
        this.isPerformingLeap = false;

        this.specialCooldownCurrent = specialCooldown;
        this.teleportX = this.teleportY = this.teleportZ = 0.0D;

        this.wasOnGroundPreviousTick = false;
        this.isPerformingSpecial = false;
        this.shouldExplodeOnLanding = false;
        this.landingExplosionWait = 5;
        this.specialSequenceAt = 1;
    }

    @Override
    protected void entityInit() 
    {
        super.entityInit();
    }


    public void writeEntityToNBT(NBTTagCompound compound) 
    {
        super.writeEntityToNBT(compound);


        compound.setInteger("QuazarSize", quazarSize);

        compound.setInteger("LeapWarning", leapWarning);
        compound.setInteger("LeapCooldown", leapCooldown);
        compound.setInteger("LeapLandingRadius", leapLandingRadius);
        compound.setFloat("LeapLandingDamage", leapLandingDamage);
        compound.setFloat("LeapLaunchMultiplier", leapLaunchMultiplier);

        compound.setInteger("SpecialWarning", specialWarning);
        compound.setInteger("SpecialCooldown", specialCooldown);
        compound.setInteger("SpecialSequenceCooldown", specialSequenceCooldown);
        compound.setInteger("SpecialSequenceMax", specialSequenceMax);
        compound.setInteger("MeteorLandingRadius", meteorLandingRadius);
        compound.setFloat("MeteorLandingDamage", meteorLandingDamage);
        compound.setFloat("MeteorLaunchMultiplier", meteorLaunchMultiplier);




        compound.setString("BehaviorState", behaviorState);

//      ArrayList<Integer> currentAttacksProjectile = new ArrayList<>();
//      ArrayList<Integer> currentAttacksMovement = new ArrayList<>();
        compound.setInteger("CurrentAttackProjectile", currentAttackProjectile);
        compound.setInteger("CurrentAttackMovement", currentAttackMovement);
        compound.setInteger("LeapCooldownCurrent", leapCooldownCurrent);
        compound.setBoolean("IsPerformingLeap", isPerformingLeap);

        compound.setInteger("SpecialCooldownCurrent", specialCooldownCurrent);
        compound.setDouble("TeleportX", teleportX);
        compound.setDouble("TeleportY", teleportY);
        compound.setDouble("TeleportZ", teleportZ);

        compound.setBoolean("WasOnGroundPreviousTick", wasOnGroundPreviousTick);
        compound.setBoolean("IsPerformingSpecial", isPerformingSpecial);
        compound.setBoolean("ShouldExplodeOnLanding", shouldExplodeOnLanding);
        compound.setInteger("LandingExplosionWait", landingExplosionWait);
        compound.setInteger("SpecialSequenceAt", specialSequenceAt);
    }

    public void readEntityFromNBT(NBTTagCompound compound) 
    {
        super.readEntityFromNBT(compound);
        if (this.hasCustomName()) { this.bossInfo.setName(this.getDisplayName()); }


        if (compound.hasKey("QuazarSize")) { quazarSize = compound.getInteger("QuazarSize"); }
        this.setSlimeSize(this.quazarSize * 2, true);

        if (compound.hasKey("LeapWarning")) { leapWarning = compound.getInteger("LeapWarning"); }
        if (compound.hasKey("LeapCooldown")) { leapCooldown = compound.getInteger("LeapCooldown"); }
        if (compound.hasKey("LeapLandingRadius")) { leapLandingRadius = compound.getInteger("LeapLandingRadius"); }
        if (compound.hasKey("LeapLandingDamage")) { leapLandingDamage = compound.getFloat("LeapLandingDamage"); }
        if (compound.hasKey("LeapLaunchMultiplier")) { leapLaunchMultiplier = compound.getFloat("LeapLaunchMultiplier"); }

        if (compound.hasKey("SpecialWarning")) { specialWarning = compound.getInteger("SpecialWarning"); }
        if (compound.hasKey("SpecialCooldown")) { specialCooldown = compound.getInteger("SpecialCooldown"); }
        if (compound.hasKey("SpecialSequenceCooldown")) { specialSequenceCooldown = compound.getInteger("SpecialSequenceCooldown"); }
        if (compound.hasKey("SpecialSequenceMax")) { specialSequenceMax = compound.getInteger("SpecialSequenceMax"); }
        if (compound.hasKey("MeteorLandingRadius")) { meteorLandingRadius = compound.getInteger("MeteorLandingRadius"); }
        if (compound.hasKey("MeteorLandingDamage")) { meteorLandingDamage = compound.getFloat("MeteorLandingDamage"); }
        if (compound.hasKey("MeteorLaunchMultiplier")) { meteorLaunchMultiplier = compound.getFloat("MeteorLaunchMultiplier"); }




        if (compound.hasKey("BehaviorState")) { behaviorState = compound.getString("BehaviorState"); }

//      ArrayList<Integer> currentAttacksProjectile = new ArrayList<>();
//      ArrayList<Integer> currentAttacksMovement = new ArrayList<>();
        if (compound.hasKey("CurrentAttackProjectile")) { currentAttackProjectile = compound.getInteger("CurrentAttackProjectile"); }
        if (compound.hasKey("CurrentAttackMovement")) { currentAttackMovement = compound.getInteger("CurrentAttackMovement"); }
        if (compound.hasKey("LeapCooldownCurrent")) { leapCooldownCurrent = compound.getInteger("LeapCooldownCurrent"); }
        if (compound.hasKey("IsPerformingLeap")) { isPerformingLeap = compound.getBoolean("IsPerformingLeap"); }

        if (compound.hasKey("SpecialCooldownCurrent")) { specialCooldownCurrent = compound.getInteger("SpecialCooldownCurrent"); }
        if (compound.hasKey("TeleportX")) { teleportX = compound.getDouble("TeleportX"); }
        if (compound.hasKey("TeleportY")) { teleportY = compound.getDouble("TeleportY"); }
        if (compound.hasKey("TeleportZ")) { teleportZ = compound.getDouble("TeleportZ"); }

        if (compound.hasKey("WasOnGroundPreviousTick")) { wasOnGroundPreviousTick = compound.getBoolean("WasOnGroundPreviousTick"); }
        if (compound.hasKey("IsPerformingSpecial")) { isPerformingSpecial = compound.getBoolean("IsPerformingSpecial"); }
        if (compound.hasKey("ShouldExplodeOnLanding")) { shouldExplodeOnLanding = compound.getBoolean("ShouldExplodeOnLanding"); }
        if (compound.hasKey("LandingExplosionWait")) { landingExplosionWait = compound.getInteger("LandingExplosionWait"); }
        if (compound.hasKey("SpecialSequenceAt")) { specialSequenceAt = compound.getInteger("SpecialSequenceAt"); }
    }


    @Override
    protected void initEntityAI()
    {
/*
        this.tasks.addTask(1, new EntitySlime.AISlimeFloat(this));
        this.tasks.addTask(2, new EntitySlime.AISlimeAttack(this));
        this.tasks.addTask(3, new EntitySlime.AISlimeFaceRandom(this));
        this.tasks.addTask(5, new EntitySlime.AISlimeHop(this));
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
        this.targetTasks.addTask(3, new EntityAIFindEntityNearest(this, EntityIronGolem.class));
*/

        this.targetTasks.addTask(1, new EntityAIRelentlessTargetPlayers(this, 300.0D));
//      this.tasks.addTask(0, new EntityAIStun(this));
    }
    

    public void resetBehaviorState()
    {
//Reset leap cooldown
        this.leapCooldownCurrent = BetterSlimesConfigMobs.leapCooldown; 
//Reset leap state
        this.isPerformingLeap = false;  
//Reset special cooldown
        this.specialCooldownCurrent = BetterSlimesConfigMobs.specialWarning;
//Reset performing special
        this.behaviorState = "default";
//Reset leap state and leaps performed
        this.isPerformingSpecial = false;
        this.specialSequenceAt = 1;
//Reset landing explosions
        this.shouldExplodeOnLanding = false;
        this.landingExplosionWait = 5;
    }




//CLIENT SIDE STUFF HERE
    @Override
    public void onUpdate() 
    {
//When Quazar lands
        if (this.onGround && !this.wasOnGroundPreviousTick)
        {
//Execute landing VFX
            this.onLandingNormal();
        }


//Set if was on ground previous tick
        this.wasOnGroundPreviousTick = this.onGround;


        super.onUpdate();
    }




//SERVER SIDE STUFF HERE
    public void onLivingUpdate() 
    {
//Super update
        super.onLivingUpdate();


/*
//Update is server-side
        if (this.world.isRemote) 
        {
            return;
        }

        System.out.println("=== QUAZAR STATE DEBUG ===\n" +
            "Timers: LeapCD=" + leapCooldownCurrent + "/" + leapCooldown + ", SpecialCD=" + specialCooldownCurrent + "/" + specialCooldown + "\n" +
            "LeapSeq: At=" + specialSequenceAt + "/" + specialSequenceMax + ", Warn=" + leapWarning + "/" + specialWarning + "\n" +
            "Attacks: Proj=" + currentAttackProjectile + ", Move=" + currentAttackMovement + "\n" +
            "States: Behavior='" + behaviorState + "', IsLeap=" + isPerformingLeap + ", IsSpecLeap=" + isPerformingSpecial + "\n" +
            "Flags: Explode=" + shouldExplodeOnLanding + ", Wait=" + landingExplosionWait + ", GroundPrev=" + wasOnGroundPreviousTick);
*/




        EntityLivingBase attackTarget = this.getAttackTarget();
//If this has target
        if (attackTarget != null) 
        {
            System.out.println("=== QUAZAR HAS TARGET ===");
//		    this.getLookHelper().setLookPositionWithEntity(attackTarget, 10.0F, 10.0F);
            this.faceEntity(attackTarget, 10.0F, 10.0F);
            this.rotationYawHead = this.rotationYaw;
            this.renderYawOffset = this.rotationYaw;
//          this.setPositionAndUpdate(this.posX, this.posY, this.posZ);
//          ((EntitySlime.SlimeMoveHelper)this.getMoveHelper()).setDirection(this.rotationYaw, true);
//          this.rotationYawHead = this.rotationYaw;
//          this.renderYawOffset = this.rotationYaw;


/*
//Every 30 seconds
            if((this.ticksExisted % 200) == 0)
            {
                double radianAt = 0.0D;

                for(int pieceAt = 0; pieceAt < 1; pieceAt++)
                {
                    double randomDistance = 8.0D + (rand.nextDouble() * 16.0D);
                    

                    EntityEarthPiece earthPiece = new EntityEarthPiece(this.world,
                        this.posX + randomDistance * Math.cos(radianAt), this.posY + 4.0D, this.posZ + randomDistance * Math.sin(radianAt),
                        this, 
                        "spin", "cube", 2,
                        20, 
                        20, 1.0D,
                        10, 16.0D,
                        40, 1.0D,
                        40, 3.0D, 2.0D, 0.08D);

                    earthPiece.setPieceSpin(100 + ((pieceAt + 1) * 25), 24.0D, radianAt, 20);

                    this.world.spawnEntity(earthPiece);

                    radianAt += 0.25D * Math.PI;
                }
            }
*/


//Default state
            if(this.behaviorState.equals("default")) 
            {
//If not at special attack 
                if(this.specialCooldownCurrent > this.specialWarning)
                {
//If boss already should explode on landing
                    if(this.shouldExplodeOnLanding)
//Decrement explosion wait
                    {
                        --this.landingExplosionWait;
                    }

//Execute regular attacks
                    this.executeRegularAttacks();
//Decrement boss leap countdown if not mid-leap
                    if(this.leapCooldownCurrent > 0 && !this.isPerformingLeap) { this.leapCooldownCurrent--; }
//Execute regular leaps
                    this.executeRegularLeaps();
//Check if should explode on landing
                    this.checkExplode();
                }
//When boss special countdown first reaches warning stage
                else if (this.specialCooldownCurrent == this.specialWarning) 
                {
                    if(!this.world.isRemote)
                    {
                        this.world.playSound(null, 
                            attackTarget.posX, attackTarget.posY, attackTarget.posZ,
                            EternityModeSoundEvents.ENTITY_QUAZAR_NUKE_ALARM, SoundCategory.HOSTILE, 8.0F, 1.0F);
                    }

//Set preparing leap
                    this.behaviorState = "preparingleap";
                }
            }




//Preparing leap state
            else if(this.behaviorState.equals("preparingleap")) 
            { 
//Don't move
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);


//Prepare portals
                if (!this.world.isRemote && this.specialCooldownCurrent == 25)
                {
/*
                    EntityOrbVoidCustom portalOrb 
                        = new EntityOrbVoidCustom(this.world, null, this, 3, 3, (float) (1.5D * this.width), (float) (1.5D * this.width), 10, 25);
                        portalOrb.dontVisualExplosion = true;
                        portalOrb.dontSoundActive = true;
                        portalOrb.dontSoundWarning = true;  
                    portalOrb.setLocationAndAngles(this.posX, this.posY + (this.height / 2.0D), this.posZ, this.rotationYaw, 0.0F);
                    this.getEntityWorld().spawnEntity(portalOrb);
*/


                    double distanceX = this.getAttackTarget().posX - this.posX;
                    double distanceZ = this.getAttackTarget().posZ - this.posZ;
                    double targetRadians = Math.atan2(distanceZ, distanceX);
                    this.teleportX = attackTarget.posX + (Math.cos(targetRadians) * 50.0D) + (4.0D * (rand.nextDouble() - rand.nextDouble()));
                    this.teleportY = attackTarget.posY + 72.0D + (8.0D * rand.nextDouble());
                    this.teleportZ = attackTarget.posZ + (Math.sin(targetRadians) * 50.0D) + (4.0D * (rand.nextDouble() - rand.nextDouble()));


//No teleporting to nether ceiling
                    if(this.dimension == -1)
                    {
                        this.teleportY = Math.min(this.teleportY, (124.0D - this.height));
                    }


/*
                    EntityOrbVoidCustom arrivalOrb 
                        = new EntityOrbVoidCustom(this.world, null, this, 3, 3, (float) (1.5D * this.width), (float) (1.5D * this.width), 10, 25);
                        arrivalOrb.dontVisualExplosion = true;
                        arrivalOrb.dontSoundActive = true;
                        arrivalOrb.dontSoundWarning = true; 
                    arrivalOrb.setLocationAndAngles(teleportX, teleportY, teleportZ, this.rotationYaw, 0.0F);
                    this.getEntityWorld().spawnEntity(arrivalOrb);  
*/
                }


//When boss special countdown reaches zero
                if (this.specialCooldownCurrent < 1)
                {
//Set executing leap
                    this.behaviorState = "leapingspecial";
                }
            }




//Leap executing state
            else if(this.behaviorState.equals("leapingspecial")) 
            {
//If boss already should explode on landing
                if(this.shouldExplodeOnLanding)
//Decrement explosion wait
                {
                    --this.landingExplosionWait;
                }


//Set up teleport coords and portals
                if(!this.world.isRemote && this.specialCooldownCurrent == 25 && !this.isPerformingSpecial)
                {
/*
                    EntityOrbVoidCustom portalOrb 
                        = new EntityOrbVoidCustom(this.world, null, this, 3, 3, (float) (1.5D * this.width), (float) (1.5D * this.width), 10, 25); 
                        portalOrb.dontVisualExplosion = true;
                        portalOrb.dontSoundActive = true;
                        portalOrb.dontSoundWarning = true;  
                    portalOrb.setLocationAndAngles(this.posX, this.posY + (this.height / 2.0D), this.posZ, this.rotationYaw, 0.0F);
                    this.getEntityWorld().spawnEntity(portalOrb);
*/


                    double distanceX = this.getAttackTarget().posX - this.posX;
                    double distanceZ = this.getAttackTarget().posZ - this.posZ;
                    double targetRadians = Math.atan2(distanceZ, distanceX);
                    this.teleportX = attackTarget.posX + (Math.cos(targetRadians) * 50.0D) + (6.0D * (rand.nextDouble() - rand.nextDouble()));
                    this.teleportY = attackTarget.posY + 64.0D + (16.0D * rand.nextDouble());
                    this.teleportZ = attackTarget.posZ + (Math.sin(targetRadians) * 50.0D) + (6.0D * (rand.nextDouble() - rand.nextDouble()));


//No teleporting to nether ceiling
                    if(this.dimension == -1)
                    {
                        this.teleportY = Math.min(this.teleportY, (124.0D - this.height));
                    }


/*
                    EntityOrbVoidCustom arrivalOrb 
                        = new EntityOrbVoidCustom(this.world, null, this, 3, 3, (float) (1.5D * this.width), (float) (1.5D * this.width), 10, 25); 
                        arrivalOrb.dontVisualExplosion = true;
                        arrivalOrb.dontSoundActive = true;
                        arrivalOrb.dontSoundWarning = true; 
                    arrivalOrb.setLocationAndAngles(teleportX, teleportY, teleportZ, this.rotationYaw, 0.0F);
                    this.getEntityWorld().spawnEntity(arrivalOrb);     
*/              
                }


//When boss special countdown 
//is at zero and this isn't already leaping
                if (this.specialCooldownCurrent == 0 && !this.isPerformingSpecial)
                {
//Perform the special leap
                    this.executeLeapSpecial(attackTarget);
//Wait a few ticks so the boss doesn't explode immediately after leaping
                    --this.landingExplosionWait;
               }
            }


//Universal logic if has target...

//Decrement boss special countdown if not mid-special
            if(this.specialCooldownCurrent > 0 && !this.isPerformingSpecial) 
            {
//And if performing regular leap, pause above leap warning
                if(!(this.isPerformingLeap && (this.specialCooldownCurrent <= (this.specialWarning + 1))))
                { 
                    this.specialCooldownCurrent--;
                }
            }

//Check if should explode on landing
            this.checkSpecialExplode();
        }
//If no target
        else 
        {
//Reset boss state
            this.resetBehaviorState();
        }
    }




    protected void executeRegularAttacks()
    {
        if(!this.world.isRemote)
        {
/*
            if(this.currentAttackProjectile == 0 && ((this.ticksExisted % 30) == 0))
                { this.executeHomingVolley(); }
*/
            if((this.ticksExisted % 30) == 0) { this.executeHomingVolley(); }
            if(this.currentAttackProjectile == 1 && ((this.ticksExisted % 75) == 0))
                { this.executeBouncingVolley(); }
            if(this.currentAttackProjectile == 2 && ((this.ticksExisted % 200) == 0))
                { this.executeShinraTensei(); } 
        }
    }


//If on ground, not leaping,
//leap at warning time, and not close to special attack...
    protected void executeRegularLeaps()
    {
        if(this.onGround && !this.isPerformingLeap && this.leapCooldownCurrent <= 0
        && (this.specialCooldownCurrent - this.specialWarning) > (this.leapWarning + 1))
        {
//Make scheduled leap
            QueuedActionAtPos scheduledLeap = new QueuedActionAtPos(this.posX, this.posY, this.posZ, this.world.getTotalWorldTime() + this.leapWarning, 0); 
//Add scheduled leap to this
            ((IMixinEntityLiving) this).addQueuedAction(scheduledLeap);
//Reset leap cooldown
            this.leapCooldownCurrent = 100;
        }
    }


    protected void executeLeap(EntityLivingBase leapTarget)
    {
//Set performing leap
        this.isPerformingLeap = true;
//Set to explode on landing
        this.shouldExplodeOnLanding = true;

//Get target distance
        double distanceX = this.getAttackTarget().posX - this.posX;
        double distanceZ = this.getAttackTarget().posZ - this.posZ;


        this.setPositionAndUpdate(this.posX, this.posY + 2, this.posZ);


        if (!this.world.isRemote) 
        {
            this.world.playSound(null, 
                leapTarget.posX, leapTarget.posY, leapTarget.posZ,
                EternityModeSoundEvents.ENTITY_QUAZAR_LEAP_WHOOSH, SoundCategory.HOSTILE, 5.0F, 1.0F);


            int leapTypeChosen = this.rand.nextInt(4);


            switch (leapTypeChosen) 
            {
//Leap on target
                case 0:
                    this.motionX = distanceX / 6.5D;
                    this.motionY = 2.0D;
                    this.motionZ = distanceZ / 6.5D;
                    break;
//Leap behind target
                case 1:
                    this.motionX = distanceX / (4.5D);
                    this.motionY = 2.0D;
                    this.motionZ = distanceZ / (4.5D);
                    break;
//Side-leap 
                case 2:
                    double baseRadians = Math.atan2(distanceZ, distanceX);
                    double radiansSideways = 0.0D;
                    
                    int whichWay = rand.nextInt(2);

                    if(whichWay == 0) { radiansSideways = baseRadians + (0.5 * Math.PI); }
                    if(whichWay == 1) { radiansSideways = baseRadians - (0.5 * Math.PI); }

                    double targetHorizontalDistance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);

                    this.motionX = (distanceX + (Math.cos(radiansSideways) * 20.0D)) / 5.0D;
                    this.motionY = 1.0D;
                    this.motionZ = (distanceZ + (Math.sin(radiansSideways) * 20.0D)) / 5.0D;
                    break;
//Predictive leap
                case 3:
                    this.motionX = (distanceX + (leapTarget.motionX * 20)) / 5.5D;
                    this.motionY = 1.5D;
                    this.motionZ = (distanceZ + (leapTarget.motionZ * 20)) / 5.5D;
                    break; 
            }
        }
    }


    private void checkExplode() 
    {
        if (!this.shouldExplodeOnLanding || !this.onGround || (this.landingExplosionWait > 0)) 
        {
            return;
        }


        ExplosionUtil.performOptimizedExplosion(this.world, this, this.posX, this.posY + (this.height / 2), this.posZ,
        this.leapLandingRadius + (this.width / 2.0D), true, this.leapLandingDamage, true, 6.0D * (double) this.leapLaunchMultiplier, false, 9999.0F, true, 
        true, 1, false);


        MiscUtil.screenShakeForNearbyPlayers(10);


        if(!this.world.isRemote)
        {
            this.world.playSound(null, 
                this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ,
                EternityModeSoundEvents.ENTITY_QUAZAR_LANDING_EXPLOSION, SoundCategory.HOSTILE, 5.0F, 1.0F);


//8 directional shockwaves
            for(int shockwaveAt = 0; shockwaveAt < 8; shockwaveAt++)
            {
                EntityExplosiveShockwave shockwave = new EntityExplosiveShockwave(this.world, this.posX, this.posY, this.posZ,
                    this, 
                    80, false, 3.0F, 0.5D * Math.cos(Math.PI * 0.25D * shockwaveAt), 0.0D, 0.5D * Math.sin(Math.PI * 0.25D * shockwaveAt), 1.044D,
                    true, 4.5D, 20.0D,
                    1, 4.0D, 5.0F,
                    true, 2.0D, false, 1, 3);
                shockwave.setSubshockwaves(true, 50,
                    0.0D, 3.0D, 0.0D, 1.021D,
                    6, 3.0D);
		        shockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

		        this.getEntityWorld().spawnEntity(shockwave);
            }

//Target for targeted shockwaves
            EntityLivingBase quazarAttackTarget = this.getAttackTarget();

            if (quazarAttackTarget != null)
            {
                double baseRadians = Math.atan2(quazarAttackTarget.posZ - this.posZ, quazarAttackTarget.posX - this.posX);

                for(int angleAt = -2; angleAt <= 2; angleAt++)
                {
                    EntityExplosiveShockwave shockwave = new EntityExplosiveShockwave(this.world,this.posX, this.posY, this.posZ,
                        this,  
                        40, false, 3.0F, 2.25D * Math.cos(baseRadians + (Math.PI * 0.125D * angleAt)), 0.0D, 2.25D * Math.sin(baseRadians + (Math.PI * 0.125D * angleAt)), 1.021D,
                        true, 1.5D, 20.0D,
                        1, 4.0D, 5.0F,
                        true, 2.0D, false, 1, 2);
                    shockwave.setSubshockwaves(true, 25,
                        0.0D, 4.5D, 0.0D, 1.044D,
                        4, 3.0D);
		            shockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

		            this.getEntityWorld().spawnEntity(shockwave);
                }
            }
        }


//Finish regular explosion by...

//No longer exploding on landing
        this.shouldExplodeOnLanding = false;
//Setting not performing leap
        this.isPerformingLeap = false;
//Wait a bit before explosion again
        this.landingExplosionWait = 5;     
    }




    protected void executeHomingVolley()
    {
//Angle
        double currentRadians = Math.atan2(this.getAttackTarget().posZ - this.posZ, this.getAttackTarget().posX - this.posX);

//Horizontal distance
        double targetHorizontalDistance = Math.sqrt(Math.pow(this.getAttackTarget().posX - this.posX, 2) + Math.pow(this.getAttackTarget().posZ - this.posZ, 2));


//Random extra duration
        int extraDuration = rand.nextInt(30);


        EntityMeteorBlockHoming meteorHoming = new EntityMeteorBlockHoming
        (
            this.world, this.posX, this.posY + (this.height / 2.4D), this.posZ,
            this, true, 18.0F,
            160, 
            (targetHorizontalDistance + 32.0D) * Math.cos(currentRadians) / (30 + extraDuration), 
            0.25D, 
            (targetHorizontalDistance + 32.0D) * Math.sin(currentRadians) / (30 + extraDuration), 
            1.0D, 0.0D, 
            12.0F, false, true,
            200, Math.PI, 1,
            9.0F, 1.0D, 1.01D, 
            true, false, 0.5F, false, false,
            30 + extraDuration, true, 100, 1.0D, 1,
            true, false, 90.0D, 20.0D
        );

        this.world.spawnEntity(meteorHoming);
    }


    protected void executeBouncingVolley()
    {
        ArrayList<Vec3d> bouncingShotgun = ProjectileUtil.flexibleFibonnaciShotgunCoord
            (this.posX, this.posY + 16.0D, this.posZ, 
            this, this.getAttackTarget(),
            60, 0.4D * Math.PI, rand.nextInt(2), 1.0D);


        for(int projectileAt = 0; projectileAt < 60; projectileAt++)
        {
            Vec3d currentDirection = bouncingShotgun.get(projectileAt);


            EntityFlameShotBouncing flameShotBouncing = new EntityFlameShotBouncing
            (
                this.world,
                this.posX, this.posY + 16.0D, this.posZ,
                this,
                120, 
                currentDirection.x, 
                currentDirection.y, 
                currentDirection.z, 
                1.01D, 0.08D, 
                0.3D, true, false, 1.0F, 
                5, 2, 0.06D,
                true, 20,
                false, true, 0.3D,
                20, true, 0.5F, false, false
            );
            flameShotBouncing.posY = this.posY + 16.0D;


            this.world.spawnEntity(flameShotBouncing);
        }
    }

    
    protected void executeShinraTensei()
    {
        EntityOrbVoidCustom shinraTensei 
            = new EntityOrbVoidCustom(this.world, null, this, 7, 5, 2.0F, 2.0F, 280, 300);
        shinraTensei.orbCustomType = "homingfountain";
        shinraTensei.setOrbFountain
            (5.0D, 5.0D, 8.0D, 1.0D, 1.0F,
            250, 150,
            1.0D, true, false, 90.0D, 20.0D);
        shinraTensei.setLocationAndAngles(this.posX, this.posY + (this.height / 2.0D), this.posZ, this.rotationYaw, 0.0F);
        this.getEntityWorld().spawnEntity(shinraTensei);
    }




    protected void executeLeapSpecial(EntityLivingBase leapTarget) 
    {
//Set performing leap
        this.isPerformingSpecial = true;
//Set to explode on landing
        this.shouldExplodeOnLanding = true;

/*
//Get target distance
        double distanceX = this.getAttackTarget().posX - this.posX;
        double distanceZ = this.getAttackTarget().posZ - this.posZ;


        this.setPositionAndUpdate(this.posX, this.posY + 2, this.posZ);


        if (!this.world.isRemote) 
        {
            this.world.playSound(null, 
                leapTarget.posX, leapTarget.posY, leapTarget.posZ,
                EternityModeSoundEvents.ENTITY_QUAZAR_LEAP_WHOOSH, SoundCategory.HOSTILE, 5.0F, 1.0F);


            int leapTypeChosen = this.rand.nextInt(4);


            switch (leapTypeChosen) 
            {
//Leap on target
                case 0:
                    this.motionX = distanceX / 6.5D;
                    this.motionY = 2.0D;
                    this.motionZ = distanceZ / 6.5D;
                    break;
//Leap behind target
                case 1:
                    this.motionX = distanceX / (4.5D);
                    this.motionY = 2.0D;
                    this.motionZ = distanceZ / (4.5D);
                    break;
//Side-leap 
                case 2:
                    double baseRadians = Math.atan2(distanceZ, distanceX);
                    double radiansSideways = 0.0D;
                    
                    int whichWay = rand.nextInt(2);

                    if(whichWay == 0) { radiansSideways = baseRadians + (0.5D * Math.PI); }
                    if(whichWay == 1) { radiansSideways = baseRadians - (0.5D * Math.PI); }

                    double targetHorizontalDistance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);

                    this.motionX = (distanceX + (Math.cos(radiansSideways) * 20.0D)) / 5.0D;
                    this.motionY = 1.0D;
                    this.motionZ = (distanceZ + (Math.sin(radiansSideways) * 20.0D)) / 5.0D;
                    break;
//Predictive leap
                case 3:
                    this.motionX = (distanceX + (leapTarget.motionX * 20.0D)) / 5.5D;
                    this.motionY = 1.5D;
                    this.motionZ = (distanceZ + (leapTarget.motionZ * 20.0D)) / 5.5D;
                    break; 
            }
        }
*/
        ExplosionUtil.performOptimizedExplosion(this.world, this, this.teleportX, this.teleportY + (this.height / 2.0D), this.teleportZ,
            this.meteorLandingRadius + (this.width / 2.0D), true, this.meteorLandingDamage, true, 8.0D * (double) this.meteorLaunchMultiplier, true, 9999.0F, true, 
            true, 1, false);
/*
        ExplosionUtil.performOptimizedExplosion(this.world, this, this.teleportX, this.teleportY - (this.height / 2.0D), this.teleportZ,
            this.meteorLandingRadius, true, this.meteorLandingDamage, true, 8.0D * (double) this.meteorLaunchMultiplier, true, 9999.0F, true, 
            true, 2, false);
*/

        this.setPositionAndUpdate(this.teleportX, this.teleportY, this.teleportZ);
    }


//When falling during special attack
    protected void performSpecialFall()
    {
//Accelerate downwards
        if(this.motionY > (-1.0D * this.meteorLandingRadius / 6.0D))
        {
            this.motionY -= 0.16D;
        }


//Explode periodically
        if(this.livingEntityMixin.getRealTicksExisted() % 5 == 0)
        {
            ExplosionUtil.performOptimizedExplosion(this.world, this, this.posX, this.posY + (this.height / 2.0D), this.posZ,
                this.meteorLandingRadius + (this.width / 2.0D), true, this.meteorLandingDamage, true, 8.0D * (double) this.meteorLaunchMultiplier, true, 9999.0F, true, 
                true, 1, false);
/*
            ExplosionUtil.performOptimizedExplosion(this.world, this, this.posX, this.posY - (this.height / 2.0D), this.posZ,
                this.meteorLandingRadius + (this.width / 2.0D), true, this.meteorLandingDamage, true, 8.0D * (double) this.meteorLaunchMultiplier, true, 9999.0F, true, 
                true, 2, false);
*/
        }
    }


//Check for special explosion
    private void checkSpecialExplode() 
    {
        if (this.shouldExplodeOnLanding && this.isPerformingSpecial)
        {
            EntityLivingBase attackTarget = this.getAttackTarget();


//Check for if in air, Y high and above target
            if((!this.onGround && this.posY > 6.0D && (this.posY > attackTarget.posY)) || (this.landingExplosionWait > 0))
            {
                this.performSpecialFall();
            }
            else
            {
//Track if last leap in sequence
                boolean lastLeapInSequence = (this.specialSequenceAt >= this.specialSequenceMax);


                ExplosionUtil.performOptimizedExplosion(this.world, this, this.posX, this.posY + (this.height / 2), this.posZ,
                this.meteorLandingRadius + (this.width / 2.0D), true, this.meteorLandingDamage, true, 8.0D * (double) this.meteorLaunchMultiplier, false, 9999.0F, true, 
                true, 1, false);


                MiscUtil.screenShakeForNearbyPlayers(13);


                if(!this.world.isRemote)
                {
                    this.world.playSound(null, 
                        attackTarget.posX, attackTarget.posY, attackTarget.posZ,
                        EternityModeSoundEvents.ENTITY_QUAZAR_LANDING_EXPLOSION, SoundCategory.HOSTILE, 5.0F, 1.0F);

//Testing new eruption
                    this.scheduleLandingEruptionEncroaching(5, 30,
                        13, 6.0D, 2.0D, 0.5D);




//Now the fireball spread
//Get target distance
                    double horizontalDistance = 
                        Math.sqrt(Math.pow(attackTarget.posX - this.posX, 2) + Math.pow(attackTarget.posZ - this.posZ, 2));
//And angle
                    double flameStartingRadians = 
                        Math.atan2(attackTarget.posZ - this.posZ, attackTarget.posX - this.posX);


                    for(int meteorShotAt = -4; meteorShotAt < 4; meteorShotAt++)
                    {
//Get distance addition (Up to 32 blocks offset)
                        double totalHorizontalDistance = horizontalDistance + 60.0D + (5.0D * (rand.nextDouble()));
//                      if(totalHorizontalDistance > 80.0D) { totalHorizontalDistance = 80.0D; }

//Up to double the angle and projectile count randomly
                        double randomSpreadScale = rand.nextDouble();

/*
                        EntityFlameShotLinearSplits flameShotExplosive = new EntityFlameShotLinearSplits
                        (
                            this.world,
                            this.posX, this.posY + (this.height / 2.0D), this.posZ,
                            this, 
                            15, 
                            Math.cos(flameStartingRadians + (0.1D * Math.PI * flameShotAt)) * (totalHorizontalDistance) / 15.0D,
                            2.5D * (1.0D + (rand.nextDouble() * 0.2D)),
                            Math.sin(flameStartingRadians + (0.1D * Math.PI * flameShotAt)) * (totalHorizontalDistance) / 15.0D,
                            1.0D, 0.04D, 
                            1.2D, true, true, 5.0F, 
                            10, 5, 0.06D,
                            (int) (50 * randomSpreadScale), (0.175D * Math.PI) * randomSpreadScale, 0,
                            1.0F, 1.5D, 1.04D,
                            20, true, 6.0F, true, true,
                            false, 0.5F, false, false
                        );
                        flameShotExplosive.setNoGravity(false);
                        flameShotExplosive.setLocationAndAngles(this.posX, this.posY + (this.height / 2.0D), this.posZ, this.rotationYaw, 0.0F);
                        this.getEntityWorld().spawnEntity(flameShotExplosive);
*/

                        EntityMeteorBlock meteorExplosive = new EntityMeteorBlock
                        (
                            this.world, this.posX, this.posY + (this.height / 2.0D), this.posZ, 
                            this, true, this.meteorLandingDamage,
                            15, 
                            Math.cos(flameStartingRadians + (0.125D * Math.PI * meteorShotAt)) * (totalHorizontalDistance) / 15.0D,
                            2.5D * (1.0D + (rand.nextDouble() * 0.2D)),
                            Math.sin(flameStartingRadians + (0.125D * Math.PI * meteorShotAt)) * (totalHorizontalDistance) / 15.0D,
                            1.0D, 0.0D,
                            (float) (this.width / 2.0D), false, true,
                            (int) (125 - (randomSpreadScale * 25)), (0.225D + (0.075D * randomSpreadScale)) * Math.PI, 0,
                            10.0F, 2.0D, 1.01D,
                            true, false, 6.9420F, false, false
                        );
                        meteorExplosive.setNoGravity(true);
                        meteorExplosive.setLocationAndAngles(this.posX, this.posY + (this.height / 2.0D), this.posZ, this.rotationYaw, 0.0F);
                        this.getEntityWorld().spawnEntity(meteorExplosive);
                    }
                }


//Finish special explosion by...

//No longer exploding on landing
                this.shouldExplodeOnLanding = false;
//Setting not performing leap
                this.isPerformingSpecial = false;


//If not max leap
                if(this.specialSequenceAt < this.specialSequenceMax) 
                { 
//Applying short cooldown
                    this.specialCooldownCurrent = this.specialSequenceCooldown;
//Incrementing leaps executed
                    ++this.specialSequenceAt;
//Wait a bit before explosion again
                    this.landingExplosionWait = 5;
                }

//If last leap
                else
                {
//Applying longer cooldown
                    this.specialCooldownCurrent = this.specialCooldown;
//Resetting leaps executed
                    this.specialSequenceAt = 1;
//Wait a bit before explosion again
                    this.landingExplosionWait = 5;
//Reset leap cooldown
                    this.leapCooldownCurrent = 200;     
//Resetting state
                    this.behaviorState = "default";   
                }
            }
        }
    }


/*
//How do i do this bruh smh my head
    public void scheduleLandingEruptionTotal(double eruptionPoints)
    {
            
    
    }
*/


//Closing in version
//This one specifically needs odd ringAmount to work properly
    public void scheduleLandingEruptionEncroaching(int baseDelay, int delayIncrement,
    int ringAmount, double ringDistanceInitial, double ringDistanceStep, double fillFraction)
    {
//For outwards and inwards iteration
        int encroachSteps = (ringAmount / 2);
        int middlePoint = (ringAmount / 2) + 1;


//Current distance from one ring to another
        double currentDistanceStep = 0.0D;


//Iterative encroach
        for(int encroachAt = 1; encroachAt <= encroachSteps; encroachAt++)
        {
            int inwardEncroachAt = (ringAmount + 1) - encroachAt;


//Sequential delay
            int delayAt = baseDelay + (delayIncrement * (encroachAt - 1));


//Sequential ring radii
            double outwardRingRadius = (double) AbsurdcraftMathUtils.simpleSummationDouble(ringDistanceInitial, ringDistanceStep, encroachAt);
//Sequential ring radii
            double inwardRingRadius = (double) AbsurdcraftMathUtils.simpleSummationDouble(ringDistanceInitial, ringDistanceStep, inwardEncroachAt);
            

//Outwards distance step (used as int for blast spot count)
            double distanceStepOutward = ringDistanceInitial + (ringDistanceStep * (0 + encroachAt));
//Inwards distance step (used as int for blast spot count)
            double distanceStepInward = ringDistanceInitial + (ringDistanceStep * ((ringAmount + 1) - encroachAt));


//Schedule outwards ring
            this.scheduleLandingEruptionRing(delayAt, outwardRingRadius, (int) distanceStepOutward, fillFraction);
//Schedule inwards ring
            this.scheduleLandingEruptionRing(delayAt, inwardRingRadius, (int) distanceStepInward, fillFraction);
        }


//Middle ring distance step
        currentDistanceStep = ringDistanceInitial + (ringDistanceStep * middlePoint);
//Middle ring radius
        double middleRingRadius = (double) AbsurdcraftMathUtils.simpleSummationDouble(ringDistanceInitial, ringDistanceStep, middlePoint);
//Schedule middle ring
        this.scheduleLandingEruptionRing(baseDelay + (delayIncrement * (middlePoint - 1)), middleRingRadius, (int) currentDistanceStep, fillFraction);
    }


//Schedule an eruption ring
    public void scheduleLandingEruptionRing(int delay, double ringRadius, int blastSpotCount, double fillFraction)
    {
//Get circumference from radius
        double ringCircumference = 2.0D * Math.PI * ringRadius;
//Get explosion size from 
//circumference, and fill fraction, and spot count
        double blastSize = 0.5D * (ringCircumference * fillFraction) / blastSpotCount;
//Get radianStep
        double radianStep = (2.0D * Math.PI) / (double) blastSpotCount;


//Schedule each spot to blast
        for(int spotAt = 1; spotAt <= blastSpotCount; spotAt++)
        {
//Relative to boss
            double spotX = this.posX + (ringRadius * Math.cos(radianStep * spotAt));
            double spotZ = this.posZ + (ringRadius * Math.sin(radianStep * spotAt));
            
            this.scheduleLandingEruptionSpot(spotX, spotZ, delay, blastSize);
        }        
    }


//Schedule location for an eruption
    public void scheduleLandingEruptionSpot(double atX, double atZ, int delay, double blastSize)
    {
        QueuedActionAtPos scheduledBlast = new QueuedActionAtPos(atX, 69420.0D, atZ, this.world.getTotalWorldTime() + delay, 1);
        scheduledBlast.actionScale = blastSize; 
//Add scheduled blast to this
        ((IMixinEntityLiving) this).addQueuedAction(scheduledBlast);
    }




//Executes a leap or individual eruption blast
    public void queuedActionExecute(QueuedActionAtPos queuedAction)
    {
//Execute leap
        if(queuedAction.actionType == 0)
        {
            EntityLivingBase attackTarget = this.getAttackTarget();
//If this has target
            if (attackTarget != null)
            { 
//If this has target
                this.executeLeap(attackTarget);
            }
        }
//Execute eruption
        else if(queuedAction.actionType == 1)
        {
//If position within standard simulation distance
            if(EntityUtil.isPosCloseToAnyPlayer(this.world, queuedAction.actionX, queuedAction.actionZ, 160.0D, false))
            {
//Warning shockwave
                EntityExplosiveShockwave warningShockwave = new EntityExplosiveShockwave(this.world, queuedAction.actionX, 40.0D, queuedAction.actionZ,
                    this,  
                    20, false, 3.0F, 
                    0.0D, 10.0D, 0.0D, 1.0D,
                    false, 3.0D, 9,
                    1, queuedAction.actionScale, 0.0F, 
                    false, 0.0D, false, 0, 69420);
                warningShockwave.harmlessSwitch = true;
//          warningShockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

                if(!this.world.isRemote) { this.getEntityWorld().spawnEntity(warningShockwave); }


/*
//Slower shockwave
            EntityExplosiveShockwave slowerShockwave = new EntityExplosiveShockwave(this.world, queuedAction.actionX, 0.0D, queuedAction.actionZ,
                this,  
                70, false, 3.0F, 
                0.0D, 0.6D, 0.0D, 1.044D,
                false, 3.0D, 9,
                5, queuedAction.actionScale, 20.0F, 
                false, 0.0D, false, 1, 69420);
//          slowerShockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

             if(!this.world.isRemote) { this.getEntityWorld().spawnEntity(slowerShockwave); }
*/


//Faster shockwave
                EntityExplosiveShockwave fasterShockwave = new EntityExplosiveShockwave(this.world, queuedAction.actionX, 0.0D, queuedAction.actionZ,
                    this,  
                    30, false, 3.0F, 
                    0.0D, 2.0D, 0.0D, 1.1D,
                    false, 3.0D, 9,
                    2, queuedAction.actionScale, 20.0F, 
                    false, 0.0D, false, 1, 69420);
//              fasterShockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

                if(!this.world.isRemote) { this.getEntityWorld().spawnEntity(fasterShockwave); }
            }
        }
    }




//Stun logic
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
//If attacked in the middle of max leap
        if(this.isPerformingSpecial && (this.specialSequenceAt >= this.specialSequenceMax))
        {
//If damage is melee and high enough
            if((source.damageType.equals("arrow") || source.damageType.equals("player"))
            && (amount >= (float) 10.0F))
            {
//And apply stun
                this.livingEntityMixin.setAbsurdcraftStunned(true, 200);
            }      
        }
        

        return super.attackEntityFrom(source, amount);
    }

    public void onAbsurdcraftStunnedExtra()
    {
        this.resetBehaviorState();
    }




    protected void onLandingNormal()
    {
//Get width
        int quazarWidth = (int) this.width;

//16 particles for each block of width
        for (int particleAt = 0; particleAt < quazarWidth * 16; particleAt++)
        {
            float randomAngle = this.rand.nextFloat() * (2F * (float) Math.PI);
            float quarterToFull = 0.25F + this.rand.nextFloat() * 0.75F;

//Particle offset in random angle
//and multiplied by 0.25-1.0 Quazar's width
            float particleOffsetX = MathHelper.sin(randomAngle) * (float) quazarWidth * quarterToFull;
            float particleOffsetZ = MathHelper.cos(randomAngle) * (float) quazarWidth * quarterToFull;

            World world = this.world;

//Offset from boss position
            double particlePositionX = this.posX + (double) particleOffsetX;
            double particlePositionZ = this.posZ + (double) particleOffsetZ;

//Red slime particles
            world.spawnParticle(EnumParticleTypes.ITEM_CRACK, particlePositionX, this.getEntityBoundingBox().minY, particlePositionZ, 0.0D, 0.0D, 0.0D, Item.getIdFromItem(Item.getByNameOrId("betterslimes:red_slime")));

//Spawn half as many lava particles
            if (particleAt % 2 == 0) 
            {
                world.spawnParticle(EnumParticleTypes.LAVA, particlePositionX, this.getEntityBoundingBox().minY, particlePositionZ, 0.0D, 0.0D, 0.0D);
            }
        }
    }




    @Override
    public void addTrackingPlayer(EntityPlayerMP player) 
    {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }


    public void removeTrackingPlayer(EntityPlayerMP player) 
    {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }




//Don't split
    @Override
    public void setDead() 
    {
        this.isDead = true;
    }


    @Override
    public boolean getCanSpawnHere() 
    {
        if (this.world.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(rand, world)) 
        {
            return false;
        } 
        else 
        {
            if (this.world.getDifficulty() != EnumDifficulty.PEACEFUL) 
            {
                return true;
            }

            return false;
        }
    }


    @Override
    protected int getAttackStrength() 
    {
        return (int) (1.0D * attackMod);
    }


    @Override
    protected void applyEntityAttributes() 
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }


    @Nullable
    protected ResourceLocation getLootTable() 
    {
        return LootTables.quazarLT;
    }




    @Override
    public boolean canBePushed() 
    {
        return false;
    }

    public void setCustomNameTag(String name) 
    {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }


// Needs to be true in order to disable vanilla slime particles.
// Custom particles are implemented in the OnUpdate method
    @Override
    protected boolean spawnCustomParticles() { return true; }

// Disable cobweb slowdown
    @Override
    public void setInWeb() { }

// Disable water pushing
    @Override
    public boolean isPushedByWater() { return false; }

// Makes entity unaffected by water
    @Override
    public boolean isInWater() { return false; }

// Makes entity unaffected by lava
    @Override
    public boolean isInLava() { return false; }

// Disable fall damage so the boss doesn't kill itself when it leaps
    @Override
    public boolean isEntityInvulnerable(DamageSource source) 
    {
        if (source == DamageSource.FALL) 
        { return true; }

        return super.isEntityInvulnerable(source);
    }

//    @Override
//    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
//    {
//        return SoundsHandler.ENTITY_QUAZAR_HURT;
//    }

//    @Override
//    protected SoundEvent getDeathSound()
//    {
//        return SoundsHandler.ENTITY_QUAZAR_DEATH;
//    }
//
    // Idle sound
//    @Override
//    protected SoundEvent getSquishSound()
//    {
//        return this.isSmallSlime() ? SoundEvents.ENTITY_SMALL_SLIME_SQUISH : SoundEvents.ENTITY_SLIME_SQUISH;
//    }




    @Override
    protected void updateAITasks() 
    {
        super.updateAITasks();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }


//I wonder if this will actually stop the jumping
    @Override
    protected void jump() 
    {

    }


//Trying to remove contact damage
    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) 
    {
        return;
    }

//Trying to remove contact damage
    @Override
    public void applyEntityCollision(Entity entityIn) 
    {
        return;
    }


    @Override
    public boolean isInRangeToRenderDist(double distance)
    {
        return true;
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z)
    {
        return true;
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return true;
    }


    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange)
    {
        float f = MathHelper.wrapDegrees(targetAngle - sourceAngle);

        if (f > maximumChange)
        {
            f = maximumChange;
        }

        if (f < -maximumChange)
        {
            f = -maximumChange;
        }

        float f1 = sourceAngle + f;

        if (f1 < 0.0F)
        {
            f1 += 360.0F;
        }
        else if (f1 > 360.0F)
        {
            f1 -= 360.0F;
        }

        return f1;
    }
}
