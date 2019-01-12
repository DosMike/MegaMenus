package de.dosmike.sponge.megamenus.api.elements;

import de.dosmike.sponge.megamenus.api.util.Tickable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface IIcon extends Tickable {

    ItemStackSnapshot render();
    /** might be called before render() to notify animated icons in order to render the correct frame */
    default boolean tick(int ms) {
        return false;
    }

    //Region builder
    /**
     * A convenience builder for animated IIcons.
     * Specify the item snapshots and frequency and the rest should automatically be handled.
     * The animation will play at a constant rate.
     */
    class Builder {
        private int frameTime=0;
        private List<ItemStackSnapshot> items = new LinkedList<>();

        private Builder() {}

        public Builder addFrame(ItemStackSnapshot snapshot) {
            items.add(snapshot);
            return this;
        }
        public Builder addFrame(ItemStack stack) {
            items.add(stack.createSnapshot());
            return this;
        }
        public Builder addFrame(ItemType type) {
            items.add(type.getTemplate());
            return this;
        }
        public Builder addFrames(ItemStackSnapshot... snapshots) {
            Collections.addAll(items, snapshots);
            return this;
        }
        public Builder addFrames(ItemStack... stacks) {
            for (ItemStack stack : stacks)
                items.add(stack.createSnapshot());
            return this;
        }
        public Builder addFrames(ItemType... types) {
            for (ItemType type : types)
                items.add(type.getTemplate());
            return this;
        }
        public Builder addFrameSnapshots(Collection<ItemStackSnapshot> snapshots) {
            items.addAll(snapshots);
            return this;
        }
        public Builder addFrameItemStacks(Collection<ItemStack> stacks) {
            for (ItemStack stack : stacks)
                items.add(stack.createSnapshot());
            return this;
        }
        public Builder addFrameItemTypes(Collection<ItemType> types) {
            for (ItemType type : types)
                items.add(type.getTemplate());
            return this;
        }

        /** set the FrameTime for this animation in ticks.
         * this will result in the icon to advance a frame every specified ticks.
         * A FrameTime of &lt;= 0 will prevent animation. */
        public Builder setFrametime(int ticksPerFrame) {
            frameTime = ticksPerFrame*20;
            return this;
        }
        /** set the FrameTime for this animation in ticks.
         * this will result in the icon to advance the specified amount of times per second.
         * A framerate of &lt;= 0 will prevent animation, framerate &gt; 20 might lag the network/game. */
        public Builder setFPS(Double framesPerSecond) {
            frameTime = (int)(1000/framesPerSecond);
            return this;
        }


        public IIcon build() {
            return new IIcon() {
                ItemStackSnapshot[] icons = items.toArray(new ItemStackSnapshot[0]);
                int frame = 0;
                int frameTime = Math.max(Builder.this.frameTime, 0);
                @Override
                public ItemStackSnapshot render() {
                    return icons[frame];
                }
                int passedTime = 0;
                @Override
                public boolean tick(int ms) {
                    boolean change = false;
                    passedTime += ms;
                    while (passedTime > frameTime) {
                        change = true;
                        if (++frame > icons.length)
                            frame = 0;
                    }
                    return change;
                }
            };
        }
    }

    static Builder builder() {
        return new Builder();
    }
    //endregion

    /**
     * convenience function for retrieving a static IIcon from a ItemStackSnapshot
     */
    static IIcon of(ItemStackSnapshot snapshot) {
        return new IIcon() {
            ItemStackSnapshot display = snapshot.copy();
            @Override
            public ItemStackSnapshot render() {
                return display;
            }
        };
    }
    /**
     * convenience function for retrieving a static IIcon from a ItemStack
     */
    static IIcon of(ItemStack stack) {
        return new IIcon() {
            ItemStackSnapshot display = stack.createSnapshot();
            @Override
            public ItemStackSnapshot render() {
                return display;
            }
        };
    }
    /**
     * convenience function for retrieving a static IIcon from a ItemType
     */
    static IIcon of(ItemType type) {
        return new IIcon() {
            ItemStackSnapshot display = type.getTemplate();
            @Override
            public ItemStackSnapshot render() {
                return display;
            }
        };
    }

}
