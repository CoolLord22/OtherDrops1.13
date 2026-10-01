package com.gmail.zariust.otherdrops.parameters;

import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;

/**
 * A condition with a side effect (taking items, starting a cooldown) that must only happen once the
 * drop section has passed every check AND has been selected to run (after weight/UNIQUE selection).
 * <p>
 * Lifecycle for one event:
 * <ol>
 *   <li>{@code checkInstance} runs during gathering. It must NOT change anything; it may store what it
 *       found with {@link OccurredEvent#setCommitData(Object, Object)}.</li>
 *   <li>{@link #canCommit} runs for every Committable on the section (and its parent groups) right before
 *       scheduling. It re-verifies, with no side effects.</li>
 *   <li>{@link #commit} runs only if every canCommit on the section (and its parent groups) returned true.</li>
 * </ol>
 */
public interface Committable {
    /** Re-verify right before the section runs (e.g. the item is still there). No side effects. */
    boolean canCommit(CustomDrop drop, OccurredEvent occurrence);

    /** Apply the side effect. Only called after every Committable on the section returned true from canCommit. */
    void commit(CustomDrop drop, OccurredEvent occurrence);
}
