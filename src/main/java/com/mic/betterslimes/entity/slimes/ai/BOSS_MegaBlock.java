/*
//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "Z:\home\inyourwalls\Downloads\Mappings16\fields.csv"!

package sthullen.bosscrafttwo;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BOSS_MegaBlock extends EntityMob {
   Random rand = new Random();
   public int special1;
   public int special2;

   public BOSS_MegaBlock(World world) {
      super(world);
      this.isImmuneToFire = true;
      this.setSize(3.0F, 3.0F);
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(210.0D);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
   }

   public void jump(int mx, int mz) {
      if (this.onGround) {
         this.motionY += 2.0D;
         this.motionX += (double)mx;
         this.motionZ += (double)mz;
         if (!this.worldObj.isRemote) {
            this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 2.5F, false);
         }
      }

      for(int a = 0; a < 6; ++a) {
         this.worldObj.spawnParticle("magicCrit", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 5.0D, 5.0D, 5.0D);
      }

   }

   public boolean getCanSpawnHere() {
      return !this.worldObj.isRemote;
   }

   public void onDeath(DamageSource par1DamageSource) {
      super.onDeath(par1DamageSource);
      if (!this.worldObj.isRemote) {
         this.dropItem(Bosscraft.skyHammer.itemID, 1);
      }

      this.setDead();
   }

   protected void attackEntity(Entity par1Entity, float par2) {
      if (this.attackTime <= 0 && par2 < 2.0F && par1Entity.boundingBox.maxY > this.boundingBox.minY && par1Entity.boundingBox.minY < this.boundingBox.maxY) {
         this.attackTime = 20;
         this.attackEntityAsMob(par1Entity);
      } else if (par2 < 100.0F) {
         double d = par1Entity.posX - this.posX;
         double d1 = par1Entity.boundingBox.minY + (double)(par1Entity.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
         double d2 = par1Entity.posZ - this.posZ;
         if (this.attackTime == 0) {
            float f = MathHelper.sqrt_float(par2) * 0.5F;
            this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1009, (int)this.posX, (int)this.posY, (int)this.posZ, 0);

            for(int i = 0; i < 1; ++i) {
               EntitySmallFireball entitysmallfireball = new EntitySmallFireball(this.worldObj, this, d + this.rand.nextGaussian() * (double)f, d1, d2 + this.rand.nextGaussian() * (double)f);
               entitysmallfireball.posY = this.posY + (double)(this.height / 2.0F) + 0.5D;
               this.worldObj.spawnEntityInWorld(entitysmallfireball);
               if (this.rand.nextInt(2) == 0) {
                  this.attackTime = 25;
               } else {
                  this.attackTime = 10;
               }

               this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, "fire.fire", 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F);
            }
         }

         this.rotationYaw = (float)(Math.atan2(d2, d) * 180.0D / 3.141592653589793D) - 90.0F;
         this.hasAttacked = true;
      }

   }

   public void onUpdate() {
      System.out.println(this.getHealth());
      super.onUpdate();
      if (this.posY > 180.0D) {
         this.posY = 179.0D;
      }

      this.fallDistance = 0.0F;
      ++this.special1;
      ++this.special2;
      if (!this.worldObj.isRemote) {
         if (this.special1 > 300 && this.special1 < 330) {
            this.motionY = 1.5D;
         } else if (this.special1 > 330 && this.special1 < 350) {
            this.worldObj.createExplosion(this, this.posX + (double)this.rand.nextInt(6) - 3.0D, this.posY + (double)this.rand.nextInt(6) - 3.0D, this.posZ + (double)this.rand.nextInt(6) - 3.0D, 2.0F, false);
         } else if (this.special1 > 350 && !this.onGround && this.special1 < 450) {
            this.motionY = -5.0D;
         }

         if (this.onGround && this.special1 > 350) {
            this.worldObj.createExplosion(this, this.posX, this.posY - 1.0D, this.posZ, 6.0F, true);
            this.special1 = 0;
         }

         if (this.special1 > 450) {
            this.special1 = 0;
         }
      }

      if (this.special2 > 500) {
         this.motionY = 0.0D;
         if (this.posY < 105.0D) {
            ++this.posY;
         } else if (this.posY > 110.0D) {
            --this.posY;
         }

         this.rotationYaw += 40.0F;
         EntityTNTPrimed tnt = new EntityTNTPrimed(this.worldObj);
         tnt.setPosition(this.posX, this.posY, this.posZ);
         tnt.fuse = 120 + this.rand.nextInt(100);
         tnt.addVelocity((double)(this.rand.nextFloat() * 2.0F - 1.0F), 0.0D, (double)(this.rand.nextFloat() * 2.0F - 1.0F));
         if (!this.worldObj.isRemote) {
            this.worldObj.spawnEntityInWorld(tnt);
         }
      }

      if (this.special2 > 525) {
         this.special2 = 0;
      }

      if (this.attackingPlayer != null) {
         float px = (float)this.attackingPlayer.posX;
         float pz = (float)this.attackingPlayer.posZ;
         if ((double)px > this.posX) {
            this.jump(1, 0);
         } else if ((double)px < this.posX) {
            this.jump(-1, 0);
         }

         if ((double)pz > this.posZ) {
            this.jump(0, 1);
         } else if ((double)pz < this.posZ) {
            this.jump(0, -1);
         }
      } else {
         this.jump(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);
      }

   }

   protected String getLivingSound() {
      return "step.stone";
   }

   protected String getHurtSound() {
      return "step.stone";
   }

   protected String getDeathSound() {
      return "random.break";
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEFINED;
   }
}
*/
