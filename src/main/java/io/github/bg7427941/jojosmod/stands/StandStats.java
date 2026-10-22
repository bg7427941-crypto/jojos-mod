package io.github.bg7427941.jojosmod.stands;

/** Initial 1–5 ratings; power, speed and range drive the prototype's basic attack. */
public record StandStats(int power, int speed, int range, int durability, int precision, int potential) {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final float DAMAGE_PER_POWER = 2.0F;
    private static final int ATTACK_TICK_BUDGET = 20;
    private static final double BLOCKS_PER_RANGE = 4.0;
    private static final double BASIC_ATTACK_REACH = 3.0;

    public StandStats {
        for (int rating : new int[] {power, speed, range, durability, precision, potential}) {
            if (rating < MIN_RATING || rating > MAX_RATING) {
                throw new IllegalArgumentException("Stand ratings must be between 1 and 5");
            }
        }
    }

    public float attackDamage() {
        return power * DAMAGE_PER_POWER;
    }

    public int attackCooldownTicks() {
        return Math.max(1, ATTACK_TICK_BUDGET / speed);
    }

    public double attackReach() {
        return BASIC_ATTACK_REACH;
    }

    public double maxOwnerDistance() {
        return range * BLOCKS_PER_RANGE;
    }
}
