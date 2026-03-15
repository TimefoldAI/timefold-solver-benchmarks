package ai.timefold.solver.benchmarks.examples.conferencescheduling.domain;

import java.util.List;
import java.util.SequencedSet;

import ai.timefold.solver.benchmarks.examples.common.domain.AbstractPersistable;
import ai.timefold.solver.benchmarks.examples.common.persistence.jackson.JacksonUniqueIdGenerator;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
@JsonIdentityInfo(generator = JacksonUniqueIdGenerator.class)
public class Talk extends AbstractPersistable {

    private String code;
    private String title;
    private TalkType talkType;
    private List<Speaker> speakerList;
    private SequencedSet<String> themeTrackTagSet;
    private SequencedSet<String> sectorTagSet;
    private SequencedSet<String> audienceTypeSet;
    private int audienceLevel;
    private SequencedSet<String> contentTagSet;
    private String language;
    private SequencedSet<String> requiredRoomTagSet;
    private SequencedSet<String> preferredRoomTagSet;
    private SequencedSet<String> prohibitedRoomTagSet;
    private SequencedSet<String> undesiredRoomTagSet;
    private SequencedSet<String> mutuallyExclusiveTalksTagSet;
    private SequencedSet<Talk> prerequisiteTalkSet;
    private int favoriteCount;
    private int crowdControlRisk;

    @PlanningPin
    private boolean pinnedByUser = false;

    @PlanningVariable
    private Timeslot timeslot;

    @PlanningVariable
    private Room room;

    public Talk() {
    }

    public Talk(long id) {
        super(id);
    }

    public boolean hasSpeaker(Speaker speaker) {
        return speakerList.contains(speaker);
    }

    public int overlappingThemeTrackCount(Talk other) {
        return overlappingCount(themeTrackTagSet, other.themeTrackTagSet);
    }

    private static <Item_> int overlappingCount(SequencedSet<Item_> left, SequencedSet<Item_> right) {
        int leftSize = left.size();
        if (leftSize == 0) {
            return 0;
        }
        int rightSize = right.size();
        if (rightSize == 0) {
            return 0;
        }
        SequencedSet<Item_> smaller = leftSize < rightSize ? left : right;
        SequencedSet<Item_> other = smaller == left ? right : left;
        int overlappingCount = 0;
        for (Item_ item : smaller) { // Iterate over smaller set, lookup in the larger.
            if (other.contains(item)) {
                overlappingCount++;
            }
        }
        return overlappingCount;
    }

    public int overlappingSectorCount(Talk other) {
        return overlappingCount(sectorTagSet, other.sectorTagSet);
    }

    public int overlappingAudienceTypeCount(Talk other) {
        return overlappingCount(audienceTypeSet, other.audienceTypeSet);

    }

    public int overlappingContentCount(Talk other) {
        return overlappingCount(contentTagSet, other.contentTagSet);

    }

    private static <Item_> int missingCount(SequencedSet<Item_> required, SequencedSet<Item_> available) {
        int requiredCount = required.size();
        if (requiredCount == 0) {
            return 0; // If no items are required, none can be missing.
        }
        int availableCount = available.size();
        if (availableCount == 0) {
            return requiredCount; // All the items are missing.
        }
        int missingCount = 0;
        for (Item_ item : required) {
            if (!available.contains(item)) {
                missingCount++;
            }
        }
        return missingCount;
    }

