package io.github.bg7427941.jojosmod.journey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Stable temple/keeper identities and pending offline rewards survive restarts. */
public final class JourneyWorldData extends SavedData {
    public record Shrine(BlockPos temple,BlockPos keeperPosition,UUID keeper,long readyAt) {
        static final Codec<Shrine> CODEC=RecordCodecBuilder.create(i->i.group(
                BlockPos.CODEC.fieldOf("temple").forGetter(Shrine::temple),BlockPos.CODEC.fieldOf("keeper_position").forGetter(Shrine::keeperPosition),
                UUIDUtil.CODEC.fieldOf("keeper").forGetter(Shrine::keeper),Codec.LONG.optionalFieldOf("ready_at",0L).forGetter(Shrine::readyAt)).apply(i,Shrine::new));
        public Shrine cooldown(long until){return new Shrine(temple,keeperPosition,keeper,until);}
    }
    public static final Codec<JourneyWorldData> CODEC=RecordCodecBuilder.create(i->i.group(
            Shrine.CODEC.listOf().optionalFieldOf("shrines",List.of()).forGetter(d->List.copyOf(d.shrines)),
            UUIDUtil.CODEC.listOf().optionalFieldOf("pending_victories",List.of()).forGetter(d->d.pending.stream().sorted().toList())).apply(i,JourneyWorldData::new));
    public static final SavedDataType<JourneyWorldData> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("jojosmod","stardust_journey"),JourneyWorldData::new,CODEC);
    public final List<Shrine> shrines=new ArrayList<>();
    public final Set<UUID> pending=new HashSet<>();
    public JourneyWorldData(){}
    private JourneyWorldData(List<Shrine> shrines,List<UUID> pending){this.shrines.addAll(shrines);this.pending.addAll(pending);}
    public static JourneyWorldData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(TYPE);}
    public Shrine shrine(BlockPos pos){return shrines.stream().filter(s->s.temple.equals(pos)).findFirst().orElse(null);}
    public void replace(Shrine shrine){shrines.removeIf(s->s.temple.equals(shrine.temple));shrines.add(shrine);setDirty();}
    public void queue(UUID player){if(pending.add(player))setDirty();}
    public void delivered(UUID player){if(pending.remove(player))setDirty();}
}
