//package net.lostpatrol.mobspvpmaster.entity.ai;
//
//import net.minecraft.world.entity.EntitySelector;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.entity.animal.Cat;
//import net.minecraft.world.entity.monster.Phantom;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//
//import java.util.EnumSet;
//import java.util.List;
//
//public class PhantomRocketChargeAttackGoal extends Goal{
//
//    private final Phantom phantom;
//    private int attackTime;
//    private int chargeTime;
//    private int attackCooldown;
//
//    PhantomRocketChargeAttackGoal(Phantom phantom) {
//        this.phantom = phantom;
//    }
//
//    @Override
//    public boolean canUse() {
//        return phantom.getTarget() != null && phantom.getTarget().isAlive();
//    }
//
//    abstract class PhantomMoveTargetGoal extends Goal {
//        public PhantomMoveTargetGoal() {
//            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
//        }
//
//        protected boolean touchingTarget() {
//            return phantom.moveTargetPoint.distanceToSqr(Phantom.this.getX(), Phantom.this.getY(), Phantom.this.getZ()) < 4.0;
//        }
//    }
//
//
//    class PhantomSweepAttackGoal extends Phantom.PhantomMoveTargetGoal {
//        PhantomSweepAttackGoal() {}
//        private static final int CAT_SEARCH_TICK_DELAY = 20;
//        private boolean isScaredOfCat;
//        private int catSearchTick;
//
//        @Override
//        public boolean canUse() {
//            return Phantom.this.getTarget() != null && Phantom.this.attackPhase == Phantom.AttackPhase.SWOOP;
//        }
//
//        @Override
//        public boolean canContinueToUse() {
//            LivingEntity livingentity = Phantom.this.getTarget();
//            if (livingentity == null) {
//                return false;
//            } else if (!livingentity.isAlive()) {
//                return false;
//            } else if (livingentity instanceof Player player && (livingentity.isSpectator() || player.isCreative())) {
//                return false;
//            } else if (!this.canUse()) {
//                return false;
//            } else {
//                if (Phantom.this.tickCount > this.catSearchTick) {
//                    this.catSearchTick = Phantom.this.tickCount + 20;
//                    List<Cat> list = Phantom.this.level()
//                            .getEntitiesOfClass(Cat.class, Phantom.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
//
//                    for (Cat cat : list) {
//                        cat.hiss();
//                    }
//
//                    this.isScaredOfCat = !list.isEmpty();
//                }
//
//                return !this.isScaredOfCat;
//            }
//        }
//
//        @Override
//        public void start() {
//        }
//
//        @Override
//        public void stop() {
//            Phantom.this.setTarget(null);
//            Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
//        }
//
//        @Override
//        public void tick() {
//            LivingEntity livingentity = Phantom.this.getTarget();
//            if (livingentity != null) {
//                Phantom.this.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ());
//                if (Phantom.this.getBoundingBox().inflate(0.2F).intersects(livingentity.getBoundingBox())) {
//                    Phantom.this.doHurtTarget(getServerLevel(Phantom.this.level()), livingentity);
//                    Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
//                    if (!Phantom.this.isSilent()) {
//                        Phantom.this.level().levelEvent(1039, Phantom.this.blockPosition(), 0);
//                    }
//                } else if (Phantom.this.horizontalCollision || Phantom.this.hurtTime > 0) {
//                    Phantom.this.attackPhase = Phantom.AttackPhase.CIRCLE;
//                }
//            }
//        }
//    }
//}
//
