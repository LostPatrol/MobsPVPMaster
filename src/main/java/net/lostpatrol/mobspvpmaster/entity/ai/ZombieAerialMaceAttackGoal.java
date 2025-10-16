    package net.lostpatrol.mobspvpmaster.entity.ai;

    import net.lostpatrol.mobspvpmaster.MobsPVPMaster;
    import net.lostpatrol.mobspvpmaster.event.equips.ZombieEquipmentHandler;
    import net.minecraft.core.Direction;
    import net.minecraft.core.particles.ParticleTypes;
    import net.minecraft.core.registries.BuiltInRegistries;
    import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
    import net.minecraft.server.level.ServerLevel;
    import net.minecraft.server.level.ServerPlayer;
    import net.minecraft.sounds.SoundEvent;
    import net.minecraft.sounds.SoundEvents;
    import net.minecraft.sounds.SoundSource;
    import net.minecraft.tags.BlockTags;
    import net.minecraft.util.Mth;
    import net.minecraft.util.random.WeightedList;
    import net.minecraft.world.InteractionHand;
    import net.minecraft.world.damagesource.DamageSource;
    import net.minecraft.world.entity.*;
    import net.minecraft.world.entity.ai.attributes.Attributes;
    import net.minecraft.world.entity.ai.goal.Goal;
    import net.minecraft.world.entity.decoration.ArmorStand;
    import net.minecraft.world.entity.monster.Zombie;
    import net.minecraft.world.entity.player.Player;
    import net.minecraft.world.entity.projectile.Projectile;
    import net.minecraft.world.entity.projectile.windcharge.WindCharge;
    import net.minecraft.world.item.ItemStack;
    import net.minecraft.world.item.Items;
    import net.minecraft.world.item.MaceItem;
    import net.minecraft.world.item.WindChargeItem;
    import net.minecraft.world.item.enchantment.EnchantmentHelper;
    import net.minecraft.world.item.enchantment.Enchantments;
    import net.minecraft.world.level.ExplosionDamageCalculator;
    import net.minecraft.world.level.Level;
    import net.minecraft.world.level.SimpleExplosionDamageCalculator;
    import net.minecraft.world.phys.AABB;
    import net.minecraft.world.phys.Vec3;
    import org.slf4j.Logger;

    import java.util.EnumSet;
    import java.util.Optional;
    import java.util.function.Function;
    import java.util.function.Predicate;

    import net.lostpatrol.mobspvpmaster.event.equips.ZombieEquipmentHandler.ArmorLevel;

    public class ZombieAerialMaceAttackGoal extends Goal {

        public Logger logger = MobsPVPMaster.LOGGER;

        private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
                true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
        );

        private final Zombie zombie;
        private final ArmorLevel armorLevel;
        private final double speedModifier;
        private final boolean followingTargetEvenIfNotSeen;
        private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F; // 0.82

        private boolean isWindJumping = false;
        private int ticksSinceJump = 0;
        private long lastCanUseCheck;
        private int ticksUntilNextAttack;

        private static final int MIN_JUMP_DISTANCE = 4;
        private static final int MAX_JUMP_DISTANCE = 8;

        private static final int MAX_JUMP_TICKS = 50;
        private static final float WIND_JUMP_CHANCE = 0.8f;


        public ZombieAerialMaceAttackGoal(Zombie zombie, ArmorLevel armorLevel, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            this.zombie = zombie;
            this.armorLevel = armorLevel;
            this.speedModifier = speedModifier;
            this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity livingentity = this.zombie.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                return false;
            } else if (this.zombie.isWithinMeleeAttackRange(livingentity)) {
                return false;
            } else if (!hasMace() || !hasWindCharge()) {
                return false;
            } else if (zombie.distanceTo(livingentity)< MIN_JUMP_DISTANCE || zombie.distanceTo(livingentity) > MAX_JUMP_DISTANCE){
                return false;
            }

            long i = this.zombie.level().getGameTime();
            if (i - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = i;
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (isWindJumping)
                return true;

            LivingEntity livingentity = this.zombie.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                return false;
            } else if (!hasMace() || !hasWindCharge()) {
                return false;
    //        }else if (zombie.distanceTo(livingentity)< MIN_JUMP_DISTANCE || zombie.distanceTo(livingentity) > MAX_JUMP_DISTANCE){
            } else if (zombie.isWithinMeleeAttackRange(livingentity) || zombie.distanceTo(livingentity) > MAX_JUMP_DISTANCE) {
                return false;
            } else if (livingentity instanceof Player player && (player.isSpectator() || player.isCreative())){
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            this.zombie.setAggressive(true);
            this.isWindJumping = false;
            this.ticksSinceJump = 0;
        }

        @Override
        public void stop() {
            logger.info("ZombieAerialMaceAttackGoal.stop()");
            this.isWindJumping = false;
            this.ticksSinceJump = 0;

            LivingEntity livingentity = this.zombie.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.zombie.setTarget(null);
            }
            this.zombie.setAggressive(false);
            this.zombie.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity livingentity = this.zombie.getTarget();
            if (livingentity == null || !livingentity.isAlive()) return;

            double distance = zombie.distanceTo(livingentity);
            zombie.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);

            if (zombie.onGround() || zombie.isInWater()) {
                isWindJumping = false;
                ticksSinceJump += 1;
                if (ticksSinceJump >= MAX_JUMP_TICKS) {
                    if (distance >= MIN_JUMP_DISTANCE && distance <= MAX_JUMP_DISTANCE &&
                            zombie.onGround() &&
                            isTimeToAttack() &&
                            zombie.getRandom().nextFloat() < WIND_JUMP_CHANCE) {

                        performWindJump(livingentity);
                    }
                } else {
                    zombie.getNavigation().moveTo(livingentity, speedModifier);
                }
            } else if (isWindJumping) {
                ticksSinceJump = 0;
                // min horizontal speed in air
                moveInAir(livingentity);
                if (canZombieSmashAttack()&&
    //                    distance <= getAttackReach(livingentity) &&
                        this.isWithinAerialMaceAttackRange(livingentity) &&
                        isTimeToAttack() &&
                        zombie.getSensing().hasLineOfSight(livingentity)) {

                    performMaceSmashAttack(livingentity);
                }
            }
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        }

        private void moveInAir(LivingEntity target) {
            Vec3 toTarget = target.position().subtract(zombie.position());
            double horizontalSpeed = Math.min(toTarget.horizontalDistance() * 0.2, 1.2)*0.5;
            Vec3 targetSpeed = target.getDeltaMovement();
            Vec3 horizontalDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();
            int k = armorLevel.ordinal() + 1;
            Vec3 jumpVelocity = new Vec3(
                    horizontalDirection.x * (horizontalSpeed + targetSpeed.x * 1.0 * k + 0.2 * zombie.getRandom().nextFloat()),
                    zombie.getDeltaMovement().y(),
                    horizontalDirection.z * (horizontalSpeed + targetSpeed.z * 1.0 * k + 0.2 * zombie.getRandom().nextFloat())
            );

            zombie.setDeltaMovement(jumpVelocity);
        }

        private void performWindJump(LivingEntity target) {
            ItemStack windChargeStack = zombie.getOffhandItem();
            if (windChargeStack.getItem() instanceof WindChargeItem) {
                isWindJumping = true;
                WindCharge windCharge = new WindCharge(EntityType.WIND_CHARGE, zombie.level());

                zombie.level().playSound(
                        null,
                        zombie.getX(),
                        zombie.getY(),
                        zombie.getZ(),
                        SoundEvents.WIND_CHARGE_THROW,
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.4F / (zombie.getRandom().nextFloat() * 0.4F + 0.8F)
                );

                windCharge.level()
                        .explode(
                                windCharge,
                                null,
                                EXPLOSION_DAMAGE_CALCULATOR,
                                zombie.getX(),
                                zombie.getY(),
                                zombie.getZ(),
                                1.2F,
                                false,
                                Level.ExplosionInteraction.TRIGGER,
                                ParticleTypes.GUST_EMITTER_SMALL,
                                ParticleTypes.GUST_EMITTER_LARGE,
                                WeightedList.of(),
                                SoundEvents.WIND_CHARGE_BURST
                        );

                windChargeStack.shrink(1);
                if (windChargeStack.isEmpty()) {
                    zombie.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                }

                Vec3 toTarget = target.position().subtract(zombie.position());
                double horizontalSpeed = Math.min(toTarget.horizontalDistance() * 0.2, 1.2)*0.85;
                Vec3 targetSpeed = target.getDeltaMovement();
                Vec3 horizontalDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();
                int k = armorLevel.ordinal() + 1;
                Vec3 jumpVelocity = new Vec3(
                        horizontalDirection.x * (horizontalSpeed + targetSpeed.x * 1.5 * k + 0.2 * zombie.getRandom().nextFloat()),
                        0.8 + zombie.getRandom().nextFloat() * 0.3 * k,
                        horizontalDirection.z * (horizontalSpeed + targetSpeed.z * 1.5 * k + 0.2 * zombie.getRandom().nextFloat())
                );

                zombie.setDeltaMovement(jumpVelocity);
            }
        }

        private void performMaceSmashAttack(LivingEntity target) {
            logger.info("ZombieAerialMaceAttackGoal.performMaceSmashAttack()");
            zombie.swing(InteractionHand.MAIN_HAND);

            SoundEvent soundevent = zombie.fallDistance > 5.0 ?
                    SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            zombie.level().playSound(null, zombie.blockPosition(),
                    soundevent, SoundSource.HOSTILE, 1.0F, 1.0F);

    //        MaceItem mace = (MaceItem) zombie.getMainHandItem().getItem();
    //        float bonus = getAttackDamageBonus(target, 0,  zombie.damageSources().mobAttack(zombie));
    //        logger.info("ZombieAerialMaceAttackGoal.performMaceSmashAttack() bonus: " + bonus);
    //        mace.hurtEnemy(zombie.getMainHandItem(), target, zombie);
            zombie.doHurtTarget((ServerLevel) zombie.level(),  target);

            knockback(zombie.level(), zombie, target);

            zombie.setDeltaMovement(zombie.getDeltaMovement().with(Direction.Axis.Y, 0.01F));
            zombie.fallDistance = 0;
            isWindJumping = false;
            ticksSinceJump = 0;
        }

        private double getAttackReach(LivingEntity target) {
            return zombie.getBbWidth() + target.getBbWidth() + 2.0;
        }

        private boolean canZombieSmashAttack() {
            return MaceItem.canSmashAttack(zombie);
        }

        private boolean hasMace() {
            return zombie.getMainHandItem().getItem() == Items.MACE;
        }

        private boolean hasWindCharge() {
            return !zombie.getOffhandItem().isEmpty() && zombie.getOffhandItem().getItem()==Items.WIND_CHARGE;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        // These methods below are copied from MaceItem.java because they are private and I don't want to use AT.
        private static void knockback(Level level, Entity attacker, Entity target) {
            level.levelEvent(2013, target.getOnPos(), 750); // Smash Particles spawn here
            level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.5), knockbackPredicate(attacker, target))
                    .forEach(livingEntity -> {
                        Vec3 vec3 = livingEntity.position().subtract(target.position());
                        double d0 = getKnockbackPower(attacker, livingEntity, vec3);
                        Vec3 vec31 = vec3.normalize().scale(d0);
                        if (d0 > 0.0) {
                            livingEntity.push(vec31.x, 0.7F, vec31.z);
                            if (livingEntity instanceof ServerPlayer serverplayer) {
                                serverplayer.connection.send(new ClientboundSetEntityMotionPacket(serverplayer));
                            }
                        }
                    });
        }

        private static double getKnockbackPower(Entity attacker, LivingEntity entity, Vec3 offset) {
            return (3.5 - offset.length())
                    * 0.7F
                    * (attacker.fallDistance > 5.0 ? 2 : 1)
                    * (1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }

        private static Predicate<LivingEntity> knockbackPredicate(Entity attacker, Entity target) {
            return livingEntity -> {
                boolean flag = !livingEntity.isSpectator();
                boolean flag1 = livingEntity != attacker && livingEntity != target;
                boolean flag2 = !attacker.isAlliedTo(livingEntity);
                boolean flag3 = !(
                        livingEntity instanceof TamableAnimal tamableanimal
                                && target instanceof LivingEntity livingentity
                                && tamableanimal.isTame()
                                && tamableanimal.isOwnedBy(livingentity)
                );
                boolean flag4 = !(livingEntity instanceof ArmorStand armorstand && armorstand.isMarker());
                boolean flag5 = target.distanceToSqr(livingEntity) <= Math.pow(3.5, 2.0);
                return flag && flag1 && flag2 && flag3 && flag4 && flag5;
            };
        }

        public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
            if (damageSource.getDirectEntity() instanceof LivingEntity livingentity) {
                double d3 = 3.0;
                double d0 = 8.0;
                double d1 = livingentity.fallDistance;
                double d2;
                if (d1 <= 3.0) {
                    d2 = 4.0 * d1;
                } else if (d1 <= 8.0) {
                    d2 = 12.0 + 2.0 * (d1 - 3.0);
                } else {
                    d2 = 22.0 + d1 - 8.0;
                }
                float bonus =  livingentity.level() instanceof ServerLevel serverlevel
                        ? (float)(d2 + EnchantmentHelper.modifyFallBasedDamage(serverlevel, livingentity.getWeaponItem(), target, damageSource, 0.0F) * d1)
                        : (float)d2;
                if (bonus >= 20.0F){
                    logger.info("A pvp master zombie just dealt {} damage in a single hit!", bonus);
                }
                return bonus;
            } else {
                return 0.0F;
            }
        }

        public boolean isWithinAerialMaceAttackRange(LivingEntity entity) {
            return getAttackBoundingBox(DEFAULT_ATTACK_REACH + 0.4 + 0.2 * armorLevel.ordinal()).intersects(entity.getHitbox());
        }

        protected AABB getAttackBoundingBox(double attackReach) {
            Entity entity = zombie.getVehicle();
            AABB aabb;
            if (entity != null) {
                AABB aabb1 = entity.getBoundingBox();
                AABB aabb2 = zombie.getBoundingBox();
                aabb = new AABB(
                        Math.min(aabb2.minX, aabb1.minX),
                        aabb2.minY,
                        Math.min(aabb2.minZ, aabb1.minZ),
                        Math.max(aabb2.maxX, aabb1.maxX),
                        aabb2.maxY,
                        Math.max(aabb2.maxZ, aabb1.maxZ)
                );
            } else {
                aabb = zombie.getBoundingBox();
            }
            return aabb.inflate(attackReach, 0.0, attackReach);
        }
    }