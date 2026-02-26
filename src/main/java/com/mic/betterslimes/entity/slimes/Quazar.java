package com.mic.betterslimes.entity.slimes;

import java.util.ArrayList;

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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
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

import org.sporotofpoorety.eternitymode.entity.EntityEarthPiece;
import org.sporotofpoorety.eternitymode.entity.EntityExplosiveShockwave;
import org.sporotofpoorety.eternitymode.entity.EntityOrbVoidCustom;
import org.sporotofpoorety.eternitymode.entity.EntityThrownBlock;
import org.sporotofpoorety.eternitymode.entity.ai.EntityAIStun;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotBouncing;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotHoming;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotLinearSplits;
import org.sporotofpoorety.eternitymode.interfacemixins.IMixinEntityLiving;
import org.sporotofpoorety.eternitymode.util.AbsurdcraftMathUtils;
import org.sporotofpoorety.eternitymode.util.EntityUtil;
import org.sporotofpoorety.eternitymode.util.ProjectileUtil;

import java.util.List;




// Renamed from King Slime to Quazar
public class Quazar extends EntityBetterSlime implements ISpecialSlime {

//These are unrelated to the boss itself
    public static final int MAX = Short.MAX_VALUE;
    private final BossInfoServer bossInfo 
        = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS));
//Creeper state
    private static final DataParameter<Integer> STATE = EntityDataManager.<Integer>createKey(Quazar.class, DataSerializers.VARINT);
    private int timeSinceIgnited;
    private int fuseTime;




//Interface with mixin
    public IMixinEntityLiving livingEntityMixin;


//Attack state
    public enum BehaviorState 
    {
        DEFAULT,
        PREPARING_LEAP,
        LEAPING_SPECIAL
    }
    public BehaviorState behaviorState;


//  ArrayList<Integer> currentAttacksProjectile = new ArrayList<>();
//  ArrayList<Integer> currentAttacksMovement = new ArrayList<>();
    int currentAttackProjectile;
    int currentAttackMovement;    


    public int bossSpecialCountdown;


    private boolean wasOnGroundPreviousTick;
    private boolean isPerformingLeap;
    protected boolean shouldExplodeOnLanding;
    private int landingExplosionWait;
    public int leapSequenceAt;




//Values config
    public boolean spawnMinions;
    private static final DataParameter<Integer> SPAWN_TIME 
        = EntityDataManager.<Integer>createKey(Quazar.class, DataSerializers.VARINT);
    public static String splitSlimeString;
    protected Class<? extends Entity> SplitSlime;


    private double movementSpeedAttribute;
    public float movementSpeedMultiplier;


    public int specialCooldown;


    public int leapSequenceMax;
    public int leapSequenceCooldown;
    public int leapWarning;
    public float leapVelocityMultiplierXZ;
    public float leapVelocityMultiplierY;
    public int leapLandingRadius;
    public float leapLandingDamage;




    public Quazar(World worldIn) 
    {
        super(worldIn);
        this.setAttackModifier(1);
        this.setHealthModifier(1);
        this.setSlimeSize(16, true);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.damageMultiplier = BetterSlimesConfigMobs.damageMultiplier;

        this.isImmuneToFire = true;

//These are unrelated to the boss itself
        this.dataManager.register(STATE, Integer.valueOf(-1));
        this.setCreeperState(-1);
        this.timeSinceIgnited = 0;
        this.fuseTime = 30;




//Interface with mixin
        this.livingEntityMixin = (IMixinEntityLiving) this;




//Values config
        this.spawnMinions = BetterSlimesConfigMobs.spawnMinions;
            if (!this.spawnMinions) { this.splitChance = 0; }
        this.splitSlimeString = BetterSlimesConfigMobs.splitSlimeString;
        SplitSlime = EntityList.getClass(new ResourceLocation(splitSlimeString));

        this.movementSpeedMultiplier = BetterSlimesConfigMobs.movementSpeedMultiplier;
        this.movementSpeedAttribute = 0.02 * movementSpeedMultiplier;
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);

        this.specialCooldown = BetterSlimesConfigMobs.specialCooldown;

        this.leapSequenceMax = 3;
        this.leapSequenceCooldown = 20;
        this.leapWarning = BetterSlimesConfigMobs.leapWarning;
        this.leapVelocityMultiplierXZ = BetterSlimesConfigMobs.leapVelocityMultiplierXZ;
        this.leapVelocityMultiplierY = BetterSlimesConfigMobs.leapVelocityMultiplierY;
        this.leapLandingRadius = BetterSlimesConfigMobs.leapLandingRadius;
        this.leapLandingDamage = BetterSlimesConfigMobs.leapLandingDamage;




