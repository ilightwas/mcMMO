package com.gmail.nossr50.util.player;

import static com.gmail.nossr50.util.player.NotificationManager.generateOffset;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

class NotificationManagerTest {

    final static int ITERATIONS = 1_000_000;

    @Test
    void generateOffsetShouldRespectInterval() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < ITERATIONS; ++i) {
            int levelInterval = rnd.nextInt(1, Integer.MAX_VALUE);
            int offset = generateOffset(rnd.nextInt(1, Integer.MAX_VALUE), levelInterval, UUID.randomUUID(),
                    rnd.nextInt(), rnd.nextInt());
            assertThat(offset).isBetween(0, levelInterval - 1);
        }
    }

    @Test
    void generateOffsetMostlyReturnsDifferentValuesForMostlyEqualInputs() {
        final int block = 1;
        final int levelInterval = 100;
        final UUID uuid = UUID.randomUUID();
        final int extra1 = 1;
        final int extra2 = 9000;

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int blockCollisions = 0;
        int uuidCollisions = 0;
        int extra1Collisions = 0;
        int extra2Collisions = 0;

        for (int i = 0; i < ITERATIONS; ++i) {

            // block
            int offset1 = generateOffset(rnd.nextInt(0, Integer.MAX_VALUE), levelInterval, uuid, extra1, extra2);
            int offset2 = generateOffset(rnd.nextInt(0, Integer.MAX_VALUE), levelInterval, uuid, extra1, extra2);

            if (offset1 == offset2) {
                blockCollisions++;
            }

            // levelInterval is tested in generateOffsetShouldRespectInterval

            // uuid
            offset1 = generateOffset(block, levelInterval, UUID.randomUUID(), extra1, extra2);
            offset2 = generateOffset(block, levelInterval, UUID.randomUUID(), extra1, extra2);

            if (offset1 == offset2) {
                uuidCollisions++;
            }

            // extra1
            offset1 = generateOffset(block, levelInterval, uuid, rnd.nextInt(), extra2);
            offset2 = generateOffset(block, levelInterval, uuid, rnd.nextInt(), extra2);

            if (offset1 == offset2) {
                extra1Collisions++;
            }

            // extra2
            offset1 = generateOffset(block, levelInterval, uuid, extra1, rnd.nextInt());
            offset2 = generateOffset(block, levelInterval, uuid, extra1, rnd.nextInt());

            if (offset1 == offset2) {
                extra2Collisions++;
            }

        }

        // at max 5% of the time
        final int atMax = (int) (ITERATIONS * 0.05);

        System.out.println("AtMax: " + atMax);
        System.out.println("blockCollisions: " + blockCollisions);
        System.out.println("uuidCollisions: " + uuidCollisions);
        System.out.println("extra1Collisions: " + extra1Collisions);
        System.out.println("extra2Collisions: " + extra2Collisions);

        assertThat(blockCollisions).isLessThan(atMax);
        assertThat(uuidCollisions).isLessThan(atMax);
        assertThat(extra1Collisions).isLessThan(atMax);
        assertThat(extra2Collisions).isLessThan(atMax);
    }

}
