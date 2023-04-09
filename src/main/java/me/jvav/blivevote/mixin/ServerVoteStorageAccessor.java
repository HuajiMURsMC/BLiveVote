package me.jvav.blivevote.mixin;

import net.minecraft.voting.votes.ServerVoteStorage;
import net.minecraft.voting.votes.VoterMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerVoteStorage.class)
public interface ServerVoteStorageAccessor {
    @Accessor
    VoterMap getVotes();
}