//Attack state
        this.behaviorState = BehaviorState.DEFAULT;

        this.currentAttackProjectile = rand.nextInt(3);
        this.currentAttackMovement = rand.nextInt(2);

        this.bossSpecialCountdown = specialCooldown;

        this.wasOnGroundPreviousTick = false;
        this.isPerformingLeap = false;
        this.shouldExplodeOnLanding = false;
        this.landingExplosionWait = 2;
        this.leapSequenceAt = 1;




        this.tasks.addTask(0, new EntityAIStun(this));        
    }


    @Override
    protected void entityInit() 
    {
        this.dataManager.register(SPAWN_TIME, Integer.valueOf(0));
        super.entityInit();
    }

    public void resetBehaviorState()
    {
//Reset special cooldown
        this.bossSpecialCountdown = 40;
//Reset performing special
        this.behaviorState = BehaviorState.DEFAULT;
//Reset leap state and leaps performed
        this.isPerformingLeap = false;
        this.leapSequenceAt = 1;
//Reset landing explosions
        this.shouldExplodeOnLanding = false;
        this.landingExplosionWait = 2;
//Reset movement speed attribute
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);
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


//If this is alive
        if (this.isEntityAlive()) 
        {

//When reaching leap warning
            if (this.bossSpecialCountdown <= this.leapWarning) 
            {
//Set creeper state
                this.setCreeperState(1);
            }

//Check if should ignite
            this.creeperIgniteCheck();
        }

        super.onUpdate();
    }




//SERVER SIDE STUFF HERE
    public void onLivingUpdate() 
    {
//Super update
        super.onLivingUpdate();


//Update is server-side
        if (this.world.isRemote) 
        {
            return;
        }


        EntityLivingBase attackTarget = this.getAttackTarget();

//If this has target
        if (attackTarget != null) 
        {
//Every 30 seconds
            if((this.ticksExisted % 600) == 0)
            {
                double radianAt = 0.0D;

                for(int pieceAt = 0; pieceAt < 8; pieceAt++)
                {
                    double randomDistance = 16.0D + (rand.nextDouble() * 48.0D);
                    
                    EntityEarthPiece earthPiece = new EntityEarthPiece(this.world, this,
                    this.posX + randomDistance * Math.cos(radianAt), this.posY + 4.0D, this.posZ + randomDistance * Math.sin(radianAt), 
                    "spin", "cube", 1,
                    10, 20, 1.0D,
                    10, 16.0D,
                    40, 0.5D,
                    1.0D, 1.0D, 0.08D,
                    20);

                    earthPiece.setPieceSpin(100 + ((pieceAt + 1) * 25), 24.0D, radianAt, 0.1D);

                    this.world.spawnEntity(earthPiece);

                    radianAt += 0.25D;
                }



                ITextComponent msg = new TextComponentTranslation
                (
                        "chat.type.text",
                        "Quazar",
                        new TextComponentString("Earth's sorcery is quite advanced")
                );
                world.getMinecraftServer().getPlayerList().sendMessage(msg);
            }

//Default state
            if(this.behaviorState == BehaviorState.DEFAULT) 
            { 
//Execute regular attacks
                this.executeRegularAttacks();


//When boss special countdown first reaches warning stage
                if (this.bossSpecialCountdown == this.leapWarning) 
                {
//Set creeper state
                    this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 2.0F, 0.8F);
                    this.setCreeperState(1);

//Set preparing leap
                    this.behaviorState = BehaviorState.PREPARING_LEAP;
                }
            }




//Preparing leap state
            else if(this.behaviorState == BehaviorState.PREPARING_LEAP) 
            { 
//Get target distance and if too short clear path
/*
                double dist = this.getDistanceSq(attackTarget);
                if (dist < 40) 
                {
                    this.getNavigator().clearPath();
                }
*/


//Don't move
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);


//When boss special countdown reaches zero
                if (this.bossSpecialCountdown < 1)
                {
//Some creeper stuff
                    this.timeSinceIgnited = 0;
                    this.fuseTime = 30;
                    this.setCreeperState(-1); 

//Set executing leap
                    this.behaviorState = BehaviorState.LEAPING_SPECIAL;
                }
            }




