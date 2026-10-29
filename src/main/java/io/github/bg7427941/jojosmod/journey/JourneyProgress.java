package io.github.bg7427941.jojosmod.journey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Private persistent milestones; reward claims are distinct from the victory itself. */
public record JourneyProgress(boolean metKeeper, boolean defeatedDio, boolean claimedCore, Optional<BlockPos> keeper, java.util.List<String> masteryClaims) {
    private static final java.util.Set<String> CLAIMABLE=java.util.Set.of("star_platinum","hierophant_green","silver_chariot","magicians_red","hermit_purple","the_fool");
    private static final Codec<java.util.List<String>> CLAIMS=Codec.STRING.listOf(0,6).validate(values->new java.util.HashSet<>(values).size()==values.size()&&CLAIMABLE.containsAll(values)?com.mojang.serialization.DataResult.success(values):com.mojang.serialization.DataResult.error(()->"Invalid mastery core claims"));
    public JourneyProgress(boolean met,boolean won,boolean claimed,Optional<BlockPos> keeper){this(met,won,claimed,keeper,java.util.List.of());}
    public JourneyProgress {masteryClaims=java.util.List.copyOf(masteryClaims);}
    public static final JourneyProgress EMPTY=new JourneyProgress(false,false,false,Optional.empty());
    public static final MapCodec<JourneyProgress> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            Codec.BOOL.optionalFieldOf("met_keeper",false).forGetter(JourneyProgress::metKeeper),
            Codec.BOOL.optionalFieldOf("defeated_dio",false).forGetter(JourneyProgress::defeatedDio),
            Codec.BOOL.optionalFieldOf("claimed_core",false).forGetter(JourneyProgress::claimedCore),
            BlockPos.CODEC.optionalFieldOf("keeper").forGetter(JourneyProgress::keeper),
            CLAIMS.optionalFieldOf("mastery_claims",java.util.List.of()).forGetter(JourneyProgress::masteryClaims)).apply(i,JourneyProgress::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,JourneyProgress> STREAM_CODEC=StreamCodec.of((b,p)->{
        b.writeBoolean(p.metKeeper);b.writeBoolean(p.defeatedDio);b.writeBoolean(p.claimedCore);b.writeBoolean(p.keeper.isPresent());p.keeper.ifPresent(b::writeBlockPos);
        b.writeVarInt(p.masteryClaims.size());for(var id:p.masteryClaims)b.writeUtf(id,64);
    },b->{
        boolean met=b.readBoolean(),won=b.readBoolean(),claimed=b.readBoolean();var keeper=b.readBoolean()?Optional.of(b.readBlockPos()):Optional.<BlockPos>empty();
        int count=b.readVarInt();if(count<0||count>6)throw new io.netty.handler.codec.DecoderException("Invalid mastery claim count");
        var claims=new java.util.ArrayList<String>();for(int i=0;i<count;i++){var id=b.readUtf(64);if(!CLAIMABLE.contains(id)||claims.contains(id))throw new io.netty.handler.codec.DecoderException("Invalid mastery claim");claims.add(id);}
        return new JourneyProgress(met,won,claimed,keeper,claims);
    });
    public JourneyProgress locate(BlockPos pos){return new JourneyProgress(metKeeper,defeatedDio,claimedCore,Optional.of(pos.immutable()),masteryClaims);}
    public JourneyProgress meet(BlockPos pos){return new JourneyProgress(true,defeatedDio,claimedCore,Optional.of(pos.immutable()),masteryClaims);}
    public JourneyProgress victory(){return new JourneyProgress(metKeeper,true,claimedCore,keeper,masteryClaims);}
    public JourneyProgress claimed(){return new JourneyProgress(metKeeper,defeatedDio,true,keeper,masteryClaims);}
    public JourneyProgress claimMastery(String id){var next=new java.util.ArrayList<>(masteryClaims);next.add(id);return new JourneyProgress(metKeeper,defeatedDio,claimedCore,keeper,next);}
}
