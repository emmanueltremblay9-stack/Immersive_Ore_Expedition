package com.oblixorprime.ioe.budding;

/** A single attempt's FE budget. Persist the snapshot with the machine, never in a global cache. */
public final class FlawlessPayment {
    private final String identity;
    private final double cost;
    private double paid;

    public FlawlessPayment(String identity, double cost, double paid) {
        if (identity == null || identity.isBlank() || !Double.isFinite(cost) || cost < 1
                || !Double.isFinite(paid) || paid < 0 || paid > cost)
            throw new IllegalArgumentException("Invalid Flawless payment");
        this.identity = identity; this.cost = cost; this.paid = paid;
    }
    public String identity() { return identity; }
    public double cost() { return cost; }
    public double paid() { return paid; }
    public double request(double limit) {
        if (!Double.isFinite(limit) || limit < 0) throw new IllegalArgumentException("Invalid debit limit");
        return Math.min(limit, cost - paid);
    }
    public void credit(double extracted) {
        if (!Double.isFinite(extracted) || extracted < 0 || extracted > cost - paid)
            throw new IllegalArgumentException("Invalid extracted energy");
        paid += extracted;
    }
    public boolean complete() { return paid == cost; }
}
