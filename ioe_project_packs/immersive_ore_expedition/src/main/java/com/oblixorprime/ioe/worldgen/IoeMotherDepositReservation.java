package com.oblixorprime.ioe.worldgen;

/**
 * Transaction boundary for an optional native Immersive Engineering deposit prepared before site blocks are written.
 * Implementations defer the visible IE mutation until {@link #commit()}, allow compensation after a successful commit,
 * and must never remove a pre-existing IE vein when rolled back.
 */
public interface IoeMotherDepositReservation {
    boolean createdByIoe();

    default boolean requiredForSiteQuality() {
        return true;
    }

    void commit();

    void rollback();
}
