package com.oblixorprime.ioe.worldgen;

/**
 * Transaction boundary for an Immersive Petroleum reservoir prepared alongside an IOE expedition site.
 * Implementations must defer IP SavedData mutation until {@link #commit()} and must never remove a
 * pre-existing reservoir during {@link #rollback()}.
 */
public interface IoePetroleumReservoirReservation {
    boolean createdByIoe();

    void commit();

    void rollback();
}
