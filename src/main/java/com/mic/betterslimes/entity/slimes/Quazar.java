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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import org.sporotofpoorety.eternitymode.entity.EntityExplosiveShockwave;
import org.sporotofpoorety.eternitymode.entity.EntityThrownBlock;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotBouncing;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotHoming;
import org.sporotofpoorety.eternitymode.entity.projectile.EntityFlameShotLinearSplits;
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




//Attack state
//  ArrayList<Integer> currentAttacksProjectile = new ArrayList<>();
//  ArrayList<Integer> currentAttacksMovement = new ArrayList<>();
    int currentAttackProjectile;
    int currentAttackMovement;    


    public boolean isPerformingSpecial;
    public int bossSpecialCountdown;


    Integer targetLastPosX;
    Integer targetLastPosZ;
    private boolean wasOnGroundPreviousTick;
    private boolean isPerformingLeap;
    protected boolean shouldExplodeOnLanding;
    private int landingExplosionWait;
    public int leapSequenceAt;




//Values config
    public static boolean spawnMinions;
    private static final DataParameter<Integer> SPAWN_TIME 
        = EntityDataManager.<Integer>createKey(Quazar.class, DataSerializers.VARINT);
    public static String splitSlimeString;
    protected Class<? extends Entity> SplitSlime;


    private double movementSpeedAttribute;
    public static float movementSpeedMultiplier;


    public static int specialCooldown;


    public int leapSequenceMax;
    public int leapSequenceCooldown;
    public static int leapWarning;
    public static float leapVelocityMultiplierXZ;
    public static float leapVelocityMultiplierY;
    public static int leapLandingRadius;
    public static float leapLandingDamage;




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




//Attack state
        this.currentAttackProjectile = rand.nextInt(3);
        this.currentAttackMovement = rand.nextInt(2);

        this.isPerformingSpecial = false;
        this.bossSpecialCountdown = specialCooldown;

        this.targetLastPosX = null;
        this.targetLastPosZ = null;
        this.wasOnGroundPreviousTick = false;
        this.isPerformingLeap = false;
        this.shouldExplodeOnLanding = false;
        this.landingExplosionWait = 2;
        this.leapSequenceAt = 1;




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
        this.leapSequenceCooldown = 40;
        this.leapWarning = BetterSlimesConfigMobs.leapWarning;
        this.leapVelocityMultiplierXZ = BetterSlimesConfigMobs.leapVelocityMultiplierXZ;
        this.leapVelocityMultiplierY = BetterSlimesConfigMobs.leapVelocityMultiplierY;
        this.leapLandingRadius = BetterSlimesConfigMobs.leapLandingRadius;
        this.leapLandingDamage = BetterSlimesConfigMobs.leapLandingDamage;
    }


    @Override
    protected void entityInit() 
    {
        this.dataManager.register(SPAWN_TIME, Integer.valueOf(0));
        super.entityInit();
    }




//On update
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
            if (this.bossSpecialCountdown <= leapWarning) 
            {
//Set creeper state
                this.setCreeperState(1);
            }

//Check if should ignite
            this.creeperIgniteCheck();
        }

        super.onUpdate();
    }