    public int missingRequiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return missingCount(requiredRoomTagSet, room.getTagSet());

    }

    public int missingPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return missingCount(preferredRoomTagSet, room.getTagSet());
    }

    public int prevailingProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(prohibitedRoomTagSet, room.getTagSet());

    }

    public int prevailingUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(undesiredRoomTagSet, room.getTagSet());
    }

    public int missingSpeakerPreferredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += missingCount(speaker.getPreferredTimeslotTagSet(), timeslot.getTagSet());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += overlappingCount(speaker.getUndesiredTimeslotTagSet(), timeslot.getTagSet());
        }
        return count;
    }

    public int missingSpeakerRequiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += missingCount(speaker.getRequiredRoomTagSet(), room.getTagSet());
        }
        return count;
    }

    public int missingSpeakerPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += missingCount(speaker.getPreferredRoomTagSet(), room.getTagSet());
        }
        return count;
    }

    public int prevailingSpeakerProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += overlappingCount(speaker.getProhibitedRoomTagSet(), room.getTagSet());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakerList) {
            count += overlappingCount(speaker.getUndesiredRoomTagSet(), room.getTagSet());
        }
        return count;
    }

    public int overlappingMutuallyExclusiveTalksTagCount(Talk other) {
        return overlappingCount(mutuallyExclusiveTalksTagSet, other.mutuallyExclusiveTalksTagSet);
    }

    public boolean hasMutualSpeaker(Talk other) {
        for (Speaker speaker : speakerList) {
            if (other.hasSpeaker(speaker)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public Integer getDurationInMinutes() {
        return timeslot == null ? null : timeslot.getDurationInMinutes();
    }

    public boolean overlapsTime(Talk other) {
        return timeslot != null && other.getTimeslot() != null && timeslot.overlapsTime(other.getTimeslot());
    }

    public int overlappingDurationInMinutes(Talk other) {
        if (timeslot == null) {
            return 0;
        }
        if (other.getTimeslot() == null) {
            return 0;
        }
        return timeslot.getOverlapInMinutes(other.getTimeslot());
    }

    public int combinedDurationInMinutes(Talk other) {
        if (timeslot == null) {
            return 0;
        }
        if (other.getTimeslot() == null) {
            return 0;
        }
        return timeslot.getDurationInMinutes() + other.getTimeslot().getDurationInMinutes();
    }

    @Override
    public String toString() {
        return code;
    }

    @ValueRangeProvider
    public SequencedSet<Timeslot> getTimeslotRange() {
        return talkType.getCompatibleTimeslotSet();
    }

    @ValueRangeProvider
    public SequencedSet<Room> getRoomRange() {
        return talkType.getCompatibleRoomSet();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TalkType getTalkType() {
        return talkType;
    }

    public void setTalkType(TalkType talkType) {
        this.talkType = talkType;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<Speaker> getSpeakerList() {
        return speakerList;
    }

    public SequencedSet<String> getThemeTrackTagSet() {
        return themeTrackTagSet;
    }

    public void setThemeTrackTagSet(SequencedSet<String> themeTrackTagSet) {
        this.themeTrackTagSet = themeTrackTagSet;
    }

    public SequencedSet<String> getSectorTagSet() {
        return sectorTagSet;
    }

    public void setSectorTagSet(SequencedSet<String> sectorTagSet) {
        this.sectorTagSet = sectorTagSet;
    }

    public SequencedSet<String> getAudienceTypeSet() {
        return audienceTypeSet;
    }

    public void setAudienceTypeSet(SequencedSet<String> audienceTypeSet) {
        this.audienceTypeSet = audienceTypeSet;
    }

    public int getAudienceLevel() {
        return audienceLevel;
    }

    public void setAudienceLevel(int audienceLevel) {
        this.audienceLevel = audienceLevel;
    }

    public SequencedSet<String> getContentTagSet() {
        return contentTagSet;
    }

    public void setContentTagSet(SequencedSet<String> contentTagSet) {
        this.contentTagSet = contentTagSet;
    }

    public String getLanguage() {
        return language;
    }

    public void setSpeakerList(List<Speaker> speakerList) {
        this.speakerList = speakerList;
    }

    public SequencedSet<String> getRequiredRoomTagSet() {
        return requiredRoomTagSet;
    }

    public void setRequiredRoomTagSet(SequencedSet<String> requiredRoomTagSet) {
        this.requiredRoomTagSet = requiredRoomTagSet;
    }

    public SequencedSet<String> getPreferredRoomTagSet() {
        return preferredRoomTagSet;
    }

    public void setPreferredRoomTagSet(SequencedSet<String> preferredRoomTagSet) {
        this.preferredRoomTagSet = preferredRoomTagSet;
    }

    public SequencedSet<String> getProhibitedRoomTagSet() {
        return prohibitedRoomTagSet;
    }

    public void setProhibitedRoomTagSet(SequencedSet<String> prohibitedRoomTagSet) {
        this.prohibitedRoomTagSet = prohibitedRoomTagSet;
    }

    public SequencedSet<String> getUndesiredRoomTagSet() {
        return undesiredRoomTagSet;
    }

    public void setUndesiredRoomTagSet(SequencedSet<String> undesiredRoomTagSet) {
        this.undesiredRoomTagSet = undesiredRoomTagSet;
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean pinnedByUser) {
        this.pinnedByUser = pinnedByUser;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public SequencedSet<String> getMutuallyExclusiveTalksTagSet() {
        return mutuallyExclusiveTalksTagSet;
    }

    public void setMutuallyExclusiveTalksTagSet(SequencedSet<String> mutuallyExclusiveTalksTagSet) {
        this.mutuallyExclusiveTalksTagSet = mutuallyExclusiveTalksTagSet;
    }

    public SequencedSet<Talk> getPrerequisiteTalkSet() {
        return prerequisiteTalkSet;
    }

    public void setPrerequisiteTalkSet(SequencedSet<Talk> prerequisiteTalkSet) {
        this.prerequisiteTalkSet = prerequisiteTalkSet;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getCrowdControlRisk() {
        return crowdControlRisk;
    }

    public void setCrowdControlRisk(int crowdControlRisk) {
        this.crowdControlRisk = crowdControlRisk;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public Talk withTalkType(TalkType talkType) {
        this.talkType = talkType;
        return this;
    }

    public Talk withSpeakerList(List<Speaker> speakerList) {
        this.speakerList = speakerList;
        return this;
    }

    public Talk withThemeTrackTagSet(SequencedSet<String> themeTrackTagSet) {
        this.themeTrackTagSet = themeTrackTagSet;
        return this;
    }

    public Talk withSectorTagSet(SequencedSet<String> sectorTagSet) {
        this.sectorTagSet = sectorTagSet;
        return this;
    }

    public Talk withAudienceTypeSet(SequencedSet<String> audienceTypeSet) {
        this.audienceTypeSet = audienceTypeSet;
        return this;
    }

    public Talk withAudienceLevel(int audienceLevel) {
        this.audienceLevel = audienceLevel;
        return this;
    }

    public Talk withContentTagSet(SequencedSet<String> contentTagSet) {
        this.contentTagSet = contentTagSet;
        return this;
    }

    public Talk withLanguage(String language) {
        this.language = language;
        return this;
    }

    public Talk withRequiredRoomTagSet(SequencedSet<String> requiredRoomTagSet) {
        this.requiredRoomTagSet = requiredRoomTagSet;
        return this;
    }

    public Talk withPreferredRoomTagSet(SequencedSet<String> preferredRoomTagSet) {
        this.preferredRoomTagSet = preferredRoomTagSet;
        return this;
    }

    public Talk withProhibitedRoomTagSet(SequencedSet<String> prohibitedRoomTagSet) {
        this.prohibitedRoomTagSet = prohibitedRoomTagSet;
        return this;
    }

    public Talk withUndesiredRoomTagSet(SequencedSet<String> undesiredRoomTagSet) {
        this.undesiredRoomTagSet = undesiredRoomTagSet;
        return this;
    }

    public Talk withMutuallyExclusiveTalksTagSet(SequencedSet<String> mutuallyExclusiveTalksTagSet) {
        this.mutuallyExclusiveTalksTagSet = mutuallyExclusiveTalksTagSet;
        return this;
    }

    public Talk withPrerequisiteTalksCodesSet(SequencedSet<Talk> prerequisiteTalksCodesSet) {
        this.prerequisiteTalkSet = prerequisiteTalksCodesSet;
        return this;
    }

    public Talk withFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
        return this;
    }

    public Talk withTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
        return this;
    }

    public Talk withRoom(Room room) {
        this.room = room;
        return this;
    }

}