//Leap executing state
            else if(this.behaviorState == BehaviorState.LEAPING_SPECIAL) 
            {
//If boss already should explode on landing
                if(this.shouldExplodeOnLanding)
//Decrement explosion wait
                {
                    --this.landingExplosionWait;
                }

//When boss special countdown 
//is at zero and this isn't already leaping
                if (this.bossSpecialCountdown == 0 && !this.isPerformingLeap)
                {
//Perform the leap
//Provide boolean for whether this is last leap or not
                    if(this.leapSequenceAt < this.leapSequenceMax) 
                    { this.executeLeap(attackTarget, false); }
                        else { this.executeLeap(attackTarget, true); }

//Set performing leap
                    this.isPerformingLeap = true;
//Set to explode on landing
                    this.shouldExplodeOnLanding = true;
//But wait 2 ticks so the boss doesn't explode immediately after leaping
                    --this.landingExplosionWait;

//Some creeper stuff
                    this.timeSinceIgnited = 0;
                    this.fuseTime = 30;
                    this.setCreeperState(-1); 
               }
            }


//Universal logic if has target...

//Decrement boss special countdown if not mid-special
            if(this.bossSpecialCountdown > 0 && !this.isPerformingLeap) { this.bossSpecialCountdown--; }

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




    protected void onLandingNormal()
    {
//Get width
        int quazarWidth = (int) this.width;

//16 particles for each block of width
        for (int particleAt = 0; particleAt < quazarWidth * 16; particleAt++)
        {
            float randomAngle = this.rand.nextFloat() * (2F * (float) Math.PI);
            float quarterToHalf = this.rand.nextFloat() * 0.25F + 0.25F;

//Particle offset in random angle
//and multiplied by 0.25-0.5 Quazar's width
            float particleOffsetX = MathHelper.sin(randomAngle) * (float) quazarWidth * quarterToHalf;
            float particleOffsetZ = MathHelper.cos(randomAngle) * (float) quazarWidth * quarterToHalf;

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




    protected void creeperIgniteCheck()
    {
//If on first tick of ignition
        int igniteState = this.getCreeperState();
        if (igniteState > 0 && this.timeSinceIgnited == 0) 
        {
//Play creeper primed sound
            this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.7F);
        }


//Increment ignite time
        this.timeSinceIgnited += igniteState;


//Also ignite time can't go below zero
        if (this.timeSinceIgnited < 0) 
        {
            this.timeSinceIgnited = 0;
        }

//Ignite time can't go over fuse time
        if (this.timeSinceIgnited >= this.fuseTime) 
        {
            this.timeSinceIgnited = this.fuseTime;
        }
    }




    protected void executeRegularAttacks()
    {
        if(this.currentAttackProjectile == 0 && ((this.ticksExisted % 30) == 0))
            { this.executeHomingVolley(); }
        if(this.currentAttackProjectile == 1 && ((this.ticksExisted % 75) == 0))
            { this.executeBouncingVolley(); }

/*
        if(this.currentAttackMovement == 0 && ((this.ticksExisted % 150) == 0))
            { this.executeLeap(); }
*/
    }




    protected void executeHomingVolley()
    {
//Angle
        double currentRadians = Math.atan2(this.getAttackTarget().posZ - this.posZ, this.getAttackTarget().posX - this.posX);

//Horizontal distance
        double targetHorizontalDistance = Math.sqrt(Math.pow(this.getAttackTarget().posX - this.posX, 2) + Math.pow(this.getAttackTarget().posZ - this.posZ, 2));


//Random extra duration
        int extraDuration = rand.nextInt(30);


        EntityFlameShotHoming flameShotHoming = new EntityFlameShotHoming
        (
            this.world, this,
            this.posX, this.posY, this.posZ,
            200,
            (targetHorizontalDistance + 32.0D) * Math.cos(currentRadians) / (30 + extraDuration), 
            0.25D, 
            (targetHorizontalDistance + 32.0D) * Math.sin(currentRadians) / (30 + extraDuration), 
            1.0D, 0.0D, 
            1.2D, true, true, 1.0F,
            5, 2, 0.06D,
            100, Math.PI, 0,
            1.0F, 1.0D, 1.01D, 
            30 + extraDuration, true, 100, 1.0D, 1, 
            20, true, 2.0F, false, true,
            true, false, 0.5F, false, false
        );

        this.world.spawnEntity(flameShotHoming);
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
                this.world, this,
                this.posX, this.posY + 16.0D, this.posZ,
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




    protected void executeLeap(EntityLivingBase leapTarget, boolean lastLeapInSequence) 
    {
//Set performing leap
        this.isPerformingLeap = true;


        double distanceX = this.getAttackTarget().posX - this.posX;
        double distanceZ = this.getAttackTarget().posZ - this.posZ;


        this.playSound(SoundEvents.BLOCK_CLOTH_PLACE, 2.0F, 0.3F);
        this.playSound(SoundEvents.BLOCK_SAND_FALL, 2.0F, 0.8F);
        this.setPositionAndUpdate(this.posX, this.posY + 2, this.posZ);


        if (!this.world.isRemote) 
        {
//If not last leap do a basic leap
            if(!lastLeapInSequence)
            {

            ITextComponent msg = new TextComponentTranslation(
                    "chat.type.text",
                    "Quazar",
                    new TextComponentString("Furnace, open")
            );

            world.getMinecraftServer().getPlayerList().sendMessage(msg);

//Leap at target
                this.motionX = distanceX / 6.5D;
                this.motionY = 2.0D;
                this.motionZ = distanceZ / 6.5D;
            }
//If last leap do a harder leap
            else
            {
                ITextComponent msg = new TextComponentTranslation
                (
                        "chat.type.text",
                        "Quazar",
                        new TextComponentString("Domain Expansion - Malevolent Shrine")
                );

                world.getMinecraftServer().getPlayerList().sendMessage(msg);

                int finalLeapTypeChosen = this.rand.nextInt(3);


                switch (finalLeapTypeChosen) 
                {
//Leap behind target
                    case 0:
                        this.motionX = distanceX / (4.5D);
                        this.motionY = 2.0D;
                        this.motionZ = distanceZ / (4.5D);
                        break;
//Side-leap 
                    case 1:
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
                    case 2:
                        this.motionX = (distanceX + (leapTarget.motionX * 20)) / 5.5D;
                        this.motionY = 1.5D;
                        this.motionZ = (distanceZ + (leapTarget.motionZ * 20)) / 5.5D;
                        break; 
                }
            }
        }
    }




    private void checkSpecialExplode() 
    {
        if (!this.shouldExplodeOnLanding || !this.onGround || (this.landingExplosionWait > 0)) 
        {
            return;
        }




        List<EntityLivingBase> entitiesInBlast = this.world.getEntitiesWithinAABB
        (
            EntityLivingBase.class, 
            new AxisAlignedBB(this.getPosition()).grow(leapLandingRadius, 32, leapLandingRadius), 
//WIP predicate, always true
            entityInBlast -> true
        );


        for (EntityLivingBase entityInBlast : entitiesInBlast) 
        {           
            if (!entityInBlast.isOnSameTeam(this))
            {
                double dist = this.getDistanceSq(entityInBlast) + 1;

                if (entityInBlast != this && this.getDistanceSq(entityInBlast) < (leapLandingRadius * leapLandingRadius)) 
                {
//Change entity's position, add velocity, set velocity changed
                    entityInBlast.setPositionAndUpdate(entityInBlast.posX, entityInBlast.posY + 1.5, entityInBlast.posZ);
                    entityInBlast.addVelocity((0.8 / (entityInBlast.posX - this.posX)) * leapVelocityMultiplierXZ, MathHelper.clamp(32 / (dist) * leapVelocityMultiplierY, 1, 16), 0.8 / (entityInBlast.posZ - this.posZ) * leapVelocityMultiplierXZ);
                    entityInBlast.velocityChanged = true;


//Attack entity
                    entityInBlast.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.leapLandingDamage);


//Set entity attacked
                    entityInBlast.setLastAttackedEntity(this);
                    entityInBlast.setRevengeTarget(this);
                }
            }
        }




        if(!this.world.isRemote)
        {
            for(int shockwaveAt = 0; shockwaveAt < 8; shockwaveAt++)
            {
                EntityExplosiveShockwave shockwave = new EntityExplosiveShockwave(this.world, this, this.posX, this.posY, this.posZ, 
                100, true, 3.0F, 1.0D * Math.cos(Math.PI * 0.25D * shockwaveAt), 0.0D, 1.0D * Math.sin(Math.PI * 0.25D * shockwaveAt), 1.015D,
                true, 3.0D, 15,
                2, 3.0F, 6, 
                false,
                0.0D, 4.0D, 0.0D, 1.01D,
                10, 3.0F);
		        shockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

		        this.getEntityWorld().spawnEntity(shockwave);
            }

//Target for targeted shockwaves
            EntityLivingBase quazarAttackTarget = this.getAttackTarget();

//Only last batch set fire to ground
            boolean aimedShockwavesSetFire = (this.leapSequenceAt >= this.leapSequenceMax);

            if (quazarAttackTarget != null)
            {
                double baseRadians = Math.atan2(quazarAttackTarget.posZ - this.posZ, quazarAttackTarget.posX - this.posX);

                for(int angleAt = -2; angleAt <= 2; angleAt++)
                {
                    EntityExplosiveShockwave shockwave = new EntityExplosiveShockwave(this.world, this, this.posX, this.posY, this.posZ, 
                    50, true, 3.0F, 2.0D * Math.cos(baseRadians + (Math.PI * 0.125D * angleAt)), 0.0D, 2.0D * Math.sin(baseRadians + (Math.PI * 0.125D * angleAt)), 1.015D,
                    true, 3.0D, 15, 
                    2, 3.0F, 6,
                    false,
                    0.0D, 4.0D, 0.0D, 1.01D,
                    10, 3.0F);
		            shockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

		            this.getEntityWorld().spawnEntity(shockwave);
                }
            } 




//Now the thrown blocks and exploding fireballs

            double horizontalDistance = 32.0D;

            if(this.getAttackTarget() != null)
            {
//Get hypotenuse to the target
                horizontalDistance = 
                    Math.sqrt(Math.pow(this.getAttackTarget().posX - this.posX, 2) + Math.pow(this.getAttackTarget().posZ - this.posZ, 2));


                double flameStartingRadians = 
                    Math.atan2(this.getAttackTarget().posZ - this.posZ, this.getAttackTarget().posX - this.posX);


                for(int flameShotAt = -2; flameShotAt < 2; flameShotAt++)
                {
//Get distance addition (Up to 32 blocks offset)
                    double distanceAddition = 32.0D * (rand.nextDouble());

//Up to double the angle and projectile count randomly
                    double randomSpreadScale = rand.nextDouble() * 2.0D;

                    EntityFlameShotLinearSplits flameShotExplosive = new EntityFlameShotLinearSplits
                    (
                        this.world, this,
                        this.posX, this.posY, this.posZ,
                        15, 
                        Math.cos(flameStartingRadians + (0.125D * Math.PI * flameShotAt)) * (horizontalDistance + distanceAddition) / 25.0D,
//Random amount 0.64D to 0.8D
                        0.64D * (1.0D + (rand.nextDouble() * 0.25D)),
                        Math.sin(flameStartingRadians + (0.125D * Math.PI * flameShotAt)) * (horizontalDistance + distanceAddition) / 25.0D,
                        1.0D, 0.08D, 
                        1.2D, true, true, 5.0F, 
                        10, 5, 0.06D,
                        (int) (50 * randomSpreadScale), (0.175D * Math.PI) * randomSpreadScale, rand.nextInt(2),
                        1.0F, 1.5D, 1.01D,
                        20, true, 6.0F, true, true,
                        false, 0.5F, false, false
                    );
                    flameShotExplosive.setNoGravity(false);
                    flameShotExplosive.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                    this.getEntityWorld().spawnEntity(flameShotExplosive);
                }
            }




            EntityOrbVoidCustom shinraTensei 
                = new EntityOrbVoidCustom(this.world, null, this, 7, 5, 2.0F, 2.0F, 80, 90);
            shinraTensei.orbCustomType = "blockshower";
            shinraTensei.setOrbShower
                (3.0D, 80.0D,
                150, 100,
                10.0D, -2.0D, 0.96D);
            shinraTensei.setLocationAndAngles(this.posX, this.posY + 16.0D, this.posZ, this.rotationYaw, 0.0F);
            this.getEntityWorld().spawnEntity(shinraTensei);
        }


//Finish special explosion by...

//No longer exploding on landing
        this.shouldExplodeOnLanding = false;
//Setting not performing leap
        this.isPerformingLeap = false;


//If not max leap
        if(this.leapSequenceAt < this.leapSequenceMax) 
        { 
//Applying short cooldown
            this.bossSpecialCountdown = this.leapSequenceCooldown;
//Incrementing leaps executed
            ++this.leapSequenceAt;
//Wait a bit before explosion again
            this.landingExplosionWait = 2;
        }

//If last leap
        else
        {
//Restoring movement speed
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);
//Applying longer cooldown
            this.bossSpecialCountdown = this.specialCooldown;
//Resetting leaps executed
            this.leapSequenceAt = 1;
//Wait a bit before explosion again
            this.landingExplosionWait = 2;     
//Resetting state
            this.behaviorState = BehaviorState.DEFAULT;   
        }
    }