//On living update
    public void onLivingUpdate() 
    {
//Super update
        super.onLivingUpdate();


//Update is server-side
        if (this.world.isRemote) 
        {
            return;
        }


//Check if should explode on landing
        this.checkLandingExplosions();


//If this has target
        if (this.getAttackTarget() != null) 
        {
//Execute regular attacks
            if(!this.isPerformingSpecial) { this.executeRegularAttacks(); }
//Check if should perform special leap warning
            checkSpecialLeapPreparation(this.getAttackTarget());
//Check if should perform special leap execution
            checkSpecialLeapExecution(this.getAttackTarget());
        }
//If no target 
        else 
        {
//Reset special cooldown
            this.bossSpecialCountdown = 40;
//Reset performing special
            this.isPerformingSpecial = false;
//Reset leap state and leaps performed
            this.isPerformingLeap = false;
            this.leapSequenceAt = 1;
//Reset movement speed attribute
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);
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
            true, true, 0.5F, false, false
        );

        this.world.spawnEntity(flameShotHoming);
    }




    protected void executeBouncingVolley()
    {
/*
        Vec3d[] targetBasis = AbsurdcraftMathUtils.makeOrthonormalBasis
            (new Vec3d(this.getAttackTarget().posX - this.posX, this.getAttackTarget().posY - (this.posY + 16.0D), this.getAttackTarget().posZ - this.posZ));
        Vec3d orthoRight = targetBasis[0];
        Vec3d orthoUp = targetBasis[1];
        Vec3d orthoForward = targetBasis[2];



//Spin initial ring for fireball angles
        for(int shotUpAt = 0; shotUpAt < 5; shotUpAt++)
        {
//Current ortho-cos
            Vec3d fireballForward
            = orthoForward.scale
            (
                Math.cos
                (
                    (0.2D + (shotUpAt * 0.125D)) * Math.PI + ((rand.nextDouble() - rand.nextDouble()) * 0.2D)
                )
            )
//Add current ortho-sin
            .add
            (
                orthoUp.scale
                (
                    Math.sin
                    (
                        (0.2D + (shotUpAt * 0.125D)) * Math.PI + ((rand.nextDouble() - rand.nextDouble()) * 0.2D)
                    )
                )
            );


//For each band on that ring, rotate around its X axis
            for(int shotSideAt = -2; shotSideAt <= 2; shotSideAt++)
            {
                Vec3d[] ringBasis = AbsurdcraftMathUtils.makeOrthonormalBasis(fireballForward);
                Vec3d ringRight = ringBasis[0];


//Finalized 3d-rotated vector
                Vec3d fireballNormalized
//Current ortho-cos
                = fireballForward.scale
                (
                    Math.cos
                    (
                        (0.125D * shotSideAt) + ((rand.nextDouble() - rand.nextDouble()) * 0.2D)
                    )
                )
//Add current ortho-sin
                .add
                (
                    ringRight.scale
                    (
                        Math.sin
                        (
                            (0.125D * shotSideAt) + ((rand.nextDouble() - rand.nextDouble()) * 0.2D)
                        )
                    )
                );


//Use final vector for new bouncing fireball
                EntityFlameShotBouncing flameShotBouncing = new EntityFlameShotBouncing
                (
                    this.world, this,
                    this.posX, this.posY + 16.0D, this.posZ,
                    200, 
                    fireballNormalized.x, 
                    fireballNormalized.y, 
                    fireballNormalized.z, 
                    1.0D, 0.08D, 
                    0.3D, true, false, 5.0F, 
                    5, 2, 0.06D,
                    false, true, 0.3D,
                    20, false, false
                );


                this.world.spawnEntity(flameShotBouncing);
            }
        }
*/

        ArrayList<Vec3d> bouncingShotgun = ProjectileUtil.flexibleFibonnaciShotgunCoord
            (this.posX, this.posY + 16.0D, this.posZ, 
            this, this.getAttackTarget(),
            60, 0.4 * Math.PI, rand.nextInt(2), 1.0D);


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




    protected void checkSpecialLeapPreparation(EntityLivingBase leapTarget) 
    {
//Get target distance and if too short clear path
        double dist = this.getDistanceSq(leapTarget);
        if (dist < 40) 
        {
            this.getNavigator().clearPath();
        }


//Decrement boss leap countdown
        if (this.bossSpecialCountdown > 0) 
        {
            this.bossSpecialCountdown--;
        }


//If this is first leap in a sequence
        if(this.leapSequenceAt == 1)
        {
//When boss special countdown first reaches warning stage
            if (this.bossSpecialCountdown == leapWarning) 
            {
//Set creeper state
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 2.0F, 0.8F);
                this.setCreeperState(1);


//Set performing special
                this.isPerformingSpecial = true;
            }


//When boss special countdown is mid warning stage
            if (this.bossSpecialCountdown <= leapWarning && this.bossSpecialCountdown > 0) 
            {
//Stops moving, but will still look at target entity's position
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
            } 


            else 
            {
//If not at warning, normal movement speed
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.movementSpeedAttribute);
            }
        }
    }




    protected void checkSpecialLeapExecution(EntityLivingBase leapTarget) 
    {
//If the boss leap is ready and not mid-leap
        if (this.bossSpecialCountdown < 1 && !this.isPerformingLeap)
        {
//Get target position
            this.targetLastPosX = (int) leapTarget.posX;
            this.targetLastPosZ = (int) leapTarget.posZ;


//Perform the leap
//Provide boolean for whether this is last leap or not
            if(this.leapSequenceAt < this.leapSequenceMax) 
            { this.executeLeap(leapTarget, false); }
                else { this.executeLeap(leapTarget, true); }


//Set is performing leap
            this.isPerformingLeap = true;
//Set to explode on landing
            this.shouldExplodeOnLanding = true;
//But wait 2 ticks so the boss doesn't explode immediately after leaping
            this.landingExplosionWait = 2;


//Some creeper stuff
            this.timeSinceIgnited = 0;
            this.fuseTime = 30;
            this.setCreeperState(-1); 
        }
    }




    protected void executeLeap(EntityLivingBase leapTarget, boolean lastLeapInSequence) 
    {
        double distanceX;
        double distanceZ;


        if (this.targetLastPosX != null && this.targetLastPosZ != null) 
        {
            distanceX = this.targetLastPosX - this.posX;
            distanceZ = this.targetLastPosZ - this.posZ;
        } 
        else // fallback position, in case of null
        {
            distanceX = 0;
            distanceZ = 0;
        }


        this.playSound(SoundEvents.BLOCK_CLOTH_PLACE, 2.0F, 0.3F);
        this.playSound(SoundEvents.BLOCK_SAND_FALL, 2.0F, 0.8F);
        this.setPositionAndUpdate(this.posX, this.posY + 2, this.posZ);


        if (!this.world.isRemote) 
        {
//If not last leap do a basic leap
            if(!lastLeapInSequence)
            {
//Leap behind target
                this.motionX = distanceX / 6.5D;
                this.motionY = 2;
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
                        this.motionY = 2.0;
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
                        this.motionY = 1.0;
                        this.motionZ = (distanceZ + (Math.sin(radiansSideways) * 20.0D)) / 5.0D;
                        break;
    //Predictive leap
                    case 2:
                        this.motionX = (distanceX + (leapTarget.motionX * 20)) / 7.0D;
                        this.motionY = 2;
                        this.motionZ = (distanceZ + (leapTarget.motionZ * 20)) / 7.0D;
                        break; 
                }
            }
        }
    }



    protected void checkLandingExplosions()
    {

//If this is on ground
        if (this.onGround)
        {

//And this should explode on landing
            if (this.shouldExplodeOnLanding) 
            {
//And just landed for explosion
                if(this.landingExplosionWait == 2)
                {
//Perform warning explosion
                    this.world.newExplosion(this, this.posX, this.posY, this.posZ, (float) this.leapLandingRadius, false, false); 
                }


//If explosion wait over
                if (this.landingExplosionWait < 1) 
                {
//Giga explosion
                    this.specialExplode();
//Then reset explosion wait
                    this.landingExplosionWait = 2;
                }


                else 
                {
//Else decrement explosion wait
                    this.landingExplosionWait--;
                }
            } 
        }
    }




    private void specialExplode() 
    {
        if (!this.shouldExplodeOnLanding) 
        {
            return;
        }


        ITextComponent msg = new TextComponentTranslation(
                "chat.type.text",
                "Quazar",
                new TextComponentString("Furnace, open")
        );

        world.getMinecraftServer().getPlayerList().sendMessage(msg);


        this.shouldExplodeOnLanding = false;
        this.targetLastPosX = null;
        this.targetLastPosZ = null;

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
                2, 3.0F, 6, false, false, 8,
                100, 0.15D, 1.5D, 
                1.01D, 0.0D, 
                0.3D, true, true, 5.0F,
                20, true,
                0.0D, 4.0D, 0.0D, 1.01D,
                10, 3.0F, false);
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
                    2, 3.0F, 6, aimedShockwavesSetFire, false, 8,
                    100, 0.15D, 1.5D,
                    1.01D, 0.0D,
                    0.3D, true, true, 5.0F,
                    20, true,
                    0.0D, 4.0D, 0.0D, 1.01D,
                    10, 3.0F, false);
		            shockwave.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

		            this.getEntityWorld().spawnEntity(shockwave);
                }
            } 
        }


        if(!this.world.isRemote)
        {
//Now the thrown blocks and exploding fireballs

            double horizontalDistance = 32.0D;

            if(this.getAttackTarget() != null)
            {
//Get hypotenuse to the target
                horizontalDistance = 
                    Math.sqrt(Math.pow(this.getAttackTarget().posX - this.posX, 2) + Math.pow(this.getAttackTarget().posZ - this.posZ, 2));


                double flameStartingRadians = 
                    Math.atan2(this.getAttackTarget().posZ - this.posZ, this.getAttackTarget().posX - this.posX) - (-1.3D * Math.PI);
                double blockStartingRadians = 
                    Math.atan2(this.getAttackTarget().posZ - this.posZ, this.getAttackTarget().posX - this.posX);
                double flameCurrentRadians = flameStartingRadians;
                double blockCurrentRadians = blockStartingRadians;


                for(int flameShotAt = 0; flameShotAt < 4; flameShotAt++)
                {
//Get distance addition (Up to 32 blocks offset)
                    double distanceAddition = 32.0D * (rand.nextDouble() - rand.nextDouble());

//Up to double the angle and projectile count randomly
                    double randomSpreadScale = rand.nextDouble() * 2.0D;

                    EntityFlameShotLinearSplits flameShotExplosive = new EntityFlameShotLinearSplits
                    (
                        this.world, this,
                        this.posX, this.posY, this.posZ,
                        25, 
                        Math.cos(flameCurrentRadians) * (horizontalDistance + distanceAddition) / 40.0D,
//Random amount 1.5D to 2.0D
                        1.0D * (0.5D + (rand.nextDouble() * 0.2D)),
                        Math.sin(flameCurrentRadians) * (horizontalDistance + distanceAddition) / 40.0D,
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

                    flameCurrentRadians += (0.5D + (0.3D * rand.nextDouble())) * Math.PI;
                }

 

//Throw the blocks
                for(int thrownBlockAt = 0; thrownBlockAt < 50; thrownBlockAt++)
                {
//Search blockpos to determine appearance
                    BlockPos blockOrigin = EntityUtil.findSolidBlockBelow(this, 16);

                    if(blockOrigin != null)
                    {
//Get distance addition (Up to 32 blocks offset)
                        double distanceAddition = 32.0D * (rand.nextDouble() - rand.nextDouble());

                        EntityThrownBlock thrownBlock = new EntityThrownBlock
                        (
                            this.world, this.posX, this.posY, this.posZ, this, blockOrigin, 10.0F
                        );
                        thrownBlock.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

                        thrownBlock.motionX = Math.cos(blockCurrentRadians) * ((horizontalDistance * rand.nextDouble()) + distanceAddition) / 18D;
                        thrownBlock.motionY = 2.0D * (1.0D + (rand.nextDouble() * 0.5D));
                        thrownBlock.motionZ = Math.sin(blockCurrentRadians) * ((horizontalDistance * rand.nextDouble()) + distanceAddition) / 18D;

                        this.getEntityWorld().spawnEntity(thrownBlock);

                        blockCurrentRadians += (0.5D + (0.3D * rand.nextDouble())) * Math.PI;
                    }
                }


                for(int aimedBlockAt = 0; aimedBlockAt < 50; aimedBlockAt++)
                {
//Search blockpos to determine appearance
                    BlockPos blockOrigin = EntityUtil.findSolidBlockBelow(this, 16);

                    if(blockOrigin != null)
                    {
//Get distance addition (Up to 32 blocks offset)
                        double distanceAddition = 32.0D * (rand.nextDouble() - rand.nextDouble());

                        EntityThrownBlock thrownBlock = new EntityThrownBlock
                        (
                            this.world, this.posX, this.posY, this.posZ, this, blockOrigin, 10.0F
                        );
                        thrownBlock.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);

                        thrownBlock.motionX =
                            Math.cos(blockStartingRadians + ((rand.nextDouble() - rand.nextDouble()) * (0.5 * Math.PI))) * ((horizontalDistance * rand.nextDouble()) + distanceAddition) / 18D;
                        thrownBlock.motionY = 2.0D * (1.0D + (rand.nextDouble() * 0.5D));
                        thrownBlock.motionZ =
                            Math.sin(blockStartingRadians + ((rand.nextDouble() - rand.nextDouble()) * (0.5 * Math.PI))) * ((horizontalDistance * rand.nextDouble()) + distanceAddition) / 18D;

                        this.getEntityWorld().spawnEntity(thrownBlock);
                    }
                }     
            }
        }


//Finish special explosion by 

//Incrementing leaps executed
        ++this.leapSequenceAt;


//Applying different cooldowns
        if(this.leapSequenceAt <= this.leapSequenceMax) 
        { 
//Separate cooldowns based on which leap at
            this.bossSpecialCountdown = this.leapSequenceCooldown;
        }
//If last leap
        else
        { 
            this.bossSpecialCountdown = this.specialCooldown;
//Reset special state
            this.isPerformingSpecial = false; 
            this.leapSequenceAt = 1;        
        }


//Set not performing leap
        this.isPerformingLeap = false;
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