//Stun logic
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
//If attacked in the middle of max leap
        if(this.isPerformingLeap && (this.leapSequenceAt >= this.leapSequenceMax))
        {
//If damage is melee and high enough
            if((source.damageType.equals("mob") || source.damageType.equals("player"))
            && (amount >= (float) 10.0F))
            {
//And apply stun
                this.livingEntityMixin.setAbsurdcraftStunned(true);
                this.livingEntityMixin.setAbsurdcraftStunnedTimer(200);
            }      
        }
        

        return super.attackEntityFrom(source, amount);
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




    @Override
    protected EntityBetterSlime createInstance() 
    {
        if (EntityBetterSlime.class.isAssignableFrom(SplitSlime)) 
        {
            return (EntityBetterSlime) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(splitSlimeString)).newInstance(this.world);
        } else {
            return new BlueSlime(this.world);
        }
    }


    @Override
    public void setDead() 
    {
        if (!spawnMinions) 
        {
            this.isDead = true;
        } else {
            super.setDead();
        }
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




    public int getSpawnTime() 
    {
        return ((Integer) this.dataManager.get(SPAWN_TIME)).intValue();
    }

    public void setSpawnTime(int time) 
    {
        this.dataManager.set(SPAWN_TIME, Integer.valueOf(time));
    }


    public int getCreeperState() 
    {
        return ((Integer) this.dataManager.get(STATE)).intValue();
    }

    public void setCreeperState(int state) 
    {
        this.dataManager.set(STATE, Integer.valueOf(state));
    }




    @Override
    protected void updateAITasks() 
    {
        if (this.getSpawnTime() > 0) 
        {
            int j1 = this.getSpawnTime() - 1;

            if (spawnMinions && j1 <= 0) 
            {
                this.playSound(this.getSquishSound(), (float) (this.getSoundVolume() * 1.2), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
                for (int x = 0; x < 10; x++)
                    world.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.getEntityBoundingBox().minY, this.posY, 0.0D, 0.0D, 0.0D);
//				this.world.spawnParticle(EnumParticleTypes.SLIME, this.posX, this.posY, this.posZ, 0, 0, 0);
                KnightSlime b;
                for (int x = 0; x < 4; x++) 
                {
                    b = new KnightSlime(this.world);
                    b.setSlimeSize(2, true);
                    b.setLocationAndAngles(this.posX + rand.nextInt(10) - 5, this.posY + rand.nextInt(1) + 1,
                            this.posZ + rand.nextInt(10) - 5, this.rotationYaw, this.rotationPitch);
                    this.world.spawnEntity(b);
                }
            }

            this.setSpawnTime(j1);
        } 
        else 
        {
            this.setSpawnTime(240);
        }
        super.updateAITasks();
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }




    public void writeEntityToNBT(NBTTagCompound compound) 
    {
        super.writeEntityToNBT(compound);

        compound.setInteger("Spawn", this.getSpawnTime());

        compound.setInteger("SpecialCooldown", specialCooldown);
        compound.setInteger("LeapWarning", leapWarning);
        compound.setFloat("LeapVelocityMultiplierY", leapVelocityMultiplierY);
        compound.setFloat("LeapVelocityMultiplierXZ", leapVelocityMultiplierXZ);
        compound.setFloat("LeapLandingDamage", leapLandingDamage);
        compound.setInteger("LeapLandingRadius", leapLandingRadius);

        compound.setFloat("MovementSpeedMultiplier", movementSpeedMultiplier);

        compound.setBoolean("SpawnMinions", spawnMinions);
    }

    public void readEntityFromNBT(NBTTagCompound compound) 
    {
        super.readEntityFromNBT(compound);
        if (this.hasCustomName()) { this.bossInfo.setName(this.getDisplayName()); }

        if (compound.hasKey("Spawn")) { this.setSpawnTime(compound.getInteger("Spawn")); }

        if (compound.hasKey("SpecialCooldown")) { specialCooldown = compound.getInteger("SpecialCooldown"); }

        if (compound.hasKey("LeapWarning")) { leapWarning = compound.getInteger("LeapWarning"); }
        if (compound.hasKey("LeapVelocityMultiplierY")) { leapVelocityMultiplierY = compound.getFloat("LeapVelocityMultiplierY"); }
        if (compound.hasKey("LeapVelocityMultiplierXZ")) { leapVelocityMultiplierXZ = compound.getFloat("LeapVelocityMultiplierXZ"); }
        if (compound.hasKey("LeapLandingDamage")) { leapLandingDamage = compound.getFloat("LeapLandingDamage"); }
        if (compound.hasKey("LeapLandingRadius")) { leapLandingRadius = compound.getInteger("LeapLandingRadius"); }


        if (compound.hasKey("MovementSpeedMultiplier")) { movementSpeedMultiplier = compound.getFloat("MovementSpeedMultiplier"); }
        this.movementSpeedAttribute = 0.02 * movementSpeedMultiplier;
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);


        if (compound.hasKey("SpawnMinions")) { spawnMinions = compound.getBoolean("SpawnMinions"); }
        if (!this.spawnMinions) 
        {
            this.splitChance = 0;
        }
    }

}
